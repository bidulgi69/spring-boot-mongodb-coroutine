package kr.dove.coroutines

import com.mongodb.client.result.DeleteResult
import kr.dove.coroutines.persistence.ReviewEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.ContextConfiguration
import reactor.test.StepVerifier
import java.util.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException

@ContextConfiguration(classes = [MongoDbTestConfiguration::class])
@DataMongoTest(excludeAutoConfiguration = [EmbeddedMongoAutoConfiguration::class])
class ReviewPersistenceTest(
    @Autowired private val reactiveMongoTestTemplate: ReactiveMongoTemplate,
): MongoTestContainer() {

    private var reviewEntity: ReviewEntity? = null

    @BeforeEach
    fun setup() {

        //  cleanup
        reactiveMongoTestTemplate
            .remove(ReviewEntity::class.java)
            .all()
            .block()

        //  setup
        val review = ReviewEntity(
            id = UUID.randomUUID(),
            productId = 1L,
            reviewId = 1L,
            author = "Author",
            2.5f,
            "Contents"
        )

        StepVerifier
            .create(reactiveMongoTestTemplate.save(review))
            .expectNextMatches { createdEntity ->
                reviewEntity = createdEntity
                review.isEqual(createdEntity)
            }
            .verifyComplete()
    }

    @Test
    @DisplayName("Create a new entity")
    fun create() {
        val reviewSequence = 2L
        val newEntity = ReviewEntity(
            UUID.randomUUID(),
            1L,
            reviewSequence,
            "Author2",
            5f,
            "Contents - 2"
        )

        StepVerifier
            .create(reactiveMongoTestTemplate.save(newEntity))
            .expectNextMatches { createdEntity ->
                newEntity.isEqual(createdEntity)
            }
            .verifyComplete()

        StepVerifier
            .create(reactiveMongoTestTemplate.find(
                Query.query(Criteria.where("reviewId").`is`(reviewSequence)),
                ReviewEntity::class.java
            ))
            .expectNextMatches { foundEntity ->
                newEntity.isEqual(foundEntity)
            }
            .verifyComplete()

        StepVerifier
            .create(reactiveMongoTestTemplate
                .findAll(ReviewEntity::class.java)
                .count()
            )
            .expectNextMatches { cnt -> cnt == 2L }
            .verifyComplete()
    }

    @Test
    @DisplayName("Update field value and verify @Version field works fine.")
    fun update() {
        with(reviewEntity!!) {
            val currentVersion = this.version
            this.author = "Author - 2"
            StepVerifier
                .create(reactiveMongoTestTemplate.save(this))
                .expectNextMatches { updatedEntity ->
                    updatedEntity.author == "Author - 2"
                }
                .verifyComplete()

            //  version field's value will be incremented. (by updating author field)
            StepVerifier
                .create(reactiveMongoTestTemplate.find(
                    Query.query(Criteria.where("reviewId").`is`(1L)),
                    ReviewEntity::class.java
                ))
                .expectNextMatches { foundEntity ->
                    foundEntity.author == "Author - 2"
                            && foundEntity.version == currentVersion + 1
                }
                .verifyComplete()
        }
    }

    @Test
    @DisplayName("Delete an entity")
    fun delete() {
        with(reviewEntity!!) {
            StepVerifier
                .create(reactiveMongoTestTemplate.remove(this))
                .expectNext(DeleteResult.acknowledged(1L))
                .verifyComplete()

            StepVerifier
                .create(reactiveMongoTestTemplate.exists(
                    Query.query(Criteria.where("reviewId").`is`(1L)),
                    ReviewEntity::class.java
                ))
                .expectNext(false)  //  after deleting, the entity will be not found
                .verifyComplete()
        }
    }

    @Test
    @DisplayName("Get by reviewId")
    fun getByReviewId() {
        with(reviewEntity!!) {
            StepVerifier
                .create(reactiveMongoTestTemplate.find(
                    Query.query(Criteria.where("reviewId").`is`(this.reviewId)),
                    ReviewEntity::class.java
                ))
                .expectNextMatches { foundEntity ->
                    this.isEqual(foundEntity)
                }
                .verifyComplete()
        }
    }

    @Test
    @DisplayName("Verify unique property on compound index works fine")
    fun isUniqueIndexPropertyWorksFine() {
        val duplicatedCompoundIndexEntity = ReviewEntity(
            UUID.randomUUID(),  //  different id
            1L,
            1L, //  compound index will be generated like { productId: 1L, reviewId: 1L } and it will make conflict.
            "",
            1f,
            ""
        )
        assertEquals(reviewEntity!!.productId, duplicatedCompoundIndexEntity.productId)
        assertEquals(reviewEntity!!.reviewId, duplicatedCompoundIndexEntity.reviewId)

        StepVerifier
            .create(reactiveMongoTestTemplate.save(duplicatedCompoundIndexEntity))
            .expectError(DuplicateKeyException::class.java)
            .verify()
    }

    @Test
    @DisplayName("Verify optimistic lock(version field) works fine")
    fun isOptimisticLockWorksFine() {
        with(reviewEntity!!) {
            //  Store the saved entity in two separate entity objects
            val entity1 = reactiveMongoTestTemplate.findById(
                this.id, ReviewEntity::class.java
            ).block()
            val entity2 = reactiveMongoTestTemplate.findById(
                this.id, ReviewEntity::class.java
            ).block()

            //  Update the entity using the first entity object
            entity1!!.author= "Author - 2"
            val currentVersion = entity1.version
            reactiveMongoTestTemplate
                .save(entity1)
                .block()

            //  Update the entity using the second entity object.
            //  This should fail since the second entity now holds an old version number, i.e. Optimistic Lock Error
            StepVerifier
                .create(reactiveMongoTestTemplate.save(entity2!!))
                .expectError(OptimisticLockingFailureException::class.java)
                .verify()

            //  Get the updated entity from the database and verify it's new state
            StepVerifier
                .create(reactiveMongoTestTemplate.findById(this.id, ReviewEntity::class.java))
                .expectNextMatches { foundEntity ->
                    foundEntity.version == currentVersion + 1
                            && foundEntity.author == "Author - 2"
                }
                .verifyComplete()
        }
    }

    private fun ReviewEntity.isEqual(other: ReviewEntity): Boolean {
        return this.id == other.id
                && this.productId == other.productId
                && this.reviewId == other.reviewId
                && this.author == other.author
                && this.rating == other.rating
                && this.content == other.content
    }
}