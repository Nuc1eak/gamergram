package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.CreatePostRequest;
import com.nikita.gamergram.dto.PostResponse;
import com.nikita.gamergram.entity.Game;
import com.nikita.gamergram.entity.Post;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.entity.enums.MediaType;
import com.nikita.gamergram.exception.GameNotFoundException;
import com.nikita.gamergram.exception.PostNotFoundException;
import com.nikita.gamergram.repository.CommentRepository;
import com.nikita.gamergram.repository.GameRepository;
import com.nikita.gamergram.repository.LikeRepository;
import com.nikita.gamergram.repository.PostRepository;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void create_savesPost_withoutGame() {
        CreatePostRequest request = new CreatePostRequest("http://media/1.png", MediaType.PHOTO, "caption", null);
        User author = User.builder().id(1L).username("nikita").build();
        Post saved = Post.builder()
                .id(100L).author(author).mediaUrl("http://media/1.png")
                .mediaType(MediaType.PHOTO).caption("caption").createdAt(LocalDateTime.now())
                .build();

        when(userRepository.getReferenceById(1L)).thenReturn(author);
        when(postRepository.save(any(Post.class))).thenReturn(saved);
        when(likeRepository.countByPostId(100L)).thenReturn(0L);
        when(commentRepository.countByPostId(100L)).thenReturn(0L);

        PostResponse response = postService.create(1L, request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.authorUsername()).isEqualTo("nikita");
        assertThat(response.gameId()).isNull();
        assertThat(response.mediaType()).isEqualTo(MediaType.PHOTO);
        verify(gameRepository, never()).findById(any());
    }

    @Test
    void create_savesPost_withGame() {
        CreatePostRequest request = new CreatePostRequest("http://media/2.png", MediaType.VIDEO, "clip", 5L);
        User author = User.builder().id(1L).username("nikita").build();
        Game game = Game.builder().id(5L).title("Counter-Strike 2").build();
        Post saved = Post.builder()
                .id(101L).author(author).game(game).mediaUrl("http://media/2.png")
                .mediaType(MediaType.VIDEO).caption("clip").createdAt(LocalDateTime.now())
                .build();

        when(userRepository.getReferenceById(1L)).thenReturn(author);
        when(gameRepository.findById(5L)).thenReturn(Optional.of(game));
        when(postRepository.save(any(Post.class))).thenReturn(saved);
        when(likeRepository.countByPostId(101L)).thenReturn(0L);
        when(commentRepository.countByPostId(101L)).thenReturn(0L);

        PostResponse response = postService.create(1L, request);

        assertThat(response.gameId()).isEqualTo(5L);
        assertThat(response.gameTitle()).isEqualTo("Counter-Strike 2");
    }

    @Test
    void create_throws_whenGameNotFound() {
        CreatePostRequest request = new CreatePostRequest("http://media/3.png", MediaType.PHOTO, null, 404L);
        when(userRepository.getReferenceById(1L)).thenReturn(User.builder().id(1L).build());
        when(gameRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.create(1L, request))
                .isInstanceOf(GameNotFoundException.class);

        verify(postRepository, never()).save(any());
    }

    @Test
    void getById_returnsPost_whenExists() {
        User author = User.builder().id(1L).username("nikita").build();
        Post post = Post.builder().id(1L).author(author).mediaUrl("url").mediaType(MediaType.PHOTO)
                .createdAt(LocalDateTime.now()).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.countByPostId(1L)).thenReturn(2L);
        when(commentRepository.countByPostId(1L)).thenReturn(1L);

        PostResponse response = postService.getById(1L, 7L);

        assertThat(response.likeCount()).isEqualTo(2L);
        assertThat(response.commentCount()).isEqualTo(1L);
    }

    @Test
    void getById_throws_whenNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getById(99L, 7L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void getUserPosts_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        User author = User.builder().id(1L).username("nikita").build();
        Post post = Post.builder().id(1L).author(author).mediaUrl("url").mediaType(MediaType.PHOTO)
                .createdAt(LocalDateTime.now()).build();
        Page<Post> page = new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.findByAuthorIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);
        when(likeRepository.countByPostId(1L)).thenReturn(0L);
        when(commentRepository.countByPostId(1L)).thenReturn(0L);

        Page<PostResponse> result = postService.getUserPosts(1L, 7L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).authorUsername()).isEqualTo("nikita");
    }

    @Test
    void getFeed_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        User author = User.builder().id(2L).username("admin").build();
        Post post = Post.builder().id(1L).author(author).mediaUrl("url").mediaType(MediaType.VIDEO)
                .createdAt(LocalDateTime.now()).build();
        Page<Post> page = new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.findFeedForUser(1L, pageable)).thenReturn(page);
        when(likeRepository.countByPostId(1L)).thenReturn(0L);
        when(commentRepository.countByPostId(1L)).thenReturn(0L);

        Page<PostResponse> result = postService.getFeed(1L, pageable);

        assertThat(result.getContent().get(0).authorUsername()).isEqualTo("admin");
    }

    @Test
    void getExplore_marksOnlyPostsLikedByViewer() {
        Pageable pageable = PageRequest.of(0, 20);
        User author = User.builder().id(2L).username("admin").build();
        Post liked = Post.builder().id(1L).author(author).mediaUrl("a").mediaType(MediaType.PHOTO)
                .createdAt(LocalDateTime.now()).build();
        Post notLiked = Post.builder().id(2L).author(author).mediaUrl("b").mediaType(MediaType.PHOTO)
                .createdAt(LocalDateTime.now()).build();

        when(postRepository.findExplore(pageable))
                .thenReturn(new PageImpl<>(List.of(liked, notLiked), pageable, 2));
        when(likeRepository.findLikedPostIds(7L, List.of(1L, 2L))).thenReturn(Set.of(1L));

        Page<PostResponse> result = postService.getExplore(7L, pageable);

        assertThat(result.getContent()).extracting(PostResponse::likedByMe).containsExactly(true, false);
    }

    @Test
    void getGamePosts_throws_whenGameNotFound() {
        Pageable pageable = PageRequest.of(0, 20);
        when(gameRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> postService.getGamePosts(404L, 7L, pageable))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void delete_deletesPost_whenRequesterIsAuthor() {
        User author = User.builder().id(1L).build();
        Post post = Post.builder().id(10L).author(author).build();
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        postService.delete(10L, 1L);

        verify(postRepository).delete(post);
    }

    @Test
    void delete_throwsAccessDenied_whenRequesterIsNotAuthor() {
        User author = User.builder().id(1L).build();
        Post post = Post.builder().id(10L).author(author).build();
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.delete(10L, 2L))
                .isInstanceOf(AccessDeniedException.class);

        verify(postRepository, never()).delete(any());
    }

    @Test
    void delete_throws_whenPostNotFound() {
        when(postRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.delete(10L, 1L))
                .isInstanceOf(PostNotFoundException.class);
    }
}
