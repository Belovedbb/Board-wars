package com.board.wars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableScheduling;

//TODO cache github opaque token
//TODO token and roles
//TODO global marker method invocaton disallow after being populated

@SpringBootApplication
@EnableScheduling
@RefreshScope
public class PublicGatewayApplication {
    public static void main(String... args){
        SpringApplication.run(PublicGatewayApplication.class, args);
    }

}
