package com.nikita.gamergram.integration;

import com.nikita.gamergram.entity.Like;
import com.nikita.gamergram.entity.Post;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.entity.enums.MediaType;
import com.nikita.gamergram.repository.LikeRepository;
import com.nikita.gamergram.repository.PostRepository;
import com.nikita.gamergram.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class LikeRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    @Test
    void existsByPostIdAndUserId_and_countByPostId_reflectRealRows() {
        User author = userRepository.save(user("author"));
        User liker = userRepository.save(user("liker"));
        Post post = postRepository.save(post(author));

        assertThat(likeRepository.existsByPostIdAndUserId(post.getId(), liker.getId())).isFalse();

        likeRepository.save(Like.builder().post(post).user(liker).build());

        assertThat(likeRepository.existsByPostIdAndUserId(post.getId(), liker.getId())).isTrue();
        assertThat(likeRepository.countByPostId(post.getId())).isEqualTo(1L);
    }

    @Test
    void deleteByPostIdAndUserId_removesOnlyThatLike() {
        User author = userRepository.save(user("author2"));
        User liker1 = userRepository.save(user("liker1"));
        User liker2 = userRepository.save(user("liker2"));
        Post post = postRepository.save(post(author));
        likeRepository.save(Like.builder().post(post).user(liker1).build());
        likeRepository.save(Like.builder().post(post).user(liker2).build());

        likeRepository.deleteByPostIdAndUserId(post.getId(), liker1.getId());

        assertThat(likeRepository.countByPostId(post.getId())).isEqualTo(1L);
        assertThat(likeRepository.existsByPostIdAndUserId(post.getId(), liker2.getId())).isTrue();
    }

    @Test
    void uniqueConstraint_rejectsDuplicateLike_onRealPostgres() {
        User author = userRepository.save(user("author3"));
        User liker = userRepository.save(user("liker3"));
        Post post = postRepository.save(post(author));
        likeRepository.save(Like.builder().post(post).user(liker).build());

        Like duplicate = Like.builder().post(post).user(liker).build();

        // Mocks would miss this: exercises the real (post_id, user_id) unique constraint.
        assertThatThrownBy(() -> likeRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User user(String username) {
        return User.builder().username(username).email(username + "@test.com").passwordHash("hash").build();
    }

    private Post post(User author) {
        return Post.builder().author(author).mediaUrl("http://media/x").mediaType(MediaType.PHOTO).build();
    }
}
