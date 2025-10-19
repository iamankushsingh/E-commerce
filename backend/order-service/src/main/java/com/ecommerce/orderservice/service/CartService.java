package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.entity.Cart;
import com.ecommerce.orderservice.entity.CartItem;
import com.ecommerce.orderservice.repository.CartRepository;
import com.ecommerce.orderservice.repository.CartItemRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final ModelMapper modelMapper;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                      ProductServiceClient productServiceClient, ModelMapper modelMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productServiceClient = productServiceClient;
        this.modelMapper = modelMapper;
    }

    /**
     * Get or create cart for user
     */
    public CartDTO getOrCreateCartForUser(Long userId) {
        logger.info("Getting or creating cart for user: {}", userId);

        Optional<Cart> existingCart = cartRepository.findByUserIdWithCartItems(userId);
        
        Cart cart;
        if (existingCart.isPresent()) {
            cart = existingCart.get();
            logger.info("Found existing cart for user {}: {}", userId, cart.getId());
        } else {
            cart = new Cart(userId);
            cart = cartRepository.save(cart);
            logger.info("Created new cart for user {}: {}", userId, cart.getId());
        }

        return convertToCartDTO(cart);
    }

    /**
     * Add item to cart
     */
    public CartDTO addItemToCart(Long userId, AddToCartDTO addToCartDTO) {
        logger.info("Adding item to cart for user {}: productId={}, quantity={}", 
                   userId, addToCartDTO.getProductId(), addToCartDTO.getQuantity());

        // Get or create cart
        Cart cart = getOrCreateCart(userId);

        // Get product details
        ProductDTO product = productServiceClient.getProductById(addToCartDTO.getProductId());
        if (product == null) {
            throw new RuntimeException("Product not found: " + addToCartDTO.getProductId());
        }

        // Check product availability
        if (!productServiceClient.isProductAvailable(addToCartDTO.getProductId(), addToCartDTO.getQuantity())) {
            throw new RuntimeException("Product is not available in the requested quantity");
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(
                cart.getId(), addToCartDTO.getProductId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update existing item
            cartItem = existingItem.get();
            Integer newQuantity = cartItem.getQuantity() + addToCartDTO.getQuantity();
            
            // Validate total quantity
            if (!productServiceClient.isProductAvailable(addToCartDTO.getProductId(), newQuantity)) {
                throw new RuntimeException("Requested total quantity exceeds available stock");
            }
            
            cartItem.setQuantity(newQuantity);
            logger.info("Updated existing cart item. New quantity: {}", newQuantity);
        } else {
            // Create new cart item
            cartItem = new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    addToCartDTO.getQuantity(),
                    product.getImageUrl()
            );
            cart.addCartItem(cartItem);
            logger.info("Added new cart item for product: {}", product.getName());
        }

        cartItem = cartItemRepository.save(cartItem);
        cart.calculateTotalAmount();
        cart = cartRepository.save(cart);

        logger.info("Cart updated successfully. Total amount: {}", cart.getTotalAmount());
        return convertToCartDTO(cart);
    }

    /**
     * Update cart item quantity
     */
    public CartDTO updateCartItemQuantity(Long userId, Long cartItemId, UpdateCartItemDTO updateDTO) {
        logger.info("Updating cart item {} for user {}: new quantity={}", 
                   cartItemId, userId, updateDTO.getQuantity());

        Cart cart = getCartEntityByUserId(userId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + cartItemId));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getUserId().equals(userId)) {
            throw new RuntimeException("Cart item does not belong to user");
        }

        // Check product availability for new quantity
        if (!productServiceClient.isProductAvailable(cartItem.getProductId(), updateDTO.getQuantity())) {
            throw new RuntimeException("Requested quantity exceeds available stock");
        }

        cartItem.setQuantity(updateDTO.getQuantity());
        cartItem = cartItemRepository.save(cartItem);

        cart.calculateTotalAmount();
        cart = cartRepository.save(cart);

        logger.info("Cart item updated successfully. New total: {}", cart.getTotalAmount());
        return convertToCartDTO(cart);
    }

    /**
     * Remove item from cart
     */
    public CartDTO removeItemFromCart(Long userId, Long cartItemId) {
        logger.info("Removing cart item {} for user {}", cartItemId, userId);

        Cart cart = getCartEntityByUserId(userId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + cartItemId));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getUserId().equals(userId)) {
            throw new RuntimeException("Cart item does not belong to user");
        }

        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);

        cart.calculateTotalAmount();
        cart = cartRepository.save(cart);

        logger.info("Cart item removed successfully. New total: {}", cart.getTotalAmount());
        return convertToCartDTO(cart);
    }

    /**
     * Clear entire cart
     */
    public void clearCart(Long userId) {
        logger.info("Clearing cart for user: {}", userId);

        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.clearCart();
            cart = cartRepository.save(cart);
            logger.info("Cart cleared for user: {}", userId);
        } else {
            logger.warn("No cart found to clear for user: {}", userId);
        }
    }

    /**
     * Get cart by user ID
     */
    @Transactional(readOnly = true)
    public CartDTO getCartByUserId(Long userId) {
        logger.info("Getting cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserIdWithCartItems(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
        
        return convertToCartDTO(cart);
    }

    /**
     * Get cart total items count
     */
    @Transactional(readOnly = true)
    public Integer getCartItemsCount(Long userId) {
        return cartRepository.getTotalItemsByUserId(userId);
    }

    /**
     * Validate cart for checkout
     */
    public boolean validateCartForCheckout(Long userId) {
        logger.info("Validating cart for checkout for user: {}", userId);

        Optional<Cart> cartOpt = cartRepository.findByUserIdWithCartItems(userId);
        if (cartOpt.isEmpty()) {
            logger.warn("No cart found for user: {}", userId);
            return false;
        }

        Cart cart = cartOpt.get();
        if (cart.getCartItems().isEmpty()) {
            logger.warn("Cart is empty for user: {}", userId);
            return false;
        }

        // Validate each cart item
        for (CartItem item : cart.getCartItems()) {
            if (!productServiceClient.isProductAvailable(item.getProductId(), item.getQuantity())) {
                logger.warn("Product {} is not available in required quantity", item.getProductId());
                return false;
            }
        }

        logger.info("Cart validation successful for user: {}", userId);
        return true;
    }

    // Helper methods

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(userId);
                    return cartRepository.save(newCart);
                });
    }

    private Cart getCartEntityByUserId(Long userId) {
        return cartRepository.findByUserIdWithCartItems(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
    }

    private CartDTO convertToCartDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        
        // Map cart items
        List<CartItemDTO> cartItemDTOs = cart.getCartItems().stream()
                .map(item -> modelMapper.map(item, CartItemDTO.class))
                .collect(Collectors.toList());
        
        cartDTO.setCartItems(cartItemDTOs);
        cartDTO.setTotalItems(cart.getTotalItems());
        
        return cartDTO;
    }
} 