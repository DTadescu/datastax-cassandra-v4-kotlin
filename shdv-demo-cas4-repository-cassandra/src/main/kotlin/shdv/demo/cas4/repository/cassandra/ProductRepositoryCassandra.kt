package shdv.demo.cas4.repository.cassandra

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.config.DefaultDriverOption
import com.datastax.oss.driver.api.core.config.DriverConfigLoader
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_CREATED
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_DESCRIPTION
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_ID
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_LAST_WATCH
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_NAME
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_PRICE
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.COLUMN_PRODUCER
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.TABLE_NAME
import shdv.demo.cas4.repository.cassandra.ProductDto.Companion.of
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
            createKeyspace() // создание кейспейса
        }
        builder.withKeyspace(keyspaceName)
            .withConfigLoader(config).build().apply {
            createTypeProducer() // регистрация udt
            createTable()
        }
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

    suspend fun save(model: ProductModel) =
        dao.saveAsync(of(model)).await()

    suspend fun get(id: String) =
        dao.getAsync(id).await().toModel()
//        dao.getAsync(id).toModel()

    suspend fun delete(id: String) =
        dao.deleteAsync(id).await()
//        dao.deleteAsync(id)

    suspend fun list() =
        dao.list().await().map { it.toModel() }.toList()
//        dao.list().await().toModel()

        private fun CqlSession.createKeyspace() {
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
        execute(
            SchemaBuilder.createType(AddressDto.TYPE_NAME)
                .ifNotExists()
                .withField(AddressDto.COLUMN_STREET, DataTypes.TEXT)
                .withField(AddressDto.COLUMN_HOUSE, DataTypes.TEXT)
                .build()
        )

        execute(
            SchemaBuilder.createType(LocationDto.TYPE_NAME)
                .ifNotExists()
                .withField(LocationDto.COLUMN_CITY, DataTypes.TEXT)
                .withField(LocationDto.COLUMN_ADDRESS, SchemaBuilder.udt(AddressDto.TYPE_NAME, true))
                .build()
        )

        execute(
            SchemaBuilder.createType(ProducerDto.TYPE_NAME)
            .ifNotExists()
            .withField(ProducerDto.COLUMN_NAME, DataTypes.TEXT)
            .withField(ProducerDto.COLUMN_SITE, DataTypes.TEXT)
            .withField(ProducerDto.COLUMN_LOCATION, SchemaBuilder.udt(LocationDto.TYPE_NAME, true))
            .build()
        )
    }

    private fun CqlSession.createTable() {
        execute(
            SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .withPartitionKey(COLUMN_ID, DataTypes.TEXT)
            .withColumn(COLUMN_NAME, DataTypes.TEXT)
            .withColumn(COLUMN_PRODUCER, SchemaBuilder.udt(ProducerDto.TYPE_NAME, true))
            .withColumn(COLUMN_PRICE, DataTypes.DOUBLE)
            .withColumn(COLUMN_DESCRIPTION, DataTypes.TEXT)
            .withColumn(COLUMN_CREATED, DataTypes.DATE)
            .withColumn(COLUMN_LAST_WATCH, DataTypes.TEXT)
            .build()
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
