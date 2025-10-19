package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.entity.Cart;
import com.ecommerce.orderservice.entity.CartItem;
import com.ecommerce.orderservice.repository.CartItemRepository;
import com.ecommerce.orderservice.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CartService cartService;

    private Cart testCart;
    private CartItem testCartItem;
    private CartDTO testCartDTO;
    private ProductDTO testProduct;
    private AddToCartDTO addToCartDTO;

    @BeforeEach
    void setUp() {
        testCart = new Cart(1L);
        testCart.setId(1L);

        testCartItem = new CartItem(1L, "Test Product", new BigDecimal("99.99"), 2, "http://example.com/image.jpg");
        testCartItem.setId(1L);
        testCartItem.setCart(testCart);

        testCartDTO = new CartDTO();
        testCartDTO.setId(1L);
        testCartDTO.setUserId(1L);
        testCartDTO.setTotalAmount(new BigDecimal("199.98"));

        testProduct = new ProductDTO();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setImageUrl("http://example.com/image.jpg");

        addToCartDTO = new AddToCartDTO();
        addToCartDTO.setProductId(1L);
        addToCartDTO.setQuantity(2);
    }

    @Test
    void testGetOrCreateCartForUser_ExistingCart() {
        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(testCartDTO);

        CartDTO result = cartService.getOrCreateCartForUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testGetOrCreateCartForUser_NewCart() {
        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(testCartDTO);

        CartDTO result = cartService.getOrCreateCartForUser(1L);

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testAddItemToCart_NewItem() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProductById(1L)).thenReturn(testProduct);
        when(productServiceClient.isProductAvailable(1L, 2)).thenReturn(true);
        when(cartItemRepository.findByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(testCartDTO);

        CartDTO result = cartService.addItemToCart(1L, addToCartDTO);

        assertNotNull(result);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void testAddItemToCart_UpdateExistingItem() {
        testCart.getCartItems().add(testCartItem);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProductById(1L)).thenReturn(testProduct);
        when(productServiceClient.isProductAvailable(eq(1L), anyInt())).thenReturn(true);
        when(cartItemRepository.findByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(testCartDTO);

        CartDTO result = cartService.addItemToCart(1L, addToCartDTO);

        assertNotNull(result);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void testAddItemToCart_ProductNotFound() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProductById(1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            cartService.addItemToCart(1L, addToCartDTO);
        });
    }

    @Test
    void testAddItemToCart_ProductNotAvailable() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProductById(1L)).thenReturn(testProduct);
        when(productServiceClient.isProductAvailable(1L, 2)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            cartService.addItemToCart(1L, addToCartDTO);
        });
    }

    @Test
    void testAddItemToCart_ExceedsStock() {
        testCart.getCartItems().add(testCartItem);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProductById(1L)).thenReturn(testProduct);
        when(productServiceClient.isProductAvailable(1L, 2)).thenReturn(true);
        when(productServiceClient.isProductAvailable(1L, 4)).thenReturn(false);
        when(cartItemRepository.findByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.of(testCartItem));

        assertThrows(RuntimeException.class, () -> {
            cartService.addItemToCart(1L, addToCartDTO);
        });
    }

    @Test
    void testUpdateCartItemQuantity_Success() {
        testCart.getCartItems().add(testCartItem);
        UpdateCartItemDTO updateDTO = new UpdateCartItemDTO();
        updateDTO.setQuantity(5);

        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(productServiceClient.isProductAvailable(1L, 5)).thenReturn(true);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(testCartDTO);

        CartDTO result = cartService.updateCartItemQuantity(1L, 1L, updateDTO);

        assertNotNull(result);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void testUpdateCartItemQuantity_ItemNotFound() {
        UpdateCartItemDTO updateDTO = new UpdateCartItemDTO();
        updateDTO.setQuantity(5);

        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            cartService.updateCartItemQuantity(1L, 1L, updateDTO);
        });
    }

    @Test
    void testUpdateCartItemQuantity_WrongUser() {
        Cart otherCart = new Cart(2L);
        otherCart.setId(2L);
        testCartItem.setCart(otherCart);
        UpdateCartItemDTO updateDTO = new UpdateCartItemDTO();
        updateDTO.setQuantity(5);

        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));

        assertThrows(RuntimeException.class, () -> {
            cartService.updateCartItemQuantity(1L, 1L, updateDTO);
        });
    }

    @Test
    void testUpdateCartItemQuantity_ExceedsStock() {
        testCart.getCartItems().add(testCartItem);
        UpdateCartItemDTO updateDTO = new UpdateCartItemDTO();
        updateDTO.setQuantity(100);

        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(productServiceClient.isProductAvailable(1L, 100)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            cartService.updateCartItemQuantity(1L, 1L, updateDTO);
        });
    }

    @Test
    void testRemoveItemFromCart_Success() {
        testCart.getCartItems().add(testCartItem);
        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(any(CartItem.class));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(testCartDTO);

        CartDTO result = cartService.removeItemFromCart(1L, 1L);

        assertNotNull(result);
        verify(cartItemRepository).delete(any(CartItem.class));
    }

    @Test
    void testRemoveItemFromCart_ItemNotFound() {
        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            cartService.removeItemFromCart(1L, 1L);
        });
    }

    @Test
    void testRemoveItemFromCart_WrongUser() {
        Cart otherCart = new Cart(2L);
        otherCart.setId(2L);
        testCartItem.setCart(otherCart);

        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));

        assertThrows(RuntimeException.class, () -> {
            cartService.removeItemFromCart(1L, 1L);
        });
    }

    @Test
    void testClearCart_Success() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.clearCart(1L);

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testClearCart_NoCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        cartService.clearCart(1L);

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testGetCartByUserId_Success() {
        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(testCartDTO);

        CartDTO result = cartService.getCartByUserId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
    }

    @Test
    void testGetCartByUserId_NotFound() {
        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            cartService.getCartByUserId(1L);
        });
    }

    @Test
    void testGetCartItemsCount() {
        when(cartRepository.getTotalItemsByUserId(1L)).thenReturn(5);

        Integer result = cartService.getCartItemsCount(1L);

        assertEquals(5, result);
    }

    @Test
    void testValidateCartForCheckout_Success() {
        testCart.getCartItems().add(testCartItem);
        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.isProductAvailable(anyLong(), anyInt())).thenReturn(true);

        boolean result = cartService.validateCartForCheckout(1L);

        assertTrue(result);
    }

    @Test
    void testValidateCartForCheckout_NoCart() {
        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.empty());

        boolean result = cartService.validateCartForCheckout(1L);

        assertFalse(result);
    }

    @Test
    void testValidateCartForCheckout_EmptyCart() {
        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));

        boolean result = cartService.validateCartForCheckout(1L);

        assertFalse(result);
    }

    @Test
    void testValidateCartForCheckout_ProductNotAvailable() {
        testCart.getCartItems().add(testCartItem);
        when(cartRepository.findByUserIdWithCartItems(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.isProductAvailable(anyLong(), anyInt())).thenReturn(false);

        boolean result = cartService.validateCartForCheckout(1L);

        assertFalse(result);
    }
}

