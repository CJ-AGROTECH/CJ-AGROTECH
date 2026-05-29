package com.cj.agrotech.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.bson.UuidRepresentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
@Slf4j
public class MongoConfig {

    @Bean
    public MongoClient mongoClient(Environment environment) {
        String uri = environment.getProperty("spring.data.mongodb.uri");

        if (uri == null || uri.isBlank()) {
            uri = environment.getProperty("SPRING_DATA_MONGODB_URI");
        }

        if (uri == null || uri.isBlank()) {
            String host = environment.getProperty("MONGO_HOST", "localhost");
            String port = environment.getProperty("MONGO_PORT", "27017");
            String database = environment.getProperty("MONGO_DB", "agrotech_telemetria");
            uri = "mongodb://" + host + ":" + port + "/" + database;
        }

        log.info("Configured MongoDB URI: {}", uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient, Environment environment) {
        String database = environment.getProperty("spring.data.mongodb.database");
        if (database == null || database.isBlank()) {
            database = environment.getProperty("MONGO_DB", "agrotech_telemetria");
        }
        log.info("Configured MongoDB database: {}", database);
        return new SimpleMongoClientDatabaseFactory(mongoClient, database);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}
