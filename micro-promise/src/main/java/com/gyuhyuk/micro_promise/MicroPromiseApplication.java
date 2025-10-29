package com.gyuhyuk.micro_promise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MicroPromiseApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroPromiseApplication.class, args);
	}

//	@Bean
//	@Profile("test") // "test" 프로파일에서만 활성화
//	public DataSource DataSource() {
//		DriverManagerDataSource dataSource = new DriverManagerDataSource();
//		dataSource.setDriverClassName("org.h2.Driver");
//		dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
//		dataSource.setUsername("sa");
//		dataSource.setPassword("");
//		return dataSource;
//	}
}
