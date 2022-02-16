package com.jqp.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@SpringBootApplication
//超时时间两小时
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 7200)
public class JqpAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(JqpAdminApplication.class, args);
    }

}
