package kr.dove.coroutines

import kr.dove.coroutines.api.Product
import kr.dove.coroutines.persistence.ProductEntity
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
class ProductServiceTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val reactiveMongoTestTemplate: ReactiveMongoTemplate,
): MongoTestContainer() {

    @BeforeEach
    fun cleanup() {
        reactiveMongoTestTemplate
            .remove(ProductEntity::class.java)
            .all()
            .block()
    }

    @Test
    @DisplayName("Post a new product")
    fun post() {
        val product = Product(
            name = "Cup - 1",
            cost = 38000
        )
        sendCreateNewProduct(product).value {
            Assertions.assertNotNull(it.id)
            Assertions.assertNotNull(it.productId)
            Assertions.assertEquals(product.name, it.name)
            Assertions.assertEquals(product.cost, it.cost)
            Assertions.assertEquals(2, it.version)
        }
    }

    @Test
    @DisplayName("Update fields of product")
    fun put() {
        var product = Product(
            name = "Cup - 1",
            cost = 38000
        )

        sendCreateNewProduct(product)
            .value { saved ->
                Assertions.assertNotNull(saved.id)
                Assertions.assertNotNull(saved.productId)
                Assertions.assertEquals(product.name, saved.name)
                Assertions.assertEquals(product.cost, saved.cost)
                //  apply changes
                product = saved
            }

        product.name = "Cup - 2"
        product.cost = 40000
        webTestClient
            .put()
            .uri("/product/")
            .bodyValue(product)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(Product::class.java)
            .value {
                Assertions.assertNotNull(it.id)
                Assertions.assertEquals(product.productId, it.productId)
                Assertions.assertEquals(product.name, it.name)
                Assertions.assertEquals(product.cost, it.cost)
                Assertions.assertEquals(3, it.version)
            }
    }

    @Test
    @DisplayName("Get product by productId")
    fun get() {
        var product = Product(
            name = "Cup - 1",
            cost = 38000
        )
        sendCreateNewProduct(product)
            .value { product = it }

        webTestClient
            .get()
            .uri("/product/${product.productId}")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(Product::class.java)
            .value { found ->
                Assertions.assertEquals(product.id, found.id)
                Assertions.assertEquals(product.productId, found.productId)
                Assertions.assertEquals(product.name, found.name)
                Assertions.assertEquals(product.cost, found.cost)
                Assertions.assertEquals(product.version, found.version)
                Assertions.assertEquals(product.created, found.created)
                Assertions.assertEquals(product.modified, found.modified)
            }
    }

    @Test
    @DisplayName("Delete product by productId")
    fun delete() {
        var product = Product(
            name = "Cup - 1",
            cost = 38000
        )
        sendCreateNewProduct(product)
            .value { product = it }

        webTestClient
            .delete()
            .uri("/product/${product.productId}")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(Long::class.java)
            .value {
                Assertions.assertEquals(product.productId, it)
            }
    }

    @Test
    @DisplayName("Read all products saved in database")
    fun findAll() {
        sendCreateNewProduct(Product(
            name = "Cup - 1",
            cost = 10000
        ))
        sendCreateNewProduct(
            Product(
            name = "Cup - 2",
            cost = 20000
        ))
        sendCreateNewProduct(Product(
            name = "Cup - 3",
            cost = 30000
        ))

        val names = listOf("Cup - 1", "Cup - 2", "Cup - 3")
        val costs = listOf(10000, 20000, 30000)

        webTestClient
            .get()
            .uri("/product/all")
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBodyList(Product::class.java)
            .value<WebTestClient.ListBodySpec<Product>> {
                Assertions.assertEquals(3, it.size)
                it.forEach { prd ->
                    Assertions.assertNotNull(prd.id)
                    Assertions.assertNotNull(prd.productId)
                    Assertions.assertTrue(prd.name in names)
                    Assertions.assertTrue(prd.cost in costs)
                }
            }
    }

    private fun sendCreateNewProduct(product: Product): WebTestClient.BodySpec<Product, *> =
        webTestClient
            .post()
            .uri("/product/")
            .bodyValue(product)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(Product::class.java)
}