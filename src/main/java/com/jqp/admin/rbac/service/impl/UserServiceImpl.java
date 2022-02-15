package com.jqp.admin.rbac.service.impl;

import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.rbac.constants.UserType;
import com.jqp.admin.rbac.data.Enterprise;
import com.jqp.admin.rbac.data.User;
import com.jqp.admin.rbac.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("userService")
public class UserServiceImpl implements UserService {
    @Resource
    private JdbcService jdbcService;
    @Override
    public List<Enterprise> getUserEnterpriseList(User user) {
        List<Enterprise> list = null;
        if(UserType.Admin.equals(user.getUserType())){
            list = jdbcService.find("select * from enterprise",Enterprise.class);
        }else{
            list = jdbcService.find("select * from enterprise where id in(" +
                    "select enterprise_id id from enterprise_manager " +
                    "where user_id = ? " +
                    "union all " +
                    "select enterprise_id from enterprise_user " +
                    "where user_id = ? ) ",Enterprise.class,user.getId(),user.getId());
        }
        return list;
    }
}
