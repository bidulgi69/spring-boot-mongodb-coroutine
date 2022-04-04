package kr.dove.coroutines

import kr.dove.coroutines.api.Review
import kr.dove.coroutines.persistence.ReviewEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@ContextConfiguration(classes = [MongoDbTestConfiguration::class])
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ReviewServiceTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val reactiveMongoTestTemplate: ReactiveMongoTemplate,
): MongoTestContainer() {

    private val productId = 1L

    @BeforeEach
    fun setup() {
        reactiveMongoTestTemplate
            .remove(ReviewEntity::class.java)
            .all()
            .block()
    }

    @Test
    @DisplayName("Post a new review")
    fun post() {
        val review = Review(
            productId = productId,
            author = "Author - 1",
            rating = 5f,
            content = "Contents - 1"
        )

        sendCreateNewReview(review)
            .value {
                Assertions.assertNotNull(it.id)
                Assertions.assertNotNull(it.reviewId)
                Assertions.assertEquals(review.productId, it.productId)
                Assertions.assertEquals(review.author, it.author)
                Assertions.assertEquals(review.rating, it.rating)
                Assertions.assertEquals(review.content, it.content)
                Assertions.assertEquals(2, it.version)
            }
    }

    @Test
    @DisplayName("Update fields of review")
    fun put() {
        var review = Review(
            productId = productId,
            author = "Author - 1",
            rating = 5f,
            content = "Contents - 1"
        )
        sendCreateNewReview(review)
            .value { saved ->
                Assertions.assertNotNull(saved.id)
                Assertions.assertNotNull(saved.reviewId)
                Assertions.assertEquals(review.productId, saved.productId)
                Assertions.assertEquals(review.author, saved.author)
                Assertions.assertEquals(review.rating, saved.rating)
                Assertions.assertEquals(review.content, saved.content)
                //  apply changes
                review = saved
            }

        review.author = "Author - 2"
        review.rating = 1f
        review.content = "Contents - 2"
        webTestClient
            .put()
            .uri("/review/")
            .bodyValue(review)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(Review::class.java)
            .value {
                Assertions.assertNotNull(it.id)
                Assertions.assertNotNull(it.reviewId)
                Assertions.assertEquals(review.productId, it.productId)
                Assertions.assertEquals(review.author, it.author)
                Assertions.assertEquals(review.rating, it.rating)
                Assertions.assertEquals(review.content, it.content)
                Assertions.assertEquals(3, it.version)
            }
    }

    @Test
    @DisplayName("Delete review by reviewId")
    fun delete() {
        var review = Review(
            productId = productId,
            author = "Author - 1",
            rating = 5f,
            content = "Contents - 1"
        )
        sendCreateNewReview(review)
            .value { review = it }

        webTestClient
            .delete()
            .uri("/review/${review.reviewId}")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(Long::class.java)
            .value {
                Assertions.assertEquals(review.reviewId, it)
            }
    }

    @Test
    @DisplayName("Read all reviews of product saved in database")
    fun get() {
        sendCreateNewReview(
            Review(
            productId = productId,
            author = "Author - 0",
            rating = 1f,
            content = "Contents - 0"
        ))
        sendCreateNewReview(Review(
            productId = productId,
            author = "Author - 1",
            rating = 2f,
            content = "Contents - 1"
        ))
        sendCreateNewReview(Review(
            productId = productId,
            author = "Author - 2",
            rating = 3f,
            content = "Contents - 2"
        ))
        sendCreateNewReview(
            Review(
            productId = productId,
            author = "Author - 3",
            rating = 4f,
            content = "Contents - 3"
        )
        )
        sendCreateNewReview(Review(
            productId = productId,
            author = "Author - 4",
            rating = 5f,
            content = "Contents - 4"
        ))

        val authors = listOf(
            "Author - 0",
            "Author - 1",
            "Author - 2",
            "Author - 3",
            "Author - 4"
        )
        val contents = listOf(
            "Contents - 0",
            "Contents - 1",
            "Contents - 2",
            "Contents - 3",
            "Contents - 4"
        )

        webTestClient
            .get()
            .uri("/review/$productId")
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBodyList(Review::class.java)
            .value<WebTestClient.ListBodySpec<Review>> {
                Assertions.assertEquals(5, it.size)
                it.forEach { rev ->
                    Assertions.assertNotNull(rev.id)
                    Assertions.assertNotNull(rev.reviewId)
                    Assertions.assertEquals(productId, rev.productId)
                    Assertions.assertTrue(rev.author in authors)
                    Assertions.assertTrue(rev.rating in 1f..5f)
                    Assertions.assertTrue(rev.content in contents)
                }
            }
    }

    private fun sendCreateNewReview(review: Review): WebTestClient.BodySpec<Review, *> =
        webTestClient
            .post()
            .uri("/review/${review.productId}")
            .bodyValue(review)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(Review::class.java)
}