package com.jqp.admin.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class PageController {

    public static final String PREFIX = "/page/static";

    @RequestMapping(PREFIX +"/**")
    public String page(HttpServletRequest request, Model model){
        String uri = request.getRequestURI();
        uri = uri.substring(PREFIX.length());
        log.info(uri);
        model.addAttribute("js","/json"+uri+".js?_rt="+System.currentTimeMillis());
        return "page";
    }
}
