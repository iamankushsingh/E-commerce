package com.ecommerce.productservice.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class ProductCreateDTOTest {

    private ProductCreateDTO productCreateDTO;
    private Validator validator;

    @BeforeEach
    void setUp() {
        productCreateDTO = new ProductCreateDTO();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testAllSettersAndGetters() {
        // Test all setter/getter pairs
        productCreateDTO.setName("Test Product");
        productCreateDTO.setDescription("Test Description");
        productCreateDTO.setPrice(new BigDecimal("99.99"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setImageUrl("http://test.com/image.jpg");
        productCreateDTO.setStock(50);
        productCreateDTO.setStatus("active");

        // Verify all values
        assertThat(productCreateDTO.getName()).isEqualTo("Test Product");
        assertThat(productCreateDTO.getDescription()).isEqualTo("Test Description");
        assertThat(productCreateDTO.getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(productCreateDTO.getCategory()).isEqualTo("Electronics");
        assertThat(productCreateDTO.getImageUrl()).isEqualTo("http://test.com/image.jpg");
        assertThat(productCreateDTO.getStock()).isEqualTo(50);
        assertThat(productCreateDTO.getStatus()).isEqualTo("active");
    }

    @Test
    void testDefaultConstructor() {
        ProductCreateDTO newProduct = new ProductCreateDTO();
        
        assertThat(newProduct.getName()).isNull();
        assertThat(newProduct.getDescription()).isNull();
        assertThat(newProduct.getPrice()).isNull();
        assertThat(newProduct.getCategory()).isNull();
        assertThat(newProduct.getImageUrl()).isNull();
        assertThat(newProduct.getStock()).isNull();
        assertThat(newProduct.getStatus()).isNull();
    }

    @Test
    void testNullValues() {
        // Test setting null values
        productCreateDTO.setName(null);
        productCreateDTO.setDescription(null);
        productCreateDTO.setPrice(null);
        productCreateDTO.setCategory(null);
        productCreateDTO.setImageUrl(null);
        productCreateDTO.setStock(null);

        assertThat(productCreateDTO.getName()).isNull();
        assertThat(productCreateDTO.getDescription()).isNull();
        assertThat(productCreateDTO.getPrice()).isNull();
        assertThat(productCreateDTO.getCategory()).isNull();
        assertThat(productCreateDTO.getImageUrl()).isNull();
        assertThat(productCreateDTO.getStock()).isNull();
    }

    @Test
    void testValidProduct() {
        // Given - valid product
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setImageUrl("http://valid.com/image.jpg");
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should be valid
        assertThat(violations).isEmpty();
    }

    @Test
    void testInvalidProduct_NullName() {
        // Given - invalid product with null name
        productCreateDTO.setName(null);
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testInvalidProduct_EmptyName() {
        // Given - invalid product with empty name
        productCreateDTO.setName("");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStock(1);

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testInvalidProduct_NegativePrice() {
        // Given - invalid product with negative price
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("-10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStock(1);

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testInvalidProduct_NegativeStock() {
        // Given - invalid product with negative stock
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStock(-1);

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testInvalidProduct_InvalidStatus() {
        // Given - invalid product with invalid status
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("invalid_status");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testInvalidProduct_NullStatus() {
        // Given - invalid product with null status
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus(null);

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testInvalidProduct_TooLongName() {
        // Given - invalid product with name exceeding 255 characters
        String longName = "a".repeat(256);
        productCreateDTO.setName(longName);
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name must not exceed 255 characters");
    }

    @Test
    void testInvalidProduct_TooLongDescription() {
        // Given - invalid product with description exceeding 1000 characters
        String longDescription = "a".repeat(1001);
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription(longDescription);
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description must not exceed 1000 characters");
    }

    @Test
    void testInvalidProduct_TooLongCategory() {
        // Given - invalid product with category exceeding 100 characters
        String longCategory = "a".repeat(101);
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory(longCategory);
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category must not exceed 100 characters");
    }

    @Test
    void testInvalidProduct_TooLongImageUrl() {
        // Given - invalid product with imageUrl exceeding 500 characters
        String longUrl = "http://example.com/" + "a".repeat(500);
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setImageUrl(longUrl);
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Image URL must not exceed 500 characters");
    }

    @Test
    void testInvalidProduct_EmptyCategory() {
        // Given - invalid product with empty category
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("");
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category is required");
    }

    @Test
    void testInvalidProduct_NullPrice() {
        // Given - invalid product with null price
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(null);
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Price is required");
    }

    @Test
    void testInvalidProduct_ZeroPrice() {
        // Given - invalid product with zero price
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(BigDecimal.ZERO);
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Price must be greater than 0");
    }

    @Test
    void testInvalidProduct_NullStock() {
        // Given - invalid product with null stock
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStock(null);
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have violations
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Stock quantity is required");
    }

    @Test
    void testBoundaryValues_ExactLimits() {
        // Given - values at exact limits
        String exactName = "a".repeat(255); // Exactly 255 characters
        String exactDescription = "a".repeat(1000); // Exactly 1000 characters
        String exactCategory = "a".repeat(100); // Exactly 100 characters
        String exactImageUrl = "http://example.com/" + "a".repeat(481); // Exactly 500 characters

        productCreateDTO.setName(exactName);
        productCreateDTO.setDescription(exactDescription);
        productCreateDTO.setPrice(new BigDecimal("0.01")); // Minimum valid price
        productCreateDTO.setCategory(exactCategory);
        productCreateDTO.setImageUrl(exactImageUrl);
        productCreateDTO.setStock(0); // Minimum valid stock
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should be valid
        assertThat(violations).isEmpty();
    }

    @Test
    void testValidProduct_InactiveStatus() {
        // Given - valid product with inactive status
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setImageUrl("http://valid.com/image.jpg");
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("inactive");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should be valid
        assertThat(violations).isEmpty();
    }

    @Test
    void testValidProduct_NullImageUrl() {
        // Given - valid product with null imageUrl (optional field)
        productCreateDTO.setName("Valid Product");
        productCreateDTO.setDescription("Valid Description");
        productCreateDTO.setPrice(new BigDecimal("10.00"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setImageUrl(null);
        productCreateDTO.setStock(1);
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should be valid
        assertThat(violations).isEmpty();
    }

    @Test
    void testMultipleViolations() {
        // Given - product with multiple validation errors
        productCreateDTO.setName(""); // Empty name
        productCreateDTO.setDescription(null); // Null description
        productCreateDTO.setPrice(new BigDecimal("-5.00")); // Negative price
        productCreateDTO.setCategory(""); // Empty category
        productCreateDTO.setStock(-1); // Negative stock
        productCreateDTO.setStatus("invalid_status"); // Invalid status

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should have multiple violations
        assertThat(violations).hasSize(6);
    }

    @Test
    void testValidProduct_LargeValidValues() {
        // Given - valid product with large but valid values
        productCreateDTO.setName("Valid Product Name");
        productCreateDTO.setDescription("This is a very detailed product description that provides comprehensive information about the product features, benefits, and specifications to help customers make informed purchasing decisions.");
        productCreateDTO.setPrice(new BigDecimal("9999999999.99")); // Large valid price
        productCreateDTO.setCategory("Electronics & Technology Devices");
        productCreateDTO.setImageUrl("https://example.com/very/long/path/to/product/images/high-resolution-product-image-showing-all-details.jpg");
        productCreateDTO.setStock(Integer.MAX_VALUE); // Maximum valid stock
        productCreateDTO.setStatus("active");

        // When - validate
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(productCreateDTO);

        // Then - should be valid
        assertThat(violations).isEmpty();
    }

    @Test
    void testParameterizedConstructor() {
        // Test the parameterized constructor
        ProductCreateDTO product = new ProductCreateDTO(
            "Test Product", "Test Description", new BigDecimal("99.99"),
            "Electronics", "http://test.com/image.jpg", 50, "active");

        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getDescription()).isEqualTo("Test Description");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(product.getCategory()).isEqualTo("Electronics");
        assertThat(product.getImageUrl()).isEqualTo("http://test.com/image.jpg");
        assertThat(product.getStock()).isEqualTo(50);
        assertThat(product.getStatus()).isEqualTo("active");
    }

    @Test
    void testToString() {
        // Test the toString method
        productCreateDTO.setName("Test Product");
        productCreateDTO.setDescription("Test Description");
        productCreateDTO.setPrice(new BigDecimal("99.99"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setImageUrl("http://test.com/image.jpg");
        productCreateDTO.setStock(50);
        productCreateDTO.setStatus("active");

        String result = productCreateDTO.toString();
        
        assertThat(result).contains("ProductCreateDTO{");
        assertThat(result).contains("name='Test Product'");
        assertThat(result).contains("description='Test Description'");
        assertThat(result).contains("price=99.99");
        assertThat(result).contains("category='Electronics'");
        assertThat(result).contains("imageUrl='http://test.com/image.jpg'");
        assertThat(result).contains("stock=50");
        assertThat(result).contains("status='active'");
    }

    @Test
    void testEdgeCaseValues() {
        // Test with edge case values
        productCreateDTO.setPrice(BigDecimal.ZERO);
        productCreateDTO.setStock(0);
        productCreateDTO.setName("A");
        productCreateDTO.setCategory("");
        productCreateDTO.setImageUrl("");
        productCreateDTO.setDescription("");
        productCreateDTO.setStatus("inactive");

        assertThat(productCreateDTO.getPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(productCreateDTO.getStock()).isEqualTo(0);
        assertThat(productCreateDTO.getName()).isEqualTo("A");
        assertThat(productCreateDTO.getCategory()).isEmpty();
        assertThat(productCreateDTO.getImageUrl()).isEmpty();
        assertThat(productCreateDTO.getDescription()).isEmpty();
        assertThat(productCreateDTO.getStatus()).isEqualTo("inactive");
    }
} 