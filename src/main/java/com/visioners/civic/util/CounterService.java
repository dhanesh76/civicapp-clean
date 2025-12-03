package com.visioners.civic.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CounterService {
    private final StringRedisTemplate redisTemplate;
    private static final String COMPLAINT_ID = "complaint_id";

    public long increment(){
        Long value = redisTemplate.opsForValue().increment(COMPLAINT_ID);
        return value != null ? value : 0L;
    }
}
