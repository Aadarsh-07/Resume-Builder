package com.adarsh.resumebuilder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/** Enables @CreatedDate / @LastModifiedDate auditing on documents. */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
