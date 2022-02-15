package com.jqp.admin.common.config;

import com.jqp.admin.common.constants.Constants;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.rbac.data.User;
import com.jqp.admin.util.SpringContextUtil;
import com.jqp.admin.util.TokenUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author hyz
 * @date 2021/3/3 13:49
 */
@Component
@Log4j2
public class SessionContext {

    @Value("${server.servlet.session.timeout}")
    private int SessionTimeOut;

    @Resource
    private JdbcService jdbcService;

    static final String SPLIT = "$_$";

    public UserSession getSession(HttpServletRequest request){
        UserSession userSession = (UserSession) request.getSession().getAttribute(Constants.USER_SESSION);
        if(userSession != null){

            User user = jdbcService.getById(User.class, userSession.getUserId());
            if(!TokenUtil.verify(user.getSalt(),userSession.getToken(),user.getPassword())){
                //log.info("token失效,超时或者修改密码");
                return null;
            }
            return userSession;
        }
        String token = request.getHeader("token");
        if(StringUtils.isBlank(token)){
            token = request.getParameter("token");
        }
        if(StringUtils.isNotBlank(token)){
            String key = TokenUtil.getKey(token);
            String[] arr = key.split(SPLIT);
            String userCode = arr[0];
            Long enterpriseId = Long.parseLong(arr[1]);

            User user = jdbcService.findOne(User.class, "userCode", userCode);
            if(user != null){
                boolean verify = TokenUtil.verify(user.getSalt(),token, user.getPassword());
                if(verify){
                    userSession = newSession(request,user,enterpriseId);
                    return userSession;
                }
            }
        }
        return null;
    }



    public UserSession newSession(HttpServletRequest request, User user,Long enterpriseId){
        UserSession userSession = new UserSession();
        userSession.setUserId(user.getId());
        String token = TokenUtil.getToken(user.getSalt(),user.getMobile()+SPLIT+enterpriseId, user.getPassword(), SessionTimeOut * 1000);
        userSession.setToken(token);
        userSession.setUserType(user.getUserType());
        userSession.setEnterpriseId(enterpriseId);

//        List<String> menuCodes = baseService.find(" select distinct m.menuCode from " +
//                "UserPosition up," +
//                "PositionPermission pp," +
//                "PermissionMenu pm, " +
//                "Menu m " +
//                "where up.userId = ?0 " +
//                "and up.positionId = pp.positionId " +
//                "and pp.permissionId = pm.permissionId " +
//                "and pm.menuId = m.id " +
//                "and m.isButton = ?1 ", ObjUtil.newList(
//                user.getId(),
//                Whether.Yes
//        ));
//        userSession.setMenuCodes(menuCodes);
        request.getSession().setAttribute(Constants.USER_SESSION,userSession);
        return userSession;
    }

    public static UserSession getSession(){
        SessionContext sessionContext = SpringContextUtil.getBean(SessionContext.class);
        HttpServletRequest request = SpringContextUtil.getRequest();
        return sessionContext.getSession(request);
    }

    public void deleteSession(HttpServletRequest request){
        request.getSession().removeAttribute(Constants.USER_SESSION);
    }
}
