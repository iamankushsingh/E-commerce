package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.UserDTO;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.entity.UserRole;
import com.ecommerce.userservice.entity.UserStatus;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User adminUser;
    private User customerUser;
    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create admin user
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password123"));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUser = userRepository.save(adminUser);

        adminToken = jwtUtil.generateToken(
            adminUser.getEmail(),
            adminUser.getId(),
            adminUser.getRole().toString(),
            adminUser.getFirstName(),
            adminUser.getLastName()
        );

        // Create customer user
        customerUser = new User();
        customerUser.setUsername("customer");
        customerUser.setEmail("customer@example.com");
        customerUser.setPassword(passwordEncoder.encode("password123"));
        customerUser.setFirstName("Customer");
        customerUser.setLastName("User");
        customerUser.setRole(UserRole.CUSTOMER);
        customerUser.setStatus(UserStatus.ACTIVE);
        customerUser.setCreatedAt(LocalDateTime.now());
        customerUser.setUpdatedAt(LocalDateTime.now());
        customerUser = userRepository.save(customerUser);

        customerToken = jwtUtil.generateToken(
            customerUser.getEmail(),
            customerUser.getId(),
            customerUser.getRole().toString(),
            customerUser.getFirstName(),
            customerUser.getLastName()
        );
    }

    @Test
    void testGetAllUsers_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void testGetAllUsers_AsCustomer_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllUsers_WithoutToken_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllUsers_WithFilters() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .param("role", "CUSTOMER")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].role").value("CUSTOMER"));
    }

    @Test
    void testGetAllUsers_WithSearch() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .param("search", "customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("customer"));
    }

    @Test
    void testGetUserById_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + customerUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("customer@example.com"));
    }

    @Test
    void testUpdateUser_AsAdmin() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("Customer");
        updateDTO.setRole(UserRole.CUSTOMER);
        updateDTO.setStatus(UserStatus.ACTIVE);

        mockMvc.perform(put("/api/admin/users/" + customerUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.firstName").value("Updated"));

        // Verify in database
        User updatedUser = userRepository.findById(customerUser.getId()).orElseThrow();
        assertEquals("Updated", updatedUser.getFirstName());
    }

    @Test
    void testBlockUser_AsAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/users/" + customerUser.getId() + "/block")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.status").value("BLOCKED"));

        // Verify in database
        User blockedUser = userRepository.findById(customerUser.getId()).orElseThrow();
        assertEquals(UserStatus.BLOCKED, blockedUser.getStatus());
    }

    @Test
    void testUnblockUser_AsAdmin() throws Exception {
        // First block the user
        customerUser.setStatus(UserStatus.BLOCKED);
        userRepository.save(customerUser);

        // Then unblock
        mockMvc.perform(post("/api/admin/users/" + customerUser.getId() + "/unblock")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.status").value("ACTIVE"));

        // Verify in database
        User unblockedUser = userRepository.findById(customerUser.getId()).orElseThrow();
        assertEquals(UserStatus.ACTIVE, unblockedUser.getStatus());
    }

    @Test
    void testDeleteUser_AsAdmin() throws Exception {
        Long userIdToDelete = customerUser.getId();

        mockMvc.perform(delete("/api/admin/users/" + userIdToDelete)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify user is deleted
        assertFalse(userRepository.existsById(userIdToDelete));
    }

    @Test
    void testDeleteUser_AsCustomer_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + adminUser.getId())
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserStats_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(2))
                .andExpect(jsonPath("$.activeUsers").value(2))
                .andExpect(jsonPath("$.adminUsers").value(1))
                .andExpect(jsonPath("$.customerUsers").value(1));
    }

    @Test
    void testGetUserStats_AsCustomer_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/admin/users/stats")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateUser_NonExistent() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setFirstName("Test");

        mockMvc.perform(put("/api/admin/users/99999")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testBlockUser_NonExistent() throws Exception {
        mockMvc.perform(post("/api/admin/users/99999/block")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testDeleteUser_NonExistent() throws Exception {
        mockMvc.perform(delete("/api/admin/users/99999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testPagination() throws Exception {
        // Create more users for pagination test
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPassword(passwordEncoder.encode("password"));
            user.setFirstName("User");
            user.setLastName("" + i);
            user.setRole(UserRole.CUSTOMER);
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        // Test first page
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements").value(12)) // 2 original + 10 new
                .andExpect(jsonPath("$.totalPages").value(3));

        // Test second page
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)));
    }
}

