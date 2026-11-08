package com.nikita.gamergram.service;

import com.nikita.gamergram.entity.Like;
import com.nikita.gamergram.entity.Post;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.exception.PostNotFoundException;
import com.nikita.gamergram.repository.LikeRepository;
import com.nikita.gamergram.repository.PostRepository;
import com.nikita.gamergram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // Idempotent: a repeat like is not an error.
    @Transactional
    public void like(Long postId, Long userId) {
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return;
        }

        if (!postRepository.existsById(postId)) {
            throw PostNotFoundException.byId(postId);
        }

        Post postRef = postRepository.getReferenceById(postId);
        User userRef = userRepository.getReferenceById(userId);

        Like like = Like.builder()
                .post(postRef)
                .user(userRef)
                .build();
        likeRepository.save(like);
    }

    // Also idempotent.
    @Transactional
    public void unlike(Long postId, Long userId) {
        likeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long postId, Long userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }
}
