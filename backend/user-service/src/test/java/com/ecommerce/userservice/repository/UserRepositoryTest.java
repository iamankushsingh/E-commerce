package com.ecommerce.userservice.repository;

import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.entity.UserRole;
import com.ecommerce.userservice.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.CUSTOMER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testSaveUser() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        assertEquals(testUser.getUsername(), savedUser.getUsername());
    }

    @Test
    void testFindByEmail_Success() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals(testUser.getEmail(), found.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        // When
        Optional<User> found = userRepository.findByEmail("notfound@example.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByUsername_Success() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertTrue(found.isPresent());
        assertEquals(testUser.getUsername(), found.get().getUsername());
    }

    @Test
    void testFindByUsername_NotFound() {
        // When
        Optional<User> found = userRepository.findByUsername("notfound");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmail_True() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    void testExistsByEmail_False() {
        // When
        boolean exists = userRepository.existsByEmail("notfound@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    void testExistsByUsername_True() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertTrue(exists);
    }

    @Test
    void testExistsByUsername_False() {
        // When
        boolean exists = userRepository.existsByUsername("notfound");

        // Then
        assertFalse(exists);
    }

    @Test
    void testCountByRole() {
        // Given
        entityManager.persist(testUser);
        
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(adminUser);
        entityManager.flush();

        // When
        long customerCount = userRepository.countByRole(UserRole.CUSTOMER);
        long adminCount = userRepository.countByRole(UserRole.ADMIN);

        // Then
        assertEquals(1, customerCount);
        assertEquals(1, adminCount);
    }

    @Test
    void testCountByStatus() {
        // Given
        entityManager.persist(testUser);
        
        User blockedUser = new User();
        blockedUser.setUsername("blocked");
        blockedUser.setEmail("blocked@example.com");
        blockedUser.setPassword("password");
        blockedUser.setFirstName("Blocked");
        blockedUser.setLastName("User");
        blockedUser.setRole(UserRole.CUSTOMER);
        blockedUser.setStatus(UserStatus.BLOCKED);
        blockedUser.setCreatedAt(LocalDateTime.now());
        blockedUser.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(blockedUser);
        entityManager.flush();

        // When
        long activeCount = userRepository.countByStatus(UserStatus.ACTIVE);
        long blockedCount = userRepository.countByStatus(UserStatus.BLOCKED);

        // Then
        assertEquals(1, activeCount);
        assertEquals(1, blockedCount);
    }

    @Test
    void testFindWithFilters_SearchByEmail() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Page<User> result = userRepository.findWithFilters(
            null, null, "test@example", PageRequest.of(0, 10)
        );

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getEmail(), result.getContent().get(0).getEmail());
    }

    @Test
    void testFindWithFilters_SearchByUsername() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Page<User> result = userRepository.findWithFilters(
            null, null, "testuser", PageRequest.of(0, 10)
        );

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getUsername(), result.getContent().get(0).getUsername());
    }

    @Test
    void testFindWithFilters_SearchByFirstName() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Page<User> result = userRepository.findWithFilters(
            null, null, "Test", PageRequest.of(0, 10)
        );

        // Then
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFindWithFilters_ByRoleAndStatus() {
        // Given
        entityManager.persist(testUser);
        
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(adminUser);
        entityManager.flush();

        // When
        Page<User> customerResult = userRepository.findWithFilters(
            UserRole.CUSTOMER, UserStatus.ACTIVE, null, PageRequest.of(0, 10)
        );

        Page<User> adminResult = userRepository.findWithFilters(
            UserRole.ADMIN, UserStatus.ACTIVE, null, PageRequest.of(0, 10)
        );

        // Then
        assertEquals(1, customerResult.getTotalElements());
        assertEquals(1, adminResult.getTotalElements());
        assertEquals(UserRole.CUSTOMER, customerResult.getContent().get(0).getRole());
        assertEquals(UserRole.ADMIN, adminResult.getContent().get(0).getRole());
    }

    @Test
    void testFindWithFilters_AllFilters() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Page<User> result = userRepository.findWithFilters(
            UserRole.CUSTOMER, UserStatus.ACTIVE, "test", PageRequest.of(0, 10)
        );

        // Then
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFindWithFilters_NoMatch() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Page<User> result = userRepository.findWithFilters(
            UserRole.ADMIN, UserStatus.ACTIVE, null, PageRequest.of(0, 10)
        );

        // Then
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testUpdateUser() {
        // Given
        User savedUser = entityManager.persist(testUser);
        entityManager.flush();
        Long userId = savedUser.getId();

        // When
        savedUser.setFirstName("Updated");
        savedUser.setLastName("Name");
        userRepository.save(savedUser);
        entityManager.flush();

        // Then
        User updatedUser = entityManager.find(User.class, userId);
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
    }

    @Test
    void testDeleteUser() {
        // Given
        User savedUser = entityManager.persist(testUser);
        entityManager.flush();
        Long userId = savedUser.getId();

        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        User deletedUser = entityManager.find(User.class, userId);
        assertNull(deletedUser);
    }

    @Test
    void testFindAll_Pagination() {
        // Given
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPassword("password");
            user.setFirstName("User");
            user.setLastName("" + i);
            user.setRole(UserRole.CUSTOMER);
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            entityManager.persist(user);
        }
        entityManager.flush();

        // When
        Page<User> firstPage = userRepository.findAll(PageRequest.of(0, 3));
        Page<User> secondPage = userRepository.findAll(PageRequest.of(1, 3));

        // Then
        assertEquals(3, firstPage.getContent().size());
        assertEquals(2, secondPage.getContent().size());
        assertEquals(5, firstPage.getTotalElements());
    }

    @Test
    void testUniqueConstraint_Email() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        User duplicateUser = new User();
        duplicateUser.setUsername("different");
        duplicateUser.setEmail("test@example.com"); // Same email
        duplicateUser.setPassword("password");
        duplicateUser.setFirstName("Duplicate");
        duplicateUser.setLastName("User");
        duplicateUser.setRole(UserRole.CUSTOMER);
        duplicateUser.setStatus(UserStatus.ACTIVE);
        duplicateUser.setCreatedAt(LocalDateTime.now());
        duplicateUser.setUpdatedAt(LocalDateTime.now());

        // When/Then
        assertThrows(Exception.class, () -> {
            entityManager.persist(duplicateUser);
            entityManager.flush();
        });
    }

    @Test
    void testUniqueConstraint_Username() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        User duplicateUser = new User();
        duplicateUser.setUsername("testuser"); // Same username
        duplicateUser.setEmail("different@example.com");
        duplicateUser.setPassword("password");
        duplicateUser.setFirstName("Duplicate");
        duplicateUser.setLastName("User");
        duplicateUser.setRole(UserRole.CUSTOMER);
        duplicateUser.setStatus(UserStatus.ACTIVE);
        duplicateUser.setCreatedAt(LocalDateTime.now());
        duplicateUser.setUpdatedAt(LocalDateTime.now());

        // When/Then
        assertThrows(Exception.class, () -> {
            entityManager.persist(duplicateUser);
            entityManager.flush();
        });
    }
}

