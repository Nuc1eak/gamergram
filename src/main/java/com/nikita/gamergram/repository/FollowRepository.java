package com.nikita.gamergram.repository;

import com.nikita.gamergram.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // The user's follower count.
    long countByFollowingId(Long userId);

    // How many users this user follows.
    long countByFollowerId(Long userId);

    // Users this user follows.
    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

    // The user's followers.
    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);
}
