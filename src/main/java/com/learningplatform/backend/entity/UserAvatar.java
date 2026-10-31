package com.learningplatform.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A user's profile photo, kept in its own table so that loading a user
 * (at every login) never drags the image bytes along.
 */
@Entity
@Table(name = "user_avatars")
public class UserAvatar {

    // Biggest photo we accept, in bytes (2 MB). The browser shrinks photos to
    // about 20-40 KB before uploading, so real photos are far below this.
    public static final int MAX_BYTES = 2 * 1024 * 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userEmail;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false, length = MAX_BYTES)
    private byte[] data;

    private LocalDateTime updatedAt;

    public UserAvatar() {
    }

    public UserAvatar(String userEmail) {
        this.userEmail = userEmail;
    }

    @PrePersist
    @PreUpdate
    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public String getContentType() { return contentType; }
    public byte[] getData() { return data; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setImage(String contentType, byte[] data) {
        this.contentType = contentType;
        this.data = data;
    }
}
