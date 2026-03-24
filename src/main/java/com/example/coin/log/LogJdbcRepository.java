// src/main/java/com/example/coin/log/LogJdbcRepository.java

package com.example.coin.log;

import com.example.coin.log.dto.LogCursorDto;
import com.example.coin.log.dto.UnifiedLogItemDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 통합 로그(UNION ALL) JDBC 조회 레포
 * - JPQL은 UNION을 지원하지 않으므로 JDBC/native SQL로 처리한다.
 * - MySQL에서 안전하게 동작하도록 (a,b,c) 튜플 비교 대신 OR 비교식을 사용한다.
 */
@Repository
public class LogJdbcRepository
{
    private final JdbcTemplate jdbcTemplate;

    public LogJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UnifiedLogItemDto> fetchNextPage(long userId,
                                                 LocalDate date,
                                                 LocalDateTime start,
                                                 LocalDateTime end,
                                                 int size,
                                                 LogCursorDto cursor)
    {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append(buildBaseUnionSql());
        // base params
        params.add(userId);
        params.add(date);     // trades.trade_date = ?
        params.add(userId);
        params.add(Timestamp.valueOf(start));
        params.add(Timestamp.valueOf(end));

        if (cursor != null && cursor.occurredAt != null) {
            sql.append(" where ( ")
                    .append(" logs.occurred_at > ? ")
                    .append(" or (logs.occurred_at = ? and logs.kind_rank > ?) ")
                    .append(" or (logs.occurred_at = ? and logs.kind_rank = ? and logs.id > ?) ")
                    .append(" ) ");

            Timestamp ts = Timestamp.valueOf(cursor.occurredAt);
            params.add(ts);
            params.add(ts);
            params.add(cursor.kindRank);
            params.add(ts);
            params.add(cursor.kindRank);
            params.add(cursor.id);
        }

        sql.append(" order by logs.occurred_at asc, logs.kind_rank asc, logs.id asc ");
        sql.append(" limit ").append(size);

        return jdbcTemplate.query(sql.toString(), rowMapper(), params.toArray());
    }

    public List<UnifiedLogItemDto> fetchPrevPage(long userId,
                                                 LocalDate date,
                                                 LocalDateTime start,
                                                 LocalDateTime end,
                                                 int size,
                                                 LogCursorDto cursor)
    {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append(buildBaseUnionSql());
        // base params
        params.add(userId);
        params.add(date);
        params.add(userId);
        params.add(Timestamp.valueOf(start));
        params.add(Timestamp.valueOf(end));

        if (cursor != null && cursor.occurredAt != null) {
            sql.append(" where ( ")
                    .append(" logs.occurred_at < ? ")
                    .append(" or (logs.occurred_at = ? and logs.kind_rank < ?) ")
                    .append(" or (logs.occurred_at = ? and logs.kind_rank = ? and logs.id < ?) ")
                    .append(" ) ");

            Timestamp ts = Timestamp.valueOf(cursor.occurredAt);
            params.add(ts);
            params.add(ts);
            params.add(cursor.kindRank);
            params.add(ts);
            params.add(cursor.kindRank);
            params.add(cursor.id);
        }

        sql.append(" order by logs.occurred_at desc, logs.kind_rank desc, logs.id desc ");
        sql.append(" limit ").append(size);

        return jdbcTemplate.query(sql.toString(), rowMapper(), params.toArray());
    }

    public boolean existsBefore(long userId,
                                LocalDate date,
                                LocalDateTime start,
                                LocalDateTime end,
                                LogCursorDto boundary)
    {
        if (boundary == null || boundary.occurredAt == null) {
            return false;
        }

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("select 1 from (");
        sql.append(buildUnionBodySql());
        sql.append(") logs ");
        sql.append(" where ( ")
                .append(" logs.occurred_at < ? ")
                .append(" or (logs.occurred_at = ? and logs.kind_rank < ?) ")
                .append(" or (logs.occurred_at = ? and logs.kind_rank = ? and logs.id < ?) ")
                .append(" ) limit 1 ");

        // base params for union body
        params.add(userId);
        params.add(date);
        params.add(userId);
        params.add(Timestamp.valueOf(start));
        params.add(Timestamp.valueOf(end));

        Timestamp ts = Timestamp.valueOf(boundary.occurredAt);
        params.add(ts);
        params.add(ts);
        params.add(boundary.kindRank);
        params.add(ts);
        params.add(boundary.kindRank);
        params.add(boundary.id);

        List<Integer> rows = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> 1, params.toArray());
        return !rows.isEmpty();
    }

    public boolean existsAfter(long userId,
                               LocalDate date,
                               LocalDateTime start,
                               LocalDateTime end,
                               LogCursorDto boundary)
    {
        if (boundary == null || boundary.occurredAt == null) {
            return false;
        }

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("select 1 from (");
        sql.append(buildUnionBodySql());
        sql.append(") logs ");
        sql.append(" where ( ")
                .append(" logs.occurred_at > ? ")
                .append(" or (logs.occurred_at = ? and logs.kind_rank > ?) ")
                .append(" or (logs.occurred_at = ? and logs.kind_rank = ? and logs.id > ?) ")
                .append(" ) limit 1 ");

        // base params for union body
        params.add(userId);
        params.add(date);
        params.add(userId);
        params.add(Timestamp.valueOf(start));
        params.add(Timestamp.valueOf(end));

        Timestamp ts = Timestamp.valueOf(boundary.occurredAt);
        params.add(ts);
        params.add(ts);
        params.add(boundary.kindRank);
        params.add(ts);
        params.add(boundary.kindRank);
        params.add(boundary.id);

        List<Integer> rows = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> 1, params.toArray());
        return !rows.isEmpty();
    }

    private String buildBaseUnionSql()
    {
        // "from ( ... ) logs" 형태까지 포함하고, 밖에서 where/order/limit을 붙인다.
        return "select " +
                "logs.kind as kind, " +
                "logs.side as side, " +
                "logs.amount_krw as amount_krw, " +
                "logs.volume as volume, " +
                "logs.pnl_krw as pnl_krw, " +
                "logs.occurred_at as occurred_at, " +
                "logs.memo as memo, " +
                "logs.id as id, " +
                "logs.kind_rank as kind_rank " +
                "from (" + buildUnionBodySql() + ") logs ";
    }

    private String buildUnionBodySql()
    {
        // trades는 trade_date로 필터링, capital_flows는 occurred_at between으로 필터링
        return
                "select " +
                        "  'TRADE' as kind, " +
                        "  case when t.side = 'BUY' then 'BUY' else 'SELL' end as side, " +
                        "  t.amount_krw as amount_krw, " +
                        "  t.volume as volume, " +
                        "  t.pnl_krw as pnl_krw, " +
                        "  t.trade_time as occurred_at, " +
                        "  null as memo, " +
                        "  t.id as id, " +
                        "  1 as kind_rank " +
                        "from trades t " +
                        "where t.user_id = ? and t.trade_date = ? " +
                        "union all " +
                        "select " +
                        "  'CAPITAL' as kind, " +
                        "  case when cf.flow_type = 'DEPOSIT' then 'DEPOSIT' else 'WITHDRAWAL' end as side, " +
                        "  cf.amount_krw as amount_krw, " +
                        "  null as volume, " +
                        "  null as pnl_krw, " +
                        "  cf.occurred_at as occurred_at, " +
                        "  cf.memo as memo, " +
                        "  cf.id as id, " +
                        "  2 as kind_rank " +
                        "from capital_flows cf " +
                        "where cf.user_id = ? and cf.occurred_at between ? and ? ";
    }

    private RowMapper<UnifiedLogItemDto> rowMapper()
    {
        return (rs, rowNum) -> {
            String kind = rs.getString("kind");
            String side = rs.getString("side");

            java.math.BigDecimal amount = rs.getBigDecimal("amount_krw");
            java.math.BigDecimal volume = rs.getBigDecimal("volume");
            java.math.BigDecimal pnl = rs.getBigDecimal("pnl_krw");

            Timestamp occurredAtTs = rs.getTimestamp("occurred_at");
            LocalDateTime occurredAt = (occurredAtTs != null ? occurredAtTs.toLocalDateTime() : null);

            String memo = rs.getString("memo");
            long id = rs.getLong("id");
            int kindRank = rs.getInt("kind_rank");

            return new UnifiedLogItemDto(
                    kind,
                    side,
                    amount,
                    volume,
                    pnl,
                    occurredAt,
                    memo,
                    id,
                    kindRank
            );
        };
    }
}
