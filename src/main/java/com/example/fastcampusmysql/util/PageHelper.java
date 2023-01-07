package com.example.fastcampusmysql.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

import java.util.List;

@Slf4j
public class PageHelper {

    public static String orderBy(Sort sort) {
        log.info("sort = {}", sort);
        if (sort.isEmpty()) {
            return "id DESC";
        }

        List<Sort.Order> orders = sort.toList();
        List<String> orderBys = orders.stream().map(order -> order.getProperty() + " " + order.getDirection()).toList();

        log.info("join ={}", orderBys);

        return String.join(", ", orderBys);
    }
}
