package com.ecommerce.productservice.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class ProductStatusTest {

    @Test
    void testActiveStatus_HasCorrectValue() {
        // When & Then
        assertThat(ProductStatus.ACTIVE.getValue()).isEqualTo("active");
    }

    @Test
    void testInactiveStatus_HasCorrectValue() {
        // When & Then
        assertThat(ProductStatus.INACTIVE.getValue()).isEqualTo("inactive");
    }

    @Test
    void testAllEnumValues_HaveNonNullValues() {
        // Given & When & Then
        for (ProductStatus status : ProductStatus.values()) {
            assertThat(status.getValue()).isNotNull();
            assertThat(status.getValue()).isNotBlank();
        }
    }

    @ParameterizedTest
    @EnumSource(ProductStatus.class)
    void testGetValue_ReturnsNonNullValue(ProductStatus status) {
        // When & Then
        assertThat(status.getValue()).isNotNull();
        assertThat(status.getValue()).isNotEmpty();
    }

    @Test
    void testFromValue_WithActiveString_ReturnsActiveEnum() {
        // When
        ProductStatus result = ProductStatus.fromValue("active");

        // Then
        assertThat(result).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void testFromValue_WithInactiveString_ReturnsInactiveEnum() {
        // When
        ProductStatus result = ProductStatus.fromValue("inactive");

        // Then
        assertThat(result).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void testFromValue_WithUppercaseActive_ReturnsActiveEnum() {
        // When
        ProductStatus result = ProductStatus.fromValue("ACTIVE");

        // Then
        assertThat(result).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void testFromValue_WithUppercaseInactive_ReturnsInactiveEnum() {
        // When
        ProductStatus result = ProductStatus.fromValue("INACTIVE");

        // Then
        assertThat(result).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void testFromValue_WithMixedCaseActive_ReturnsActiveEnum() {
        // When
        ProductStatus result = ProductStatus.fromValue("AcTiVe");

        // Then
        assertThat(result).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void testFromValue_WithMixedCaseInactive_ReturnsInactiveEnum() {
        // When
        ProductStatus result = ProductStatus.fromValue("InAcTiVe");

        // Then
        assertThat(result).isEqualTo(ProductStatus.INACTIVE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"active", "ACTIVE", "AcTiVe", "aCTIVE"})
    void testFromValue_WithVariousActiveFormats_ReturnsActiveEnum(String input) {
        // When
        ProductStatus result = ProductStatus.fromValue(input);

        // Then
        assertThat(result).isEqualTo(ProductStatus.ACTIVE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"inactive", "INACTIVE", "InAcTiVe", "iNACTIVE"})
    void testFromValue_WithVariousInactiveFormats_ReturnsInactiveEnum(String input) {
        // When
        ProductStatus result = ProductStatus.fromValue(input);

        // Then
        assertThat(result).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void testFromValue_WithInvalidString_ThrowsIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> ProductStatus.fromValue("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid product status: invalid");
    }

    @Test
    void testFromValue_WithNullValue_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> ProductStatus.fromValue(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid product status: null");
    }

    @Test
    void testFromValue_WithEmptyString_ThrowsIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> ProductStatus.fromValue(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid product status: ");
    }

    @Test
    void testFromValue_WithWhitespaceString_ThrowsIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> ProductStatus.fromValue("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid product status:    ");
    }

    @ParameterizedTest
    @ValueSource(strings = {"pending", "deleted", "draft", "archived", "unknown"})
    void testFromValue_WithVariousInvalidValues_ThrowsIllegalArgumentException(String invalidValue) {
        // When & Then
        assertThatThrownBy(() -> ProductStatus.fromValue(invalidValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid product status: " + invalidValue);
    }

    @Test
    void testEnumValues_ContainsExpectedValues() {
        // Given
        ProductStatus[] values = ProductStatus.values();

        // When & Then
        assertThat(values).hasSize(2);
        assertThat(values).contains(ProductStatus.ACTIVE, ProductStatus.INACTIVE);
    }

    @Test
    void testValueOf_WithValidEnumName_ReturnsCorrectEnum() {
        // When & Then
        assertThat(ProductStatus.valueOf("ACTIVE")).isEqualTo(ProductStatus.ACTIVE);
        assertThat(ProductStatus.valueOf("INACTIVE")).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void testValueOf_WithInvalidEnumName_ThrowsIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> ProductStatus.valueOf("PENDING"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testToString_ReturnsEnumName() {
        // When & Then
        assertThat(ProductStatus.ACTIVE.toString()).isEqualTo("ACTIVE");
        assertThat(ProductStatus.INACTIVE.toString()).isEqualTo("INACTIVE");
    }

    @Test
    void testName_ReturnsEnumName() {
        // When & Then
        assertThat(ProductStatus.ACTIVE.name()).isEqualTo("ACTIVE");
        assertThat(ProductStatus.INACTIVE.name()).isEqualTo("INACTIVE");
    }

    @Test
    void testOrdinal_ReturnsCorrectOrder() {
        // When & Then
        assertThat(ProductStatus.ACTIVE.ordinal()).isEqualTo(0);
        assertThat(ProductStatus.INACTIVE.ordinal()).isEqualTo(1);
    }

    @Test
    void testEquals_WithSameEnum_ReturnsTrue() {
        // Given
        ProductStatus status1 = ProductStatus.ACTIVE;
        ProductStatus status2 = ProductStatus.ACTIVE;

        // When & Then
        assertThat(status1).isEqualTo(status2);
        assertThat(status1 == status2).isTrue(); // Enum identity
    }

    @Test
    void testEquals_WithDifferentEnum_ReturnsFalse() {
        // Given
        ProductStatus activeStatus = ProductStatus.ACTIVE;
        ProductStatus inactiveStatus = ProductStatus.INACTIVE;

        // When & Then
        assertThat(activeStatus).isNotEqualTo(inactiveStatus);
    }

    @Test
    void testHashCode_SameEnumsSameHashCode() {
        // Given
        ProductStatus status1 = ProductStatus.ACTIVE;
        ProductStatus status2 = ProductStatus.ACTIVE;

        // When & Then
        assertThat(status1.hashCode()).isEqualTo(status2.hashCode());
    }

    @Test
    void testCompareTo_WithSameEnum_ReturnsZero() {
        // Given
        ProductStatus status1 = ProductStatus.ACTIVE;
        ProductStatus status2 = ProductStatus.ACTIVE;

        // When & Then
        assertThat(status1.compareTo(status2)).isEqualTo(0);
    }

    @Test
    void testCompareTo_WithDifferentEnums_ReturnsCorrectOrder() {
        // Given
        ProductStatus activeStatus = ProductStatus.ACTIVE;
        ProductStatus inactiveStatus = ProductStatus.INACTIVE;

        // When & Then
        assertThat(activeStatus.compareTo(inactiveStatus)).isLessThan(0);
        assertThat(inactiveStatus.compareTo(activeStatus)).isGreaterThan(0);
    }

    @Test
    void testRoundTripConversion() {
        // Test that value -> fromValue -> getValue returns the original value
        for (ProductStatus status : ProductStatus.values()) {
            String originalValue = status.getValue();
            ProductStatus convertedStatus = ProductStatus.fromValue(originalValue);
            String convertedValue = convertedStatus.getValue();
            
            assertThat(convertedValue).isEqualTo(originalValue);
            assertThat(convertedStatus).isEqualTo(status);
        }
    }
} 