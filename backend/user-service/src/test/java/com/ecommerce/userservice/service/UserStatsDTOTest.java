package com.ecommerce.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserStatsDTOTest {

    private UserService.UserStatsDTO userStatsDTO;

    @BeforeEach
    void setUp() {
        userStatsDTO = new UserService.UserStatsDTO();
    }

    @Test
    void testConstructor() {
        assertNotNull(userStatsDTO);
    }

    @Test
    void testGettersAndSetters() {
        userStatsDTO.setTotalUsers(100L);
        userStatsDTO.setActiveUsers(80L);
        userStatsDTO.setBlockedUsers(20L);
        userStatsDTO.setAdminUsers(5L);
        userStatsDTO.setCustomerUsers(95L);

        assertEquals(100L, userStatsDTO.getTotalUsers());
        assertEquals(80L, userStatsDTO.getActiveUsers());
        assertEquals(20L, userStatsDTO.getBlockedUsers());
        assertEquals(5L, userStatsDTO.getAdminUsers());
        assertEquals(95L, userStatsDTO.getCustomerUsers());
    }

    @Test
    void testDefaultValues() {
        assertEquals(0L, userStatsDTO.getTotalUsers());
        assertEquals(0L, userStatsDTO.getActiveUsers());
        assertEquals(0L, userStatsDTO.getBlockedUsers());
        assertEquals(0L, userStatsDTO.getAdminUsers());
        assertEquals(0L, userStatsDTO.getCustomerUsers());
    }
}

