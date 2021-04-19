package shdv.demo.cas4.repository.cassandra

import com.datastax.oss.driver.api.mapper.annotations.*
import com.google.common.util.concurrent.ListenableFuture
import java.time.Instant
import java.time.LocalDate

    @Entity
    data class AddressDto(
        val street: String? = null,
        val house: String? = null,
    ) {
        fun toModel() = AddressModel(
            street = street?: "",
            house = house?: "",
        )

        companion object {
            const val TYPE_NAME = "address"
            const val COLUMN_STREET = "street"
            const val COLUMN_HOUSE = "house"

            fun of(model: AddressModel) = AddressDto(
                street = model.street.takeIf { it.isNotBlank() },
                house = model.house.takeIf { it.isNotBlank() },
            )
        }
    }

    @Entity
    data class LocationDto(
        val city: String? = null,
        val address: AddressDto? = null,
    ) {
        fun toModel() = LocationModel(
            city = city?: "",
            address = address?.toModel()?: AddressModel.NONE,
        )

        companion object {
            const val TYPE_NAME = "location"
            const val COLUMN_CITY = "city"
            const val COLUMN_ADDRESS = "address"

            fun of(model: LocationModel) = LocationDto(
                city = model.city.takeIf { it.isNotBlank() },
                address = model.address.takeIf { it != AddressModel.NONE }?.let { AddressDto.of(it) },
            )
        }
    }

    @Entity
    data class ProducerDto(
        @CqlName(COLUMN_NAME)
        val name: String? = null,
        @CqlName(COLUMN_SITE)
        val site: String? = null,
        @CqlName(COLUMN_LOCATION)
        val location: LocationDto? = null,
    )
{
    companion object {
        const val TYPE_NAME = "producer"
        const val COLUMN_NAME = "name"
        const val COLUMN_SITE = "site"
        const val COLUMN_LOCATION = "location"

        fun of(model: ProducerModel) = ProducerDto(
            name = model.name.takeIf { it.isNotBlank() },
            site = model.site.takeIf { it.isNotBlank() },
            location = model.location.takeIf { it != LocationModel.NONE }?.let { LocationDto.of(it) },
        )
    }

    fun toModel() = ProducerModel(
        name = name?: "",
        site = site?: "",
        location = location?.toModel()?: LocationModel.NONE
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
    )
{
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

        @Select
        @StatementAttributes(consistencyLevel = "QUORUM")
        fun list(): ListenableFuture<Collection<ProductDto>>

        @Insert(ifNotExists = true)
        @StatementAttributes(consistencyLevel = "ONE")
        fun  saveAsync(product: ProductDto): ListenableFuture<Boolean>

        @Delete(ifExists = true, entityClass = [ProductDto::class])
        fun deleteAsync(id: String): ListenableFuture<Boolean>
//        fun deleteAsync(id: String): Boolean
    }

    @Mapper
    interface ProductMapper {

        @DaoFactory
        fun productDao(
            @DaoKeyspace keyspace: String,
            @DaoTable table: String
        ): ProductDao
    }


