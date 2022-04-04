package kr.dove.coroutines

import com.mongodb.client.result.DeleteResult
import kr.dove.coroutines.persistence.ProductEntity
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
class ProductPersistenceTest(
    @Autowired private val reactiveMongoTestTemplate: ReactiveMongoTemplate,
): MongoTestContainer() {

    private var productEntity: ProductEntity? = null

    @BeforeEach
    fun setup() {
        //  cleanup
        reactiveMongoTestTemplate
            .remove(ProductEntity::class.java)
            .all()
            .block()

        //  setup
        val product = ProductEntity(
            id = UUID.randomUUID(),
            productId = 1L,
            name = "Cup - 1",
            cost = 10000
        )
        StepVerifier
            .create(
                reactiveMongoTestTemplate
                    .save(product)
            )
            .expectNextMatches { createdEntity ->
                productEntity = createdEntity
                product.isEqual(createdEntity)
            }
            .verifyComplete()
    }

    @Test
    @DisplayName("Create a new entity")
    fun create() {
        val productSequence = 2L
        val newEntity = ProductEntity(
            UUID.randomUUID(),
            productSequence,
            "Cellphone - 1",
            300000
        )

        StepVerifier
            .create(reactiveMongoTestTemplate.save(newEntity))
            .expectNextMatches { createdEntity ->
                newEntity.isEqual(createdEntity)
            }
            .verifyComplete()

        StepVerifier
            .create(reactiveMongoTestTemplate.find(
                Query.query(Criteria.where("productId").`is`(productSequence)),
                ProductEntity::class.java
            ))
            .expectNextMatches { foundEntity ->
                newEntity.isEqual(foundEntity)
            }
            .verifyComplete()

        StepVerifier
            .create(reactiveMongoTestTemplate
                .findAll(ProductEntity::class.java)
                .count()
            )
            .expectNextMatches { cnt -> cnt == 2L }
            .verifyComplete()
    }

    @Test
    @DisplayName("Update field value and verify @Version field works fine.")
    fun update() {
        assertNotNull(productEntity)
        with(productEntity!!) {
            val currentVersion = this.version
            this.name = "Cup - 2"
            StepVerifier
                .create(reactiveMongoTestTemplate.save(this))
                .expectNextMatches { updatedEntity ->
                    updatedEntity.name == "Cup - 2"
                }
                .verifyComplete()

            //  version field's value will be incremented. (by updating name field)
            StepVerifier
                .create(reactiveMongoTestTemplate.find(
                    Query.query(Criteria.where("productId").`is`(1L)),
                    ProductEntity::class.java
                ))
                .expectNextMatches { foundEntity ->
                    foundEntity.name == "Cup - 2"
                            && foundEntity.version == currentVersion + 1
                }
                .verifyComplete()
        }
    }

    @Test
    @DisplayName("Delete an entity")
    fun delete() {
        with(productEntity!!) {
            StepVerifier
                .create(reactiveMongoTestTemplate.remove(this))
                .expectNext(DeleteResult.acknowledged(1L))
                .verifyComplete()

            StepVerifier
                .create(reactiveMongoTestTemplate.exists(
                    Query.query(Criteria.where("productId").`is`(1L)),
                    ProductEntity::class.java
                ))
                .expectNext(false)  //  after deleting, the entity will be not found
                .verifyComplete()
        }
    }

    @Test
    @DisplayName("Get by productId")
    fun getByProductId() {
        with(productEntity!!) {
            StepVerifier
                .create(reactiveMongoTestTemplate.find(
                    Query.query(Criteria.where("productId").`is`(this.productId!!)),
                    ProductEntity::class.java
                ))
                .expectNextMatches { foundEntity ->
                    this.isEqual(foundEntity)
                }
                .verifyComplete()
        }
    }

    @Test
    @DisplayName("Verify unique index property on productId field works fine")
    fun isUniqueIndexPropertyWorksFine() {
        val duplicatedProductIdEntity = ProductEntity(
            UUID.randomUUID(),  //  different id
            1L, //  duplicated product id
        "",
            0
        )
        assertEquals(productEntity!!.productId, duplicatedProductIdEntity.productId)

        StepVerifier
            .create(reactiveMongoTestTemplate.save(duplicatedProductIdEntity))
            .expectError(DuplicateKeyException::class.java)
            .verify()
    }

    @Test
    @DisplayName("Verify optimistic lock(version field) works fine")
    fun isOptimisticLockWorksFine() {
        with(productEntity!!) {
            //  Store the saved entity in two separate entity objects
            val entity1 = reactiveMongoTestTemplate.findById(
                this.id, ProductEntity::class.java
            ).block()
            val entity2 = reactiveMongoTestTemplate.findById(
                this.id, ProductEntity::class.java
            ).block()

            //  Update the entity using the first entity object
            entity1!!.name = "Cup - 2"
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
                .create(reactiveMongoTestTemplate.findById(this.id, ProductEntity::class.java))
                .expectNextMatches { foundEntity ->
                    foundEntity.version == currentVersion + 1
                            && foundEntity.name == "Cup - 2"
                }
                .verifyComplete()
        }
    }

    private fun ProductEntity.isEqual(other: ProductEntity): Boolean {
        return this.id == other.id
                && this.productId == other.productId
                && this.name == other.name
                && this.cost == other.cost
    }
}