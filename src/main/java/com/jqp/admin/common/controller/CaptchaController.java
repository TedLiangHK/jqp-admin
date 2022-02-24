package com.jqp.admin.common.controller;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.GifCaptcha;
import cn.hutool.captcha.ShearCaptcha;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.common.constants.Constants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class CaptchaController {
    @Resource
    SessionContext sessionContext;
    @RequestMapping("/captcha.png")
    public void captcha(HttpServletRequest request, HttpServletResponse response){
//        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(100, 37, 4, 3);
//        AbstractCaptcha captcha = CaptchaUtil.createGifCaptcha(100, 37, 4);
//        AbstractCaptcha captcha = CaptchaUtil.createCircleCaptcha(100, 37, 4,10);
//        AbstractCaptcha captcha = CaptchaUtil.createLineCaptcha(100, 37, 4,10);
        AbstractCaptcha captcha = CaptchaUtil.createShearCaptcha(100, 37, 4,3);
        String code = captcha.getCode();
        request.getSession().setAttribute(Constants.CAPTCHA_CODE,code);
        request.getSession().setAttribute(Constants.CAPTCHA_TIMEOUT,System.currentTimeMillis()+5*60*1000);
        try {
            captcha.write(response.getOutputStream());
            response.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
