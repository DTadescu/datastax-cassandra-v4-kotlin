package shdv.demo.cas4.repository.cassandra

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
    hosts: String = "",
    port: Int = 9042,
    user: String = "cassandra",
    pass: String = "cassandra",
    private val timeout: Duration = Duration.ofSeconds(10),
    private val searchParallelism: Int = 1,
    private val replicationFactor: Int = 1,
    initObjects: Collection<ProductModel> = emptyList(),
) {
    private val tableName = "products"
    private val session = CqlSession.builder()
        .addContactPoints(parseAddresses(hosts, port))
        .withAuthCredentials(user, pass)
        .build().apply {
            createTable()
        }
    private val productMapper = ProductMapperBuilder(session).build()
    private val dao = productMapper.productDao(keyspace, tableName).apply {
        runBlocking {
            initObjects.map {
                saveAsync(ProductDto.of(it)).await()
            }
        }
    }

    suspend fun get(id: String) =
        dao.getAsync(id).await().toModel()

    suspend fun list() =
        dao.list().await().map { it.toModel() }.toList()

    private fun createKeyspace() {
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
    }

    private fun createTable() {
        session.execute(
            SchemaBuilder.createTable(tableName)
                .ifNotExists()
                .withPartitionKey(COLUMN_ID, DataTypes.TEXT)
                .withColumn(COLUMN_NAME, DataTypes.TEXT)
                .withColumn(COLUMN_PRICE, DataTypes.DOUBLE)
                .withColumn(COLUMN_DESCRIPTION, DataTypes.TEXT)
                .withColumn(COLUMN_CREATED, DataTypes.DATE)
                .withColumn(COLUMN_LAST_WATCHED, DataTypes.TIMESTAMP)
                .build()
        )
    }

    private fun parseAddresses(hosts: String, port: Int): Collection<InetSocketAddress> = hosts
        .split(Regex("""\s*,\s*"""))
//            .map { it.split(":") }
        .map { InetSocketAddress.createUnresolved(it, port) }
}
