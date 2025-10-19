package com.ecommerce.userservice.config;

import com.ecommerce.userservice.entity.*;
import com.ecommerce.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired  
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            logger.info("🔍 Checking existing users in database...");
            long userCount = userRepository.count();
            logger.info("Current user count: {}", userCount);
            
            // Check if admin already exists
            boolean adminExists = userRepository.findByEmail("admin@ecommerce.com").isPresent();
            logger.info("Admin user exists: {}", adminExists);
            
            if (!adminExists) {
                logger.info("🛠️ Creating admin user...");
                
                // Generate encoded password and log it for manual insertion
                String encodedPassword = passwordEncoder.encode("admin123");
                logger.info("🔑 Encoded password for admin123: {}", encodedPassword);
                
                // Create sample users
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@ecommerce.com");
                admin.setPassword(encodedPassword);
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setRole(UserRole.ADMIN);
                admin.setStatus(UserStatus.ACTIVE);
                
                User savedAdmin = userRepository.save(admin);
                logger.info("✅ Admin user created successfully with ID: {}", savedAdmin.getId());
                
                System.out.println("=".repeat(80));
                System.out.println("🎉 ADMIN USER CREATED SUCCESSFULLY!");
                System.out.println("📧 Email: admin@ecommerce.com");
                System.out.println("🔒 Password: admin123");
                System.out.println("=".repeat(80));
                
            } else {
                logger.info("✅ Admin user already exists, skipping creation");
            }
            
        } catch (Exception e) {
            logger.error("❌ Error in DataLoader: {}", e.getMessage(), e);
            
            // Print SQL for manual insertion
            String encodedPassword = passwordEncoder.encode("admin123");
            System.out.println("\n" + "=".repeat(80));
            System.out.println("❌ DATALOADER FAILED - USE THIS SQL TO INSERT ADMIN MANUALLY:");
            System.out.println("=".repeat(80));
            System.out.println("USE ecommerce_users;");
            System.out.println("INSERT INTO users (username, email, password, first_name, last_name, role, status, created_at, updated_at)");
            System.out.println("VALUES ('admin', 'admin@ecommerce.com', '" + encodedPassword + "', 'System', 'Administrator', 'ADMIN', 'ACTIVE', NOW(), NOW());");
            System.out.println("=".repeat(80));
        }
    }
} 