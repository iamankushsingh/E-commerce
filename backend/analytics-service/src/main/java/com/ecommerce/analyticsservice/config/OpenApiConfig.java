package com.ecommerce.analyticsservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:" + serverPort);
        devServer.setDescription("Development server");

        Contact contact = new Contact();
        contact.setName("E-commerce Team");
        contact.setEmail("team@ecommerce.com");
        contact.setUrl("https://www.ecommerce.com");

        License license = new License()
            .name("Apache 2.0")
            .url("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
            .title("Analytics Service API")
            .version("1.0.0")
            .contact(contact)
            .description("This API exposes endpoints for business analytics, reporting, and dashboard data.")
            .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}
