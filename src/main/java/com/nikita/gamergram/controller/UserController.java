package com.nikita.gamergram.controller;

import com.nikita.gamergram.dto.UpdateProfileRequest;
import com.nikita.gamergram.dto.UserProfileResponse;
import com.nikita.gamergram.dto.UserPublicProfileResponse;
import com.nikita.gamergram.dto.UserSummaryResponse;
import com.nikita.gamergram.security.UserPrincipal;
import com.nikita.gamergram.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// All endpoints here require a valid Bearer token.
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getById(principal.getId());
    }

    // Public profile, never includes email.
    @GetMapping("/{id}")
    public UserPublicProfileResponse getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return userService.getPublicProfile(id, principal.getId());
    }

    @GetMapping("/by-username/{username}")
    public UserPublicProfileResponse getByUsername(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String username
    ) {
        return userService.getPublicProfileByUsername(username, principal.getId());
    }

    @GetMapping
    public Page<UserSummaryResponse> search(
            @RequestParam(defaultValue = "") String query,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return userService.search(query, pageable);
    }

    @PatchMapping("/me")
    public UserProfileResponse updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateProfile(principal.getId(), request);
    }
}
