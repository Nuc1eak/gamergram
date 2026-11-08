package com.nikita.gamergram.controller;

import com.nikita.gamergram.dto.CreatePostRequest;
import com.nikita.gamergram.dto.PostResponse;
import com.nikita.gamergram.security.UserPrincipal;
import com.nikita.gamergram.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePostRequest request
    ) {
        PostResponse response = postService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public PostResponse getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return postService.getById(id, principal.getId());
    }

    // Static segments take priority over "{id}", so these do not collide with getById.
    @GetMapping("/user/{userId}")
    public Page<PostResponse> getUserPosts(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return postService.getUserPosts(userId, principal.getId(), pageable);
    }

    @GetMapping("/game/{gameId}")
    public Page<PostResponse> getGamePosts(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long gameId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return postService.getGamePosts(gameId, principal.getId(), pageable);
    }

    @GetMapping("/explore")
    public Page<PostResponse> getExplore(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return postService.getExplore(principal.getId(), pageable);
    }

    @GetMapping("/feed")
    public Page<PostResponse> getFeed(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return postService.getFeed(principal.getId(), pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        postService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
