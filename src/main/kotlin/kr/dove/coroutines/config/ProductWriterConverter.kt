package kr.dove.coroutines.config

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import kr.dove.coroutines.persistence.ProductEntity
import org.springframework.core.convert.converter.Converter

/**
 * custom converter
 */
//@Component
class ProductWriterConverter: Converter<ProductEntity, DBObject> {
    override fun convert(source: ProductEntity): DBObject {
        val dbObject = BasicDBObject()
        dbObject["id"] = source.id
        dbObject["productId"] = source.productId
        dbObject["name"] = source.name
        dbObject["cost"] = source.cost
        dbObject.removeField("_class")
        return dbObject
    }
}