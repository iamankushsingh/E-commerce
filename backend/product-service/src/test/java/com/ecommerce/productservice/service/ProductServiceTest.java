package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.PaginatedResponse;
import com.ecommerce.productservice.dto.ProductCreateDTO;
import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.ProductStatus;
import com.ecommerce.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private ProductDTO sampleProductDTO;
    private ProductCreateDTO sampleProductCreateDTO;

    @BeforeEach
    void setUp() {
        sampleProduct = createSampleProduct();
        sampleProductDTO = createSampleProductDTO();
        sampleProductCreateDTO = createSampleProductCreateDTO();
    }

    private Product createSampleProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("99.99"));
        product.setCategory("Electronics");
        product.setImageUrl("http://test.com/image.jpg");
        product.setStock(10);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    private ProductDTO createSampleProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setId(1L);
        dto.setName("Test Product");
        dto.setDescription("Test Description");
        dto.setPrice(new BigDecimal("99.99"));
        dto.setCategory("Electronics");
        dto.setImageUrl("http://test.com/image.jpg");
        dto.setStock(10);
        dto.setStatus("active");
        return dto;
    }

    private ProductCreateDTO createSampleProductCreateDTO() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setName("New Product");
        dto.setDescription("New Description");
        dto.setPrice(new BigDecimal("149.99"));
        dto.setCategory("Electronics");
        dto.setImageUrl("http://test.com/new-image.jpg");
        dto.setStock(15);
        dto.setStatus("active");
        return dto;
    }

    @Test
    void testGetProductsForAdmin_WithAllFilters() {
        // Given
        String category = "Electronics";
        String status = "active";
        String search = "test";
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("200.00");
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "asc";

        List<Product> products = Arrays.asList(sampleProduct);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(page, size), 1);

        when(productRepository.findProductsWithFilters(eq(category), eq(ProductStatus.ACTIVE), 
             eq(search), eq(minPrice), eq(maxPrice), any(Pageable.class)))
             .thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When
        PaginatedResponse<ProductDTO> result = productService.getProductsForAdmin(
            category, status, search, minPrice, maxPrice, page, size, sortBy, sortDirection);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getPage()).isEqualTo(1); // 1-based
        assertThat(result.getPageSize()).isEqualTo(size);
        assertThat(result.getTotalPages()).isEqualTo(1);

        verify(productRepository).findProductsWithFilters(eq(category), eq(ProductStatus.ACTIVE), 
               eq(search), eq(minPrice), eq(maxPrice), any(Pageable.class));
    }

    @Test
    void testGetProductsForAdmin_WithDescendingSort() {
        // Given
        Page<Product> productPage = new PageImpl<>(Arrays.asList(sampleProduct));
        when(productRepository.findProductsWithFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
             .thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When
        PaginatedResponse<ProductDTO> result = productService.getProductsForAdmin(
            null, null, null, null, null, 0, 10, "price", "desc");

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findProductsWithFilters(any(), any(), any(), any(), any(), 
               argThat(pageable -> pageable.getSort().getOrderFor("price").getDirection() == Sort.Direction.DESC));
    }

    @Test
    void testGetProductsForAdmin_WithNullSortBy() {
        // Given
        Page<Product> productPage = new PageImpl<>(Arrays.asList(sampleProduct));
        when(productRepository.findProductsWithFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
             .thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When
        productService.getProductsForAdmin(null, null, null, null, null, 0, 10, null, "asc");

        // Then
        verify(productRepository).findProductsWithFilters(any(), any(), any(), any(), any(), 
               argThat(pageable -> pageable.getSort().getOrderFor("updatedAt") != null));
    }

    @Test
    void testGetAllProductsForAdmin() {
        // Given
        List<Product> products = Arrays.asList(sampleProduct);
        when(productRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))).thenReturn(products);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When
        List<ProductDTO> result = productService.getAllProductsForAdmin();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(sampleProductDTO);
        verify(productRepository).findAll(Sort.by(Sort.Direction.DESC, "updatedAt"));
    }

    @Test
    void testGetActiveProducts() {
        // Given
        Page<Product> productPage = new PageImpl<>(Arrays.asList(sampleProduct));
        when(productRepository.findActiveProductsWithFilters(any(), any(), any(), any(), any(Pageable.class)))
             .thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When
        PaginatedResponse<ProductDTO> result = productService.getActiveProducts(
            "Electronics", "search", new BigDecimal("10"), new BigDecimal("200"), 0, 10, "name", "asc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        verify(productRepository).findActiveProductsWithFilters(
            eq("Electronics"), eq("search"), eq(new BigDecimal("10")), eq(new BigDecimal("200")), any(Pageable.class));
    }

    @Test
    void testCreateProduct_Success() {
        // Given
        when(productRepository.existsByNameIgnoreCase("New Product")).thenReturn(false);
        when(modelMapper.map(sampleProductCreateDTO, Product.class)).thenReturn(sampleProduct);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When
        ProductDTO result = productService.createProduct(sampleProductCreateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).existsByNameIgnoreCase("New Product");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testCreateProduct_DuplicateName_ThrowsException() {
        // Given
        when(productRepository.existsByNameIgnoreCase("New Product")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(sampleProductCreateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product with name 'New Product' already exists");

        verify(productRepository).existsByNameIgnoreCase("New Product");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testGetProductById_Found() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When
        Optional<ProductDTO> result = productService.getProductById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(sampleProductDTO);
        verify(productRepository).findById(1L);
    }

    @Test
    void testGetProductById_NotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<ProductDTO> result = productService.getProductById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(productRepository).findById(999L);
    }

    @Test
    void testUpdateProduct_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.existsByNameIgnoreCaseAndIdNot("Updated Product", 1L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        ProductDTO updateDTO = createSampleProductDTO();
        updateDTO.setName("Updated Product");

        // When
        ProductDTO result = productService.updateProduct(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).existsByNameIgnoreCaseAndIdNot("Updated Product", 1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_ProductNotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(999L, sampleProductDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found with id: 999");

        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_DuplicateName() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.existsByNameIgnoreCaseAndIdNot("Duplicate Name", 1L)).thenReturn(true);

        ProductDTO updateDTO = createSampleProductDTO();
        updateDTO.setName("Duplicate Name");

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(1L, updateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product with name 'Duplicate Name' already exists");

        verify(productRepository).findById(1L);
        verify(productRepository).existsByNameIgnoreCaseAndIdNot("Duplicate Name", 1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_Success() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = productService.deleteProduct(1L);

        // Then
        assertThat(result).isTrue();
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void testDeleteProduct_NotFound() {
        // Given
        when(productRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found with id: 999");

        verify(productRepository).existsById(999L);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void testGetAllCategories() {
        // Given
        List<String> categories = Arrays.asList("Electronics", "Clothing", "Books");
        when(productRepository.findDistinctCategories()).thenReturn(categories);

        // When
        List<String> result = productService.getAllCategories();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("Electronics", "Clothing", "Books");
        verify(productRepository).findDistinctCategories();
    }

    @Test
    void testGetActiveCategories() {
        // Given
        List<String> categories = Arrays.asList("Electronics", "Clothing");
        when(productRepository.findDistinctCategories()).thenReturn(categories);

        // When
        List<String> result = productService.getActiveCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("Electronics", "Clothing");
        verify(productRepository).findDistinctCategories();
    }

    @Test
    void testBulkDeleteProducts_Success() {
        // Given
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);
        List<Product> productsToDelete = Arrays.asList(sampleProduct, sampleProduct, sampleProduct);
        when(productRepository.findAllById(productIds)).thenReturn(productsToDelete);

        // When
        int result = productService.bulkDeleteProducts(productIds);

        // Then
        assertThat(result).isEqualTo(3);
        verify(productRepository).findAllById(productIds);
        verify(productRepository).deleteAll(productsToDelete);
    }

    @Test
    void testBulkDeleteProducts_NoProductsFound() {
        // Given
        List<Long> productIds = Arrays.asList(999L, 998L);
        when(productRepository.findAllById(productIds)).thenReturn(Arrays.asList());

        // When
        int result = productService.bulkDeleteProducts(productIds);

        // Then
        assertThat(result).isEqualTo(0);
        verify(productRepository).findAllById(productIds);
        verify(productRepository, never()).deleteAll(any());
    }

    @Test
    void testGetProductCountByStatus() {
        // Given
        when(productRepository.countByStatus(ProductStatus.ACTIVE)).thenReturn(5L);

        // When
        long result = productService.getProductCountByStatus("active");

        // Then
        assertThat(result).isEqualTo(5L);
        verify(productRepository).countByStatus(ProductStatus.ACTIVE);
    }

    @Test
    void testGetTotalProductCount() {
        // Given
        when(productRepository.count()).thenReturn(10L);

        // When
        long result = productService.getTotalProductCount();

        // Then
        assertThat(result).isEqualTo(10L);
        verify(productRepository).count();
    }

    @Test
    void testMapToDTO_VerifyStatusMapping() {
        // Given
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        // When
        Optional<ProductDTO> result = productService.getProductById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo("active");
    }

    @Test
    void testMapToEntity_VerifyStatusMapping() {
        // Given
        when(productRepository.existsByNameIgnoreCase(any())).thenReturn(false);
        when(modelMapper.map(any(ProductCreateDTO.class), eq(Product.class))).thenReturn(sampleProduct);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When
        productService.createProduct(sampleProductCreateDTO);

        // Then
        verify(modelMapper).map(sampleProductCreateDTO, Product.class);
        verify(productRepository).save(argThat(product -> 
            product.getStatus() == ProductStatus.ACTIVE));
    }

    @Test
    void testGetProductsForAdmin_WithNullStatus() {
        // Given - Test the branch where status is null/empty
        Page<Product> productPage = new PageImpl<>(Arrays.asList(sampleProduct));
        when(productRepository.findProductsWithFilters(any(), eq(null), any(), any(), any(), any(Pageable.class)))
             .thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When - Pass null status to test the branch
        PaginatedResponse<ProductDTO> result = productService.getProductsForAdmin(
            "Electronics", null, "search", null, null, 0, 10, "name", "asc");

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findProductsWithFilters(eq("Electronics"), eq(null), 
               eq("search"), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void testGetProductsForAdmin_WithEmptyStatus() {
        // Given - Test the branch where status is empty string
        Page<Product> productPage = new PageImpl<>(Arrays.asList(sampleProduct));
        when(productRepository.findProductsWithFilters(any(), eq(null), any(), any(), any(), any(Pageable.class)))
             .thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When - Pass empty string status to test the branch
        PaginatedResponse<ProductDTO> result = productService.getProductsForAdmin(
            "Electronics", "", "search", null, null, 0, 10, "name", "asc");

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findProductsWithFilters(eq("Electronics"), eq(null), 
               eq("search"), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void testGetActiveProducts_WithAscendingSort() {
        // Given - Test ascending sort direction (non-desc case)
        Page<Product> productPage = new PageImpl<>(Arrays.asList(sampleProduct));
        when(productRepository.findActiveProductsWithFilters(any(), any(), any(), any(), any(Pageable.class)))
             .thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When - Use "asc" to test the ASC branch
        PaginatedResponse<ProductDTO> result = productService.getActiveProducts(
            "Electronics", "search", null, null, 0, 10, "name", "asc");

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findActiveProductsWithFilters(
            eq("Electronics"), eq("search"), eq(null), eq(null), 
            argThat(pageable -> pageable.getSort().getOrderFor("name").getDirection() == Sort.Direction.ASC));
    }

    @Test
    void testGetActiveProducts_WithNullSortBy() {
        // Given - Test null sortBy to use default "updatedAt"
        Page<Product> productPage = new PageImpl<>(Arrays.asList(sampleProduct));
        when(productRepository.findActiveProductsWithFilters(any(), any(), any(), any(), any(Pageable.class)))
             .thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(sampleProductDTO);

        // When - Pass null sortBy to test default behavior
        PaginatedResponse<ProductDTO> result = productService.getActiveProducts(
            "Electronics", "search", null, null, 0, 10, null, "desc");

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findActiveProductsWithFilters(
            eq("Electronics"), eq("search"), eq(null), eq(null), 
            argThat(pageable -> pageable.getSort().getOrderFor("updatedAt") != null));
    }
}
