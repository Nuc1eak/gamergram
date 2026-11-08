package com.nikita.gamergram.controller;

import com.nikita.gamergram.security.UserPrincipal;
import com.nikita.gamergram.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    // Idempotent, so always 204.
    @PostMapping
    public ResponseEntity<Void> like(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId
    ) {
        likeService.like(postId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unlike(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId
    ) {
        likeService.unlike(postId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public long count(@PathVariable Long postId) {
        return likeService.getLikeCount(postId);
    }

    @GetMapping("/me")
    public boolean likedByMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId
    ) {
        return likeService.isLikedByUser(postId, principal.getId());
    }
}
