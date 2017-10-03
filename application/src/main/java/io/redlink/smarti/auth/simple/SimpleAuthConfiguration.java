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
package io.redlink.smarti.auth.simple;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.UUID;

@Configuration
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SimpleAuthConfiguration extends WebSecurityConfigurerAdapter {

    private Logger log = LoggerFactory.getLogger(SimpleAuthConfiguration.class);

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/rocket/**", "/conversation/**").permitAll()
                .anyRequest().hasRole("ADMIN")
                .and()
            .httpBasic()
                .realmName("smarti admin area")
                .and()
            .csrf()
                .disable()
        ;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, @Value("${security.password:}") String password) throws Exception {
        if (StringUtils.isBlank(password)) {
            password = UUID.randomUUID().toString();
            log.error("No password configured in 'security.password', using '{}' for user 'admin'", password);
        } else {
            log.info("Configuring password from 'security.password' for user 'admin'");
        }
        auth
            .inMemoryAuthentication()
                .withUser("admin").password(password).roles("ADMIN");
    }



}
