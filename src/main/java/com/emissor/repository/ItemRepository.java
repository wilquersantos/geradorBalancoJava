package com.emissor.repository;

import com.emissor.model.Item;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public ItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureSchemaCompatibility() {
        Integer columnCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM pragma_table_info('items') WHERE name = 'coleta'",
            Integer.class
        );
        if (columnCount == null || columnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE items ADD COLUMN coleta VARCHAR(255) NOT NULL DEFAULT 'GERAL'");
        }
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_items_coleta ON items(coleta)");
        jdbcTemplate.execute("UPDATE items SET coleta = 'GERAL' WHERE coleta IS NULL OR TRIM(coleta) = ''");
    }

    private static class ItemRowMapper implements RowMapper<Item> {
        @Override
        public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setColeta(rs.getString("coleta"));
            item.setCodigoReferencia(rs.getString("codigo_referencia"));
            item.setQuantidade(rs.getInt("quantidade"));
            item.setDescricao(rs.getString("descricao"));

            Timestamp timestamp = rs.getTimestamp("data_recebimento");
            if (timestamp != null) {
                item.setDataRecebimento(timestamp.toLocalDateTime());
            }
            return item;
        }
    }

    public Item save(Item item) {
        String sql = "INSERT INTO items (coleta, codigo_referencia, quantidade, descricao, data_recebimento) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, item.getColeta());
            ps.setString(2, item.getCodigoReferencia());
            ps.setInt(3, item.getQuantidade());
            ps.setString(4, item.getDescricao());
            ps.setTimestamp(5, Timestamp.valueOf(item.getDataRecebimento() != null
                ? item.getDataRecebimento() : LocalDateTime.now()));
            return ps;
        }, keyHolder);

        item.setId(keyHolder.getKey().longValue());
        return item;
    }

    public Optional<Item> findById(Long id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        List<Item> items = jdbcTemplate.query(sql, new ItemRowMapper(), id);
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }

    public Optional<Item> findFirstByCodigoReferenciaAndColeta(String codigoReferencia, String coleta) {
        String sql = "SELECT * FROM items WHERE codigo_referencia = ? AND coleta = ? ORDER BY id DESC LIMIT 1";
        List<Item> items = jdbcTemplate.query(sql, new ItemRowMapper(), codigoReferencia, coleta);
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }

    public List<Item> findAllByColeta(String coleta) {
        String sql = "SELECT * FROM items WHERE coleta = ? ORDER BY data_recebimento DESC";
        return jdbcTemplate.query(sql, new ItemRowMapper(), coleta);
    }

    public List<Item> findByCodigoOrDescricaoAndColeta(String searchTerm, String coleta) {
        String sql = "SELECT * FROM items WHERE coleta = ? AND (codigo_referencia LIKE ? OR descricao LIKE ?) ORDER BY data_recebimento DESC";
        String searchPattern = "%" + searchTerm + "%";
        return jdbcTemplate.query(sql, new ItemRowMapper(), coleta, searchPattern, searchPattern);
    }

    public List<String> findAllColetas() {
        String sql = "SELECT DISTINCT coleta FROM items WHERE coleta IS NOT NULL AND TRIM(coleta) <> '' ORDER BY coleta";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public void update(Item item) {
        String sql = "UPDATE items SET coleta = ?, codigo_referencia = ?, quantidade = ?, descricao = ? WHERE id = ?";
        jdbcTemplate.update(sql, item.getColeta(), item.getCodigoReferencia(), item.getQuantidade(), item.getDescricao(), item.getId());
    }

    public void updateReceivedData(Item item) {
        String sql = "UPDATE items SET quantidade = ?, descricao = ?, data_recebimento = ? WHERE id = ?";
        jdbcTemplate.update(
            sql,
            item.getQuantidade(),
            item.getDescricao(),
            Timestamp.valueOf(item.getDataRecebimento() != null ? item.getDataRecebimento() : LocalDateTime.now()),
            item.getId()
        );
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM items WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public long countByColeta(String coleta) {
        String sql = "SELECT COUNT(*) FROM items WHERE coleta = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, coleta);
    }

    public Map<String, Long> countByColetas() {
        String sql = "SELECT coleta, COUNT(*) AS total FROM items GROUP BY coleta";
        Map<String, Long> counts = new HashMap<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : rows) {
            String coleta = String.valueOf(row.get("coleta"));
            Object total = row.get("total");
            long value = total instanceof Number ? ((Number) total).longValue() : 0L;
            counts.put(coleta, value);
        }
        return counts;
    }
}
