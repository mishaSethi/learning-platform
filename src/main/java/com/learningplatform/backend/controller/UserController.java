package com.learningplatform.backend.controller;

import com.learningplatform.backend.dto.ProfileDetailsRequest;
import com.learningplatform.backend.dto.ProfileDetailsResponse;
import com.learningplatform.backend.dto.UserResponse;
import com.learningplatform.backend.entity.UserAvatar;
import com.learningplatform.backend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me")
public class UserController {

    private final ProfileService profileService;

    public UserController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public UserResponse me(Authentication authentication) {
        return profileService.getProfile(authentication.getName());
    }

    @GetMapping("/details")
    public ProfileDetailsResponse details(Authentication authentication) {
        return profileService.getDetails(authentication.getName());
    }

    // Name and email are not part of this request, so they can never be changed
    @PutMapping("/details")
    public ProfileDetailsResponse updateDetails(@Valid @RequestBody ProfileDetailsRequest request,
                                                Authentication authentication) {
        return profileService.updateDetails(authentication.getName(), request);
    }

    @PutMapping("/avatar")
    public UserResponse uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) {
        return profileService.uploadAvatar(authentication.getName(), file);
    }

    @GetMapping("/avatar")
    public ResponseEntity<byte[]> avatar(Authentication authentication) {
        UserAvatar avatar = profileService.getAvatar(authentication.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.getContentType()))
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(avatar.getData());
    }

    @DeleteMapping("/avatar")
    public UserResponse deleteAvatar(Authentication authentication) {
        return profileService.deleteAvatar(authentication.getName());
    }
}
