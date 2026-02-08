package com.example.authentification.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.authentification.dto.ApiResponse;
import com.example.authentification.service.ParametreService;

@RestController
@RequestMapping("/api/params")
public class ParametreController {

    private final ParametreService parametreService;

    public ParametreController(ParametreService parametreService) {
        this.parametreService = parametreService;
    }

    @GetMapping
    public ApiResponse<Map<String, Integer>> getParametres() {
        try {
            return new ApiResponse<Map<String, Integer>>(true, Map.of(
                    "maxFailedAttempts", parametreService.getIntValue("MAX_FAILED_ATTEMPTS"),
                    "sessionDuration", parametreService.getIntValue("SESSION_DURATION")), null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping
    public ApiResponse<String> updateMaxFailedAttempts(
            @RequestParam int maxAttemps, @RequestParam int sessionDuration) {
        try {
            parametreService.setParam("MAX_FAILED_ATTEMPTS", String.valueOf(maxAttemps), "INTEGER",
                    "Max failed attemps");
            parametreService.setParam("SESSION_DURATION", String.valueOf(sessionDuration), "INTEGER",
                    "Session duration");
            return new ApiResponse<String>(true, "Updated successfully", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
