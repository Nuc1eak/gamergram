package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.UserSummaryResponse;
import com.nikita.gamergram.entity.Follow;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.exception.BadRequestException;
import com.nikita.gamergram.exception.UserNotFoundException;
import com.nikita.gamergram.repository.FollowRepository;
import com.nikita.gamergram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BadRequestException("Cannot follow yourself");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            return; // idempotent, like likes
        }

        if (!userRepository.existsById(followingId)) {
            throw UserNotFoundException.byId(followingId);
        }

        User followerRef = userRepository.getReferenceById(followerId);
        User followingRef = userRepository.getReferenceById(followingId);

        Follow follow = Follow.builder()
                .follower(followerRef)
                .following(followingRef)
                .build();
        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> getFollowers(Long userId, Pageable pageable) {
        return followRepository.findByFollowingId(userId, pageable)
                .map(f -> toSummary(f.getFollower()));
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> getFollowing(Long userId, Pageable pageable) {
        return followRepository.findByFollowerId(userId, pageable)
                .map(f -> toSummary(f.getFollowing()));
    }

    @Transactional(readOnly = true)
    public long getFollowersCount(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    private UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(user.getId(), user.getUsername(), user.getAvatarUrl());
    }
}
