package shdv.demo.cas4.repository.cassandra

import com.datastax.oss.driver.api.mapper.annotations.CqlName
import java.time.Instant
import java.time.LocalDate

data class ProductModel(
    val id: String = "",
    val name: String = "",
    val producer: ProducerModel = ProducerModel.NONE,
    val price: Double = Double.MIN_VALUE,
    val description: String = "",
    val created: LocalDate = LocalDate.MIN,
    val lastWatch: Instant = Instant.MIN,
) {
    companion object {
        val NONE = ProductModel()
    }
}

data class ProducerModel(
    val name: String = "",
    val site: String = "",
) {
    companion object {
        val NONE = ProducerModel()
    }
}
