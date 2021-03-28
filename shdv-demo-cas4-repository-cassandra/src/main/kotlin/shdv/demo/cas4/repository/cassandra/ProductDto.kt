package shdv.demo.cas4.repository.cassandra

import com.datastax.oss.driver.api.mapper.annotations.*
import com.google.common.util.concurrent.ListenableFuture
import java.time.Instant
import java.time.LocalDate

@Entity
data class ProducerDto(
    @CqlName(COLUMN_NAME)
    val name: String? = null,
    @CqlName(COLUMN_SITE)
    val site: String? = null,
) {
    companion object {
        const val TYPE_NAME = "producer"
        const val COLUMN_NAME = "name"
        const val COLUMN_SITE = "site"

        fun of(model: ProducerModel) = ProducerDto(
            name = model.name.takeIf { it.isNotBlank() },
            site = model.site.takeIf { it.isNotBlank() },
        )
    }

    fun toModel() = ProducerModel(
        name = name?: "",
        site = site?: "",
    )
}

@Entity
data class ProductDto(
    @PartitionKey()
    @CqlName(COLUMN_ID)
    val id: String? = null,
    @CqlName(COLUMN_NAME)
    val name: String? = null,
    @CqlName(COLUMN_PRODUCER)
    val producer: ProducerDto? = null,
    @CqlName(COLUMN_PRICE)
    val price: Double? = null,
    @CqlName(COLUMN_DESCRIPTION)
    val description: String? = null,
    @CqlName(COLUMN_CREATED)
    val created: LocalDate? = null,
    @CqlName(COLUMN_LAST_WATCH)
    val lastWatch: String? = null,
) {
    fun toModel() = ProductModel(
        id = id?: "",
        name = name?: "",
        producer = producer?.toModel()?: ProducerModel.NONE,
        price = price?: Double.MIN_VALUE,
        description = description?: "",
        created = created?: LocalDate.MIN,
        lastWatch = lastWatch?.let { Instant.parse(it) }?: Instant.MIN,
    )
    companion object {
        const val TABLE_NAME = "products"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRODUCER = "producer"
        const val COLUMN_PRICE = "price"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_CREATED = "created"
        const val COLUMN_LAST_WATCH = "last_watch"

        fun of(model: ProductModel) = ProductDto(
            id = model.id.takeIf { it.isNotBlank() },
            name = model.name.takeIf { it.isNotBlank() },
            producer = model.producer.takeIf { it != ProducerModel.NONE }?.let { ProducerDto.of(it) },
            price = model.price.takeIf { it != Double.MIN_VALUE },
            description = model.description.takeIf { it.isNotBlank() },
            created = model.created.takeIf { it != LocalDate.MIN },
            lastWatch = model.lastWatch.takeIf { it != Instant.MIN }?.toString(),
        )
    }
}

@Dao
interface ProductDao {

    @Select
    fun getAsync(id: String): ListenableFuture<ProductDto>
//    fun getAsync(id: String): ProductDto

    @Select
    fun list(): ListenableFuture<Collection<ProductDto>>
//    fun list(): ListenableFuture<ProductDto>

    @Insert()
    fun  saveAsync(product: ProductDto): ListenableFuture<Unit>
//    fun  saveAsync(product: ProductDto): Boolean

}

@Mapper
interface ProductMapper {

    @DaoFactory
    fun productDao(@DaoKeyspace keyspace: String, @DaoTable table: String): ProductDao
}

