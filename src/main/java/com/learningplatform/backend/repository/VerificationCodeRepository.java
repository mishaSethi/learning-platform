package com.learningplatform.backend.repository;

import com.learningplatform.backend.entity.VerificationCode;
import com.learningplatform.backend.entity.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, VerificationPurpose purpose);

    void deleteByEmailAndPurpose(String email, VerificationPurpose purpose);
}
