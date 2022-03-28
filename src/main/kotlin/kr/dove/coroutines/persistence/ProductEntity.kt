package kr.dove.coroutines.persistence

import kr.dove.coroutines.api.Product
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "products")
data class ProductEntity(
    @Id val id: UUID,
    @Indexed(unique = true) var productId: Long? = null,
    var name: String,
    var cost: Int,
    @Version var version: Int = 0,
): Times() {

    companion object {
        @Transient const val SEQUENCE_NAME = "_product_sequence"
    }

    fun mapToApi(): Product =
        Product(
            id = this.id,
            productId = this.productId,
            name = this.name,
            cost = this.cost,
            version = this.version,
            created = this.created,
            modified = this.modified
        )
}