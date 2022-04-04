package kr.dove.coroutines.api

import java.time.ZonedDateTime
import java.util.*

data class Product(
    val id: UUID? = null,
    val productId: Long? = null,
    var name: String,
    var cost: Int = 0,
    var version: Int = 0,
    val created: ZonedDateTime? = null,
    val modified: ZonedDateTime? = null
)

data class Review(
    val id: UUID? = null,
    val productId: Long,
    val reviewId: Long? = null,
    var author: String,
    var rating: Float = 2.5f,
    var content: String = "",
    var version: Int = 0,
    val created: ZonedDateTime? = null,
    val modified: ZonedDateTime? = null,
)
