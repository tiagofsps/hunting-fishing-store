package com.tiagofsps.hunting_fishing_store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HuntingFishingStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(HuntingFishingStoreApplication.class, args);
	}

}
