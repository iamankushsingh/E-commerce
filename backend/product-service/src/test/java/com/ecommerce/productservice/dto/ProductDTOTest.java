package com.ecommerce.productservice.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class ProductDTOTest {

    private ProductDTO productDTO;
    private LocalDateTime testDateTime;
    private Validator validator;

    @BeforeEach
    void setUp() {
        productDTO = new ProductDTO();
        testDateTime = LocalDateTime.now();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testAllSettersAndGetters() {
        // Test all setter/getter pairs
        productDTO.setId(1L);
        productDTO.setName("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setPrice(new BigDecimal("99.99"));
        productDTO.setCategory("Electronics");
        productDTO.setImageUrl("http://test.com/image.jpg");
        productDTO.setStock(50);
        productDTO.setStatus("active");
        productDTO.setCreatedAt(testDateTime);
        productDTO.setUpdatedAt(testDateTime);

        // Verify all values
        assertThat(productDTO.getId()).isEqualTo(1L);
        assertThat(productDTO.getName()).isEqualTo("Test Product");
        assertThat(productDTO.getDescription()).isEqualTo("Test Description");
        assertThat(productDTO.getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(productDTO.getCategory()).isEqualTo("Electronics");
        assertThat(productDTO.getImageUrl()).isEqualTo("http://test.com/image.jpg");
        assertThat(productDTO.getStock()).isEqualTo(50);
        assertThat(productDTO.getStatus()).isEqualTo("active");
        assertThat(productDTO.getCreatedAt()).isEqualTo(testDateTime);
        assertThat(productDTO.getUpdatedAt()).isEqualTo(testDateTime);
    }

    @Test
    void testDefaultConstructor() {
        ProductDTO newProduct = new ProductDTO();
        
        assertThat(newProduct.getId()).isNull();
        assertThat(newProduct.getName()).isNull();
        assertThat(newProduct.getDescription()).isNull();
        assertThat(newProduct.getPrice()).isNull();
        assertThat(newProduct.getCategory()).isNull();
        assertThat(newProduct.getImageUrl()).isNull();
        assertThat(newProduct.getStock()).isNull();
        assertThat(newProduct.getStatus()).isNull();
        assertThat(newProduct.getCreatedAt()).isNull();
        assertThat(newProduct.getUpdatedAt()).isNull();
    }

    @Test
    void testNullValues() {
        // Test setting null values
        productDTO.setId(null);
        productDTO.setName(null);
        productDTO.setDescription(null);
        productDTO.setPrice(null);
        productDTO.setCategory(null);
        productDTO.setImageUrl(null);
        productDTO.setStock(null);
        productDTO.setStatus(null);
        productDTO.setCreatedAt(null);
        productDTO.setUpdatedAt(null);

        assertThat(productDTO.getId()).isNull();
        assertThat(productDTO.getName()).isNull();
        assertThat(productDTO.getDescription()).isNull();
        assertThat(productDTO.getPrice()).isNull();
        assertThat(productDTO.getCategory()).isNull();
        assertThat(productDTO.getImageUrl()).isNull();
        assertThat(productDTO.getStock()).isNull();
        assertThat(productDTO.getStatus()).isNull();
        assertThat(productDTO.getCreatedAt()).isNull();
        assertThat(productDTO.getUpdatedAt()).isNull();
    }

    @Test
    void testToString() {
        // Test that toString method works and contains expected information
        productDTO.setId(1L);
        productDTO.setName("Test Product");
        productDTO.setPrice(new BigDecimal("99.99"));
        productDTO.setCategory("Electronics");

        String result = productDTO.toString();
        
        // Since we don't know the exact toString implementation, just verify it's not null
        // and contains some basic object information
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void testEdgeCaseValues() {
        // Test with edge case values
        productDTO.setPrice(BigDecimal.ZERO);
        productDTO.setStock(0);
        productDTO.setName("");
        productDTO.setCategory("");
        productDTO.setImageUrl("");
        productDTO.setStatus("");
        productDTO.setDescription("");

        assertThat(productDTO.getPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(productDTO.getStock()).isEqualTo(0);
        assertThat(productDTO.getName()).isEmpty();
        assertThat(productDTO.getCategory()).isEmpty();
        assertThat(productDTO.getImageUrl()).isEmpty();
        assertThat(productDTO.getStatus()).isEmpty();
        assertThat(productDTO.getDescription()).isEmpty();
    }

    @Test
    void testParameterizedConstructor() {
        // Test the parameterized constructor
        LocalDateTime now = LocalDateTime.now();
        BigDecimal price = new BigDecimal("99.99");
        ProductDTO product = new ProductDTO(
            1L, "Test Product", "Test Description", price,
            "Electronics", "http://test.com/image.jpg", 50, "active",
            now, now);

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getDescription()).isEqualTo("Test Description");
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getCategory()).isEqualTo("Electronics");
        assertThat(product.getImageUrl()).isEqualTo("http://test.com/image.jpg");
        assertThat(product.getStock()).isEqualTo(50);
        assertThat(product.getStatus()).isEqualTo("active");
        assertThat(product.getCreatedAt()).isEqualTo(now);
        assertThat(product.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testCompleteToString() {
        // Test toString with all fields populated
        LocalDateTime testTime = LocalDateTime.of(2023, 12, 25, 10, 30, 0);
        productDTO.setId(1L);
        productDTO.setName("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setPrice(new BigDecimal("99.99"));
        productDTO.setCategory("Electronics");
        productDTO.setImageUrl("http://test.com/image.jpg");
        productDTO.setStock(50);
        productDTO.setStatus("active");
        productDTO.setCreatedAt(testTime);
        productDTO.setUpdatedAt(testTime);

        String result = productDTO.toString();
        
        assertThat(result).contains("ProductDTO{");
        assertThat(result).contains("id=1");
        assertThat(result).contains("name='Test Product'");
        assertThat(result).contains("description='Test Description'");
        assertThat(result).contains("price=99.99");
        assertThat(result).contains("category='Electronics'");
        assertThat(result).contains("imageUrl='http://test.com/image.jpg'");
        assertThat(result).contains("stock=50");
        assertThat(result).contains("status='active'");
        assertThat(result).contains("createdAt=" + testTime);
        assertThat(result).contains("updatedAt=" + testTime);
    }

    @Test
    void testValidationAnnotations_ValidProduct() {
        // Given - valid product
        productDTO.setName("Valid Product");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory("Electronics");
        productDTO.setImageUrl("http://valid.com/image.jpg");
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should be valid
        assertThat(violations).isEmpty();
    }

    @Test
    void testNameValidation_NullName_ViolatesConstraint() {
        // Given - null name
        productDTO.setName(null);
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory("Electronics");
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name is required");
    }

    @Test
    void testNameValidation_BlankName_ViolatesConstraint() {
        // Given - blank name
        productDTO.setName("   ");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory("Electronics");
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name is required");
    }

    @Test
    void testNameValidation_TooLongName_ViolatesConstraint() {
        // Given - name exceeding 255 characters
        String longName = "a".repeat(256);
        productDTO.setName(longName);
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory("Electronics");
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name must not exceed 255 characters");
    }

    @Test
    void testDescriptionValidation_NullDescription_ViolatesConstraint() {
        // Given - null description
        productDTO.setName("Valid Product");
        productDTO.setDescription(null);
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory("Electronics");
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product description is required");
    }

    @Test
    void testDescriptionValidation_TooLongDescription_ViolatesConstraint() {
        // Given - description exceeding 1000 characters
        String longDescription = "a".repeat(1001);
        productDTO.setName("Valid Product");
        productDTO.setDescription(longDescription);
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory("Electronics");
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description must not exceed 1000 characters");
    }

    @Test
    void testPriceValidation_NullPrice_ViolatesConstraint() {
        // Given - null price
        productDTO.setName("Valid Product");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(null);
        productDTO.setCategory("Electronics");
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Price is required");
    }

    @Test
    void testPriceValidation_ZeroPrice_ViolatesConstraint() {
        // Given - zero price
        productDTO.setName("Valid Product");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(BigDecimal.ZERO);
        productDTO.setCategory("Electronics");
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Price must be greater than 0");
    }

    @Test
    void testPriceValidation_NegativePrice_ViolatesConstraint() {
        // Given - negative price
        productDTO.setName("Valid Product");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("-1.00"));
        productDTO.setCategory("Electronics");
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Price must be greater than 0");
    }

    @Test
    void testCategoryValidation_NullCategory_ViolatesConstraint() {
        // Given - null category
        productDTO.setName("Valid Product");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory(null);
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category is required");
    }

    @Test
    void testCategoryValidation_TooLongCategory_ViolatesConstraint() {
        // Given - category exceeding 100 characters
        String longCategory = "a".repeat(101);
        productDTO.setName("Valid Product");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory(longCategory);
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category must not exceed 100 characters");
    }

    @Test
    void testImageUrlValidation_TooLongUrl_ViolatesConstraint() {
        // Given - imageUrl exceeding 500 characters
        String longUrl = "http://example.com/" + "a".repeat(500);
        productDTO.setName("Valid Product");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory("Electronics");
        productDTO.setImageUrl(longUrl);
        productDTO.setStock(1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Image URL must not exceed 500 characters");
    }

    @Test
    void testStockValidation_NullStock_ViolatesConstraint() {
        // Given - null stock
        productDTO.setName("Valid Product");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory("Electronics");
        productDTO.setStock(null);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Stock quantity is required");
    }

    @Test
    void testStockValidation_NegativeStock_ViolatesConstraint() {
        // Given - negative stock
        productDTO.setName("Valid Product");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory("Electronics");
        productDTO.setStock(-1);
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Stock cannot be negative");
    }

    @Test
    void testStatusValidation_NullStatus_ViolatesConstraint() {
        // Given - null status
        productDTO.setName("Valid Product");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory("Electronics");
        productDTO.setStock(1);
        productDTO.setStatus(null);

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Status is required");
    }

    @Test
    void testStatusValidation_InvalidStatusPattern_ViolatesConstraint() {
        // Given - invalid status pattern
        productDTO.setName("Valid Product");
        productDTO.setDescription("Valid Description");
        productDTO.setPrice(new BigDecimal("10.00"));
        productDTO.setCategory("Electronics");
        productDTO.setStock(1);
        productDTO.setStatus("invalid_status");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Status must be 'active' or 'inactive'");
    }

    @Test
    void testBoundaryValues_ExactSizeLimits() {
        // Given - values at exact size limits
        String exactName = "a".repeat(255); // Exactly 255 chars
        String exactDescription = "a".repeat(1000); // Exactly 1000 chars
        String exactCategory = "a".repeat(100); // Exactly 100 chars
        String exactImageUrl = "http://example.com/" + "a".repeat(481); // Exactly 500 chars

        productDTO.setName(exactName);
        productDTO.setDescription(exactDescription);
        productDTO.setPrice(new BigDecimal("0.01")); // Smallest valid price
        productDTO.setCategory(exactCategory);
        productDTO.setImageUrl(exactImageUrl);
        productDTO.setStock(0); // Minimum valid stock
        productDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should be valid
        assertThat(violations).isEmpty();
    }

    @Test
    void testMultipleViolations() {
        // Given - product with multiple validation errors
        productDTO.setName(""); // Blank name
        productDTO.setDescription(null); // Null description
        productDTO.setPrice(BigDecimal.ZERO); // Zero price
        productDTO.setCategory(null); // Null category
        productDTO.setStock(null); // Null stock
        productDTO.setStatus("invalid"); // Invalid status

        // When - validate
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);

        // Then - should have multiple violations
        assertThat(violations).hasSize(6);
    }
} 