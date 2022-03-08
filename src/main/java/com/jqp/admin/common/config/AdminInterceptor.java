package com.jqp.admin.common.config;

import cn.hutool.json.JSONUtil;
import com.jqp.admin.common.Result;
import com.jqp.admin.common.constants.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author hyz
 * @date 2021/3/1 14:14
 */
@Component
@Slf4j
public class AdminInterceptor implements HandlerInterceptor {
    @Resource
    SessionContext sessionContext;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("拦截url:"+request.getRequestURI());
        String uri = request.getRequestURI();
        UserSession userSession = sessionContext.getSession(request);
        if(userSession == null){
            if(isAjax(request)){
                response.setContentType("application/json");

                response.getWriter().println(JSONUtil.toJsonStr(new Result(ResultCode.NotLogin,"登录失效",null)));
            }else{
                response.sendRedirect("/admin/lyear_pages_login.html");
            }
            return false;
        }
        if(!SessionContext.hasUrlPermission(uri)){
            if(isAjax(request)){
                response.setContentType("application/json");
                response.getWriter().println(JSONUtil.toJsonStr(new Result(ResultCode.NoAuth,"无此权限",null)));
            }else{
                response.sendRedirect("/admin/lyear_pages_error.html?url="+uri);
            }
            return false;
        }
        //logger.info(userSession.getToken());
        return true;
    }

    protected boolean isAjax(HttpServletRequest request){
        String header = request.getHeader("X-Requested-With");
        String dest = request.getHeader("Sec-Fetch-Dest");
        String requestURI = request.getRequestURI();
        if(requestURI.contains(".") || "iframe".equals(dest)|| "document".equals(dest) || "requestURI".endsWith("/")){
            return false;
        }
//        String contentType = request.getContentType();
//        if(header != null && header.equals("XMLHttpRequest") || "application/json".equals(contentType)){
//            return true;
//        }
        return true;
    }
}
