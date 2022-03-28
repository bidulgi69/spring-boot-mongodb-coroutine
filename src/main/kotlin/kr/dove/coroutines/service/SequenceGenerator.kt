package kr.dove.coroutines.service

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kr.dove.coroutines.persistence.ProductSequences
import kr.dove.coroutines.persistence.ReviewSequences
import org.springframework.data.mongodb.core.FindAndModifyOptions.options
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.where
import org.springframework.stereotype.Service

@Service
class SequenceGenerator(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) {

    suspend fun generateProductSequence(seqName: String): Long {
        val counter: ProductSequences? = reactiveMongoTemplate.findAndModify(
            Query.query(
                where(ProductSequences::id).`is`(seqName),
            ),
            Update().inc("sequence", 1),
            options().returnNew(true).upsert(true),
            ProductSequences::class.java
        ).awaitSingleOrNull()

        return counter ?. let { counter.sequence } ?: run { 1L }
    }

    suspend fun generateReviewSequence(seqName: String): Long {
        val counter: ReviewSequences? = reactiveMongoTemplate.findAndModify(
            Query.query(
                where(ReviewSequences::id).`is`(seqName),
            ),
            Update().inc("sequence", 1),
            options().returnNew(true).upsert(true),
            ReviewSequences::class.java
        ).awaitSingleOrNull()

        return counter ?. let { counter.sequence } ?: run { 1L }
    }
}