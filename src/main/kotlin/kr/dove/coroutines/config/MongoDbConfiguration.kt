package kr.dove.coroutines.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import kr.dove.coroutines.persistence.ZonedDateTimeReadConverter
import kr.dove.coroutines.persistence.ZonedDateTimeWriteConverter
import org.bson.UuidRepresentation
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb")
@EnableReactiveMongoAuditing
class MongoDbConfiguration: AbstractReactiveMongoConfiguration() {
    lateinit var database: String
    lateinit var username: String
    lateinit var password: String
    lateinit var host: String
    lateinit var port: String

    override fun getDatabaseName(): String = database

    override fun reactiveMongoClient(): MongoClient {
        return MongoClients.create(MongoClientSettings.builder()
            .applyConnectionString(ConnectionString("mongodb://$username:$password@$host:$port/$databaseName?authSource=admin"))
            .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
            .build())
    }

    override fun reactiveMongoTemplate(
        databaseFactory: ReactiveMongoDatabaseFactory,
        mongoConverter: MappingMongoConverter
    ): ReactiveMongoTemplate {
        mongoConverter.setTypeMapper(DefaultMongoTypeMapper(null))  //  remove _class field
        return ReactiveMongoTemplate(databaseFactory, mongoConverter)
    }

    override fun configureConverters(converterConfigurationAdapter: MongoCustomConversions.MongoConverterConfigurationAdapter) {
        //  adding ZonedDateTimeConverter
        converterConfigurationAdapter.registerConverter(ZonedDateTimeReadConverter())
        converterConfigurationAdapter.registerConverter(ZonedDateTimeWriteConverter())
    }
}