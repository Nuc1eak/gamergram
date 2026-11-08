package com.nikita.gamergram.integration;

import com.nikita.gamergram.entity.Follow;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.repository.FollowRepository;
import com.nikita.gamergram.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class FollowRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByFollowerIdAndFollowingId_and_counts_reflectRealRows() {
        User a = userRepository.save(user("a"));
        User b = userRepository.save(user("b"));

        assertThat(followRepository.existsByFollowerIdAndFollowingId(a.getId(), b.getId())).isFalse();

        followRepository.save(Follow.builder().follower(a).following(b).build());

        assertThat(followRepository.existsByFollowerIdAndFollowingId(a.getId(), b.getId())).isTrue();
        assertThat(followRepository.countByFollowingId(b.getId())).isEqualTo(1L);
        assertThat(followRepository.countByFollowerId(a.getId())).isEqualTo(1L);
    }

    @Test
    void findByFollowingId_returnsFollowersOfUser() {
        User target = userRepository.save(user("target"));
        User follower1 = userRepository.save(user("f1"));
        User follower2 = userRepository.save(user("f2"));
        followRepository.save(Follow.builder().follower(follower1).following(target).build());
        followRepository.save(Follow.builder().follower(follower2).following(target).build());

        Page<Follow> page = followRepository.findByFollowingId(target.getId(), PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(f -> f.getFollower().getUsername())
                .containsExactlyInAnyOrder("f1", "f2");
    }

    @Test
    void findByFollowerId_returnsWhoUserFollows() {
        User me = userRepository.save(user("me"));
        User followed = userRepository.save(user("followed"));
        followRepository.save(Follow.builder().follower(me).following(followed).build());

        Page<Follow> page = followRepository.findByFollowerId(me.getId(), PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getFollowing().getUsername()).isEqualTo("followed");
    }

    @Test
    void deleteByFollowerIdAndFollowingId_removesFollow() {
        User a = userRepository.save(user("aa"));
        User b = userRepository.save(user("bb"));
        followRepository.save(Follow.builder().follower(a).following(b).build());

        followRepository.deleteByFollowerIdAndFollowingId(a.getId(), b.getId());

        assertThat(followRepository.existsByFollowerIdAndFollowingId(a.getId(), b.getId())).isFalse();
    }

    @Test
    void uniqueConstraint_rejectsDuplicateFollow_onRealPostgres() {
        User a = userRepository.save(user("aaa"));
        User b = userRepository.save(user("bbb"));
        followRepository.save(Follow.builder().follower(a).following(b).build());

        Follow duplicate = Follow.builder().follower(a).following(b).build();

        assertThatThrownBy(() -> followRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User user(String username) {
        return User.builder().username(username).email(username + "@test.com").passwordHash("hash").build();
    }
}
