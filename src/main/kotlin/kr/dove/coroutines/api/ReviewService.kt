package kr.dove.coroutines.api

import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.*

interface ReviewService {

    @GetMapping(
        value = ["/{productId}"],
        produces = ["application/x-ndjson"]
    )
    suspend fun get(@PathVariable(name = "productId") productId: Long): Flow<Review>

    @PostMapping(
        value = ["/{productId}"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    suspend fun post(@PathVariable(name = "productId") productId: Long,
                     @RequestBody review: Review): Review

    @PutMapping(
        value = ["/"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    suspend fun put(@RequestBody review: Review): Review

    @DeleteMapping(
        value = ["/{id}"],
        produces = ["application/json"]
    )
    suspend fun delete(@PathVariable(name = "id") id: Long): Long
}