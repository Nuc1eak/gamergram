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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse create(Long postId, Long authorId, CreateCommentRequest request) {
        if (!postRepository.existsById(postId)) {
            throw PostNotFoundException.byId(postId);
        }

        Post postRef = postRepository.getReferenceById(postId);
        User authorRef = userRepository.getReferenceById(authorId);

        Comment comment = Comment.builder()
                .post(postRef)
                .author(authorRef)
                .body(request.body())
                .build();

        Comment saved = commentRepository.save(comment);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getByPost(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void delete(Long commentId, Long requesterId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> CommentNotFoundException.byId(commentId));

        if (!comment.getAuthor().getId().equals(requesterId)) {
            throw new AccessDeniedException("Cannot delete another user's comment");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername(),
                comment.getAuthor().getAvatarUrl(),
                comment.getBody(),
                comment.getCreatedAt()
        );
    }
}
