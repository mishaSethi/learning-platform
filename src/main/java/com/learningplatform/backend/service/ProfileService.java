package com.learningplatform.backend.service;

import com.learningplatform.backend.dto.ProfileDetailsRequest;
import com.learningplatform.backend.dto.ProfileDetailsResponse;
import com.learningplatform.backend.dto.UserResponse;
import com.learningplatform.backend.entity.User;
import com.learningplatform.backend.entity.UserAvatar;
import com.learningplatform.backend.entity.UserProfile;
import com.learningplatform.backend.repository.UserAvatarRepository;
import com.learningplatform.backend.repository.UserProfileRepository;
import com.learningplatform.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

@Service
public class ProfileService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private static final LocalDate EARLIEST_BIRTH_DATE = LocalDate.of(1900, 1, 1);

    private final UserRepository userRepository;
    private final UserAvatarRepository avatarRepository;
    private final UserProfileRepository profileRepository;

    public ProfileService(UserRepository userRepository, UserAvatarRepository avatarRepository,
                          UserProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.avatarRepository = avatarRepository;
        this.profileRepository = profileRepository;
    }

    public UserResponse getProfile(String email) {
        return toResponse(findUser(email));
    }

    /** The full profile for the My Profile page. */
    public ProfileDetailsResponse getDetails(String email) {
        User user = findUser(email);
        UserProfile profile = profileRepository.findByUserEmail(email).orElse(new UserProfile(email));
        return toDetailsResponse(user, profile);
    }

    /**
     * Saves the extra details. Name and email are never changed here. The phone is
     * locked once set - it can only be added if the account has none yet.
     */
    @Transactional
    public ProfileDetailsResponse updateDetails(String email, ProfileDetailsRequest request) {
        User user = findUser(email);

        String newPhone = clean(request.phone());
        if (newPhone != null) {
            if (user.getPhone() == null || user.getPhone().isBlank()) {
                user.setPhone(newPhone);
                userRepository.save(user);
            } else if (!newPhone.equals(user.getPhone())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number can't be changed");
            }
        }

        if (request.dateOfBirth() != null && request.dateOfBirth().isBefore(EARLIEST_BIRTH_DATE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a real date of birth");
        }

        UserProfile profile = profileRepository.findByUserEmail(email).orElse(new UserProfile(email));
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setGender(clean(request.gender()));
        profile.setAddressLine(clean(request.addressLine()));
        profile.setCity(clean(request.city()));
        profile.setState(clean(request.state()));
        profile.setPostalCode(clean(request.postalCode()));
        profile.setCountry(clean(request.country()));
        profile.setExperienceLevel(clean(request.experienceLevel()));
        profile.setEmergencyContactName(clean(request.emergencyContactName()));
        profile.setEmergencyContactPhone(clean(request.emergencyContactPhone()));
        profile.setBio(clean(request.bio()));

        return toDetailsResponse(user, profileRepository.save(profile));
    }

    // Trims text; an empty box means "not filled in"
    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProfileDetailsResponse toDetailsResponse(User user, UserProfile p) {
        boolean phoneLocked = user.getPhone() != null && !user.getPhone().isBlank();
        return new ProfileDetailsResponse(user.getName(), user.getEmail(), user.getPhone(), phoneLocked,
                p.getDateOfBirth(), p.getGender(), p.getAddressLine(), p.getCity(), p.getState(),
                p.getPostalCode(), p.getCountry(), p.getExperienceLevel(),
                p.getEmergencyContactName(), p.getEmergencyContactPhone(), p.getBio());
    }

    public UserResponse uploadAvatar(String email, MultipartFile file) {
        User user = findUser(email);

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No photo was sent");
        }
        if (file.getSize() > UserAvatar.MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Photo is larger than 2 MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPEG or PNG photos are allowed");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
            // The file's name and type can be faked - check it really opens as an image
            if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file is not a valid image");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file is not a valid image");
        }

        // One photo per user: replace the old one if there is one
        UserAvatar avatar = avatarRepository.findByUserEmail(email).orElse(new UserAvatar(email));
        avatar.setImage(file.getContentType(), bytes);
        avatarRepository.save(avatar);

        return toResponse(user);
    }

    public UserAvatar getAvatar(String email) {
        return avatarRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No photo"));
    }

    @Transactional
    public UserResponse deleteAvatar(String email) {
        User user = findUser(email);
        avatarRepository.deleteByUserEmail(email);
        return toResponse(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                avatarRepository.existsByUserEmail(user.getEmail()));
    }
}
