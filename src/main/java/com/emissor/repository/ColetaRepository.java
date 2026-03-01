package com.emissor.repository;

import com.emissor.model.Coleta;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ColetaRepository {

    private static final String DEFAULT_COLETA = "GERAL";

    private final JdbcTemplate jdbcTemplate;

    public ColetaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initialize() {
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS coletas (" +
                "nome VARCHAR(255) PRIMARY KEY, " +
                "bloqueada INTEGER NOT NULL DEFAULT 0, " +
                "data_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)"
        );
        jdbcTemplate.update("INSERT OR IGNORE INTO coletas (nome, bloqueada) VALUES (?, 0)", DEFAULT_COLETA);
        syncFromItems();
    }

    public List<Coleta> findAll() {
        syncFromItems();
        return jdbcTemplate.query(
            "SELECT nome, bloqueada FROM coletas ORDER BY nome",
            (rs, rowNum) -> new Coleta(rs.getString("nome"), rs.getInt("bloqueada") == 1)
        );
    }

    public void ensureExists(String nome) {
        jdbcTemplate.update("INSERT OR IGNORE INTO coletas (nome, bloqueada) VALUES (?, 0)", nome);
    }

    public boolean isBloqueada(String nome) {
        Integer value = jdbcTemplate.queryForObject(
            "SELECT bloqueada FROM coletas WHERE nome = ?",
            Integer.class,
            nome
        );
        return value != null && value == 1;
    }

    public void setBloqueada(String nome, boolean bloqueada) {
        ensureExists(nome);
        jdbcTemplate.update("UPDATE coletas SET bloqueada = ? WHERE nome = ?", bloqueada ? 1 : 0, nome);
    }

    public void deleteByNome(String nome) {
        jdbcTemplate.update("DELETE FROM coletas WHERE nome = ?", nome);
    }

    public boolean exists(String nome) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM coletas WHERE nome = ?",
            Integer.class,
            nome
        );
        return count != null && count > 0;
    }

    private void syncFromItems() {
        try {
            jdbcTemplate.update(
                "INSERT OR IGNORE INTO coletas (nome, bloqueada) " +
                    "SELECT DISTINCT coleta, 0 FROM items WHERE coleta IS NOT NULL AND TRIM(coleta) <> ''"
            );
        } catch (Exception ignored) {
            // Items table may still be evolving on startup.
        }
    }
}
