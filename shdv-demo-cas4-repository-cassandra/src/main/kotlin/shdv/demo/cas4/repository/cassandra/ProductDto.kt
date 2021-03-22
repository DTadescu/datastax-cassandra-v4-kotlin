package shdv.demo.cas4.repository.cassandra

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.PagingIterable
import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.mapper.annotations.*
import com.google.common.util.concurrent.ListenableFuture
import java.time.Instant
import java.time.LocalDate

@Entity
data class ProductDto(
    @PartitionKey()
    @CqlName(COLUMN_ID)
    val id: String? = null,
    @CqlName(COLUMN_NAME)
    val name: String? = null,
    @CqlName(COLUMN_PRICE)
    val price: Double? = null,
    @CqlName(COLUMN_DESCRIPTION)
    val description: String? = null,
    @CqlName(COLUMN_CREATED)
    val created: LocalDate? = null,
    @CqlName(COLUMN_LAST_WATCHED)
    val lastWatch: Instant? = null,
) {
    fun toModel() = ProductModel(
        id = id?: "",
        name = name?: "",
        price = price?: Double.MIN_VALUE,
        description = description?: "",
        created = created?: LocalDate.MIN,
        lastWatch = lastWatch?: Instant.MIN,
    )
    companion object {
        const val TABLE_NAME = "products"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_CREATED = "created"
        const val COLUMN_LAST_WATCHED = "last-watched"

        fun of(model: ProductModel) = ProductDto(
            id = model.id.takeIf { it.isNotBlank() },
            name = model.name.takeIf { it.isNotBlank() },
            price = model.price.takeIf { it != Double.MIN_VALUE },
            description = model.description.takeIf { it.isNotBlank() },
            created = model.created.takeIf { it != LocalDate.MIN },
            lastWatch = model.lastWatch.takeIf { it != Instant.MIN },
        )
    }
}

@Dao
interface ProductDao {

    @Select
    fun getAsync(id: String): ListenableFuture<ProductDto>

    @Select
    fun list(): ListenableFuture<PagingIterable<ProductDto>>

    @Insert
    fun  saveAsync(product: ProductDto): ListenableFuture<Boolean>

}

@Mapper
interface ProductMapper {

    @DaoFactory
    fun productDao(@DaoKeyspace keyspace: String, @DaoTable table: String): ProductDao
}

