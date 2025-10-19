package com.ecommerce.analyticsservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void testWebClientConfig() {
        WebClientConfig config = new WebClientConfig();
        WebClient.Builder builder = config.loadBalancedWebClientBuilder();
        
        assertNotNull(builder);
        
        WebClient webClient = builder.build();
        assertNotNull(webClient);
    }

    @Test
    void testOpenApiConfig() {
        OpenApiConfig config = new OpenApiConfig();
        ReflectionTestUtils.setField(config, "serverPort", "8083");
        OpenAPI openAPI = config.myOpenAPI();
        
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Analytics Service API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }
}

