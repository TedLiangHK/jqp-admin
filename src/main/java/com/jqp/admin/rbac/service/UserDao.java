package com.jqp.admin.rbac.service;

import com.jqp.admin.rbac.data.User;
import org.springframework.cache.annotation.Cacheable;

/**
 * com.jqp.admin.rbac.service
 *
 * @author Leo Liu
 * @created 2022/4/8 5:15 PM
 */
public interface UserDao {
    @Cacheable(value="user", key="#id", unless = "#result==null")
    public User get(Long id);
}
