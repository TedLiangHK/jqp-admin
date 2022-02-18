package com.jqp.admin.rbac.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jqp.admin.common.CrudData;
import com.jqp.admin.common.Result;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.common.config.UserSession;
import com.jqp.admin.common.constants.Constants;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.db.service.TransactionOption;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.rbac.constants.UserType;
import com.jqp.admin.rbac.data.Enterprise;
import com.jqp.admin.rbac.data.EnterpriseUser;
import com.jqp.admin.rbac.data.User;
import com.jqp.admin.rbac.service.ConfigService;
import com.jqp.admin.rbac.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
public class UserController {
    @Resource
    private JdbcService jdbcService;
    @Resource
    private ConfigService configService;

    @Resource
    private FormService formService;

    @Resource
    private SessionContext sessionContext;

    @Resource
    private UserService userService;

    @RequestMapping("/{formCode}/save")
    public Result save(@RequestBody User user, @PathVariable("formCode") String formCode){
        user = formService.getObj(user, formCode);
        Map<String,Object> params = new HashMap<>();
        params.put("id",user.getId());
        params.put("userCode",user.getUserCode());
        boolean repeat = jdbcService.isRepeat("select id from user where user_code = '$userCode' and id <> $id ", params);
        if(repeat){
            return Result.error("用户编号重复");
        }

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
    @RequestMapping("/getUserSession")
    public Result getUserSession(HttpServletRequest request){
        UserSession session = sessionContext.getSession(request);
        User user = jdbcService.getById(User.class, session.getUserId());
        Enterprise enterprise = jdbcService.getById(Enterprise.class, session.getEnterpriseId());
        Map<String,Object> data = new HashMap<>();
        data.put("name", StrUtil.format("{}({})",user.getName(),enterprise.getName()));
        data.put("avatar",user.getAvatar());
        data.put("menus",session.getCurrentUserMenu());

        return Result.success(data);
    }

    @PostMapping("/login")
    @ResponseBody
    public Result login(String username,String password,String captcha, HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession();
        String captchaCode = (String) session.getAttribute(Constants.CAPTCHA_CODE);
        Long captchaTimeout = (Long) session.getAttribute(Constants.CAPTCHA_TIMEOUT);
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return Result.error("用户名/密码不能为空");
        }
        if(StringUtils.isBlank(captcha)){
            return Result.error("验证码不能为空");
        }
        if(StringUtils.isBlank(captchaCode) || captchaTimeout == null || captchaTimeout < System.currentTimeMillis() || !captchaCode.equals(captcha)){
            return Result.error("验证码错误");
        }

        User user = jdbcService.findOne(User.class,new String[]{
                "userCode"
        },new Object[]{
                username
        });
        if(user == null){
            return Result.error("用户名/密码错误");
        }
        if(!user.getPassword().equals(SecureUtil.md5(password + user.getSalt()))){
            return Result.error("用户名/密码错误");
        }
        List<Enterprise> userEnterpriseList = userService.getUserEnterpriseList(user);
        if(userEnterpriseList.size() == 1){
            UserSession userSession = sessionContext.newSession(request, user,userEnterpriseList.get(0).getId());
            return Result.success(userSession);
        }
        if(userEnterpriseList.isEmpty()){
            return Result.error("当前用户没有加入任何企业");
        }
        session.setAttribute(Constants.USER_CHOOSE_ENTERPRISE,user.getId());
        return Result.success("choose","选择企业");
    }

    @RequestMapping("/login/getUserChooseEnterprise")
    public Result getUserChooseEnterprise(HttpServletRequest request){
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute(Constants.USER_CHOOSE_ENTERPRISE);
        if(userId == null){
            return Result.error("重新登录");
        }
        User user = jdbcService.getById(User.class, userId);
        List<Enterprise> userEnterpriseList = userService.getUserEnterpriseList(user);
        return Result.success(userEnterpriseList);
    }
    @RequestMapping("/login/userChooseEnterprise")
    public Result userChooseEnterprise(HttpServletRequest request,Long enterpriseId){
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute(Constants.USER_CHOOSE_ENTERPRISE);
        if(userId == null){
            return Result.error("重新登录");
        }
        if(enterpriseId == null){
            return Result.error("请选择企业");
        }
        User user = jdbcService.getById(User.class, userId);
        List<Enterprise> userEnterpriseList = userService.getUserEnterpriseList(user);
        for(Enterprise enterprise:userEnterpriseList){
            if(enterprise.getId().equals(enterpriseId)){
                sessionContext.newSession(request,user,enterpriseId);
                session.removeAttribute(Constants.USER_CHOOSE_ENTERPRISE);
                return Result.success();
            }
        }
        return Result.error("没有加入此企业");
    }

    @RequestMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response){
        sessionContext.deleteSession(request);
        try {
            response.sendRedirect("/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/saveEnterpriseUser")
    public Result saveEnterpriseUser(@RequestBody JSONObject jsonObject){

        Long id = jsonObject.getLong("id");
        User user = JSONUtil.toBean(jsonObject,User.class);
        EnterpriseUser enterpriseUser = JSONUtil.toBean(jsonObject,EnterpriseUser.class);
        if(id == null){
            user.setCreateTime(new Date());
            user.setSalt(UUID.fastUUID().toString());
            user.setUserType(UserType.Common);

            String defaultPassword = configService.getValue("defaultPassword");

            String password = SecureUtil.md5(defaultPassword + user.getSalt());
            user.setPassword(password);

            enterpriseUser.setEnterpriseId(SessionContext.getSession().getEnterpriseId());
        }else{
            enterpriseUser = jdbcService.getById(EnterpriseUser.class,id);
            User dbUser = jdbcService.getById(User.class,enterpriseUser.getUserId());

            enterpriseUser.setDeptId(jsonObject.getLong("deptId"));

            dbUser.setUserCode(user.getUserCode());
            dbUser.setName(user.getName());
            dbUser.setMobile(user.getMobile());
            dbUser.setUserStatus(user.getUserStatus());
            dbUser.setOtherFiles(user.getOtherFiles());
            dbUser.setAvatar(user.getAvatar());

            user = dbUser;
        }
        User finalUser = user;
        finalUser.setUpdateTime(new Date());

        Map<String,Object> params = new HashMap<>();
        params.put("id",finalUser.getId());
        params.put("userCode",finalUser.getUserCode());
        boolean repeat = jdbcService.isRepeat("select id from user where user_code = '$userCode' and id <> $id ", params);
        if(repeat){
            return Result.error("用户编号重复");
        }

        EnterpriseUser finalEnterpriseUser = enterpriseUser;
        jdbcService.transactionOption(() -> {
            jdbcService.saveOrUpdate(finalUser);
            if(finalEnterpriseUser.getUserId() == null){
                finalEnterpriseUser.setUserId(finalUser.getId());
            }
            jdbcService.saveOrUpdate(finalEnterpriseUser);
        });
        return Result.success();
    }
}
