package com.jqp.admin.page.service;

import com.jqp.admin.page.data.Page;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

public interface PageDao {
    @Caching(evict = {@CacheEvict(value = "page", key = "#page.getCode()"), @CacheEvict(value = "page", key = "#page.getId()"),})
    void save(Page page);

    @Cacheable(value = "page", key = "#id", unless = "#result == null")
    Page get(Long id);

    @Cacheable(value = "page", key = "#pageCode", unless = "#result == null")
    Page get(String pageCode);

    @CacheEvict(value = "page", key = "#code")
    void delOldPageCache(String code);
}
