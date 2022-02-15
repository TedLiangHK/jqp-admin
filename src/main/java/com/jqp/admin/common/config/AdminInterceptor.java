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
        //logger.info("拦截url:"+request.getRequestURI());
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
        //logger.info(userSession.getToken());
        return true;
    }

    protected boolean isAjax(HttpServletRequest request){
        String header = request.getHeader("X-Requested-With");
        String contentType = request.getContentType();
        if(header != null && header.equals("XMLHttpRequest") || "application/json".equals(contentType)){
            return true;
        }
        return false;
    }
}
