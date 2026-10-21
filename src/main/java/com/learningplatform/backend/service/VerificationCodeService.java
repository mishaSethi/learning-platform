package com.learningplatform.backend.service;

import com.learningplatform.backend.entity.VerificationCode;
import com.learningplatform.backend.entity.VerificationPurpose;
import com.learningplatform.backend.repository.VerificationCodeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Creates and checks the 6-digit codes we email to users
 * (password reset, login 2FA). One place for all the security rules.
 */
@Service
public class VerificationCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ATTEMPTS = 5;

    private final VerificationCodeRepository codeRepository;
    private final PasswordEncoder passwordEncoder;

    public VerificationCodeService(VerificationCodeRepository codeRepository, PasswordEncoder passwordEncoder) {
        this.codeRepository = codeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Makes a new code (cancelling older ones), stores its hash, returns the plain code to email. */
    @Transactional
    public String createCode(String email, VerificationPurpose purpose, int validMinutes) {
        codeRepository.deleteByEmailAndPurpose(email, purpose);

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(validMinutes);
        codeRepository.save(new VerificationCode(email, purpose, passwordEncoder.encode(code), expiresAt));

        return code;
    }

    /** True only if the code is correct, not expired and not over the try limit. Correct codes are used up. */
    @Transactional
    public boolean verifyCode(String email, VerificationPurpose purpose, String code) {
        Optional<VerificationCode> found = codeRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose);
        if (found.isEmpty()) {
            return false;
        }
        VerificationCode saved = found.get();

        if (saved.getExpiresAt().isBefore(LocalDateTime.now()) || saved.getAttempts() >= MAX_ATTEMPTS) {
            codeRepository.deleteByEmailAndPurpose(email, purpose);
            return false;
        }

        if (!passwordEncoder.matches(code, saved.getCodeHash())) {
            saved.setAttempts(saved.getAttempts() + 1);
            codeRepository.save(saved);
            return false;
        }

        codeRepository.deleteByEmailAndPurpose(email, purpose); // one-time use
        return true;
    }
}
