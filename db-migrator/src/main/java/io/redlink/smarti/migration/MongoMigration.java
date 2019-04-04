/*
 * Copyright 2017 Redlink GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.redlink.smarti.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;

@SpringBootApplication
@ComponentScan(basePackageClasses = MongoMigration.class)
@EnableAutoConfiguration
public class MongoMigration {

    private static Logger log = LoggerFactory.getLogger(MongoMigration.class);

    public static void main(String... args) {
        final SpringApplication app = new SpringApplication(MongoMigration.class);
        app.setBannerMode(Banner.Mode.OFF);

        boolean hasError = false;
        try (ConfigurableApplicationContext context = app.run(args)) {
            context.getBean(MongoDbMigrationService.class).runDatabaseMigration();
        } catch (IOException e) {
            hasError = true;
            log.error("Error while running database-migration: {}", e.getMessage(), e);
        }

        if (hasError) System.exit(1);
    }

}
