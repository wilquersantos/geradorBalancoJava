package com.emissor.service;

import com.emissor.dto.ItemRequestDTO;
import com.emissor.dto.ItemResponseDTO;
import com.emissor.model.Item;
import com.emissor.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    
    private final ItemRepository itemRepository;
    
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }
    
    public ItemResponseDTO createItem(ItemRequestDTO requestDTO) {
        logger.info("Recebendo novo item - Código: {}, Quantidade: {}, Descrição: {}", 
            requestDTO.getCodigoReferencia(), requestDTO.getQuantidade(), requestDTO.getDescricao());
        
        Item item = new Item();
        item.setCodigoReferencia(requestDTO.getCodigoReferencia());
        item.setQuantidade(requestDTO.getQuantidade());
        item.setDescricao(requestDTO.getDescricao());
        item.setDataRecebimento(LocalDateTime.now());
        
        Item savedItem = itemRepository.save(item);
        
        logger.info("Item salvo com sucesso - ID: {}", savedItem.getId());
        
        return new ItemResponseDTO(savedItem);
    }
    
    public List<ItemResponseDTO> getAllItems() {
        return itemRepository.findAll().stream()
                .map(ItemResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    public List<ItemResponseDTO> searchItems(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllItems();
        }
        
        return itemRepository.findByCodigoOrDescricao(searchTerm).stream()
                .map(ItemResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    public ItemResponseDTO getItemById(Long id) {
        return itemRepository.findById(id)
                .map(ItemResponseDTO::new)
                .orElseThrow(() -> new RuntimeException("Item não encontrado com ID: " + id));
    }
    
    public ItemResponseDTO updateItem(Long id, ItemRequestDTO requestDTO) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado com ID: " + id));
        
        item.setCodigoReferencia(requestDTO.getCodigoReferencia());
        item.setQuantidade(requestDTO.getQuantidade());
        item.setDescricao(requestDTO.getDescricao());
        
        itemRepository.update(item);
        
        logger.info("Item atualizado - ID: {}", id);
        
        return new ItemResponseDTO(item);
    }
    
    public void deleteItem(Long id) {
        if (!itemRepository.findById(id).isPresent()) {
            throw new RuntimeException("Item não encontrado com ID: " + id);
        }
        
        itemRepository.deleteById(id);
        logger.info("Item excluído - ID: {}", id);
    }
    
    public long getTotalItems() {
        return itemRepository.count();
    }
}
