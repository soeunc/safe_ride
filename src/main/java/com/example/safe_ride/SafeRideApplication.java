package com.example.safe_ride;

import com.example.safe_ride.locationInfo.service.DbLoader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SafeRideApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafeRideApplication.class, args);
	}
	// CommandLineRunner 빈을 등록하여 애플리케이션 시작 시 테이블 생성 로직 실행
	@Bean
	CommandLineRunner createTable(DbLoader dbLoader) {
		return args -> {
			dbLoader.createTableIfNotExist();
		};
	}
}
