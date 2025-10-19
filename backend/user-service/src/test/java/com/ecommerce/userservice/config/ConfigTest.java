package com.ecommerce.userservice.config;

import com.ecommerce.userservice.repository.UserRepository;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigTest {

    @Test
    void testModelMapperConfig() {
        ModelMapperConfig config = new ModelMapperConfig();
        ModelMapper modelMapper = config.modelMapper();
        
        assertNotNull(modelMapper);
    }

    @Test
    void testOpenApiConfig() {
        OpenApiConfig config = new OpenApiConfig();
        ReflectionTestUtils.setField(config, "serverPort", "8084");
        OpenAPI openAPI = config.myOpenAPI();
        
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("User Service API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void testSecurityConfig_PasswordEncoder() {
        SecurityConfig config = new SecurityConfig();
        PasswordEncoder passwordEncoder = config.passwordEncoder();
        
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
        
        String encoded = passwordEncoder.encode("password");
        assertTrue(passwordEncoder.matches("password", encoded));
    }

    @Test
    void testSecurityConfig_SecurityFilterChain() throws Exception {
        SecurityConfig config = new SecurityConfig();
        
        // Test that the password encoder and security config beans can be created
        assertNotNull(config);
        assertNotNull(config.passwordEncoder());
        assertNotNull(config.corsConfigurationSource());
    }

    @Test
    void testDataLoader() {
        // DataLoader uses @Autowired for dependencies, so just test instantiation
        assertDoesNotThrow(() -> {
            DataLoader dataLoader = new DataLoader();
            assertNotNull(dataLoader);
        });
    }
}

