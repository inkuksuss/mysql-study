package com.example.fastcampusmysql.domain.post.service;


import com.example.fastcampusmysql.domain.post.entity.Timeline;
import com.example.fastcampusmysql.domain.post.repository.TimelineRepository;
import com.example.fastcampusmysql.util.CursorRequest;
import com.example.fastcampusmysql.util.PageCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelineReadService {

    private final TimelineRepository timelineRepository;

    public PageCursor<Timeline> getTimelines(Long memberId, CursorRequest cursorRequest) {
        var timelines = findAllBy(memberId, cursorRequest);
        var nextKey = timelines.stream()
                .mapToLong(Timeline::getId)
                .min().orElse(CursorRequest.NONE_KEY);

        return new PageCursor<>(cursorRequest.next(nextKey), timelines);
    }

    public List<Timeline> findAllBy(Long memberId, CursorRequest cursor) {
        if (cursor.hasKey()) {
            return timelineRepository.findAllByLessThenIdAndMemberIdAndOrderByIdDesc(cursor.key(), memberId, cursor.size());
        } else {
            return timelineRepository.findAllByMemberIdAndOrderByIdDesc(memberId, cursor.size());
        }
    }
}
