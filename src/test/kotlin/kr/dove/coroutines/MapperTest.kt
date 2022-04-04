package kr.dove.coroutines

import kr.dove.coroutines.api.Product
import kr.dove.coroutines.api.Review
import kr.dove.coroutines.persistence.ProductEntity
import kr.dove.coroutines.persistence.ReviewEntity
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*
import org.junit.jupiter.api.Assertions.*

class MapperTest {

    @Test
    @DisplayName("Verify <product> mapping from entity class to api class")
    fun product_mapper_test() {
        val uuid: UUID = UUID.randomUUID()
        val productEntity = ProductEntity(
            uuid,
            1L,
            "Cup",
            38000,
            2
        )

        val productApi: Product = productEntity.mapToApi()

        assertEquals(productEntity.id, productApi.id)
        assertEquals(productEntity.productId, productApi.productId)
        assertEquals(productEntity.name, productApi.name)
        assertEquals(productEntity.cost, productApi.cost)
        assertEquals(productEntity.version, productApi.version)
    }

    @Test
    @DisplayName("Verify <review> mapping from entity class to api class")
    fun review_mapper_test() {
        val uuid: UUID = UUID.randomUUID()
        val reviewEntity = ReviewEntity(
            uuid,
            1L,
            3L,
            "author",
            5f,
            "content",
            2
        )

        val reviewApi: Review = reviewEntity.mapToApi()

        assertEquals(reviewEntity.id, reviewApi.id)
        assertEquals(reviewEntity.productId, reviewApi.productId)
        assertEquals(reviewEntity.reviewId, reviewApi.reviewId)
        assertEquals(reviewEntity.author, reviewApi.author)
        assertEquals(reviewEntity.rating, reviewApi.rating)
        assertEquals(reviewEntity.content, reviewApi.content)
        assertEquals(reviewEntity.version, reviewApi.version)
    }
}