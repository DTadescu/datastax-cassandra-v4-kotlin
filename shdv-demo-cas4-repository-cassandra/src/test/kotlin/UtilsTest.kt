import com.datastax.oss.driver.api.core.type.reflect.GenericType
import com.google.common.util.concurrent.ListenableFuture
import kotlin.test.Test
import shdv.demo.cas4.repository.cassandra.ProductDto

class UtilsTest {

    @Test
    fun genericTypeCreateTest() {
        val type = object : GenericType<ListenableFuture<ProductDto>>() {}
        val result = type.isTargetEntity(arrayOf(ProductDto::class.java))
        println(result)
    }

    private fun GenericType<*>.isTargetEntity(entityClasses: Array<out Class<*>>): Boolean {
        val typeName = this.type.typeName
        val lfName = ListenableFuture::class.java.name
        if (this.rawType != ListenableFuture::class.java)
            return false
        if (entityClasses.isNullOrEmpty())
            return true
        println(this.type)
        val types: MutableList<String> = mutableListOf()
        entityClasses.forEach {
            types.add(lfName + "<${it.name}>")
        }
        println("Size:" + types.size)
        return types.contains(typeName)
    }

}
