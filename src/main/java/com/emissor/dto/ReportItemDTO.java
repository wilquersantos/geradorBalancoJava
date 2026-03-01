package com.emissor.dto;

public class ReportItemDTO {
    
    private final String codigoReferencia;
    private final String descricao;
    private final int quantidade;
    
    public ReportItemDTO(String codigoReferencia, String descricao, int quantidade) {
        this.codigoReferencia = codigoReferencia;
        this.descricao = descricao;
        this.quantidade = quantidade;
    }
    
    public String getCodigoReferencia() {
        return codigoReferencia;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public int getQuantidade() {
        return quantidade;
    }
}
