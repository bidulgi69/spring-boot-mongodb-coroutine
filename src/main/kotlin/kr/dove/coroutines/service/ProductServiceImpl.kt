package kr.dove.coroutines.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kr.dove.coroutines.api.Product
import kr.dove.coroutines.api.ProductService
import kr.dove.coroutines.api.Review
import kr.dove.coroutines.persistence.ProductEntity
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.where
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping(value = ["/product"])
class ProductServiceImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val sequenceGenerator: SequenceGenerator,
    private val builder: WebClient.Builder
): ProductService {

    private val webClient: WebClient? = builder.build()

    override suspend fun get(id: Long): Product {
        val product: Product = reactiveMongoTemplate.find(
            Query.query(where(ProductEntity::productId).`is`(id)), ProductEntity::class.java
        )
            .awaitFirst()
            .mapToApi()

        return product.apply {
            this.reviews = getClient()
                .get()
                .uri("http://localhost:8080/review/$id")
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .bodyToFlow<Review>()
                .toList()
        }
    }

    override suspend fun post(product: Product): Product {
        val entity: ProductEntity = reactiveMongoTemplate.save(
            ProductEntity(
                id = UUID.randomUUID(),
                name = product.name,
                cost = product.cost
            )
        ).awaitSingle()
        entity.productId = sequenceGenerator.generateProductSequence(ProductEntity.SEQUENCE_NAME)
        return reactiveMongoTemplate.save(entity)
            .awaitSingle()
            .mapToApi()
    }

    override suspend fun put(product: Product): Product {
        return product.productId ?. let { productId ->
            reactiveMongoTemplate.findAndModify(
                Query.query(where(ProductEntity::productId).`is`(productId)),
                Update().apply {
                    set("name", product.name)
                    set("cost", product.cost)
                    set("reviews", product.reviews)
                },
                FindAndModifyOptions.options()
                    .upsert(true)
                    .returnNew(true),   //  변경 직후의 document 를 반환.
                ProductEntity::class.java
            )
                .awaitSingle()
                .mapToApi()
        } ?: run {
            post(product)
        }
    }

    override suspend fun delete(id: Long): Long {
        return reactiveMongoTemplate.findAndRemove(
            Query.query(where(ProductEntity::productId).`is`(id)), ProductEntity::class.java
        )
            .awaitFirst()
            .productId!!
    }

    override suspend fun all(): Flow<Product> {
        return reactiveMongoTemplate
            .findAll(ProductEntity::class.java)
            .flatMap { en -> Mono.just(en.mapToApi()) }
            .asFlow()
    }

    private fun getClient(): WebClient = webClient ?: builder.build()
}