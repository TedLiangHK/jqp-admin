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

    @RequestMapping("/crud/{pageCode}")
    public String crudPage(Model model,@PathVariable("pageCode") String pageCode){
        model.addAttribute("js","/admin/page/js/"+pageCode+".js?_rt="+System.currentTimeMillis());
        return "page";
    }
    @RequestMapping("/oneToMany/{pageCode}/{childPageCode}")
    public String oneToManyPage(Model model,
                                @PathVariable("pageCode") String pageCode,
                                @PathVariable("childPageCode") String childPageCode){
        model.addAttribute("js","/admin/page/js/"+pageCode+"/"+childPageCode+".js?_rt="+System.currentTimeMillis());
        return "page";
    }

    @RequestMapping("/taskAudit/{taskId}")
    public String taskAudit(Model model,@PathVariable("taskId") String taskId){
        model.addAttribute("js","/admin/models/task/js/"+taskId+".js?_rt="+System.currentTimeMillis());
        return "page";
    }
    @RequestMapping("/auditRecord/{modelName}/{id}")
    public String auditRecord(Model model,@PathVariable("modelName") String modelName,@PathVariable("id") String id ){
        model.addAttribute("js","/admin/models/auditRecord/js/"+modelName+"/"+id+".js?_rt="+System.currentTimeMillis());
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
