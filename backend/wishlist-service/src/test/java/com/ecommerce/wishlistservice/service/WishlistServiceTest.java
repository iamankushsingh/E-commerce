package com.ecommerce.wishlistservice.service;

import com.ecommerce.wishlistservice.dto.*;
import com.ecommerce.wishlistservice.entity.WishlistCollection;
import com.ecommerce.wishlistservice.entity.WishlistItem;
import com.ecommerce.wishlistservice.exception.DuplicateItemException;
import com.ecommerce.wishlistservice.exception.WishlistNotFoundException;
import com.ecommerce.wishlistservice.repository.WishlistCollectionRepository;
import com.ecommerce.wishlistservice.repository.WishlistItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistCollectionRepository collectionRepository;

    @Mock
    private WishlistItemRepository itemRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private WishlistService wishlistService;

    private WishlistCollection testCollection;
    private WishlistItem testItem;
    private WishlistCollectionDTO testCollectionDTO;
    private WishlistItemDTO testItemDTO;
    private CreateCollectionDTO createCollectionDTO;
    private AddItemToCollectionDTO addItemDTO;

    @BeforeEach
    void setUp() {
        testCollection = new WishlistCollection("My Wishlist", 1L);
        testCollection.setId(1L);
        testCollection.setCreatedAt(LocalDateTime.now());
        testCollection.setUpdatedAt(LocalDateTime.now());

        testItem = new WishlistItem(1L, "Test Product", new BigDecimal("99.99"), "Electronics", "http://example.com/image.jpg");
        testItem.setId(1L);
        testItem.setCollection(testCollection);
        testItem.setAddedAt(LocalDateTime.now());

        testCollectionDTO = new WishlistCollectionDTO();
        testCollectionDTO.setId(1L);
        testCollectionDTO.setName("My Wishlist");
        testCollectionDTO.setUserId(1L);

        testItemDTO = new WishlistItemDTO();
        testItemDTO.setId(1L);
        testItemDTO.setProductId(1L);
        testItemDTO.setProductName("Test Product");
        testItemDTO.setPrice(new BigDecimal("99.99"));

        createCollectionDTO = new CreateCollectionDTO();
        createCollectionDTO.setName("New Wishlist");
        createCollectionDTO.setUserId(1L);

        addItemDTO = new AddItemToCollectionDTO();
        addItemDTO.setProductId(1L);
        addItemDTO.setProductName("Test Product");
        addItemDTO.setPrice(new BigDecimal("99.99"));
        addItemDTO.setCategory("Electronics");
        addItemDTO.setImageUrl("http://example.com/image.jpg");
    }

    @Test
    void testGetUserCollections_Success() {
        when(collectionRepository.findByUserIdWithItems(1L)).thenReturn(Arrays.asList(testCollection));
        when(modelMapper.map(any(WishlistCollection.class), eq(WishlistCollectionDTO.class))).thenReturn(testCollectionDTO);

        List<WishlistCollectionDTO> result = wishlistService.getUserCollections(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(collectionRepository).findByUserIdWithItems(1L);
    }

    @Test
    void testGetUserCollections_Empty() {
        when(collectionRepository.findByUserIdWithItems(1L)).thenReturn(Arrays.asList());

        List<WishlistCollectionDTO> result = wishlistService.getUserCollections(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testCreateCollection_Success() {
        when(collectionRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(false);
        when(collectionRepository.save(any(WishlistCollection.class))).thenReturn(testCollection);
        when(modelMapper.map(any(WishlistCollection.class), eq(WishlistCollectionDTO.class))).thenReturn(testCollectionDTO);

        WishlistCollectionDTO result = wishlistService.createCollection(createCollectionDTO);

        assertNotNull(result);
        assertEquals(testCollectionDTO.getName(), result.getName());
        verify(collectionRepository).save(any(WishlistCollection.class));
    }

    @Test
    void testCreateCollection_DuplicateName() {
        when(collectionRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(true);

        assertThrows(DuplicateItemException.class, () -> {
            wishlistService.createCollection(createCollectionDTO);
        });

        verify(collectionRepository, never()).save(any(WishlistCollection.class));
    }

    @Test
    void testGetCollection_Success() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCollection));
        when(modelMapper.map(any(WishlistCollection.class), eq(WishlistCollectionDTO.class))).thenReturn(testCollectionDTO);

        WishlistCollectionDTO result = wishlistService.getCollection(1L, 1L);

        assertNotNull(result);
        assertEquals(testCollectionDTO.getId(), result.getId());
    }

    @Test
    void testGetCollection_NotFound() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(WishlistNotFoundException.class, () -> {
            wishlistService.getCollection(1L, 1L);
        });
    }

    @Test
    void testUpdateCollection_Success() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCollection));
        when(collectionRepository.existsByNameAndUserId("Updated Name", 1L)).thenReturn(false);
        when(collectionRepository.save(any(WishlistCollection.class))).thenReturn(testCollection);
        when(modelMapper.map(any(WishlistCollection.class), eq(WishlistCollectionDTO.class))).thenReturn(testCollectionDTO);

        WishlistCollectionDTO result = wishlistService.updateCollection(1L, 1L, "Updated Name");

        assertNotNull(result);
        verify(collectionRepository).save(any(WishlistCollection.class));
    }

    @Test
    void testUpdateCollection_SameName() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCollection));
        when(collectionRepository.save(any(WishlistCollection.class))).thenReturn(testCollection);
        when(modelMapper.map(any(WishlistCollection.class), eq(WishlistCollectionDTO.class))).thenReturn(testCollectionDTO);

        WishlistCollectionDTO result = wishlistService.updateCollection(1L, 1L, "My Wishlist");

        assertNotNull(result);
        verify(collectionRepository).save(any(WishlistCollection.class));
    }

    @Test
    void testUpdateCollection_DuplicateName() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCollection));
        when(collectionRepository.existsByNameAndUserId("Existing Name", 1L)).thenReturn(true);

        assertThrows(DuplicateItemException.class, () -> {
            wishlistService.updateCollection(1L, 1L, "Existing Name");
        });
    }

    @Test
    void testUpdateCollection_NotFound() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(WishlistNotFoundException.class, () -> {
            wishlistService.updateCollection(1L, 1L, "New Name");
        });
    }

    @Test
    void testDeleteCollection_Success() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCollection));
        doNothing().when(collectionRepository).delete(any(WishlistCollection.class));

        wishlistService.deleteCollection(1L, 1L);

        verify(collectionRepository).delete(any(WishlistCollection.class));
    }

    @Test
    void testDeleteCollection_NotFound() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(WishlistNotFoundException.class, () -> {
            wishlistService.deleteCollection(1L, 1L);
        });
    }

    @Test
    void testAddItemToCollection_Success() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCollection));
        when(itemRepository.existsByCollectionIdAndProductId(1L, 1L)).thenReturn(false);
        when(itemRepository.save(any(WishlistItem.class))).thenReturn(testItem);
        when(modelMapper.map(any(WishlistItem.class), eq(WishlistItemDTO.class))).thenReturn(testItemDTO);

        WishlistItemDTO result = wishlistService.addItemToCollection(1L, 1L, addItemDTO);

        assertNotNull(result);
        assertEquals(testItemDTO.getProductId(), result.getProductId());
        verify(itemRepository).save(any(WishlistItem.class));
    }

    @Test
    void testAddItemToCollection_CollectionNotFound() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(WishlistNotFoundException.class, () -> {
            wishlistService.addItemToCollection(1L, 1L, addItemDTO);
        });
    }

    @Test
    void testAddItemToCollection_DuplicateItem() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCollection));
        when(itemRepository.existsByCollectionIdAndProductId(1L, 1L)).thenReturn(true);

        assertThrows(DuplicateItemException.class, () -> {
            wishlistService.addItemToCollection(1L, 1L, addItemDTO);
        });

        verify(itemRepository, never()).save(any(WishlistItem.class));
    }

    @Test
    void testRemoveItemFromCollection_Success() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCollection));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        doNothing().when(itemRepository).delete(any(WishlistItem.class));

        wishlistService.removeItemFromCollection(1L, 1L, 1L);

        verify(itemRepository).delete(any(WishlistItem.class));
    }

    @Test
    void testRemoveItemFromCollection_CollectionNotFound() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(WishlistNotFoundException.class, () -> {
            wishlistService.removeItemFromCollection(1L, 1L, 1L);
        });
    }

    @Test
    void testRemoveItemFromCollection_ItemNotFound() {
        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCollection));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(WishlistNotFoundException.class, () -> {
            wishlistService.removeItemFromCollection(1L, 1L, 1L);
        });
    }

    @Test
    void testRemoveItemFromCollection_ItemNotInCollection() {
        WishlistCollection otherCollection = new WishlistCollection("Other", 1L);
        otherCollection.setId(2L);
        testItem.setCollection(otherCollection);

        when(collectionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCollection));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        assertThrows(WishlistNotFoundException.class, () -> {
            wishlistService.removeItemFromCollection(1L, 1L, 1L);
        });
    }

    @Test
    void testGetAllUserItems_Success() {
        when(itemRepository.findByUserId(1L)).thenReturn(Arrays.asList(testItem));
        when(modelMapper.map(any(WishlistItem.class), eq(WishlistItemDTO.class))).thenReturn(testItemDTO);

        List<WishlistItemDTO> result = wishlistService.getAllUserItems(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemRepository).findByUserId(1L);
    }

    @Test
    void testGetAllUserItems_Empty() {
        when(itemRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        List<WishlistItemDTO> result = wishlistService.getAllUserItems(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testIsProductInWishlist_True() {
        when(itemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Arrays.asList(testItem));

        boolean result = wishlistService.isProductInWishlist(1L, 1L);

        assertTrue(result);
    }

    @Test
    void testIsProductInWishlist_False() {
        when(itemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Arrays.asList());

        boolean result = wishlistService.isProductInWishlist(1L, 1L);

        assertFalse(result);
    }

    @Test
    void testGetUserWishlistStats_Success() {
        when(collectionRepository.countByUserId(1L)).thenReturn(5L);
        when(itemRepository.countByUserId(1L)).thenReturn(20L);

        WishlistStatsDTO result = wishlistService.getUserWishlistStats(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(5L, result.getTotalCollections());
        assertEquals(20L, result.getTotalItems());
    }

    @Test
    void testConvertToCollectionDTO_WithItems() {
        testCollection.setItems(Arrays.asList(testItem));
        when(modelMapper.map(any(WishlistCollection.class), eq(WishlistCollectionDTO.class))).thenReturn(testCollectionDTO);
        when(modelMapper.map(any(WishlistItem.class), eq(WishlistItemDTO.class))).thenReturn(testItemDTO);

        List<WishlistCollectionDTO> result = wishlistService.getUserCollections(1L);
        when(collectionRepository.findByUserIdWithItems(1L)).thenReturn(Arrays.asList(testCollection));
        result = wishlistService.getUserCollections(1L);

        assertNotNull(result);
    }
}

