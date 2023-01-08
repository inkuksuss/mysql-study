package com.example.fastcampusmysql.domain.post.repository;


import com.example.fastcampusmysql.util.PageHelper;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PostRepository {

    static final String TABLE = "Post";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final RowMapper<DailyPostCount> DAILY_POST_COUNT_MAPPER = (ResultSet rs, int rowNum) ->
        new DailyPostCount(
                rs.getLong("memberId"),
                rs.getObject("createdDate", LocalDate.class),
                rs.getLong("count")
        );

    private static final RowMapper<Post> ROW_MAPPER = (ResultSet rs, int rowNum) ->
            Post.builder()
                    .id(rs.getLong("id"))
                    .memberId(rs.getLong("memberId"))
                    .contents(rs.getString("contents"))
                    .createdDate(rs.getObject("createdDate", LocalDate.class))
                    .likeCount(rs.getLong("likeCount"))
                    .version(rs.getLong("version"))
                    .createdAt(rs.getObject("createdAt", LocalDateTime.class))
                    .build();


    public List<DailyPostCount> groupByCreatedDate(DailyPostCountRequest request) {
        var sql = String.format("select createdDate, memberId, count(id) as count from %s where memberId = :memberId and createdAt between :firstDate and :lastDate group by memberId, createdDate", TABLE);
         SqlParameterSource param = new BeanPropertySqlParameterSource(request);
         return namedParameterJdbcTemplate.query(sql, param, DAILY_POST_COUNT_MAPPER);
    }

    public Page<Post> findAllByMemberId(Long memberId, Pageable pageable) {
        var param = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("size", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        var sql = String.format("select * from %s where memberId = :memberId order by %s limit :size offset :offset", TABLE, PageHelper.orderBy(pageable.getSort()));
        var posts = namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
        return new PageImpl<>(posts, pageable, getCount(memberId));
    }

    public Optional<Post> findById(Long postId, Boolean requiredLock) {
        var sql = String.format("select * from %s where id = :postId", TABLE);
        if (requiredLock) {
            sql += " for update";
        }

        var param = new MapSqlParameterSource().addValue("postId", postId);

        var nullable = namedParameterJdbcTemplate.queryForObject(sql, param, ROW_MAPPER);
        return Optional.ofNullable(nullable);
    }

    private Long getCount(Long memberId) {
        var sql = String.format("select count(id) from %s where memberId = :memberId", TABLE);
        var param = new MapSqlParameterSource().addValue("memberId", memberId);
        return namedParameterJdbcTemplate.queryForObject(sql, param, Long.class);
    }

    public List<Post> findAllByMemberIdAndOrderByIdDesc(Long memberId, int size) {
        var sql = String.format("select * from %s where memberId = :memberId order by id desc limit :size", TABLE);
        var param = new MapSqlParameterSource().addValue("memberId", memberId).addValue("size", size);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public List<Post> findAllByInMemberIdAndOrderByIdDesc(List<Long> memberIds, int size) {
        if (memberIds.isEmpty()) {
            return List.of();
        }

        var sql = String.format("select * from %s where memberId in (:memberIds) order by createdDate desc limit :size", TABLE);
        var param = new MapSqlParameterSource().addValue("memberIds", memberIds).addValue("size", size);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public List<Post> findAllByLessThenIdAndMemberIdAndOrderByIdDesc(Long id, Long memberId, int size) {
        var sql = String.format("select * from %s where memberId = :memberId and id < :id order by id desc limit :size", TABLE);
        var param = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("id", id)
                .addValue("size", size);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public List<Post> findAllByLessThenIdAndInMemberIdAndOrderByIdDesc(Long id, List<Long> memberIds, int size) {
        if (memberIds.isEmpty()) {
            return List.of();
        }

        var sql = String.format("select * from %s where memberId in (:memberIds) and id < :id order by createdDate desc limit :size", TABLE);
        var param = new MapSqlParameterSource()
                .addValue("memberIds", memberIds)
                .addValue("id", id)
                .addValue("size", size);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public List<Post> findAllByInId(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        var sql = String.format("select * from %s where id in (:ids)", TABLE);
        var param = new MapSqlParameterSource()
                .addValue("ids", ids);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public Post save(Post post) {
        if (post.getId() == null) return insert(post);
        return update(post);
    }

    public void bulkInsert(List<Post> posts) {
        var sql = String.format("INSERT INTO %s (memberId, contents, createdDate, createdAt) VALUES (:memberId, :contents, :createdDate, :createdAt) ", TABLE);

        SqlParameterSource[] param = posts
                .stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(sql, param);
    }

    private Post insert(Post post) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");

        SqlParameterSource param = new BeanPropertySqlParameterSource(post);
        var id = jdbcInsert.executeAndReturnKey(param).longValue();

        return Post.builder()
                .id(id)
                .memberId(post.getMemberId())
                .contents(post.getContents())
                .createdAt(post.getCreatedAt())
                .likeCount(post.getLikeCount())
                .createdDate(post.getCreatedDate())
                .build();
    }

    private Post update(Post post) {
        var sql = String.format("update %s set " +
                "memberId = :memberId, contents = :contents, createdDate = :createdDate, likeCount = :likeCount, createdAt = :createdAt, version = :version + 1" +
                " WHERE id = :id and version = :version", TABLE);
        var param = new BeanPropertySqlParameterSource(post);
        var updatedCount = namedParameterJdbcTemplate.update(sql, param);
        if (updatedCount == 0) {
            throw new RuntimeException("fail");
        }

        return post;
    }
}
