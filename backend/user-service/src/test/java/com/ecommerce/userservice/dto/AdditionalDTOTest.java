package com.ecommerce.userservice.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AdditionalDTOTest {

    @Test
    void testUserRegistrationDTO() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPhoneNumber("1234567890");

        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("password123", dto.getPassword());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("1234567890", dto.getPhoneNumber());
    }

    @Test
    void testUserLoginDTO() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        assertEquals("test@example.com", dto.getEmail());
        assertEquals("password123", dto.getPassword());
    }

    @Test
    void testPaginatedResponse() {
        UserDTO userDTO = new UserDTO();
        PaginatedResponse<UserDTO> response = new PaginatedResponse<>(
                Arrays.asList(userDTO), 0, 10, 1L);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(1L, response.getTotalElements());
    }

    @Test
    void testPaginatedResponseSetters() {
        PaginatedResponse<UserDTO> response = new PaginatedResponse<>();
        response.setContent(Arrays.asList(new UserDTO()));
        response.setPageNumber(1);
        response.setPageSize(20);
        response.setTotalElements(50L);

        assertEquals(1, response.getPageNumber());
        assertEquals(20, response.getPageSize());
        assertEquals(50L, response.getTotalElements());
    }
}

