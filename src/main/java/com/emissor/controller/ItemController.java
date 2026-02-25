package com.emissor.controller;

import com.emissor.dto.ItemRequestDTO;
import com.emissor.dto.ItemResponseDTO;
import com.emissor.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ItemController {
    
    private final ItemService itemService;
    
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "EmissorJava");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/items")
    public ResponseEntity<ItemResponseDTO> createItem(@Valid @RequestBody ItemRequestDTO requestDTO) {
        ItemResponseDTO response = itemService.createItem(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/items")
    public ResponseEntity<List<ItemResponseDTO>> getAllItems(
            @RequestParam(required = false) String search) {
        
        List<ItemResponseDTO> items;
        if (search != null && !search.trim().isEmpty()) {
            items = itemService.searchItems(search);
        } else {
            items = itemService.getAllItems();
        }
        
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/items/{id}")
    public ResponseEntity<ItemResponseDTO> getItemById(@PathVariable Long id) {
        ItemResponseDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }
    
    @PutMapping("/items/{id}")
    public ResponseEntity<ItemResponseDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequestDTO requestDTO) {
        
        ItemResponseDTO response = itemService.updateItem(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/items/count")
    public ResponseEntity<Map<String, Long>> getItemsCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", itemService.getTotalItems());
        return ResponseEntity.ok(response);
    }
}
