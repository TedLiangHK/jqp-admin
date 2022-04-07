package com.jqp.admin.page.service;

import com.jqp.admin.page.data.Form;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

/**
 * com.jqp.admin.page.service
 *
 * @author Leo Liu
 * @created 2022/4/7 5:46 PM
 */
public interface FormDao {
    @Caching(evict = {
            @CacheEvict(value = "form", key = "#form.getId()"),
            @CacheEvict(value = "form",key = "#form.getCode()")
    })
    void save(Form form);
    @Cacheable(value = "form", key = "#id")
    Form get(Long id);
    @Cacheable(value = "form",key = "#code")
    Form get(String code);
}
