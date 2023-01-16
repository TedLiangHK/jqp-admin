package com.jqp.admin;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class},
    scanBasePackages = {
        "com.jqp.admin.*.**",
        "org.activiti.rest.diagram.services"
    }
)
//超时时间两小时
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 7200)
@EnableScheduling
public class JqpAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(JqpAdminApplication.class, args);
    }

}
