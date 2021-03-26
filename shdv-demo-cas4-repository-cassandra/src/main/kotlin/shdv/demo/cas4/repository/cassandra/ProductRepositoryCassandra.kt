package shdv.demo.cas4.repository.cassandra

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import com.datastax.oss.driver.api.core.type.DataType
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.runBlocking
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_CREATED
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_DESCRIPTION
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_ID
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_LAST_WATCHED
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_NAME
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_PRICE
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration
import java.util.concurrent.CompletionStage

class ProductRepositoryCassandra(
    private val keyspace: String,
    private val hosts: String = "",
    private val port: Int = 9042,
    private val user: String = "cassandra",
    private val pass: String = "cassandra",
    private val timeout: Duration = Duration.ofSeconds(10),
    private val searchParallelism: Int = 1,
    private val replicationFactor: Int = 1,
    initObjects: Collection<ProductModel> = emptyList(),
) {
//    init {
//        createKeyspace()
//        createTable()
//    }
    private val tableName = "products"
    private val session by lazy {
        val builder = CqlSession.builder()
            .addContactPoints(parseAddresses(hosts, port))
            .withLocalDatacenter("datacenter1")
            .withAuthCredentials(user, pass)
        builder.build().apply {
            createKeyspace(this)
        }
        builder.withKeyspace(keyspace).build().apply {
            createTable(this)
        }
//        CqlSession.builder().also {
//            println(it)
//        }
//            .addContactPoints(parseAddresses(hosts, port)).also {
//                println(it)
//            }
//            .withLocalDatacenter("datacenter1").also {
//                println(it)
//            }
////            .withAuthCredentials(user, pass).also {
////                println(it)
////            }
//            .withKeyspace("\"$keyspace\"").also {
//                println(it)
//            }
//            .build().also {
//                println(it)
//            }
    }
//        .apply {
//            createTable()
//        }
    private val productMapper by lazy {
    ProductMapperBuilder(session).build()
    }
    private val dao by lazy {
        productMapper.productDao(keyspace, tableName).apply {
            runBlocking {
                initObjects.map {
                    saveAsync(ProductDto.of(it))
                }
            }
        }
    }

    suspend fun get(id: String) =
        dao.getAsync(id).await().toModel()
//        dao.getAsync(id).toModel()

    suspend fun list() =
        dao.list().await().map { it.toModel() }.toList()

    private fun createKeyspace(session: CqlSession) {
//        val initSession = CqlSession.builder()
//            .addContactPoints(parseAddresses(hosts, port))
//            .withLocalDatacenter("datacenter1")
//            .withAuthCredentials(user, pass)
//            .build()
        session.execute(
            SchemaBuilder.createKeyspace(keyspace)
                .ifNotExists()
                .withSimpleStrategy(replicationFactor)
                .build()
        )
//        session.execute("""
//            CREATE KEYSPACE IF NOT EXISTS $keyspace WITH REPLICATION = {
//                'class' : 'SimpleStrategy',
//                'replication_factor' : $replicationFactor
//            }
//        """.trimIndent())
//        println(initSession.keyspace)
    }

    private fun createTable(session: CqlSession) {
        val query = SchemaBuilder.createTable(tableName)
            .ifNotExists()
            .withPartitionKey(COLUMN_ID, DataTypes.TEXT)
            .withColumn(COLUMN_NAME, DataTypes.TEXT)
            .withColumn(COLUMN_PRICE, DataTypes.DOUBLE)
            .withColumn(COLUMN_DESCRIPTION, DataTypes.TEXT)
            .withColumn(COLUMN_CREATED, DataTypes.DATE)
//                .withColumn(COLUMN_LAST_WATCHED, DataTypes.TEXT)
            .build()
        println(query.query)

//        val session1 = CqlSession.builder()
//            .addContactPoints(parseAddresses(hosts, port))
//            .withKeyspace(keyspace)
//            .withLocalDatacenter("datacenter1")
//            .withAuthCredentials(user, pass)
//            .build()

        session.execute(
            query
        )
    }

    private fun parseAddresses(hosts: String, port: Int): Collection<InetSocketAddress> = hosts
        .split(Regex("""\s*,\s*"""))
//            .map { it.split(":") }
        .map { InetSocketAddress(InetAddress.getByName(it), port) }
//        .map { InetSocketAddress("localhost", port) }
        .apply { println(this) }

    fun init() = apply {
        val mapper = productMapper
    }
}
