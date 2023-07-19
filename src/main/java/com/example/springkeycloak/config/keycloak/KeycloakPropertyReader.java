package com.example.springkeycloak.config.keycloak;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.yaml")
@AllArgsConstructor
public class KeycloakPropertyReader {

    private final Environment environment;

    public String getProperty(String key){
        return environment.getProperty(key);
    }
}
