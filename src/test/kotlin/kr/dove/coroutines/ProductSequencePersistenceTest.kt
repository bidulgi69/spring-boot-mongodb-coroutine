package kr.dove.coroutines

import kr.dove.coroutines.persistence.ProductEntity
import kr.dove.coroutines.persistence.ProductSequences
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.test.context.ContextConfiguration
import reactor.test.StepVerifier

@ContextConfiguration(classes = [MongoDbTestConfiguration::class])
@DataMongoTest(excludeAutoConfiguration = [EmbeddedMongoAutoConfiguration::class])
class ProductSequencePersistenceTest(
    @Autowired private val reactiveMongoTestTemplate: ReactiveMongoTemplate,
): MongoTestContainer() {

    @Test
    @DisplayName("Verify auto sequence works fine.")
    fun isAutoSequenceWorksFine() {
        //  upsert
        StepVerifier
            .create(reactiveMongoTestTemplate
                .findAndModify(
                    Query.query(Criteria.where("id").`is`(ProductEntity.SEQUENCE_NAME)),
                    Update().inc("sequence", 1),
                    FindAndModifyOptions.options()
                        .upsert(true)
                        .returnNew(true),
                    ProductSequences::class.java
                ))
            .expectNextMatches { createdSequence ->
                //  "returnNew=true" means it will return the value after the entity is updated.
                createdSequence.sequence == 1L
            }
            .verifyComplete()

        //
        StepVerifier
            .create(reactiveMongoTestTemplate
                .findAndModify(
                    Query.query(Criteria.where("id").`is`(ProductEntity.SEQUENCE_NAME)),
                    Update().inc("sequence", 1),
                    FindAndModifyOptions.options()
                        .returnNew(true),
                    ProductSequences::class.java
                ))
            .expectNextMatches { createdSequence ->
                createdSequence.sequence == 2L
            }
            .verifyComplete()
    }

}