package shdv.demo.cas4.repository.cassandra.producers

import com.datastax.oss.driver.api.core.cql.Statement
import com.datastax.oss.driver.api.core.type.reflect.GenericType
import com.datastax.oss.driver.api.mapper.MapperContext
import com.datastax.oss.driver.api.mapper.entity.EntityHelper
import com.datastax.oss.driver.api.mapper.result.MapperResultProducer
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import java.lang.Exception

class FutureOfEntityProducer: MapperResultProducer {
    override fun canProduce(resultType: GenericType<*>) =
        resultType.rawType == ListenableFuture::class.java

    override fun execute(
        statement: Statement<*>,
        context: MapperContext,
        entityHelper: EntityHelper<*>?
    ): ListenableFuture<*> {
        assert(entityHelper != null)
        val result = SettableFuture.create<Any>()
        val session = context.session
        session.executeAsync(statement)
            .whenComplete{
                resultSet, error ->
                    if (error != null) {
                        result.setException(error)
                    } else {
                        val row = resultSet.one()
                        result.set(if (row == null) null else entityHelper!!.get(row))
                    }
            }
        return result
    }

    override fun wrapError(e: Exception): ListenableFuture<*> {
       return Futures.immediateFailedFuture<ListenableFuture<*>>(e)
    }
}
