package com.nikita.gamergram.repository;

import com.nikita.gamergram.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Profile grid.
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    long countByAuthorId(Long authorId);

    // All posts for a game.
    Page<Post> findByGameIdOrderByCreatedAtDesc(Long gameId, Pageable pageable);

    // Posts by followed authors. join fetch avoids N+1 on author.
    @Query("""
            select p from Post p
            join fetch p.author
            where p.author.id in (
                select f.following.id from Follow f where f.follower.id = :userId
            )
            order by p.createdAt desc
            """)
    Page<Post> findFeedForUser(@Param("userId") Long userId, Pageable pageable);

    // All posts, including authors the viewer does not follow.
    @Query(value = """
            select p from Post p
            join fetch p.author
            left join fetch p.game
            order by p.createdAt desc, p.id desc
            """, countQuery = "select count(p) from Post p")
    Page<Post> findExplore(Pageable pageable);
}
