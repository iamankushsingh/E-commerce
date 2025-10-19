package com.ecommerce.userservice.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumTest {

    @Test
    void testUserRoleValues() {
        assertEquals(2, UserRole.values().length);
        assertEquals(UserRole.CUSTOMER, UserRole.valueOf("CUSTOMER"));
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
    }

    @Test
    void testUserStatusValues() {
        assertEquals(3, UserStatus.values().length);
        assertEquals(UserStatus.ACTIVE, UserStatus.valueOf("ACTIVE"));
        assertEquals(UserStatus.BLOCKED, UserStatus.valueOf("BLOCKED"));
        assertEquals(UserStatus.INACTIVE, UserStatus.valueOf("INACTIVE"));
    }

    @Test
    void testUserRoleEnumProperties() {
        for (UserRole role : UserRole.values()) {
            assertNotNull(role.name());
            assertNotNull(role.toString());
        }
    }

    @Test
    void testUserStatusEnumProperties() {
        for (UserStatus status : UserStatus.values()) {
            assertNotNull(status.name());
            assertNotNull(status.toString());
        }
    }
}

