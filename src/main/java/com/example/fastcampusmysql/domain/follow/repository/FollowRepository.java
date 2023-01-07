package com.example.fastcampusmysql.domain.follow.repository;

import com.example.fastcampusmysql.domain.follow.entity.Follow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FollowRepository {

    private static String TABLE = "follow";
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final static RowMapper<Follow> ROW_MAPPER = (ResultSet rs, int rowNum) -> Follow.builder()
            .id(rs.getLong("id"))
            .fromMemberId(rs.getLong("fromMemberId"))
            .toMemberId(rs.getLong("toMemberId"))
            .createdAt(rs.getObject("createdAt", LocalDateTime.class))
            .build();

    public List<Follow> findAllByFromMemberId(Long fromMemberId) {
        var sql = String.format("SELECT * FROM %s WHERE fromMemberId =:fromMemberId", TABLE);
        var param = new MapSqlParameterSource().addValue("fromMemberId", fromMemberId);
        return jdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public List<Follow> findAllByToMemberId(Long toMemberId) {
        var sql = String.format("SELECT * FROM %s WHERE toMemberId =:toMemberId", TABLE);
        var param = new MapSqlParameterSource().addValue("toMemberId", toMemberId);
        return jdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public Follow save(Follow follow) {
        if (follow.getId() == null) return insert(follow);
        throw new UnsupportedOperationException("can not update");
    }

    private Follow insert(Follow follow) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");

        SqlParameterSource param = new BeanPropertySqlParameterSource(follow);
        var id =jdbcInsert.executeAndReturnKey(param).longValue();

        return Follow.builder()
                .id(id)
                .fromMemberId(follow.getFromMemberId())
                .toMemberId(follow.getToMemberId())
                .createdAt(follow.getCreatedAt())
                .build();
    }
}
