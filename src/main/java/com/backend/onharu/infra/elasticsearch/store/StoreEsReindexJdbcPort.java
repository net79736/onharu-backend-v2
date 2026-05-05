package com.backend.onharu.infra.elasticsearch.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * MySQL `stores`를 페이징(아이디 커서)으로 읽어오는 JDBC 포트.
 *
 * <p>대량 데이터(수십만~백만 건)에서 안정적인 스트리밍을 위해</p>
 * <ul>
 *   <li>`id > :lastId` + `ORDER BY id` + `LIMIT` 패턴으로 반복 조회</li>
 *   <li>JPA 엔티티 로딩/연관 로딩을 피함</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "onharu.elasticsearch.enabled", havingValue = "true")
public class StoreEsReindexJdbcPort {

    private final JdbcTemplate jdbcTemplate;

    public StoreEsReindexJdbcPort(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<StoreRow> loadStoreRowsAfterId(long lastId, int limit) {
        String sql = """
                SELECT
                    s.id,
                    s.owner_id,
                    s.category_id,
                    s.name,
                    s.address,
                    s.phone,
                    s.introduction,
                    s.intro,
                    s.is_open,
                    s.is_sharing,
                    s.lat,
                    s.lng,
                    s.created_at,
                    s.created_by
                FROM stores s
                WHERE s.id > ?
                ORDER BY s.id ASC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, STORE_ROW_MAPPER, lastId, limit);
    }

    private static final RowMapper<StoreRow> STORE_ROW_MAPPER = new RowMapper<>() {
        @Override
        public StoreRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            Long ownerId = rs.getLong("owner_id");
            Long categoryId = rs.getLong("category_id");
            String name = rs.getString("name");
            String address = rs.getString("address");
            String phone = rs.getString("phone");
            String introduction = rs.getString("introduction");
            String intro = rs.getString("intro");
            boolean isOpen = rs.getBoolean("is_open");
            boolean isSharing = rs.getBoolean("is_sharing");
            String lat = rs.getString("lat");
            String lng = rs.getString("lng");
            LocalDateTime createdAt = rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime()
                    : null;
            String createdBy = rs.getString("created_by");

            return new StoreRow(
                    id,
                    ownerId,
                    categoryId,
                    name,
                    address,
                    phone,
                    introduction,
                    intro,
                    isOpen,
                    isSharing,
                    lat,
                    lng,
                    createdAt,
                    createdBy,
                    Collections.emptyList()
            );
        }
    };

    public record StoreRow(
            long id,
            long ownerId,
            long categoryId,
            String name,
            String address,
            String phone,
            String introduction,
            String intro,
            boolean isOpen,
            boolean isSharing,
            String lat,
            String lng,
            LocalDateTime createdAt,
            String createdBy,
            List<String> tags
    ) {
    }
}

