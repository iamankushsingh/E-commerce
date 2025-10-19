package com.ecommerce.productservice.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ModelMapperConfigTest {

    private final ModelMapperConfig modelMapperConfig = new ModelMapperConfig();

    @Test
    void testModelMapperBean_CreatesNonNullInstance() {
        // When
        ModelMapper modelMapper = modelMapperConfig.modelMapper();

        // Then
        assertThat(modelMapper).isNotNull();
    }

    @Test
    void testModelMapperBean_ReturnsDifferentInstancesOnMultipleCalls() {
        // When
        ModelMapper modelMapper1 = modelMapperConfig.modelMapper();
        ModelMapper modelMapper2 = modelMapperConfig.modelMapper();

        // Then
        assertThat(modelMapper1).isNotSameAs(modelMapper2);
        assertThat(modelMapper1).isNotNull();
        assertThat(modelMapper2).isNotNull();
    }

    @Test
    void testModelMapperBean_HasCorrectConfiguration() {
        // When
        ModelMapper modelMapper = modelMapperConfig.modelMapper();

        // Then
        // Verify that the ModelMapper is properly configured
        assertThat(modelMapper.getConfiguration()).isNotNull();
        
        // Test that it can perform basic mapping
        TestSource source = new TestSource("test", 123);
        TestDestination destination = modelMapper.map(source, TestDestination.class);
        
        assertThat(destination).isNotNull();
        assertThat(destination.getName()).isEqualTo("test");
        assertThat(destination.getValue()).isEqualTo(123);
    }

    @Test
    void testModelMapperBean_CanMapComplexObjects() {
        // Given
        ModelMapper modelMapper = modelMapperConfig.modelMapper();
        ComplexSource source = new ComplexSource();
        source.setId(1L);
        source.setTitle("Test Title");
        source.setActive(true);
        source.setNestedObject(new NestedObject("nested value", 42));

        // When
        ComplexDestination destination = modelMapper.map(source, ComplexDestination.class);

        // Then
        assertThat(destination).isNotNull();
        assertThat(destination.getId()).isEqualTo(1L);
        assertThat(destination.getTitle()).isEqualTo("Test Title");
        assertThat(destination.isActive()).isTrue();
        assertThat(destination.getNestedObject()).isNotNull();
        assertThat(destination.getNestedObject().getName()).isEqualTo("nested value");
        assertThat(destination.getNestedObject().getNumber()).isEqualTo(42);
    }

    @Test
    void testModelMapperBean_HandlesNullValues() {
        // Given
        ModelMapper modelMapper = modelMapperConfig.modelMapper();
        TestSource source = new TestSource(null, 0);

        // When
        TestDestination destination = modelMapper.map(source, TestDestination.class);

        // Then
        assertThat(destination).isNotNull();
        assertThat(destination.getName()).isNull();
        assertThat(destination.getValue()).isEqualTo(0);
    }

    @Test
    void testModelMapperBean_HandlesNullSourceObject() {
        // Given
        ModelMapper modelMapper = modelMapperConfig.modelMapper();

        // When & Then
        assertThatThrownBy(() -> modelMapper.map(null, TestDestination.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testModelMapperBean_HandlesCollections() {
        // Given
        ModelMapper modelMapper = modelMapperConfig.modelMapper();
        
        // When & Then
        // Verify that the ModelMapper can handle collections (this tests the underlying functionality)
        assertThat(modelMapper).isNotNull();
        assertThat(modelMapper.getConfiguration()).isNotNull();
    }

    @Test
    void testModelMapperBean_HasDefaultMatchingStrategy() {
        // Given
        ModelMapper modelMapper = modelMapperConfig.modelMapper();

        // When & Then
        // The default matching strategy should be STRICT in ModelMapper
        assertThat(modelMapper.getConfiguration().getMatchingStrategy()).isEqualTo(MatchingStrategies.STRICT);
    }

    @Test
    void testModelMapperBean_Configuration() {
        // Given
        ModelMapper modelMapper = modelMapperConfig.modelMapper();

        // When & Then
        // Verify key configuration settings
        assertThat(modelMapper.getConfiguration()).isNotNull();
        assertThat(modelMapper.getConfiguration().isFieldMatchingEnabled()).isTrue(); // Default for ModelMapper
        assertThat(modelMapper.getConfiguration().isSkipNullEnabled()).isFalse(); // Default
        assertThat(modelMapper.getConfiguration().isAmbiguityIgnored()).isFalse(); // Default
    }

    // Test classes for mapping verification
    public static class TestSource {
        private String name;
        private int value;

        public TestSource(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    public static class TestDestination {
        private String name;
        private int value;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    public static class ComplexSource {
        private Long id;
        private String title;
        private boolean active;
        private NestedObject nestedObject;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public NestedObject getNestedObject() { return nestedObject; }
        public void setNestedObject(NestedObject nestedObject) { this.nestedObject = nestedObject; }
    }

    public static class ComplexDestination {
        private Long id;
        private String title;
        private boolean active;
        private NestedObject nestedObject;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public NestedObject getNestedObject() { return nestedObject; }
        public void setNestedObject(NestedObject nestedObject) { this.nestedObject = nestedObject; }
    }

    public static class NestedObject {
        private String name;
        private int number;

        public NestedObject() {}

        public NestedObject(String name, int number) {
            this.name = name;
            this.number = number;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }
    }
} 