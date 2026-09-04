package com.adarsh.resumebuilder.controller;

import com.adarsh.resumebuilder.repository.ResumeRepository;
import com.adarsh.resumebuilder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Demonstrates role-based access control: only ROLE_ADMIN reaches these routes. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of(
                "totalUsers", userRepository.count(),
                "totalResumes", resumeRepository.count());
    }
}
