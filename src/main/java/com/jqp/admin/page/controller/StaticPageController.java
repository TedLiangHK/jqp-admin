package com.jqp.admin.page.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@Slf4j
public class StaticPageController {

    public static final String PREFIX = "/page/static";

    @RequestMapping(PREFIX +"/**")
    public String page(HttpServletRequest request, Model model){
        String uri = request.getRequestURI();
        uri = uri.substring(PREFIX.length());
        log.info(uri);
        model.addAttribute("js","/json"+uri+".js?_rt="+System.currentTimeMillis());
        return "page";
    }

    @RequestMapping("/crud/{pageId}")
    public String crudPage(Model model,@PathVariable("pageId") Long pageId){
        model.addAttribute("js","/admin/page/js/"+pageId+".js?_rt="+System.currentTimeMillis());
        return "page";
    }
    @RequestMapping("/oneToMany/{pageId}/{childPageId}")
    public String oneToManyPage(Model model,
                                @PathVariable("pageId") Long pageId,
                                @PathVariable("childPageId") Long childPageId){
        model.addAttribute("js","/admin/page/js/"+pageId+"/"+childPageId+".js?_rt="+System.currentTimeMillis());
        return "page";
    }

    @RequestMapping("/")
    public void index(HttpServletRequest request, HttpServletResponse response){
        try {
            response.sendRedirect("/admin/index.html?t="+System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
