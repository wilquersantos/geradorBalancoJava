package com.emissor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ItemRequestDTO {
    private String coleta;

    @NotBlank(message = "Codigo de referencia e obrigatorio")
    private String codigoReferencia;

    @NotNull(message = "Quantidade e obrigatoria")
    @Positive(message = "Quantidade deve ser maior que zero")
    private Integer quantidade;

    @NotBlank(message = "Descricao e obrigatoria")
    private String descricao;

    public ItemRequestDTO() {
    }

    public ItemRequestDTO(String codigoReferencia, Integer quantidade, String descricao) {
        this.codigoReferencia = codigoReferencia;
        this.quantidade = quantidade;
        this.descricao = descricao;
    }

    public ItemRequestDTO(String coleta, String codigoReferencia, Integer quantidade, String descricao) {
        this.coleta = coleta;
        this.codigoReferencia = codigoReferencia;
        this.quantidade = quantidade;
        this.descricao = descricao;
    }

    public String getColeta() {
        return coleta;
    }

    public void setColeta(String coleta) {
        this.coleta = coleta;
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
