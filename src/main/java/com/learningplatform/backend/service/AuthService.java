package com.learningplatform.backend.service;

import com.learningplatform.backend.dto.AuthResponse;
import com.learningplatform.backend.dto.ForgotPasswordRequest;
import com.learningplatform.backend.dto.LoginRequest;
import com.learningplatform.backend.dto.ResetPasswordRequest;
import com.learningplatform.backend.dto.SignupRequest;
import com.learningplatform.backend.dto.UserResponse;
import com.learningplatform.backend.entity.PasswordResetCode;
import com.learningplatform.backend.entity.User;
import com.learningplatform.backend.repository.PasswordResetCodeRepository;
import com.learningplatform.backend.repository.UserRepository;
import com.learningplatform.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final int CODE_EXPIRY_MINUTES = 15;
	private static final int MAX_CODE_ATTEMPTS = 5;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final PasswordResetCodeRepository resetCodeRepository;
	private final EmailService emailService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
			PasswordResetCodeRepository resetCodeRepository, EmailService emailService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.resetCodeRepository = resetCodeRepository;
		this.emailService = emailService;
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

	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {
		String email = request.email().trim().toLowerCase();

		// Unknown email: do nothing, but the controller still gives the same reply,
		// so nobody can use this form to find out who has an account
		if (!userRepository.existsByEmail(email)) {
			return;
		}

		resetCodeRepository.deleteByEmail(email); // old codes stop working

		String code = String.format("%06d", RANDOM.nextInt(1_000_000));
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);
		resetCodeRepository.save(new PasswordResetCode(email, passwordEncoder.encode(code), expiresAt));

		emailService.sendPasswordResetCode(email, code);
	}

	// noRollbackFor: keep the "attempts + 1" even though we then throw an error
	@Transactional(noRollbackFor = ResponseStatusException.class)
	public void resetPassword(ResetPasswordRequest request) {
		String email = request.email().trim().toLowerCase();
		ResponseStatusException invalidCode =
				new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired code");

		PasswordResetCode resetCode = resetCodeRepository.findTopByEmailOrderByCreatedAtDesc(email)
				.orElseThrow(() -> invalidCode);

		if (resetCode.getExpiresAt().isBefore(LocalDateTime.now())
				|| resetCode.getAttempts() >= MAX_CODE_ATTEMPTS) {
			resetCodeRepository.deleteByEmail(email);
			throw invalidCode;
		}

		if (!passwordEncoder.matches(request.code(), resetCode.getCodeHash())) {
			resetCode.setAttempts(resetCode.getAttempts() + 1);
			resetCodeRepository.save(resetCode);
			throw invalidCode;
		}

		User user = userRepository.findByEmail(email).orElseThrow(() -> invalidCode);
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);

		resetCodeRepository.deleteByEmail(email); // one-time use
	}
}
