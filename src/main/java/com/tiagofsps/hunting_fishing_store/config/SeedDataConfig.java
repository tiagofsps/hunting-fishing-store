package com.tiagofsps.hunting_fishing_store.config;

import com.tiagofsps.hunting_fishing_store.common.seeding.Seeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Comparator;
import java.util.List;

@Configuration
public class SeedDataConfig {

    @Bean
    @Profile("dev")
    CommandLineRunner seedDatabase(List<Seeder> seeders) {
        return args -> {
            System.out.println("Starting database seeding...");
            
            seeders.stream()
                    .sorted(Comparator.comparingInt(Seeder::getOrder))
                    .forEach(seeder -> {
                        System.out.println("Running seeder: " + seeder.getClass().getSimpleName());
                        seeder.seed();
                    });
            
            System.out.println("Database seeding completed!");
        };
    }
}
