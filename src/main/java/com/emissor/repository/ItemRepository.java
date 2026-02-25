package com.emissor.repository;

import com.emissor.model.Item;
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
import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public ItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    private static class ItemRowMapper implements RowMapper<Item> {
        @Override
        public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
            Item item = new Item();
            item.setId(rs.getLong("id"));
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
        String sql = "INSERT INTO items (codigo_referencia, quantidade, descricao, data_recebimento) VALUES (?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, item.getCodigoReferencia());
            ps.setInt(2, item.getQuantidade());
            ps.setString(3, item.getDescricao());
            ps.setTimestamp(4, Timestamp.valueOf(item.getDataRecebimento() != null ? 
                item.getDataRecebimento() : LocalDateTime.now()));
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
    
    public List<Item> findAll() {
        String sql = "SELECT * FROM items ORDER BY data_recebimento DESC";
        return jdbcTemplate.query(sql, new ItemRowMapper());
    }
    
    public List<Item> findByCodigoOrDescricao(String searchTerm) {
        String sql = "SELECT * FROM items WHERE codigo_referencia LIKE ? OR descricao LIKE ? ORDER BY data_recebimento DESC";
        String searchPattern = "%" + searchTerm + "%";
        return jdbcTemplate.query(sql, new ItemRowMapper(), searchPattern, searchPattern);
    }
    
    public void update(Item item) {
        String sql = "UPDATE items SET codigo_referencia = ?, quantidade = ?, descricao = ? WHERE id = ?";
        jdbcTemplate.update(sql, item.getCodigoReferencia(), item.getQuantidade(), item.getDescricao(), item.getId());
    }
    
    public void deleteById(Long id) {
        String sql = "DELETE FROM items WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    
    public long count() {
        String sql = "SELECT COUNT(*) FROM items";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}
