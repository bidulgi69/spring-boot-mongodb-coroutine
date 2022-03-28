package kr.dove.coroutines.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "sequence_product")
data class ProductSequences(
    @Id val id: String,
    var sequence: Long,
)

@Document(collection = "sequence_review")
data class ReviewSequences(
    @Id val id: String,
    var sequence: Long,
)