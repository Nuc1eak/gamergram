package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.CreatePostRequest;
import com.nikita.gamergram.dto.PostResponse;
import com.nikita.gamergram.entity.Game;
import com.nikita.gamergram.entity.Post;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.exception.GameNotFoundException;
import com.nikita.gamergram.exception.PostNotFoundException;
import com.nikita.gamergram.repository.CommentRepository;
import com.nikita.gamergram.repository.GameRepository;
import com.nikita.gamergram.repository.LikeRepository;
import com.nikita.gamergram.repository.PostRepository;
import com.nikita.gamergram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public PostResponse create(Long authorId, CreatePostRequest request) {
        User authorRef = userRepository.getReferenceById(authorId);

        Game gameRef = null;
        if (request.gameId() != null) {
            gameRef = gameRepository.findById(request.gameId())
                    .orElseThrow(() -> GameNotFoundException.byId(request.gameId()));
        }

        Post post = Post.builder()
                .author(authorRef)
                .game(gameRef)
                .mediaUrl(request.mediaUrl())
                .mediaType(request.mediaType())
                .caption(request.caption())
                .build();

        Post saved = postRepository.save(post);
        return toResponse(saved, false);
    }

    @Transactional(readOnly = true)
    public PostResponse getById(Long id, Long viewerId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> PostNotFoundException.byId(id));
        return toResponse(post, likeRepository.existsByPostIdAndUserId(id, viewerId));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getUserPosts(Long authorId, Long viewerId, Pageable pageable) {
        return toResponsePage(postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable), viewerId);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getFeed(Long userId, Pageable pageable) {
        return toResponsePage(postRepository.findFeedForUser(userId, pageable), userId);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getExplore(Long viewerId, Pageable pageable) {
        return toResponsePage(postRepository.findExplore(pageable), viewerId);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getGamePosts(Long gameId, Long viewerId, Pageable pageable) {
        if (!gameRepository.existsById(gameId)) {
            throw GameNotFoundException.byId(gameId);
        }
        return toResponsePage(postRepository.findByGameIdOrderByCreatedAtDesc(gameId, pageable), viewerId);
    }

    @Transactional
    public void delete(Long postId, Long requesterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> PostNotFoundException.byId(postId));

        if (!post.getAuthor().getId().equals(requesterId)) {
            throw new AccessDeniedException("Cannot delete another user's post");
        }

        postRepository.delete(post);
    }

    private Page<PostResponse> toResponsePage(Page<Post> posts, Long viewerId) {
        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        Set<Long> likedPostIds = postIds.isEmpty()
                ? Set.of()
                : likeRepository.findLikedPostIds(viewerId, postIds);
        return posts.map(post -> toResponse(post, likedPostIds.contains(post.getId())));
    }

    private PostResponse toResponse(Post post, boolean likedByMe) {
        // Known MVP trade-off: 2 count queries per post (N+1). Fix: one batched query grouped by post_id.
        long likeCount = likeRepository.countByPostId(post.getId());
        long commentCount = commentRepository.countByPostId(post.getId());

        Long gameId = post.getGame() != null ? post.getGame().getId() : null;
        String gameTitle = post.getGame() != null ? post.getGame().getTitle() : null;

        return new PostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getUsername(),
                post.getAuthor().getAvatarUrl(),
                gameId,
                gameTitle,
                post.getMediaUrl(),
                post.getMediaType(),
                post.getCaption(),
                post.getCreatedAt(),
                likeCount,
                commentCount,
                likedByMe
        );
    }
}
