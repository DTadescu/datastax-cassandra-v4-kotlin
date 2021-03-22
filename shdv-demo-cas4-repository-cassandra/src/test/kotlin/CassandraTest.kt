import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import kotlinx.coroutines.runBlocking
import shdv.demo.cas4.repository.cassandra.ProductMapperBuilder
import kotlin.test.Test
import kotlinx.coroutines.guava.await
import org.testcontainers.containers.GenericContainer

class CassandraContainer : GenericContainer<CassandraContainer>("cassandra")

class CassandraTest {

    companion object {
        private val PORT = 9042
        private val keyspace = "test_keyspace"
        private lateinit var container: CassandraContainer
    }
@Test
fun factoryTest() {
    runBlocking {
        val session = CqlSession.builder().build()
        val productMapper = ProductMapperBuilder(session).build()
        val dao = productMapper.productDao("test-keyspace", "products")
        val product = dao.getAsync("test-id").await()
        val products = dao.list().await().toList()
//        val guava = ListenableFuture<String>
    }

}
    // в модуле <project>-be-common
    interface IEntityRepository {
        suspend fun get(id: String): EntityModel
        suspend fun save(entity: EntityModel): EntityModel
    }

    // в модуле <project>-be-repo-inmemory
    class EntityRepositoryInMemory: IEntityRepository {
        override suspend fun get(id: String): EntityModel {
            TODO("Not yet implemented")
        }

        override suspend fun save(entity: EntityModel): EntityModel {
            TODO("Not yet implemented")
        }
    }

    // в Application.kt
    val testRepo = EntityRepositoryInMemory()

    val entityCrud = EntityCrud (testRepository = testRepo)
}

class EntityCrud(testRepository: CassandraTest.IEntityRepository) {

}

class EntityModel {

}
