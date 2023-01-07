package com.example.fastcampusmysql.domain.post;


import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.repository.PostRepository;
import com.example.fastcampusmysql.domain.util.PostFixtureFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
public class PostBulkInsertTest {

    @Autowired
    private PostRepository postRepository;


    @Test
    public void bulkInsert() {
        var easyRandom = PostFixtureFactory.get(4L,
                LocalDate.of(1970,1,1),
                LocalDate.of(2022,2,1));

        var stopWatch = new StopWatch();
        stopWatch.start();

        List<Post> posts = IntStream.range(0, 10000 * 100)
                .mapToObj(i -> easyRandom.nextObject(Post.class))
                .toList();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeSeconds());


        var queryStopWatch = new StopWatch();
        queryStopWatch.start();
        postRepository.bulkInsert(posts);
        queryStopWatch.stop();;
        System.out.println(queryStopWatch.getTotalTimeSeconds());
    }
}
