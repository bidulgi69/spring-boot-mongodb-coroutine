package kr.dove.coroutines.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kr.dove.coroutines.api.Product
import kr.dove.coroutines.api.ProductService
import kr.dove.coroutines.persistence.ProductEntity
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
@RequestMapping(value = ["/product"])
class ProductServiceImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val sequenceGenerator: SequenceGenerator,
): ProductService {

    override suspend fun get(id: Long): Product {
        return reactiveMongoTemplate.find(
            Query.query(where(ProductEntity::productId).`is`(id)), ProductEntity::class.java
        )
            .awaitFirst()
            .mapToApi()
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
}