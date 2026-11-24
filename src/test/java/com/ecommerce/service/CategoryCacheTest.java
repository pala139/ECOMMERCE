package com.ecommerce.service;

import com.ecommerce.dto.CategoryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryCacheTest {
    @Test void cacheFailureFallsBackToDatabase() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.opsForValue()).thenThrow(new RedisConnectionFailureException("offline"));
        CategoryCache cache = new CategoryCache(redis,new ObjectMapper(),true);
        assertTrue(cache.get().isEmpty());
        assertDoesNotThrow(() -> cache.put(List.of(new CategoryDTO(1L,"Books",null))));
    }

    @Test void disabledCacheDoesNotContactRedis() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        CategoryCache cache = new CategoryCache(redis,new ObjectMapper(),false);
        assertTrue(cache.get().isEmpty());
        cache.put(List.of());
        cache.evict();
        verifyNoInteractions(redis);
    }
}
