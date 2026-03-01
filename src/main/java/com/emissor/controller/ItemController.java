package com.emissor.controller;

import com.emissor.dto.ColetaResponseDTO;
import com.emissor.dto.ItemRequestDTO;
import com.emissor.dto.ItemResponseDTO;
import com.emissor.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String coleta
    ) {
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(itemService.searchItems(search, coleta));
        }
        return ResponseEntity.ok(itemService.getAllItems(coleta));
    }

    @GetMapping("/coletas")
    public ResponseEntity<List<String>> getColetas() {
        return ResponseEntity.ok(itemService.getAllColetas());
    }

    @GetMapping("/coletas/details")
    public ResponseEntity<List<ColetaResponseDTO>> getColetasDetails() {
        return ResponseEntity.ok(itemService.getColetas());
    }

    @PostMapping("/coletas")
    public ResponseEntity<Void> createColeta(@RequestParam String nome) {
        itemService.createColeta(nome);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/coletas/{nome}/block")
    public ResponseEntity<Void> blockColeta(@PathVariable String nome, @RequestParam boolean bloqueada) {
        itemService.setColetaBloqueada(nome, bloqueada);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/coletas/{nome}")
    public ResponseEntity<Void> deleteColeta(@PathVariable String nome) {
        itemService.deleteColeta(nome);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ItemResponseDTO> getItemById(@PathVariable Long id) {
        ItemResponseDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ItemResponseDTO> updateItem(
        @PathVariable Long id,
        @Valid @RequestBody ItemRequestDTO requestDTO
    ) {
        ItemResponseDTO response = itemService.updateItem(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items/count")
    public ResponseEntity<Map<String, Long>> getItemsCount(@RequestParam(required = false) String coleta) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", itemService.getTotalItems(coleta));
        return ResponseEntity.ok(response);
    }
}
