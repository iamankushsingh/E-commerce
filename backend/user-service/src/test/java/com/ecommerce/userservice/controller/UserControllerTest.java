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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO testUserDTO;
    private UserRegistrationDTO registrationDTO;
    private UserLoginDTO loginDTO;

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

        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("newuser");
        registrationDTO.setEmail("newuser@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setFirstName("New");
        registrationDTO.setLastName("User");

        loginDTO = new UserLoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");
    }

    @Test
    @WithMockUser
    void testRegisterUser_Success() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(testUserDTO);

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.user.email").value(testUserDTO.getEmail()));

        verify(userService).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @WithMockUser
    void testRegisterUser_EmailAlreadyExists() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new RuntimeException("Email already exists: " + registrationDTO.getEmail()));

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser
    void testRegisterUser_UnexpectedException() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testLoginUser_Success() throws Exception {
        when(userService.loginUser(any(UserLoginDTO.class))).thenReturn(Optional.of(testUserDTO));

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.user.email").value(testUserDTO.getEmail()));

        verify(userService).loginUser(any(UserLoginDTO.class));
    }

    @Test
    @WithMockUser
    void testLoginUser_InvalidCredentials() throws Exception {
        when(userService.loginUser(any(UserLoginDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @WithMockUser
    void testLoginUser_Exception() throws Exception {
        when(userService.loginUser(any(UserLoginDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    void testGetUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUserDTO));

        mockMvc.perform(get("/api/users/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUserDTO.getEmail()));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser
    void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/1")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetUserById_Exception() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/users/1")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testUpdateUser_Success() throws Exception {
        when(userService.updateUser(anyLong(), any(UserDTO.class))).thenReturn(testUserDTO);

        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                .andExpect(jsonPath("$.user.email").value(testUserDTO.getEmail()));

        verify(userService).updateUser(anyLong(), any(UserDTO.class));
    }

    @Test
    @WithMockUser
    void testUpdateUser_NotFound() throws Exception {
        when(userService.updateUser(anyLong(), any(UserDTO.class)))
                .thenThrow(new RuntimeException("User not found with ID: 1"));

        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    void testUpdateUser_Exception() throws Exception {
        when(userService.updateUser(anyLong(), any(UserDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testGetUserByEmail_Success() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(testUserDTO));

        mockMvc.perform(get("/api/users/email/test@example.com")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUserDTO.getEmail()));

        verify(userService).getUserByEmail(anyString());
    }

    @Test
    @WithMockUser
    void testGetUserByEmail_NotFound() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/email/notfound@example.com")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetUserByEmail_Exception() throws Exception {
        when(userService.getUserByEmail(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/users/email/test@example.com")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}

