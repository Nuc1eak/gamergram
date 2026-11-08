package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.UserSummaryResponse;
import com.nikita.gamergram.entity.Follow;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.exception.BadRequestException;
import com.nikita.gamergram.exception.UserNotFoundException;
import com.nikita.gamergram.repository.FollowRepository;
import com.nikita.gamergram.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowService followService;

    @Test
    void follow_throws_whenSelfFollow() {
        assertThatThrownBy(() -> followService.follow(1L, 1L))
                .isInstanceOf(BadRequestException.class);

        verify(followRepository, never()).save(any());
    }

    @Test
    void follow_isIdempotent_whenAlreadyFollowing() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

        followService.follow(1L, 2L);

        verify(followRepository, never()).save(any());
        verify(userRepository, never()).getReferenceById(any());
    }

    @Test
    void follow_createsFollow_whenValid() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);
        when(userRepository.existsById(2L)).thenReturn(true);
        when(userRepository.getReferenceById(1L)).thenReturn(User.builder().id(1L).build());
        when(userRepository.getReferenceById(2L)).thenReturn(User.builder().id(2L).build());

        followService.follow(1L, 2L);

        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void follow_throws_whenFollowingUserNotFound() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 404L)).thenReturn(false);
        when(userRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> followService.follow(1L, 404L))
                .isInstanceOf(UserNotFoundException.class);

        verify(followRepository, never()).save(any());
    }

    @Test
    void unfollow_callsDelete() {
        followService.unfollow(1L, 2L);

        verify(followRepository).deleteByFollowerIdAndFollowingId(1L, 2L);
    }

    @Test
    void getFollowers_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        User follower = User.builder().id(2L).username("nikita").avatarUrl("avatar.png").build();
        Follow follow = Follow.builder().id(1L).follower(follower).build();
        Page<Follow> page = new PageImpl<>(List.of(follow), pageable, 1);

        when(followRepository.findByFollowingId(1L, pageable)).thenReturn(page);

        Page<UserSummaryResponse> result = followService.getFollowers(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).username()).isEqualTo("nikita");
    }

    @Test
    void getFollowing_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        User following = User.builder().id(3L).username("admin").avatarUrl(null).build();
        Follow follow = Follow.builder().id(1L).following(following).build();
        Page<Follow> page = new PageImpl<>(List.of(follow), pageable, 1);

        when(followRepository.findByFollowerId(1L, pageable)).thenReturn(page);

        Page<UserSummaryResponse> result = followService.getFollowing(1L, pageable);

        assertThat(result.getContent().get(0).username()).isEqualTo("admin");
    }

    @Test
    void getFollowersCount_returnsCount() {
        when(followRepository.countByFollowingId(1L)).thenReturn(7L);

        assertThat(followService.getFollowersCount(1L)).isEqualTo(7L);
    }

    @Test
    void getFollowingCount_returnsCount() {
        when(followRepository.countByFollowerId(1L)).thenReturn(3L);

        assertThat(followService.getFollowingCount(1L)).isEqualTo(3L);
    }
}
