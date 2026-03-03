package com.sakata.boilerplate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync          // cho AuditService.logAsync()
@MapperScan(
    basePackages = {
        "com.sakata.boilerplate.mapper.primary",  // → MySQL SqlSessionFactory
        "com.sakata.boilerplate.mapper.audit"     // → PostgreSQL SqlSessionFactory
    }
)
public class BoilerplateApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoilerplateApplication.class, args);
	}

}
