package com.ecommerce.wishlistservice.controller;

import com.ecommerce.wishlistservice.dto.*;
import com.ecommerce.wishlistservice.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "http://localhost:4200")
public class WishlistController {

    private final WishlistService wishlistService;

    @Autowired
    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    /**
     * Get all collections for a user
     */
    @GetMapping("/user/{userId}/collections")
    public ResponseEntity<List<WishlistCollectionDTO>> getUserCollections(@PathVariable Long userId) {
        List<WishlistCollectionDTO> collections = wishlistService.getUserCollections(userId);
        return ResponseEntity.ok(collections);
    }

    /**
     * Create a new collection
     */
    @PostMapping("/collections")
    public ResponseEntity<WishlistCollectionDTO> createCollection(@Valid @RequestBody CreateCollectionDTO createCollectionDTO) {
        WishlistCollectionDTO collection = wishlistService.createCollection(createCollectionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(collection);
    }

    /**
     * Get a specific collection
     */
    @GetMapping("/collections/{collectionId}/user/{userId}")
    public ResponseEntity<WishlistCollectionDTO> getCollection(
            @PathVariable Long collectionId,
            @PathVariable Long userId) {
        WishlistCollectionDTO collection = wishlistService.getCollection(collectionId, userId);
        return ResponseEntity.ok(collection);
    }

    /**
     * Update collection name
     */
    @PutMapping("/collections/{collectionId}/user/{userId}")
    public ResponseEntity<WishlistCollectionDTO> updateCollection(
            @PathVariable Long collectionId,
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        String newName = request.get("name");
        WishlistCollectionDTO collection = wishlistService.updateCollection(collectionId, userId, newName);
        return ResponseEntity.ok(collection);
    }

    /**
     * Delete a collection
     */
    @DeleteMapping("/collections/{collectionId}/user/{userId}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable Long collectionId,
            @PathVariable Long userId) {
        wishlistService.deleteCollection(collectionId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add item to collection
     */
    @PostMapping("/collections/{collectionId}/user/{userId}/items")
    public ResponseEntity<WishlistItemDTO> addItemToCollection(
            @PathVariable Long collectionId,
            @PathVariable Long userId,
            @Valid @RequestBody AddItemToCollectionDTO addItemDTO) {
        WishlistItemDTO item = wishlistService.addItemToCollection(collectionId, userId, addItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    /**
     * Remove item from collection
     */
    @DeleteMapping("/collections/{collectionId}/user/{userId}/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCollection(
            @PathVariable Long collectionId,
            @PathVariable Long userId,
            @PathVariable Long itemId) {
        wishlistService.removeItemFromCollection(collectionId, userId, itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all items for a user across all collections
     */
    @GetMapping("/user/{userId}/items")
    public ResponseEntity<List<WishlistItemDTO>> getAllUserItems(@PathVariable Long userId) {
        List<WishlistItemDTO> items = wishlistService.getAllUserItems(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Check if a product is in user's wishlist
     */
    @GetMapping("/user/{userId}/product/{productId}/exists")
    public ResponseEntity<Map<String, Boolean>> isProductInWishlist(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        boolean exists = wishlistService.isProductInWishlist(userId, productId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Get wishlist statistics for a user
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<WishlistStatsDTO> getUserWishlistStats(@PathVariable Long userId) {
        WishlistStatsDTO stats = wishlistService.getUserWishlistStats(userId);
        return ResponseEntity.ok(stats);
    }
} 