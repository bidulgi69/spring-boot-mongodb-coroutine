package kr.dove.coroutines.api

import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.*

interface ProductService {

    @GetMapping(
        value = ["/{id}"],
        produces = ["application/json"]
    )
    suspend fun get(@PathVariable(name = "id") id: Long): Product

    @PostMapping(
        value = ["/"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    suspend fun post(@RequestBody product: Product): Product

    @PutMapping(
        value = ["/"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    suspend fun put(@RequestBody product: Product): Product

    @DeleteMapping(
        value = ["/{id}"],
        produces = ["application/json"]
    )
    suspend fun delete(@PathVariable(name = "id") id: Long): Long

    @GetMapping(
        value = ["/all"],
        produces = ["application/x-ndjson"]
    )
    suspend fun all(): Flow<Product>
}