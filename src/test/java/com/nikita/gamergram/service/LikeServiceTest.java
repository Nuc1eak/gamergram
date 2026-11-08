package com.nikita.gamergram.service;

import com.nikita.gamergram.entity.Post;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.exception.PostNotFoundException;
import com.nikita.gamergram.repository.LikeRepository;
import com.nikita.gamergram.repository.PostRepository;
import com.nikita.gamergram.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    void like_createsLike_whenNotAlreadyLiked() {
        when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(false);
        when(postRepository.existsById(1L)).thenReturn(true);
        when(postRepository.getReferenceById(1L)).thenReturn(Post.builder().id(1L).build());
        when(userRepository.getReferenceById(2L)).thenReturn(User.builder().id(2L).build());

        likeService.like(1L, 2L);

        verify(likeRepository).save(any());
    }

    @Test
    void like_isIdempotent_whenAlreadyLiked() {
        when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(true);

        likeService.like(1L, 2L);

        verify(likeRepository, never()).save(any());
        verify(postRepository, never()).existsById(any());
    }

    @Test
    void like_throws_whenPostNotFound() {
        when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(false);
        when(postRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> likeService.like(1L, 2L))
                .isInstanceOf(PostNotFoundException.class);

        verify(likeRepository, never()).save(any());
    }

    @Test
    void unlike_callsDelete() {
        likeService.unlike(1L, 2L);

        verify(likeRepository).deleteByPostIdAndUserId(1L, 2L);
    }

    @Test
    void getLikeCount_returnsCount() {
        when(likeRepository.countByPostId(1L)).thenReturn(5L);

        long count = likeService.getLikeCount(1L);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void isLikedByUser_returnsBoolean() {
        when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(true);

        boolean liked = likeService.isLikedByUser(1L, 2L);

        assertThat(liked).isTrue();
    }
}
