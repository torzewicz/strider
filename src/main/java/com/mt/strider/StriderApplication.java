package com.mt.strider;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StriderApplication {

    @Value("${googleApiKey}")
    private String googleApiKey;

    @Bean
    public GeoApiContext getGeoApiContext() {
        return new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(StriderApplication.class, args);
    }

}
