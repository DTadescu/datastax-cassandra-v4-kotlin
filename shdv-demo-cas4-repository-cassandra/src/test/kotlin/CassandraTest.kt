import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import kotlinx.coroutines.runBlocking
import shdv.demo.cas4.repository.cassandra.ProductMapperBuilder
import kotlin.test.Test
import kotlinx.coroutines.guava.await
import org.junit.AfterClass
import org.junit.BeforeClass
import org.testcontainers.containers.GenericContainer
import shdv.demo.cas4.repository.cassandra.ProductModel
import shdv.demo.cas4.repository.cassandra.ProductRepositoryCassandra
import java.time.Duration
import java.time.LocalDate
import kotlin.test.BeforeTest

class CassandraContainer : GenericContainer<CassandraContainer>("cassandra")

class CassandraTest {

    companion object {
        private val PORT = 9042
        private val keyspace = "test_keyspace"
        private lateinit var container: CassandraContainer
        private lateinit var repo: ProductRepositoryCassandra

        @BeforeClass
        @JvmStatic
        fun tearUp() {
            container = CassandraContainer()
                .withExposedPorts(PORT)
                .withStartupTimeout(Duration.ofSeconds(40L))
                .apply {
                    start()
                }

            repo = ProductRepositoryCassandra(
                keyspace = "keyspace1",
                hosts = container.host,
                port = container.getMappedPort(PORT),
                initObjects = listOf(
                    ProductModel(id = "0"),
                    ProductModel(id = "1", name = "product-1"),
                    ProductModel(id = "2", name = "product-1"),
                    ProductModel(id = "3", created = LocalDate.parse("2021-02-03")),
                    ProductModel(id = "4", created = LocalDate.parse("2021-02-03")),
                    ProductModel(id = "5", created = LocalDate.parse("2021-02-03")),
                    ProductModel(id = "6", created = LocalDate.parse("2021-02-03")),
                )
            ).init()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            container.close()
        }
    }

@Test
fun testGetById() {
    runBlocking {
        val data = repo.get("1")
        println(data)
    }
}

@Test
fun factoryTest() {
    runBlocking {
        val session = CqlSession.builder().build()
        val productMapper = ProductMapperBuilder(session).build()
        val dao = productMapper.productDao("test-keyspace", "products")
        val product = dao.getAsync("test-id").await()
//        val product = dao.getAsync("test-id")
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
