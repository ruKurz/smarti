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
package io.redlink.smarti.auth.mongo;

import io.redlink.smarti.auth.SecurityConfigurationProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.encoding.BasePasswordEncoder;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;

@Configuration
@ConditionalOnProperty(prefix = "security.config", name = "implementation", havingValue = "mongo", matchIfMissing = true)
@EnableConfigurationProperties(SecurityConfigurationProperties.class)
public class MongoPasswordHasherConfiguration {

    private final BasePasswordEncoder passwordEncoder;

    public MongoPasswordHasherConfiguration(SecurityConfigurationProperties securityConfig) {
        if (StringUtils.isNotBlank(securityConfig.getMongo().getPasswordHasher())) {
            passwordEncoder = new MessageDigestPasswordEncoder(securityConfig.getMongo().getPasswordHasher());
        } else {
            passwordEncoder = null;
        }
    }

    @Bean
    public BasePasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        if (passwordEncoder != null) return p -> passwordEncoder.encodePassword(p, null);
        return s->s;
    }

    public interface PasswordEncoder {
        String encodePassword(String password);
    }

}
