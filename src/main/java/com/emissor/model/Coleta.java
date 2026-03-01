package com.emissor.model;

public class Coleta {
    private String nome;
    private boolean bloqueada;

    public Coleta() {
    }

    public Coleta(String nome, boolean bloqueada) {
        this.nome = nome;
        this.bloqueada = bloqueada;
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
}
