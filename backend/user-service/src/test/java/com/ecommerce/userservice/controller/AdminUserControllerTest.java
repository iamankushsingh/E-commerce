package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.*;
import com.ecommerce.userservice.entity.UserRole;
import com.ecommerce.userservice.entity.UserStatus;
import com.ecommerce.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO testUserDTO;
    private PaginatedResponse<UserDTO> paginatedResponse;
    private UserService.UserStatsDTO userStatsDTO;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setFirstName("Test");
        testUserDTO.setLastName("User");
        testUserDTO.setRole(UserRole.CUSTOMER);
        testUserDTO.setStatus(UserStatus.ACTIVE);

        paginatedResponse = new PaginatedResponse<>(Arrays.asList(testUserDTO), 0, 10, 1L);

        userStatsDTO = new UserService.UserStatsDTO();
        userStatsDTO.setTotalUsers(100L);
        userStatsDTO.setActiveUsers(80L);
        userStatsDTO.setBlockedUsers(20L);
        userStatsDTO.setAdminUsers(5L);
        userStatsDTO.setCustomerUsers(95L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers_Success() throws Exception {
        when(userService.getAllUsers(any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/admin/users")
                .with(csrf())
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value(testUserDTO.getEmail()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(userService).getAllUsers(any(), any(), any(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers_WithFilters() throws Exception {
        when(userService.getAllUsers(any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/admin/users")
                .with(csrf())
                .param("role", "CUSTOMER")
                .param("status", "ACTIVE")
                .param("search", "test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());

        verify(userService).getAllUsers(any(), any(), any(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers_Exception() throws Exception {
        when(userService.getAllUsers(any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/admin/users")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUserDTO));

        mockMvc.perform(get("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUserDTO.getEmail()));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById_Exception() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser_Success() throws Exception {
        when(userService.updateUser(anyLong(), any(UserDTO.class))).thenReturn(testUserDTO);

        mockMvc.perform(put("/api/admin/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully"));

        verify(userService).updateUser(anyLong(), any(UserDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser_NotFound() throws Exception {
        when(userService.updateUser(anyLong(), any(UserDTO.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/admin/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser_Exception() throws Exception {
        when(userService.updateUser(anyLong(), any(UserDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/api/admin/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testBlockUser_Success() throws Exception {
        testUserDTO.setStatus(UserStatus.BLOCKED);
        when(userService.blockUser(1L)).thenReturn(testUserDTO);

        mockMvc.perform(post("/api/admin/users/1/block")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User blocked successfully"));

        verify(userService).blockUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testBlockUser_NotFound() throws Exception {
        when(userService.blockUser(1L))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/api/admin/users/1/block")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testBlockUser_Exception() throws Exception {
        when(userService.blockUser(1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/admin/users/1/block")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUnblockUser_Success() throws Exception {
        when(userService.unblockUser(1L)).thenReturn(testUserDTO);

        mockMvc.perform(post("/api/admin/users/1/unblock")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User unblocked successfully"));

        verify(userService).unblockUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUnblockUser_NotFound() throws Exception {
        when(userService.unblockUser(1L))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/api/admin/users/1/unblock")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUnblockUser_Exception() throws Exception {
        when(userService.unblockUser(1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/admin/users/1/unblock")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_NotFound() throws Exception {
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_Exception() throws Exception {
        doThrow(new RuntimeException("Unexpected error")).when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserStats_Success() throws Exception {
        when(userService.getUserStats()).thenReturn(userStatsDTO);

        mockMvc.perform(get("/api/admin/users/stats")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.activeUsers").value(80))
                .andExpect(jsonPath("$.blockedUsers").value(20))
                .andExpect(jsonPath("$.adminUsers").value(5))
                .andExpect(jsonPath("$.customerUsers").value(95));

        verify(userService).getUserStats();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserStats_Exception() throws Exception {
        when(userService.getUserStats())
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/admin/users/stats")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}

