package com.learningplatform.backend.repository;

import com.learningplatform.backend.entity.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    Optional<PasswordResetCode> findTopByEmailOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);
}
