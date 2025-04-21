package com.tisd.c4change;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories("com.tisd.c4change.Repository")
@EntityScan("com.tisd.c4change.Entity")
@ComponentScan("com.tisd.c4change")
public class  C4changeApplication {

	public static void main(String[] args) {
		SpringApplication.run(C4changeApplication.class, args);
	}
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
