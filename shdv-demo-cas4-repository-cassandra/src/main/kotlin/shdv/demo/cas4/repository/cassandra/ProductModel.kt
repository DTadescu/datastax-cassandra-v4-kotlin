package shdv.demo.cas4.repository.cassandra

import java.time.Instant
import java.time.LocalDate

data class ProductModel(
    val id: String = "",
    val name: String = "",
    val price: Double = Double.MIN_VALUE,
    val description: String = "",
    val created: LocalDate = LocalDate.MIN,
    val lastWatch: Instant = Instant.MIN,
) {
    companion object {
        val NONE = ProductModel()
    }
}
