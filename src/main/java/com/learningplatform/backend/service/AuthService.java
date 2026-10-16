package com.learningplatform.backend.service;

import com.learningplatform.backend.dto.AuthResponse;
import com.learningplatform.backend.dto.LoginRequest;
import com.learningplatform.backend.dto.SignupRequest;
import com.learningplatform.backend.dto.UserResponse;
import com.learningplatform.backend.entity.User;
import com.learningplatform.backend.repository.UserRepository;
import com.learningplatform.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public UserResponse signup(SignupRequest request) {
		String email = request.email().trim().toLowerCase();

		if (userRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		}

		User user = new User(request.name(), email, request.phone());
		user.setPassword(passwordEncoder.encode(request.password()));

		User saved = userRepository.save(user);
		return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getPhone());
	}

	public AuthResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase();

		// Same message for unknown email and wrong password, so attackers can't
		// find out which emails are registered
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
		}

		return new AuthResponse(jwtService.generateToken(user.getEmail()));
	}

	public UserResponse getCurrentUser(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone());
}

}
