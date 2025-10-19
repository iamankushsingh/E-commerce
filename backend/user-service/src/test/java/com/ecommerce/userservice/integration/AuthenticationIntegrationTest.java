package com.ecommerce.userservice.integration;

import com.ecommerce.userservice.dto.UserLoginDTO;
import com.ecommerce.userservice.dto.UserRegistrationDTO;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

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

    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.CUSTOMER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("123 Test St");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        jwtToken = jwtUtil.generateToken(
            testUser.getEmail(),
            testUser.getId(),
            testUser.getRole().toString(),
            testUser.getFirstName(),
            testUser.getLastName()
        );
    }

    @Test
    void testCompleteRegistrationAndLoginFlow() throws Exception {
        // Step 1: Register new user
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("newuser");
        registrationDTO.setEmail("newuser@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setFirstName("New");
        registrationDTO.setLastName("User");
        registrationDTO.setPhoneNumber("9876543210");
        registrationDTO.setAddress("456 New St");

        MvcResult registrationResult = mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
                .andReturn();

        // Verify user is in database
        assertTrue(userRepository.existsByEmail("newuser@example.com"));

        // Step 2: Login with new user
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setEmail("newuser@example.com");
        loginDTO.setPassword("password123");

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("newuser@example.com"));
    }

    @Test
    void testLoginWithValidCredentials() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.id").value(testUser.getId()))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void testLoginWithInvalidPassword() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("wrongpassword");

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void testLoginWithNonExistentUser() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setEmail("nonexistent@example.com");
        loginDTO.setPassword("password123");

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testLoginWithBlockedUser() throws Exception {
        testUser.setStatus(UserStatus.BLOCKED);
        userRepository.save(testUser);

        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetUserProfileWithValidToken() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"))
                .andExpect(jsonPath("$.address").value("123 Test St"));
    }

    @Test
    void testGetUserProfileWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUserProfileWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId())
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateUserProfileWithValidToken() throws Exception {
        testUser.setFirstName("Updated");
        testUser.setLastName("Name");
        testUser.setPhoneNumber("9999999999");

        mockMvc.perform(put("/api/users/" + testUser.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.firstName").value("Updated"))
                .andExpect(jsonPath("$.user.phoneNumber").value("9999999999"));

        // Verify in database
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("9999999999", updatedUser.getPhoneNumber());
    }

    @Test
    void testRegisterWithDuplicateEmail() throws Exception {
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("differentuser");
        registrationDTO.setEmail("test@example.com"); // Duplicate email
        registrationDTO.setPassword("password123");
        registrationDTO.setFirstName("Test");
        registrationDTO.setLastName("User");

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Email already exists")));
    }

    @Test
    void testRegisterWithDuplicateUsername() throws Exception {
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser"); // Duplicate username
        registrationDTO.setEmail("different@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setFirstName("Test");
        registrationDTO.setLastName("User");

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Username already exists")));
    }

    @Test
    void testRegisterWithoutLastName() throws Exception {
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("noname");
        registrationDTO.setEmail("noname@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setFirstName("No");
        // No last name set

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // Verify default last name
        User user = userRepository.findByEmail("noname@example.com").orElseThrow();
        assertEquals("User", user.getLastName());
    }

    @Test
    void testGetUserByEmail() throws Exception {
        mockMvc.perform(get("/api/users/email/test@example.com")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testJwtTokenExpiration() {
        // Given
        String token = jwtToken;

        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void testJwtTokenValidation() {
        // Given
        String email = jwtUtil.extractEmail(jwtToken);

        // When
        boolean isValid = jwtUtil.validateToken(jwtToken, email);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testJwtTokenContainsCorrectClaims() {
        // When
        String email = jwtUtil.extractEmail(jwtToken);
        Long userId = jwtUtil.extractUserId(jwtToken);
        String role = jwtUtil.extractRole(jwtToken);

        // Then
        assertEquals(testUser.getEmail(), email);
        assertEquals(testUser.getId(), userId);
        assertEquals(testUser.getRole().toString(), role);
    }

    @Test
    void testPasswordIsEncoded() {
        // Verify that password in database is encrypted
        User user = userRepository.findByEmail("test@example.com").orElseThrow();
        assertNotEquals("password123", user.getPassword());
        assertTrue(passwordEncoder.matches("password123", user.getPassword()));
    }
}

