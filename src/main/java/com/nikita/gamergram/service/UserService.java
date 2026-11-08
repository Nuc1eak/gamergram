package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.UpdateProfileRequest;
import com.nikita.gamergram.dto.UserProfileResponse;
import com.nikita.gamergram.dto.UserPublicProfileResponse;
import com.nikita.gamergram.dto.UserRegistrationRequest;
import com.nikita.gamergram.dto.UserSummaryResponse;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.exception.UserAlreadyExistsException;
import com.nikita.gamergram.exception.UserNotFoundException;
import com.nikita.gamergram.repository.FollowRepository;
import com.nikita.gamergram.repository.PostRepository;
import com.nikita.gamergram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;

    @Transactional
    public UserProfileResponse register(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException(
                    "Username is already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(
                    "Email is already registered: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                // Never stored in plain text.
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        User saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));
        return toProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.byUsername(username));
        return toProfileResponse(user);
    }

    // For other users: no email, unlike getById/getByUsername.
    @Transactional(readOnly = true)
    public UserPublicProfileResponse getPublicProfile(Long id, Long viewerId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));
        return toPublicProfile(user, viewerId);
    }

    @Transactional(readOnly = true)
    public UserPublicProfileResponse getPublicProfileByUsername(String username, Long viewerId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.byUsername(username));
        return toPublicProfile(user, viewerId);
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> search(String query, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCaseOrderByUsernameAsc(query, pageable)
                .map(this::toSummaryResponse);
    }

    // PATCH semantics: null keeps a field, blank clears it.
    @Transactional
    public UserProfileResponse updateProfile(Long id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl().isBlank() ? null : request.avatarUrl());
        }
        if (request.bio() != null) {
            user.setBio(request.bio().isBlank() ? null : request.bio());
        }
        return toProfileResponse(user);
    }

    private UserPublicProfileResponse toPublicProfile(User user, Long viewerId) {
        Long id = user.getId();
        return new UserPublicProfileResponse(
                id,
                user.getUsername(),
                user.getAvatarUrl(),
                user.getBio(),
                postRepository.countByAuthorId(id),
                followRepository.countByFollowingId(id),
                followRepository.countByFollowerId(id),
                followRepository.existsByFollowerIdAndFollowingId(viewerId, id)
        );
    }

    private UserSummaryResponse toSummaryResponse(User user) {
        return new UserSummaryResponse(user.getId(), user.getUsername(), user.getAvatarUrl());
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getCreatedAt()
        );
    }
}
