package com.board.wars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class V1DelegatorApplication {

    public static void main(String... args){
        SpringApplication.run(V1DelegatorApplication.class, args);
    }

}
