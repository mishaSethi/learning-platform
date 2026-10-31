package com.learningplatform.backend.repository;

import com.learningplatform.backend.entity.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAvatarRepository extends JpaRepository<UserAvatar, Long> {

    Optional<UserAvatar> findByUserEmail(String userEmail);

    boolean existsByUserEmail(String userEmail);

    void deleteByUserEmail(String userEmail);
}
