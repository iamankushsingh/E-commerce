package com.ecommerce.productservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductTest {

    private Validator validator;
    private Product validProduct;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        validProduct = createValidProduct();
    }

    private Product createValidProduct() {
        return new Product(
            "Test Product",
            "Test Description", 
            new BigDecimal("99.99"),
            "Electronics",
            "http://example.com/image.jpg",
            10,
            ProductStatus.ACTIVE
        );
    }

    @Test
    void testValidProduct_NoViolations() {
        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void testDefaultConstructor() {
        // When
        Product product = new Product();

        // Then
        assertThat(product.getId()).isNull();
        assertThat(product.getName()).isNull();
        assertThat(product.getDescription()).isNull();
        assertThat(product.getPrice()).isNull();
        assertThat(product.getCategory()).isNull();
        assertThat(product.getImageUrl()).isNull();
        assertThat(product.getStock()).isNull();
        assertThat(product.getStatus()).isNull();
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
    }

    @Test
    void testParameterizedConstructor() {
        // Given
        String name = "MacBook Pro";
        String description = "Professional laptop";
        BigDecimal price = new BigDecimal("2499.99");
        String category = "Electronics";
        String imageUrl = "http://example.com/macbook.jpg";
        Integer stock = 5;
        ProductStatus status = ProductStatus.ACTIVE;

        // When
        Product product = new Product(name, description, price, category, imageUrl, stock, status);

        // Then
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getDescription()).isEqualTo(description);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getImageUrl()).isEqualTo(imageUrl);
        assertThat(product.getStock()).isEqualTo(stock);
        assertThat(product.getStatus()).isEqualTo(status);
        assertThat(product.getId()).isNull(); // Not set in constructor
        assertThat(product.getCreatedAt()).isNull(); // Set by @PrePersist
        assertThat(product.getUpdatedAt()).isNull(); // Set by @PrePersist
    }

    @Test
    void testNameValidation_BlankName_ViolatesConstraint() {
        // Given
        validProduct.setName("");

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name is required");
    }

    @Test
    void testNameValidation_NullName_ViolatesConstraint() {
        // Given
        validProduct.setName(null);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name is required");
    }

    @Test
    void testNameValidation_TooLongName_ViolatesConstraint() {
        // Given
        String longName = "a".repeat(256); // Exceeds 255 character limit
        validProduct.setName(longName);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name must not exceed 255 characters");
    }

    @Test
    void testDescriptionValidation_BlankDescription_ViolatesConstraint() {
        // Given
        validProduct.setDescription("");

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product description is required");
    }

    @Test
    void testDescriptionValidation_TooLongDescription_ViolatesConstraint() {
        // Given
        String longDescription = "a".repeat(1001); // Exceeds 1000 character limit
        validProduct.setDescription(longDescription);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description must not exceed 1000 characters");
    }

    @Test
    void testPriceValidation_NullPrice_ViolatesConstraint() {
        // Given
        validProduct.setPrice(null);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Price is required");
    }

    @Test
    void testPriceValidation_ZeroPrice_ViolatesConstraint() {
        // Given
        validProduct.setPrice(BigDecimal.ZERO);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Price must be greater than 0");
    }

    @Test
    void testPriceValidation_NegativePrice_ViolatesConstraint() {
        // Given
        validProduct.setPrice(new BigDecimal("-10.00"));

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Price must be greater than 0");
    }

    @Test
    void testPriceValidation_InvalidFormat_ViolatesConstraint() {
        // Given - Price with too many decimal places
        validProduct.setPrice(new BigDecimal("99.999"));

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Price format is invalid");
    }

    @Test
    void testCategoryValidation_BlankCategory_ViolatesConstraint() {
        // Given
        validProduct.setCategory("");

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category is required");
    }

    @Test
    void testCategoryValidation_TooLongCategory_ViolatesConstraint() {
        // Given
        String longCategory = "a".repeat(101); // Exceeds 100 character limit
        validProduct.setCategory(longCategory);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category must not exceed 100 characters");
    }

    @Test
    void testImageUrlValidation_TooLongUrl_ViolatesConstraint() {
        // Given
        String longUrl = "http://example.com/" + "a".repeat(500); // Exceeds 500 character limit
        validProduct.setImageUrl(longUrl);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Image URL must not exceed 500 characters");
    }

    @Test
    void testImageUrlValidation_NullUrl_IsValid() {
        // Given
        validProduct.setImageUrl(null);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).isEmpty(); // imageUrl can be null
    }

    @Test
    void testStockValidation_NullStock_ViolatesConstraint() {
        // Given
        validProduct.setStock(null);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Stock quantity is required");
    }

    @Test
    void testStockValidation_NegativeStock_ViolatesConstraint() {
        // Given
        validProduct.setStock(-1);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Stock cannot be negative");
    }

    @Test
    void testStockValidation_ZeroStock_IsValid() {
        // Given
        validProduct.setStock(0);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).isEmpty(); // Zero stock is valid
    }

    @Test
    void testStatusValidation_NullStatus_ViolatesConstraint() {
        // Given
        validProduct.setStatus(null);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(validProduct);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Status is required");
    }

    @Test
    void testGettersAndSetters() {
        // Given
        Product product = new Product();
        Long id = 123L;
        String name = "iPhone 15";
        String description = "Latest iPhone";
        BigDecimal price = new BigDecimal("999.99");
        String category = "Electronics";
        String imageUrl = "http://example.com/iphone15.jpg";
        Integer stock = 20;
        ProductStatus status = ProductStatus.INACTIVE;
        LocalDateTime now = LocalDateTime.now();

        // When
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setStock(stock);
        product.setStatus(status);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        // Then
        assertThat(product.getId()).isEqualTo(id);
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getDescription()).isEqualTo(description);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getImageUrl()).isEqualTo(imageUrl);
        assertThat(product.getStock()).isEqualTo(stock);
        assertThat(product.getStatus()).isEqualTo(status);
        assertThat(product.getCreatedAt()).isEqualTo(now);
        assertThat(product.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testOnCreate_SetsTimestamps() {
        // Given
        Product product = new Product();
        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

        // When
        product.onCreate(); // Simulating @PrePersist

        // Then
        LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);
        assertThat(product.getCreatedAt()).isNotNull();
        assertThat(product.getUpdatedAt()).isNotNull();
        assertThat(product.getCreatedAt()).isBetween(beforeCreate, afterCreate);
        assertThat(product.getUpdatedAt()).isBetween(beforeCreate, afterCreate);
        assertThat(product.getCreatedAt()).isEqualTo(product.getUpdatedAt());
    }

    @Test
    void testOnUpdate_UpdatesTimestamp() {
        // Given
        Product product = new Product();
        LocalDateTime originalTime = LocalDateTime.now().minusHours(1);
        product.setCreatedAt(originalTime);
        product.setUpdatedAt(originalTime);
        
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);

        // When
        product.onUpdate(); // Simulating @PreUpdate

        // Then
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);
        assertThat(product.getCreatedAt()).isEqualTo(originalTime); // Should not change
        assertThat(product.getUpdatedAt()).isNotNull();
        assertThat(product.getUpdatedAt()).isBetween(beforeUpdate, afterUpdate);
        assertThat(product.getUpdatedAt()).isAfter(originalTime);
    }

    @Test
    void testToString_ContainsAllFields() {
        // Given
        validProduct.setId(1L);
        LocalDateTime now = LocalDateTime.now();
        validProduct.setCreatedAt(now);
        validProduct.setUpdatedAt(now);

        // When
        String toString = validProduct.toString();

        // Then
        assertThat(toString).contains("Product{");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name='Test Product'");
        assertThat(toString).contains("description='Test Description'");
        assertThat(toString).contains("price=99.99");
        assertThat(toString).contains("category='Electronics'");
        assertThat(toString).contains("imageUrl='http://example.com/image.jpg'");
        assertThat(toString).contains("stock=10");
        assertThat(toString).contains("status=ACTIVE");
        assertThat(toString).contains("createdAt=" + now);
        assertThat(toString).contains("updatedAt=" + now);
    }

    @Test
    void testMultipleValidationViolations() {
        // Given
        Product invalidProduct = new Product();
        // Set multiple invalid fields

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(invalidProduct);

        // Then
        assertThat(violations).hasSizeGreaterThan(1); // Multiple violations expected
        
        // Check that all required field violations are present
        Set<String> violationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
        
        assertThat(violationMessages).contains(
            "Product name is required",
            "Product description is required", 
            "Price is required",
            "Category is required",
            "Stock quantity is required",
            "Status is required"
        );
    }

    @Test
    void testEdgeCaseValues() {
        // Test with maximum valid values
        Product product = new Product();
        product.setName("a".repeat(255)); // Max length
        product.setDescription("b".repeat(1000)); // Max length
        product.setPrice(new BigDecimal("9999999999.99")); // Max precision
        product.setCategory("c".repeat(100)); // Max length
        product.setImageUrl("http://example.com/" + "d".repeat(470)); // Max length
        product.setStock(Integer.MAX_VALUE);
        product.setStatus(ProductStatus.ACTIVE);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations).isEmpty(); // Should be valid at maximum limits
    }
} 