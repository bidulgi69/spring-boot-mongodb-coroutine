package kr.dove.coroutines.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.dove.coroutines.persistence.ReviewEntity
import kr.dove.coroutines.service.SequenceGenerator
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent

//  If you use the ReactiveMongoRepository,
//  it is recommended to perform the event behavior using a function such as flatMap() or zipWith() rather than utilizing the event listener.
//@Component
class ReviewConvertListener(
    private val sequenceGenerator: SequenceGenerator,
): AbstractMongoEventListener<ReviewEntity>() {

    override fun onBeforeConvert(event: BeforeConvertEvent<ReviewEntity>) {
        CoroutineScope(Dispatchers.Default). launch {
            event
                .source
                .reviewId = sequenceGenerator.generateReviewSequence(ReviewEntity.SEQUENCE_NAME)
        }
    }
}