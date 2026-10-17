package com.learningplatform.backend.controller;

import com.learningplatform.backend.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple liveness endpoint used to confirm the backend is up.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

	@GetMapping("/health")
	public HealthResponse health() {
		return new HealthResponse("Backend running");
	}
}
