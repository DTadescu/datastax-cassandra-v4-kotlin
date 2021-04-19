package shdv.demo.cas4.repository.cassandra.producers

import com.datastax.oss.driver.api.mapper.result.MapperResultProducer
import com.datastax.oss.driver.api.mapper.result.MapperResultProducerService
import shdv.demo.cas4.repository.cassandra.ProductDto
import java.util.*

class GuavaFutureProducerService: MapperResultProducerService {
    override fun getProducers(): Iterable<MapperResultProducer> =
        listOf(
            FutureOfCollectionProducer(),
            FutureOfUnitProducer(),
            FutureOfBooleanProducer(),
//            FutureOfEntityProducer(ProductDto::class.java),
            FutureOfEntityProducer(),
        )
}
