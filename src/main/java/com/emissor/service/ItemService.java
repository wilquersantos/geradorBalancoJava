package com.emissor.service;

import com.emissor.dto.ColetaResponseDTO;
import com.emissor.dto.ItemRequestDTO;
import com.emissor.dto.ItemResponseDTO;
import com.emissor.model.Coleta;
import com.emissor.model.Item;
import com.emissor.repository.ColetaRepository;
import com.emissor.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemService {

    public static final String DEFAULT_COLETA = "GERAL";

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;
    private final ColetaRepository coletaRepository;

    public ItemService(ItemRepository itemRepository, ColetaRepository coletaRepository) {
        this.itemRepository = itemRepository;
        this.coletaRepository = coletaRepository;
    }

    public ItemResponseDTO createItem(ItemRequestDTO requestDTO) {
        String coleta = normalizeColeta(requestDTO.getColeta());
        String codigoReferencia = requestDTO.getCodigoReferencia().trim();

        coletaRepository.ensureExists(coleta);
        if (coletaRepository.isBloqueada(coleta)) {
            throw new RuntimeException("Coleta bloqueada: " + coleta);
        }

        Item item = itemRepository.findFirstByCodigoReferenciaAndColeta(codigoReferencia, coleta)
            .orElseGet(Item::new);

        boolean isNewItem = item.getId() == null;
        item.setColeta(coleta);
        item.setCodigoReferencia(codigoReferencia);
        item.setQuantidade(requestDTO.getQuantidade());
        item.setDescricao(requestDTO.getDescricao());
        item.setDataRecebimento(LocalDateTime.now());

        if (isNewItem) {
            Item savedItem = itemRepository.save(item);
            logger.info("Item salvo - Coleta: {}, Codigo: {}, ID: {}", coleta, codigoReferencia, savedItem.getId());
            return new ItemResponseDTO(savedItem);
        }

        itemRepository.updateReceivedData(item);
        logger.info("Item atualizado - Coleta: {}, Codigo: {}", coleta, codigoReferencia);
        return new ItemResponseDTO(item);
    }

    public List<ItemResponseDTO> getAllItems(String coleta) {
        String coletaNormalizada = normalizeColeta(coleta);
        return itemRepository.findAllByColeta(coletaNormalizada).stream()
            .map(ItemResponseDTO::new)
            .collect(Collectors.toList());
    }

    public List<ItemResponseDTO> searchItems(String searchTerm, String coleta) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllItems(coleta);
        }
        String coletaNormalizada = normalizeColeta(coleta);
        return itemRepository.findByCodigoOrDescricaoAndColeta(searchTerm.trim(), coletaNormalizada).stream()
            .map(ItemResponseDTO::new)
            .collect(Collectors.toList());
    }

    public ItemResponseDTO getItemById(Long id) {
        return itemRepository.findById(id)
            .map(ItemResponseDTO::new)
            .orElseThrow(() -> new RuntimeException("Item nao encontrado com ID: " + id));
    }

    public ItemResponseDTO updateItem(Long id, ItemRequestDTO requestDTO) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Item nao encontrado com ID: " + id));

        String coleta = normalizeColeta(requestDTO.getColeta());
        coletaRepository.ensureExists(coleta);
        if (coletaRepository.isBloqueada(coleta)) {
            throw new RuntimeException("Coleta bloqueada: " + coleta);
        }

        item.setColeta(coleta);
        item.setCodigoReferencia(requestDTO.getCodigoReferencia().trim());
        item.setQuantidade(requestDTO.getQuantidade());
        item.setDescricao(requestDTO.getDescricao());

        itemRepository.update(item);
        logger.info("Item editado - ID: {}", id);
        return new ItemResponseDTO(item);
    }

    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Item nao encontrado com ID: " + id));

        if (coletaRepository.isBloqueada(item.getColeta())) {
            throw new RuntimeException("Coleta bloqueada: " + item.getColeta());
        }

        itemRepository.deleteById(id);
    }

    public long getTotalItems(String coleta) {
        return itemRepository.countByColeta(normalizeColeta(coleta));
    }

    public List<String> getAllColetas() {
        return getColetas().stream()
            .map(ColetaResponseDTO::getNome)
            .collect(Collectors.toList());
    }

    public List<ColetaResponseDTO> getColetas() {
        Map<String, Long> countsByColeta = itemRepository.countByColetas();
        return coletaRepository.findAll().stream()
            .map(coleta -> new ColetaResponseDTO(
                coleta.getNome(),
                coleta.isBloqueada(),
                countsByColeta.getOrDefault(coleta.getNome(), 0L)
            ))
            .collect(Collectors.toList());
    }

    public void createColeta(String nome) {
        String coleta = normalizeColeta(nome);
        coletaRepository.ensureExists(coleta);
    }

    public void setColetaBloqueada(String nome, boolean bloqueada) {
        String coleta = normalizeColeta(nome);
        if (DEFAULT_COLETA.equalsIgnoreCase(coleta) && bloqueada) {
            throw new RuntimeException("A coleta GERAL nao pode ser bloqueada.");
        }
        coletaRepository.setBloqueada(coleta, bloqueada);
    }

    public void deleteColeta(String nome) {
        String coleta = normalizeColeta(nome);
        if (DEFAULT_COLETA.equalsIgnoreCase(coleta)) {
            throw new RuntimeException("A coleta GERAL nao pode ser excluida.");
        }
        long totalItens = itemRepository.countByColeta(coleta);
        if (totalItens > 0) {
            throw new RuntimeException("Nao e possivel excluir coleta com itens. Remova os itens antes.");
        }
        coletaRepository.deleteByNome(coleta);
    }

    public static String normalizeColeta(String coleta) {
        if (coleta == null || coleta.trim().isEmpty()) {
            return DEFAULT_COLETA;
        }
        return coleta.trim();
    }
}
