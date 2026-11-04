package com.example.secureapi.config;

import com.example.secureapi.post.Post;
import com.example.secureapi.post.PostRepository;
import com.example.secureapi.user.User;
import com.example.secureapi.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedData(
            UserRepository userRepository, PostRepository postRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            User user = userRepository.findByUsername("student").orElseGet(() ->
                    userRepository.save(new User("student", passwordEncoder.encode("student-password"))));
            if (postRepository.count() == 0) {
                postRepository.save(new Post("Welcome", "Protected sample data", user));
            }
        };
    }
}