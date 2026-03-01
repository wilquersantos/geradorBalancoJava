package com.emissor.dto;

public class ColetaResponseDTO {
    private String nome;
    private boolean bloqueada;
    private long totalItens;

    public ColetaResponseDTO() {
    }

    public ColetaResponseDTO(String nome, boolean bloqueada, long totalItens) {
        this.nome = nome;
        this.bloqueada = bloqueada;
        this.totalItens = totalItens;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isBloqueada() {
        return bloqueada;
    }

    public void setBloqueada(boolean bloqueada) {
        this.bloqueada = bloqueada;
    }

    public long getTotalItens() {
        return totalItens;
    }

    public void setTotalItens(long totalItens) {
        this.totalItens = totalItens;
    }
}
