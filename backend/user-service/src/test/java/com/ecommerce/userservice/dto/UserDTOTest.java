package com.ecommerce.userservice.dto;

import com.ecommerce.userservice.entity.UserRole;
import com.ecommerce.userservice.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
    }

    @Test
    void testConstructor() {
        assertNotNull(userDTO);
    }

    @Test
    void testGettersAndSetters() {
        LocalDateTime now = LocalDateTime.now();
        
        userDTO.setId(1L);
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setRole(UserRole.CUSTOMER);
        userDTO.setStatus(UserStatus.ACTIVE);
        userDTO.setAvatar("http://example.com/avatar.jpg");
        userDTO.setPhoneNumber("+1234567890");
        userDTO.setAddress("123 Main St");
        userDTO.setCreatedAt(now);
        userDTO.setUpdatedAt(now);
        userDTO.setLastLogin(now);

        assertEquals(1L, userDTO.getId());
        assertEquals("testuser", userDTO.getUsername());
        assertEquals("test@example.com", userDTO.getEmail());
        assertEquals("Test", userDTO.getFirstName());
        assertEquals("User", userDTO.getLastName());
        assertEquals(UserRole.CUSTOMER, userDTO.getRole());
        assertEquals(UserStatus.ACTIVE, userDTO.getStatus());
        assertEquals("http://example.com/avatar.jpg", userDTO.getAvatar());
        assertEquals("+1234567890", userDTO.getPhoneNumber());
        assertEquals("123 Main St", userDTO.getAddress());
        assertEquals(now, userDTO.getCreatedAt());
        assertEquals(now, userDTO.getUpdatedAt());
        assertEquals(now, userDTO.getLastLogin());
    }

    @Test
    void testGetFullName() {
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        
        assertEquals("Test User", userDTO.getFullName());
    }
}

