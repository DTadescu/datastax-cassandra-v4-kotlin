package shdv.demo.cas4.repository.cassandra

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.config.DefaultDriverOption
import com.datastax.oss.driver.api.core.config.DriverConfigLoader
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.querybuilder.QueryBuilder
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.datastax.oss.driver.internal.core.type.UserDefinedTypeBuilder
import com.datastax.oss.driver.internal.core.type.codec.UdtCodec
import com.datastax.oss.protocol.internal.request.query.QueryOptions
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import shdv.demo.cas4.repository.cassandra.ProducerDto.Companion.COLUMN_SITE
import shdv.demo.cas4.repository.cassandra.ProducerDto.Companion.TYPE_NAME
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_CREATED
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_DESCRIPTION
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_ID
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_LAST_WATCH
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_NAME
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_PRICE
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_PRODUCER
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.TABLE_NAME
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration

class ProductRepositoryCassandra(
    private val keyspaceName: String,
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
//    private val tableName = "products"
    private val config by lazy {
    DriverConfigLoader.programmaticBuilder()
        .startProfile("default")
        .withString(DefaultDriverOption.REQUEST_CONSISTENCY, ConsistencyLevel.ALL.name())
        .build()
    }

    private val session by lazy {
        val builder = CqlSession.builder()
            .addContactPoints(parseAddresses(hosts, port))
            .withLocalDatacenter("datacenter1")
            .withAuthCredentials(user, pass)
        builder.build().apply {
            createKeyspace()
        }
        builder.withKeyspace(keyspaceName)
            .withConfigLoader(config).build().apply {
            createTypeProducer()
            createTable()
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
        productMapper.productDao(keyspaceName, TABLE_NAME).apply {
            runBlocking {
                initObjects.map {
                    withTimeout(timeout.toMillis()) {
                        saveAsync(ProductDto.of(it)).await()
                    }
                }
            }
        }
    }

    suspend fun get(id: String) =
        dao.getAsync(id).await().toModel()
//        dao.getAsync(id).toModel()

    suspend fun list() =
        dao.list().await().map { it.toModel() }.toList()
//        dao.list().await().toModel()

    private fun CqlSession.createKeyspace() {
//        val initSession = CqlSession.builder()
//            .addContactPoints(parseAddresses(hosts, port))
//            .withLocalDatacenter("datacenter1")
//            .withAuthCredentials(user, pass)
//            .build()
        execute(
            SchemaBuilder.createKeyspace(keyspaceName)
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

    private fun CqlSession.createTypeProducer() {
        execute(SchemaBuilder.createType(TYPE_NAME)
            .ifNotExists()
            .withField(ProducerDto.COLUMN_NAME, DataTypes.TEXT)
            .withField(COLUMN_SITE, DataTypes.TEXT)
            .build()
        )
    }

    private fun CqlSession.createTable() {
        val query = SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .withPartitionKey(COLUMN_ID, DataTypes.TEXT)
            .withColumn(COLUMN_NAME, DataTypes.TEXT)
            .withColumn(COLUMN_PRODUCER, SchemaBuilder.udt(TYPE_NAME, true))
            .withColumn(COLUMN_PRICE, DataTypes.DOUBLE)
            .withColumn(COLUMN_DESCRIPTION, DataTypes.TEXT)
            .withColumn(COLUMN_CREATED, DataTypes.DATE)
            .withColumn(COLUMN_LAST_WATCH, DataTypes.TEXT)
            .build()
        println(query.query)

//        val session1 = CqlSession.builder()
//            .addContactPoints(parseAddresses(hosts, port))
//            .withKeyspace(keyspace)
//            .withLocalDatacenter("datacenter1")
//            .withAuthCredentials(user, pass)
//            .build()

        execute(
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
        val dao = dao
    }
}
