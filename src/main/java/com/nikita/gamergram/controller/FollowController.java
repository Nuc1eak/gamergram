package com.nikita.gamergram.controller;

import com.nikita.gamergram.dto.UserSummaryResponse;
import com.nikita.gamergram.security.UserPrincipal;
import com.nikita.gamergram.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follow")
    public ResponseEntity<Void> follow(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId
    ) {
        followService.follow(principal.getId(), userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/follow")
    public ResponseEntity<Void> unfollow(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId
    ) {
        followService.unfollow(principal.getId(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followers")
    public Page<UserSummaryResponse> getFollowers(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return followService.getFollowers(userId, pageable);
    }

    @GetMapping("/following")
    public Page<UserSummaryResponse> getFollowing(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return followService.getFollowing(userId, pageable);
    }

    @GetMapping("/followers/count")
    public long getFollowersCount(@PathVariable Long userId) {
        return followService.getFollowersCount(userId);
    }

    @GetMapping("/following/count")
    public long getFollowingCount(@PathVariable Long userId) {
        return followService.getFollowingCount(userId);
    }
}
