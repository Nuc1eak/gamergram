package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.UpdateProfileRequest;
import com.nikita.gamergram.dto.UserProfileResponse;
import com.nikita.gamergram.dto.UserPublicProfileResponse;
import com.nikita.gamergram.dto.UserRegistrationRequest;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.exception.UserAlreadyExistsException;
import com.nikita.gamergram.exception.UserNotFoundException;
import com.nikita.gamergram.repository.FollowRepository;
import com.nikita.gamergram.repository.PostRepository;
import com.nikita.gamergram.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Plain Mockito, no Spring context.
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PostRepository postRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getPublicProfile_returnsCountersAndFollowState() {
        User user = User.builder().id(2L).username("admin").bio("hi").build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(postRepository.countByAuthorId(2L)).thenReturn(3L);
        when(followRepository.countByFollowingId(2L)).thenReturn(4L);
        when(followRepository.countByFollowerId(2L)).thenReturn(5L);
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

        UserPublicProfileResponse response = userService.getPublicProfile(2L, 1L);

        assertThat(response.bio()).isEqualTo("hi");
        assertThat(response.postCount()).isEqualTo(3L);
        assertThat(response.followerCount()).isEqualTo(4L);
        assertThat(response.followingCount()).isEqualTo(5L);
        assertThat(response.followedByMe()).isTrue();
    }

    @Test
    void updateProfile_keepsNullFields_andClearsBlankOnes() {
        User user = User.builder().id(1L).username("nikita").avatarUrl("/uploads/a.png").bio("old").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.updateProfile(1L, new UpdateProfileRequest(null, " "));

        assertThat(response.avatarUrl()).isEqualTo("/uploads/a.png");
        assertThat(response.bio()).isNull();
    }

    @Test
    void register_savesNewUser_whenUsernameAndEmailAreFree() {
        UserRegistrationRequest request =
                new UserRegistrationRequest("nikita", "nikita@mail.com", "password123");

        when(userRepository.existsByUsername("nikita")).thenReturn(false);
        when(userRepository.existsByEmail("nikita@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        User savedUser = User.builder()
                .id(1L)
                .username("nikita")
                .email("nikita@mail.com")
                .passwordHash("hashed-password")
                .createdAt(LocalDateTime.now())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserProfileResponse response = userService.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("nikita");
        assertThat(response.email()).isEqualTo("nikita@mail.com");

        // The saved password must already be hashed.
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed-password");
    }

    @Test
    void register_throws_whenUsernameAlreadyTaken() {
        UserRegistrationRequest request =
                new UserRegistrationRequest("nikita", "nikita@mail.com", "password123");
        when(userRepository.existsByUsername("nikita")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        // A taken username must never reach the database.
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throws_whenEmailAlreadyTaken() {
        UserRegistrationRequest request =
                new UserRegistrationRequest("nikita", "nikita@mail.com", "password123");
        when(userRepository.existsByUsername("nikita")).thenReturn(false);
        when(userRepository.existsByEmail("nikita@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_returnsProfile_whenUserExists() {
        User user = User.builder()
                .id(1L)
                .username("nikita")
                .email("nikita@mail.com")
                .passwordHash("hashed")
                .createdAt(LocalDateTime.now())
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getById(1L);

        assertThat(response.username()).isEqualTo("nikita");
    }

    @Test
    void getById_throws_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getByUsername_throws_whenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByUsername("ghost"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
