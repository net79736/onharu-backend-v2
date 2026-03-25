package com.backend.onharu;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnharuApplication {

	public static void main(String[] args) {
        SpringApplication.run(OnharuApplication.class, args);
        
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        System.out.println("Time (Option Fixed - Seoul): " + now);
        LocalDateTime now2 = LocalDateTime.now();
        System.out.println("Time (Default): " + now2);
	}
}
