package com.jqp.admin.rbac.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.SecureUtil;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.rbac.data.User;
import com.jqp.admin.rbac.service.ConfigService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping("/admin/user")
public class UserController {
    @Resource
    private JdbcService jdbcService;
    @Resource
    private ConfigService configService;

    @Resource
    private FormService formService;

    @RequestMapping("/{formCode}/save")
    public Result save(@RequestBody User user, @PathVariable("formCode") String formCode){
        user = formService.getObj(user, formCode);
        if(user.getId() == null){
            user.setCreateTime(new Date());
            user.setSalt(UUID.fastUUID().toString());

            String defaultPassword = configService.getValue("defaultPassword");

            String password = SecureUtil.md5(defaultPassword + user.getSalt());
            user.setPassword(password);
            //user.setPassword();
        }
        user.setUpdateTime(new Date());
        jdbcService.saveOrUpdate(user);
        return Result.success(user.getId());
    }
}
