package com.jqp.admin.rbac.service.impl;

import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.rbac.data.User;
import com.jqp.admin.rbac.service.UserDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * com.jqp.admin.rbac.service.impl
 *
 * @author Leo Liu
 * @created 2022/4/8 5:16 PM
 */
@Service
public class UserDaoImpl implements UserDao {
    @Resource
    JdbcService jdbcService;

    @Override
    public User get(Long id) {
        return jdbcService.getById(User.class, id);
    }
}
