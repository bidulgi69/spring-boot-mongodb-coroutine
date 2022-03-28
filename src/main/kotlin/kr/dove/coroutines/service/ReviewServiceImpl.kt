package kr.dove.coroutines.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kr.dove.coroutines.api.Review
import kr.dove.coroutines.api.ReviewService
import kr.dove.coroutines.persistence.ReviewEntity
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.where
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping(value = ["/review"])
class ReviewServiceImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val sequenceGenerator: SequenceGenerator,
): ReviewService {
    override suspend fun get(productId: Long): Flow<Review> {
        return reactiveMongoTemplate.find(
            Query.query(where(ReviewEntity::productId).`is`(productId)),
            ReviewEntity::class.java
        )
            .flatMap { Mono.just(it.mapToApi()) }
            .asFlow()
    }

    override suspend fun post(productId: Long, review: Review): Review {
        val entity: ReviewEntity = reactiveMongoTemplate.save(
            ReviewEntity(
                id = UUID.randomUUID(),
                productId = productId,
                author = review.author,
                rating = review.rating,
                content = review.content
            )
        ).awaitSingle()
        entity.reviewId = sequenceGenerator.generateReviewSequence(ReviewEntity.SEQUENCE_NAME)
        return reactiveMongoTemplate.save(entity)
            .awaitSingle()
            .mapToApi()
    }

    override suspend fun put(review: Review): Review {
        return review.reviewId ?. let { reviewId ->
            reactiveMongoTemplate.findAndModify(
                Query.query(where(ReviewEntity::reviewId).`is`(reviewId)),
                Update().apply {
                    set("author", review.author)
                    set("rating", review.rating)
                    set("content", review.content)
                },
                FindAndModifyOptions.options()
                    .upsert(false)
                    .returnNew(true),
                ReviewEntity::class.java
            )
                .awaitSingle()
                .mapToApi()
        } ?: run {
            post(review.productId, review)
        }
    }

    override suspend fun delete(id: Long): Long {
        return reactiveMongoTemplate.findAndRemove(
            Query.query(where(ReviewEntity::reviewId).`is`(id)),
            ReviewEntity::class.java
        )
            .awaitSingle()
            .reviewId!!
    }
}