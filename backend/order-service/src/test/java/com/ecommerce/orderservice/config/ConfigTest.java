package com.ecommerce.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void testModelMapperConfig() {
        ModelMapperConfig config = new ModelMapperConfig();
        ModelMapper modelMapper = config.modelMapper();
        
        assertNotNull(modelMapper);
    }

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
        ReflectionTestUtils.setField(config, "serverPort", "8081");
        OpenAPI openAPI = config.myOpenAPI();
        
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Order Service API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void testCorsConfig() {
        CorsConfig config = new CorsConfig();
        CorsConfigurationSource source = config.corsConfigurationSource();
        
        assertNotNull(source);
    }
}

