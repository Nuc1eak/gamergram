package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.CommentResponse;
import com.nikita.gamergram.dto.CreateCommentRequest;
import com.nikita.gamergram.entity.Comment;
import com.nikita.gamergram.entity.Post;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.exception.CommentNotFoundException;
import com.nikita.gamergram.exception.PostNotFoundException;
import com.nikita.gamergram.repository.CommentRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void create_savesComment_whenPostExists() {
        CreateCommentRequest request = new CreateCommentRequest("nice clip!");
        Post postRef = Post.builder().id(1L).build();
        User authorRef = User.builder().id(2L).username("nikita").build();
        Comment saved = Comment.builder()
                .id(10L).post(postRef).author(authorRef).body("nice clip!")
                .createdAt(LocalDateTime.now())
                .build();

        when(postRepository.existsById(1L)).thenReturn(true);
        when(postRepository.getReferenceById(1L)).thenReturn(postRef);
        when(userRepository.getReferenceById(2L)).thenReturn(authorRef);
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentResponse response = commentService.create(1L, 2L, request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.authorId()).isEqualTo(2L);
        assertThat(response.authorUsername()).isEqualTo("nikita");
        assertThat(response.body()).isEqualTo("nice clip!");
    }

    @Test
    void create_throws_whenPostNotFound() {
        CreateCommentRequest request = new CreateCommentRequest("nice clip!");
        when(postRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> commentService.create(404L, 2L, request))
                .isInstanceOf(PostNotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void getByPost_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        User author = User.builder().id(2L).username("nikita").build();
        Comment comment = Comment.builder().id(1L).author(author).body("gg").createdAt(LocalDateTime.now()).build();
        Page<Comment> page = new PageImpl<>(List.of(comment), pageable, 1);

        when(commentRepository.findByPostIdOrderByCreatedAtAsc(1L, pageable)).thenReturn(page);

        Page<CommentResponse> result = commentService.getByPost(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).body()).isEqualTo("gg");
    }

    @Test
    void delete_deletesComment_whenRequesterIsAuthor() {
        User author = User.builder().id(2L).build();
        Comment comment = Comment.builder().id(10L).author(author).build();
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        commentService.delete(10L, 2L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_throwsAccessDenied_whenRequesterIsNotAuthor() {
        User author = User.builder().id(2L).build();
        Comment comment = Comment.builder().id(10L).author(author).build();
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(10L, 99L))
                .isInstanceOf(AccessDeniedException.class);

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void delete_throws_whenCommentNotFound() {
        when(commentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(10L, 2L))
                .isInstanceOf(CommentNotFoundException.class);
    }
}
