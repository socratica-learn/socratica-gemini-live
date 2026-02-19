package com.socratica;

import com.socratica.config.OAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main entry point for the Socratica application.
 */
@SpringBootApplication
@EnableConfigurationProperties(OAuthProperties.class)
public class SocraticaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocraticaApplication.class, args);
    }
}

