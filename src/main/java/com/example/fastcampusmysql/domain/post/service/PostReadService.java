package com.example.fastcampusmysql.domain.post.service;

import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.repository.PostRepository;
import com.example.fastcampusmysql.util.CursorRequest;
import com.example.fastcampusmysql.util.PageCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostReadService {

    private final PostRepository postRepository;

    public List<DailyPostCount> getDailyPostCounts(DailyPostCountRequest request) {
        return postRepository.groupByCreatedDate(request);
    }

    public Page<Post> getPosts(Long memberId, Pageable pageable) {
        return postRepository.findAllByMemberId(memberId, pageable);
    }

    public PageCursor<Post> getPosts(Long memberId, CursorRequest cursor) {
        List<Post> posts = findAllBy(memberId, cursor);
        long nextKey = getNextKey(posts);
        return new PageCursor<>(cursor.next(nextKey), posts);
    }

    public PageCursor<Post> getPosts(List<Long> memberIds, CursorRequest cursor) {
        List<Post> posts = findAllBy(memberIds, cursor);
        long nextKey = getNextKey(posts);
        return new PageCursor<>(cursor.next(nextKey), posts);
    }

    private List<Post> findAllBy(Long memberId, CursorRequest cursor) {
        if (cursor.hasKey()) {
            return postRepository.findAllByLessThenIdAndMemberIdAndOrderByIdDesc(cursor.key(), memberId, cursor.size());
        } else {
            return postRepository.findAllByMemberIdAndOrderByIdDesc(memberId, cursor.size());
        }
    }

    public List<Post> findAllBy(List<Long> memberIds, CursorRequest cursor) {
        if (cursor.hasKey()) {
            return postRepository.findAllByLessThenIdAndInMemberIdAndOrderByIdDesc(cursor.key(), memberIds, cursor.size());
        } else {
            return postRepository.findAllByInMemberIdAndOrderByIdDesc(memberIds, cursor.size());
        }
    }

    public List<Post> getPosts(List<Long> ids) {
        return postRepository.findAllByInId(ids);
    }

    private long getNextKey(List<Post> posts) {
        return posts.stream()
                .mapToLong(Post::getId)
                .min()
                .orElse(CursorRequest.NONE_KEY);
    }
}
