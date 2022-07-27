package com.jqp.admin.common.config;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.jqp.admin.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result exceptionHandler(HttpServletRequest request, Exception exception){
        log.error(exception.getMessage(),exception);
        return Result.error(ExceptionUtil.getRootCauseMessage(exception));
    }
}
