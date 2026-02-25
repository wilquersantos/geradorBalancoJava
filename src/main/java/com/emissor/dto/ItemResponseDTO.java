package com.emissor.dto;

import com.emissor.model.Item;
import java.time.LocalDateTime;

public class ItemResponseDTO {
    private Long id;
    private String codigoReferencia;
    private Integer quantidade;
    private String descricao;
    private LocalDateTime dataRecebimento;

    public ItemResponseDTO() {
    }

    public ItemResponseDTO(Item item) {
        this.id = item.getId();
        this.codigoReferencia = item.getCodigoReferencia();
        this.quantidade = item.getQuantidade();
        this.descricao = item.getDescricao();
        this.dataRecebimento = item.getDataRecebimento();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoReferencia() {
        return codigoReferencia;
    }

    public void setCodigoReferencia(String codigoReferencia) {
        this.codigoReferencia = codigoReferencia;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(LocalDateTime dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }
}
