package kr.dove.coroutines

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import kr.dove.coroutines.persistence.ZonedDateTimeReadConverter
import kr.dove.coroutines.persistence.ZonedDateTimeWriteConverter
import org.bson.UuidRepresentation
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

@TestConfiguration
@EnableReactiveMongoAuditing
class MongoDbTestConfiguration: AbstractReactiveMongoConfiguration() {
    override fun getDatabaseName(): String = "test-product"

    override fun reactiveMongoClient(): MongoClient {
        val host = MongoTestContainer.CONTAINER.host
        val port = MongoTestContainer.CONTAINER.firstMappedPort

        return MongoClients.create(MongoClientSettings.builder()
            .applyConnectionString(ConnectionString("mongodb://$host:$port/$databaseName?authSource=admin"))
            .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
            .build())
    }

    @Bean(
        "reactiveMongoTestTemplate",
        "reactiveMongoTemplate" //  for use in SequenceGenerator class
    )
    override fun reactiveMongoTemplate(
        databaseFactory: ReactiveMongoDatabaseFactory,
        mongoConverter: MappingMongoConverter
    ): ReactiveMongoTemplate {
        mongoConverter.setTypeMapper(DefaultMongoTypeMapper(null))
        return ReactiveMongoTemplate(databaseFactory, mongoConverter)
    }

    override fun configureConverters(converterConfigurationAdapter: MongoCustomConversions.MongoConverterConfigurationAdapter) {
        converterConfigurationAdapter.registerConverter(ZonedDateTimeReadConverter())
        converterConfigurationAdapter.registerConverter(ZonedDateTimeWriteConverter())
    }

    //  enable @Indexed annotation
    override fun autoIndexCreation(): Boolean = true
}