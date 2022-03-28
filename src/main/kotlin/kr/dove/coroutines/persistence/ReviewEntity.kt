package kr.dove.coroutines.persistence

import kr.dove.coroutines.api.Review
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "reviews")
@CompoundIndex(
    name = "prod-rev-id",
    unique = true,
    def = "{'productId': 1, 'reviewId': 1}"
)
data class ReviewEntity(
    @Id val id: UUID,
    val productId: Long,
    var reviewId: Long? = null,
    var author: String,
    var rating: Float = 2.5f,
    var content: String = "",
    @Version var version: Int = 0,
): Times() {
    companion object {
        @Transient const val SEQUENCE_NAME = "_review_sequence"
    }

    fun mapToApi(): Review =
        Review(
            id = this.id,
            productId = this.productId,
            reviewId = this.reviewId,
            author = this.author,
            rating = this.rating,
            content = this.content,
            version = this.version,
            created = this.created,
            modified = this.modified
        )
}
