package com.example.fastcampusmysql.domain.post.repository;

import com.example.fastcampusmysql.domain.post.entity.Timeline;
import lombok.RequiredArgsConstructor;
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
public class TimelineRepository {


    static final String TABLE = "Timeline";

    private static final RowMapper<Timeline> ROW_MAPPER = (ResultSet rs, int rowNum) ->
            Timeline.builder()
                    .id(rs.getLong("id"))
                    .memberId(rs.getLong("memberId"))
                    .postId(rs.getLong("postId"))
                    .createdAt(rs.getObject("createdAt", LocalDateTime.class))
                    .build();

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Timeline> findAllByMemberIdAndOrderByIdDesc(Long memberId, int size) {
        var sql = String.format("select * from %s where memberId = :memberId order by id desc limit :size", TABLE);
        var param = new MapSqlParameterSource().addValue("memberId", memberId).addValue("size", size);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public List<Timeline> findAllByLessThenIdAndMemberIdAndOrderByIdDesc(Long id, Long memberId, int size) {
        var sql = String.format("select * from %s where memberId = :memberId and id < :id order by id desc limit :size", TABLE);
        var param = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("id", id)
                .addValue("size", size);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public Timeline save(Timeline timeline) {
        if (timeline.getId() == null) {
            return insert(timeline);
        }
        throw new UnsupportedOperationException("not support");
    }

    private Timeline insert(Timeline timeline) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");

        SqlParameterSource param = new BeanPropertySqlParameterSource(timeline);
        var id = jdbcInsert.executeAndReturnKey(param).longValue();

        return Timeline.builder()
                .id(id)
                .memberId(timeline.getMemberId())
                .postId(timeline.getPostId())
                .createdAt(timeline.getCreatedAt())
                .build();

    }

    public void bulkInsert(List<Timeline> timelines) {
        var sql = String.format("INSERT INTO %s " +
                "(memberId, postId, createdAt) VALUES " +
                "(:memberId, :postId, :createdAt) ", TABLE);

        SqlParameterSource[] param = timelines
                .stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(sql, param);
    }
}
