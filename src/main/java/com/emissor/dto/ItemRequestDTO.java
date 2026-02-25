package com.emissor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ItemRequestDTO {
    @NotBlank(message = "Código de referência é obrigatório")
    private String codigoReferencia;
    
    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser maior que zero")
    private Integer quantidade;
    
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    public ItemRequestDTO() {
    }

    public ItemRequestDTO(String codigoReferencia, Integer quantidade, String descricao) {
        this.codigoReferencia = codigoReferencia;
        this.quantidade = quantidade;
        this.descricao = descricao;
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
}
