package com.jobtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Job Tracker application.
 *
 * @SpringBootApplication is a convenience annotation that combines:
 *   - @Configuration    → marks this class as a source of bean definitions
 *   - @EnableAutoConfiguration → tells Spring Boot to auto-configure beans
 *                                based on the JARs on the classpath
 *   - @ComponentScan    → scans this package and sub-packages for @Component,
 *                         @Service, @Repository, @Controller, etc.
 *
 * When you run main(), Spring Boot:
 *   1. Creates an ApplicationContext (the IoC container)
 *   2. Auto-configures DataSource, JPA, Web server, etc.
 *   3. Starts the embedded Tomcat on the configured port
 */
@SpringBootApplication
@EnableScheduling
public class JobTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobTrackerApplication.class, args);
    }
}
