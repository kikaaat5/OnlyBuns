package com.example.OnlyBuns;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OnlyBunsApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlyBunsApplication.class, args);
	}    //initial

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
