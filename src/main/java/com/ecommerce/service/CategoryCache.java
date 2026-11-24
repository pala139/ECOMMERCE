package com.ecommerce.service;

import com.ecommerce.dto.CategoryDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
public class CategoryCache {
    private static final Logger LOG = LoggerFactory.getLogger(CategoryCache.class);
    private static final String KEY = "ecommerce:categories:v1";
    private static final Duration TTL = Duration.ofMinutes(2);
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final boolean enabled;

    public CategoryCache(StringRedisTemplate redis, ObjectMapper mapper,
            @Value("${app.category-cache.enabled:false}") boolean enabled) {
        this.redis = redis;
        this.mapper = mapper;
        this.enabled = enabled;
    }

    public Optional<List<CategoryDTO>> get() {
        if (!enabled) return Optional.empty();
        try {
            String value = redis.opsForValue().get(KEY);
            return value == null ? Optional.empty() :
                Optional.of(mapper.readValue(value, new TypeReference<List<CategoryDTO>>() {}));
        } catch (DataAccessException | com.fasterxml.jackson.core.JsonProcessingException ex) {
            LOG.warn("Category cache read unavailable: {}", ex.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    public void put(List<CategoryDTO> categories) {
        if (!enabled) return;
        try {
            redis.opsForValue().set(KEY, mapper.writeValueAsString(categories), TTL);
        } catch (DataAccessException | com.fasterxml.jackson.core.JsonProcessingException ex) {
            LOG.warn("Category cache write unavailable: {}", ex.getClass().getSimpleName());
        }
    }

    public void evict() {
        if (!enabled) return;
        try { redis.delete(KEY); }
        catch (DataAccessException ex) {
            LOG.warn("Category cache invalidation unavailable: {}", ex.getClass().getSimpleName());
        }
    }
}
