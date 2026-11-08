package com.nikita.gamergram.integration;

import com.nikita.gamergram.entity.Follow;
import com.nikita.gamergram.entity.Post;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.entity.enums.MediaType;
import com.nikita.gamergram.repository.FollowRepository;
import com.nikita.gamergram.repository.PostRepository;
import com.nikita.gamergram.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

// Replace.NONE keeps the Testcontainers Postgres instead of embedded H2.
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PostRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowRepository followRepository;

    @Test
    void findFeedForUser_returnsOnlyPostsFromFollowedAuthors_orderedByNewestFirst() {
        User viewer = userRepository.save(user("viewer"));
        User followed = userRepository.save(user("followed"));
        User stranger = userRepository.save(user("stranger"));

        followRepository.save(Follow.builder().follower(viewer).following(followed).build());

        Post older = postRepository.save(post(followed, "clip1"));
        Post newer = postRepository.save(post(followed, "clip2"));
        postRepository.save(post(stranger, "clip3")); // must not reach the viewer's feed

        Page<Post> feed = postRepository.findFeedForUser(viewer.getId(), PageRequest.of(0, 20));

        assertThat(feed.getContent()).hasSize(2);
        assertThat(feed.getContent().get(0).getId()).isEqualTo(newer.getId());
        assertThat(feed.getContent().get(1).getId()).isEqualTo(older.getId());
        assertThat(feed.getContent()).extracting(Post::getAuthor).containsOnly(followed);
    }

    @Test
    void findFeedForUser_returnsEmpty_whenNotFollowingAnyone() {
        User viewer = userRepository.save(user("lonely"));
        User someone = userRepository.save(user("someone"));
        postRepository.save(post(someone, "clip"));

        Page<Post> feed = postRepository.findFeedForUser(viewer.getId(), PageRequest.of(0, 20));

        assertThat(feed.getContent()).isEmpty();
    }

    @Test
    void findByAuthorIdOrderByCreatedAtDesc_returnsOnlyThatAuthorsPosts() {
        User author = userRepository.save(user("author"));
        User other = userRepository.save(user("other"));
        postRepository.save(post(author, "a1"));
        postRepository.save(post(other, "o1"));

        Page<Post> page = postRepository.findByAuthorIdOrderByCreatedAtDesc(author.getId(), PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getCaption()).isEqualTo("a1");
    }

    private User user(String username) {
        return User.builder()
                .username(username)
                .email(username + "@test.com")
                .passwordHash("hash")
                .build();
    }

    private Post post(User author, String caption) {
        return Post.builder()
                .author(author)
                .mediaUrl("http://media/" + caption)
                .mediaType(MediaType.PHOTO)
                .caption(caption)
                .build();
    }
}
