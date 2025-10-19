package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.*;
import com.ecommerce.userservice.entity.*;
import com.ecommerce.userservice.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO registerUser(UserRegistrationDTO registrationDTO) {
        logger.info("Registering new user with email: {}", registrationDTO.getEmail());

        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + registrationDTO.getEmail());
        }

        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new RuntimeException("Username already exists: " + registrationDTO.getUsername());
        }

        User user = new User();
        String username = registrationDTO.getUsername() != null && !registrationDTO.getUsername().trim().isEmpty() 
            ? registrationDTO.getUsername() 
            : registrationDTO.getEmail().split("@")[0];
        user.setUsername(username);
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName() != null && !registrationDTO.getLastName().trim().isEmpty() 
            ? registrationDTO.getLastName() : null);
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setAvatar(registrationDTO.getAvatar());
        user.setPhoneNumber(registrationDTO.getPhoneNumber());
        user.setAddress(registrationDTO.getAddress());

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());

        return modelMapper.map(savedUser, UserDTO.class);
    }

    public Optional<UserDTO> loginUser(UserLoginDTO loginDTO) {
        logger.info("Login attempt for email: {}", loginDTO.getEmail());

        Optional<User> userOpt = userRepository.findByEmail(loginDTO.getEmail());
        
        if (userOpt.isEmpty()) {
            logger.warn("Login failed: User not found with email: {}", loginDTO.getEmail());
            return Optional.empty();
        }

        User user = userOpt.get();

        if (user.getStatus() != UserStatus.ACTIVE) {
            logger.warn("Login failed: User account is not active: {}", loginDTO.getEmail());
            return Optional.empty();
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            logger.warn("Login failed: Invalid password for email: {}", loginDTO.getEmail());
            return Optional.empty();
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        logger.info("Login successful for email: {}", loginDTO.getEmail());
        return Optional.of(modelMapper.map(user, UserDTO.class));
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        logger.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .map(user -> modelMapper.map(user, UserDTO.class));
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        logger.info("Fetching user with email: {}", email);
        return userRepository.findByEmail(email)
                .map(user -> modelMapper.map(user, UserDTO.class));
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<UserDTO> getAllUsers(UserRole role, UserStatus status, String search,
                                                int page, int size, String sortBy, String sortDirection) {
        logger.info("Fetching users - Page: {}, Size: {}, Role: {}, Status: {}, Search: {}", 
                   page, size, role, status, search);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage;
        
        if (role != null || status != null || search != null) {
            userPage = userRepository.findWithFilters(role, status, search, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());

        return new PaginatedResponse<>(userDTOs, page, size, userPage.getTotalElements());
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        logger.info("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Check if email is being changed and if it already exists
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new RuntimeException("Email already exists: " + userDTO.getEmail());
            }
            existingUser.setEmail(userDTO.getEmail());
        }

        // Update profile fields (don't update username to avoid conflicts)
        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName() != null && !userDTO.getLastName().trim().isEmpty() 
            ? userDTO.getLastName() : null);
        existingUser.setAvatar(userDTO.getAvatar());
        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        existingUser.setAddress(userDTO.getAddress());

        // Only update role and status for admin operations
        if (userDTO.getRole() != null) {
            existingUser.setRole(userDTO.getRole());
        }
        if (userDTO.getStatus() != null) {
            existingUser.setStatus(userDTO.getStatus());
        }

        User updatedUser = userRepository.save(existingUser);
        logger.info("User updated successfully with ID: {}", updatedUser.getId());

        return modelMapper.map(updatedUser, UserDTO.class);
    }

    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        logger.info("Changing password for user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            logger.warn("Password change failed: Current password is incorrect for user ID: {}", userId);
            throw new RuntimeException("Current password is incorrect");
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);

        logger.info("Password changed successfully for user with ID: {}", userId);
    }

    public UserDTO blockUser(Long id) {
        logger.info("Blocking user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.setStatus(UserStatus.BLOCKED);
        User updatedUser = userRepository.save(user);

        logger.info("User blocked successfully with ID: {}", id);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    public UserDTO unblockUser(Long id) {
        logger.info("Unblocking user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.setStatus(UserStatus.ACTIVE);
        User updatedUser = userRepository.save(user);

        logger.info("User unblocked successfully with ID: {}", id);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
        logger.info("User deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public UserStatsDTO getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long blockedUsers = userRepository.countByStatus(UserStatus.BLOCKED);
        long adminUsers = userRepository.countByRole(UserRole.ADMIN);
        long customerUsers = userRepository.countByRole(UserRole.CUSTOMER);

        UserStatsDTO stats = new UserStatsDTO();
        stats.setTotalUsers(totalUsers);
        stats.setActiveUsers(activeUsers);
        stats.setBlockedUsers(blockedUsers);
        stats.setAdminUsers(adminUsers);
        stats.setCustomerUsers(customerUsers);

        return stats;
    }

    public static class UserStatsDTO {
        private long totalUsers;
        private long activeUsers;
        private long blockedUsers;
        private long adminUsers;
        private long customerUsers;

        // Getters and Setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

        public long getBlockedUsers() { return blockedUsers; }
        public void setBlockedUsers(long blockedUsers) { this.blockedUsers = blockedUsers; }

        public long getAdminUsers() { return adminUsers; }
        public void setAdminUsers(long adminUsers) { this.adminUsers = adminUsers; }

        public long getCustomerUsers() { return customerUsers; }
        public void setCustomerUsers(long customerUsers) { this.customerUsers = customerUsers; }
    }
} 