package com.learningplatform.backend.service;

import com.learningplatform.backend.dto.AuthResponse;
import com.learningplatform.backend.dto.ForgotPasswordRequest;
import com.learningplatform.backend.dto.LoginRequest;
import com.learningplatform.backend.dto.ResetPasswordRequest;
import com.learningplatform.backend.dto.SignupRequest;
import com.learningplatform.backend.dto.UserResponse;
import com.learningplatform.backend.dto.VerifyTwoFactorRequest;
import com.learningplatform.backend.entity.User;
import com.learningplatform.backend.entity.VerificationPurpose;
import com.learningplatform.backend.repository.UserRepository;
import com.learningplatform.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

	private static final int RESET_CODE_MINUTES = 15;
	private static final int LOGIN_CODE_MINUTES = 5;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final VerificationCodeService verificationCodeService;
	private final EmailService emailService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
			VerificationCodeService verificationCodeService, EmailService emailService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.verificationCodeService = verificationCodeService;
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

	/** Step 1 of login: check the password, then email a 2FA code. No token yet. */
	public void login(LoginRequest request) {
		String email = request.email().trim().toLowerCase();

		// Same message for unknown email and wrong password, so attackers can't
		// find out which emails are registered
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
		}

		String code = verificationCodeService.createCode(email, VerificationPurpose.LOGIN_2FA, LOGIN_CODE_MINUTES);
		emailService.sendLoginCode(email, code);
	}

	/** Step 2 of login: a correct code is exchanged for the JWT token. */
	public AuthResponse verifyTwoFactor(VerifyTwoFactorRequest request) {
		String email = request.email().trim().toLowerCase();

		if (!verificationCodeService.verifyCode(email, VerificationPurpose.LOGIN_2FA, request.code())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired code");
		}

		return new AuthResponse(jwtService.generateToken(email));
	}

	public UserResponse getCurrentUser(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone());
	}

	public void forgotPassword(ForgotPasswordRequest request) {
		String email = request.email().trim().toLowerCase();

		// Unknown email: do nothing, but the controller still gives the same reply,
		// so nobody can use this form to find out who has an account
		if (!userRepository.existsByEmail(email)) {
			return;
		}

		String code = verificationCodeService.createCode(email, VerificationPurpose.PASSWORD_RESET, RESET_CODE_MINUTES);
		emailService.sendPasswordResetCode(email, code);
	}

	// No @Transactional here on purpose: verifyCode() saves "attempts + 1" in its own
	// transaction, so throwing the error below can't roll that counter back
	public void resetPassword(ResetPasswordRequest request) {
		String email = request.email().trim().toLowerCase();

		if (!verificationCodeService.verifyCode(email, VerificationPurpose.PASSWORD_RESET, request.code())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired code");
		}

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired code"));
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);
	}
}
