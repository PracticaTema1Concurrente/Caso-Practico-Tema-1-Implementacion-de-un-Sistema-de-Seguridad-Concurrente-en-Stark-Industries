package com.starkindustries.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ScheduleConfig {

    @Bean
    public ThreadPoolTaskScheduler sensorScheduler() {
        ThreadPoolTaskScheduler s = new ThreadPoolTaskScheduler();
        s.setPoolSize(8);
        s.setThreadNamePrefix("sched-");
        s.initialize();
        return s;
    }
}
