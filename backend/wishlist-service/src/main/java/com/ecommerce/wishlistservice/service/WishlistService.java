package com.ecommerce.wishlistservice.service;

import com.ecommerce.wishlistservice.dto.*;
import com.ecommerce.wishlistservice.entity.WishlistCollection;
import com.ecommerce.wishlistservice.entity.WishlistItem;
import com.ecommerce.wishlistservice.exception.DuplicateItemException;
import com.ecommerce.wishlistservice.exception.WishlistNotFoundException;
import com.ecommerce.wishlistservice.repository.WishlistCollectionRepository;
import com.ecommerce.wishlistservice.repository.WishlistItemRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistService {

    private final WishlistCollectionRepository collectionRepository;
    private final WishlistItemRepository itemRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public WishlistService(WishlistCollectionRepository collectionRepository,
                          WishlistItemRepository itemRepository,
                          ModelMapper modelMapper) {
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Get all collections for a user
     */
    @Transactional(readOnly = true)
    public List<WishlistCollectionDTO> getUserCollections(Long userId) {
        List<WishlistCollection> collections = collectionRepository.findByUserIdWithItems(userId);
        return collections.stream()
                .map(this::convertToCollectionDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new collection for a user
     */
    public WishlistCollectionDTO createCollection(CreateCollectionDTO createCollectionDTO) {
        // Check if collection with same name already exists for user
        if (collectionRepository.existsByNameAndUserId(createCollectionDTO.getName(), createCollectionDTO.getUserId())) {
            throw new DuplicateItemException("Collection with name '" + createCollectionDTO.getName() + "' already exists");
        }

        WishlistCollection collection = new WishlistCollection(
                createCollectionDTO.getName(),
                createCollectionDTO.getUserId()
        );

        WishlistCollection savedCollection = collectionRepository.save(collection);
        return convertToCollectionDTO(savedCollection);
    }

    /**
     * Get a specific collection by ID
     */
    @Transactional(readOnly = true)
    public WishlistCollectionDTO getCollection(Long collectionId, Long userId) {
        WishlistCollection collection = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new WishlistNotFoundException("Collection not found with id: " + collectionId));
        return convertToCollectionDTO(collection);
    }

    /**
     * Update collection name
     */
    public WishlistCollectionDTO updateCollection(Long collectionId, Long userId, String newName) {
        WishlistCollection collection = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new WishlistNotFoundException("Collection not found with id: " + collectionId));

        // Check if new name conflicts with existing collection
        if (!collection.getName().equals(newName) && 
            collectionRepository.existsByNameAndUserId(newName, userId)) {
            throw new DuplicateItemException("Collection with name '" + newName + "' already exists");
        }

        collection.setName(newName);
        WishlistCollection savedCollection = collectionRepository.save(collection);
        return convertToCollectionDTO(savedCollection);
    }

    /**
     * Delete a collection
     */
    public void deleteCollection(Long collectionId, Long userId) {
        WishlistCollection collection = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new WishlistNotFoundException("Collection not found with id: " + collectionId));
        collectionRepository.delete(collection);
    }

    /**
     * Add item to collection
     */
    public WishlistItemDTO addItemToCollection(Long collectionId, Long userId, AddItemToCollectionDTO addItemDTO) {
        WishlistCollection collection = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new WishlistNotFoundException("Collection not found with id: " + collectionId));

        // Check if item already exists in collection
        if (itemRepository.existsByCollectionIdAndProductId(collectionId, addItemDTO.getProductId())) {
            throw new DuplicateItemException("Product already exists in this collection");
        }

        WishlistItem item = new WishlistItem(
                addItemDTO.getProductId(),
                addItemDTO.getProductName(),
                addItemDTO.getPrice(),
                addItemDTO.getCategory(),
                addItemDTO.getImageUrl()
        );

        collection.addItem(item);
        WishlistItem savedItem = itemRepository.save(item);
        return convertToItemDTO(savedItem);
    }

    /**
     * Remove item from collection
     */
    public void removeItemFromCollection(Long collectionId, Long userId, Long itemId) {
        WishlistCollection collection = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new WishlistNotFoundException("Collection not found with id: " + collectionId));

        WishlistItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new WishlistNotFoundException("Item not found with id: " + itemId));

        if (!item.getCollection().getId().equals(collectionId)) {
            throw new WishlistNotFoundException("Item does not belong to this collection");
        }

        collection.removeItem(item);
        itemRepository.delete(item);
    }

    /**
     * Get all items for a user across all collections
     */
    @Transactional(readOnly = true)
    public List<WishlistItemDTO> getAllUserItems(Long userId) {
        List<WishlistItem> items = itemRepository.findByUserId(userId);
        return items.stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if a product is in user's wishlist
     */
    @Transactional(readOnly = true)
    public boolean isProductInWishlist(Long userId, Long productId) {
        List<WishlistItem> items = itemRepository.findByUserIdAndProductId(userId, productId);
        return !items.isEmpty();
    }

    /**
     * Get wishlist statistics for a user
     */
    @Transactional(readOnly = true)
    public WishlistStatsDTO getUserWishlistStats(Long userId) {
        long collectionsCount = collectionRepository.countByUserId(userId);
        long itemsCount = itemRepository.countByUserId(userId);
        
        WishlistStatsDTO stats = new WishlistStatsDTO();
        stats.setUserId(userId);
        stats.setTotalCollections(collectionsCount);
        stats.setTotalItems(itemsCount);
        return stats;
    }

    // Helper methods for DTO conversion
    private WishlistCollectionDTO convertToCollectionDTO(WishlistCollection collection) {
        WishlistCollectionDTO dto = modelMapper.map(collection, WishlistCollectionDTO.class);
        
        if (collection.getItems() != null) {
            List<WishlistItemDTO> itemDTOs = collection.getItems().stream()
                    .map(this::convertToItemDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }
        
        return dto;
    }

    private WishlistItemDTO convertToItemDTO(WishlistItem item) {
        return modelMapper.map(item, WishlistItemDTO.class);
    }
} 