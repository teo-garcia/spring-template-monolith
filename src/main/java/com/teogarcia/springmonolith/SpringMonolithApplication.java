package com.teogarcia.springmonolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import com.teogarcia.springmonolith.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableAsync
public class SpringMonolithApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringMonolithApplication.class, args);
  }
}
