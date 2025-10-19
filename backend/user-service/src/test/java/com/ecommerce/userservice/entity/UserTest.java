package com.ecommerce.userservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testDefaultConstructor() {
        User newUser = new User();
        assertNotNull(newUser);
    }

    @Test
    void testParameterizedConstructor() {
        User newUser = new User("testuser", "test@example.com", "password123", 
                               "Test", "User", UserRole.CUSTOMER);
        
        assertEquals("testuser", newUser.getUsername());
        assertEquals("test@example.com", newUser.getEmail());
        assertEquals("password123", newUser.getPassword());
        assertEquals("Test", newUser.getFirstName());
        assertEquals("User", newUser.getLastName());
        assertEquals(UserRole.CUSTOMER, newUser.getRole());
        assertEquals(UserStatus.ACTIVE, newUser.getStatus());
        assertNotNull(newUser.getCreatedAt());
        assertNotNull(newUser.getUpdatedAt());
    }

    @Test
    void testGettersAndSetters() {
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setAvatar("http://example.com/avatar.jpg");
        user.setPhoneNumber("+1234567890");
        user.setAddress("123 Main St");
        
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setLastLogin(now);

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals(UserRole.CUSTOMER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals("http://example.com/avatar.jpg", user.getAvatar());
        assertEquals("+1234567890", user.getPhoneNumber());
        assertEquals("123 Main St", user.getAddress());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
        assertEquals(now, user.getLastLogin());
    }

    @Test
    void testGetFullName() {
        user.setFirstName("Test");
        user.setLastName("User");
        
        assertEquals("Test User", user.getFullName());
    }

    @Test
    void testOnCreateCallback() {
        User newUser = new User();
        newUser.onCreate();
        
        assertNotNull(newUser.getCreatedAt());
        assertNotNull(newUser.getUpdatedAt());
        assertEquals(UserStatus.ACTIVE, newUser.getStatus());
        assertEquals(UserRole.CUSTOMER, newUser.getRole());
    }

    @Test
    void testOnCreateCallback_WithExistingStatus() {
        User newUser = new User();
        newUser.setStatus(UserStatus.BLOCKED);
        newUser.setRole(UserRole.ADMIN);
        newUser.onCreate();
        
        assertEquals(UserStatus.BLOCKED, newUser.getStatus());
        assertEquals(UserRole.ADMIN, newUser.getRole());
    }

    @Test
    void testOnUpdateCallback() {
        User existingUser = new User();
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(1);
        existingUser.setCreatedAt(originalCreatedAt);
        existingUser.setUpdatedAt(originalCreatedAt);
        
        existingUser.onUpdate();
        
        assertEquals(originalCreatedAt, existingUser.getCreatedAt());
        assertNotEquals(originalCreatedAt, existingUser.getUpdatedAt());
    }

    @Test
    void testAllRoles() {
        user.setRole(UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, user.getRole());
        
        user.setRole(UserRole.CUSTOMER);
        assertEquals(UserRole.CUSTOMER, user.getRole());
    }

    @Test
    void testAllStatuses() {
        user.setStatus(UserStatus.ACTIVE);
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        
        user.setStatus(UserStatus.BLOCKED);
        assertEquals(UserStatus.BLOCKED, user.getStatus());
        
        user.setStatus(UserStatus.INACTIVE);
        assertEquals(UserStatus.INACTIVE, user.getStatus());
    }
}

