package com.emissor.model;

import java.time.LocalDateTime;

public class Item {
    private Long id;
    private String codigoReferencia;
    private Integer quantidade;
    private String descricao;
    private LocalDateTime dataRecebimento;

    public Item() {
    }

    public Item(Long id, String codigoReferencia, Integer quantidade, String descricao, LocalDateTime dataRecebimento) {
        this.id = id;
        this.codigoReferencia = codigoReferencia;
        this.quantidade = quantidade;
        this.descricao = descricao;
        this.dataRecebimento = dataRecebimento;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", codigoReferencia='" + codigoReferencia + '\'' +
                ", quantidade=" + quantidade +
                ", descricao='" + descricao + '\'' +
                ", dataRecebimento=" + dataRecebimento +
                '}';
    }
}
