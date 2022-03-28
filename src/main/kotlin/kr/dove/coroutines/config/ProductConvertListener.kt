package kr.dove.coroutines.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.dove.coroutines.persistence.ProductEntity
import kr.dove.coroutines.service.SequenceGenerator
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent

//  If you use the ReactiveMongoRepository,
//  it is recommended to perform the event behavior using a function such as flatMap() or zipWith() rather than utilizing the event listener.
//@Component
class ProductConvertListener(
    private val sequenceGenerator: SequenceGenerator,
): AbstractMongoEventListener<ProductEntity>() {

    override fun onBeforeConvert(event: BeforeConvertEvent<ProductEntity>) {
        CoroutineScope(Dispatchers.Default). launch {
            event
                .source
                .productId = sequenceGenerator.generateProductSequence(ProductEntity.SEQUENCE_NAME)
        }
    }
}