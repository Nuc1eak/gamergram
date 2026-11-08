package com.nikita.gamergram.integration;

import com.nikita.gamergram.dto.AuthResponse;
import com.nikita.gamergram.dto.CreatePostRequest;
import com.nikita.gamergram.dto.LoginRequest;
import com.nikita.gamergram.dto.PostResponse;
import com.nikita.gamergram.dto.UserProfileResponse;
import com.nikita.gamergram.dto.UserRegistrationRequest;
import com.nikita.gamergram.entity.enums.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

// Full stack, no mocks: register -> login -> JWT -> follow -> post -> feed.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerLoginFollowPostFeed_worksEndToEnd() {
        UserProfileResponse viewer = register("viewer_e2e", "viewer_e2e@test.com");
        UserProfileResponse author = register("author_e2e", "author_e2e@test.com");

        String viewerToken = login("viewer_e2e", "password123");
        String authorToken = login("author_e2e", "password123");

        ResponseEntity<Void> followResponse = restTemplate.exchange(
                "/api/users/{userId}/follow",
                HttpMethod.POST,
                authorizedEntity(viewerToken, null),
                Void.class,
                author.id()
        );
        assertThat(followResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        CreatePostRequest createPost = new CreatePostRequest("http://media/clip.mp4", MediaType.VIDEO, "e2e clip", null);
        ResponseEntity<PostResponse> postResponse = restTemplate.exchange(
                "/api/posts",
                HttpMethod.POST,
                authorizedEntity(authorToken, createPost),
                PostResponse.class
        );
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getBody()).isNotNull();
        assertThat(postResponse.getBody().authorUsername()).isEqualTo("author_e2e");

        // Checks the viewer's feed, not GET by id, to catch JOIN FETCH bugs.
        ResponseEntity<String> feedResponse = restTemplate.exchange(
                "/api/posts/feed",
                HttpMethod.GET,
                authorizedEntity(viewerToken, null),
                String.class
        );
        assertThat(feedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(feedResponse.getBody()).contains("e2e clip");
        assertThat(feedResponse.getBody()).contains("author_e2e");
    }

    @Test
    void createPost_returns401_withoutToken() {
        CreatePostRequest createPost = new CreatePostRequest("http://media/clip.mp4", MediaType.PHOTO, null, null);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/posts", createPost, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_returns401_withWrongPassword() {
        register("wrongpass_e2e", "wrongpass_e2e@test.com");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("wrongpass_e2e", "totallyWrongPassword"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private UserProfileResponse register(String username, String email) {
        UserRegistrationRequest request = new UserRegistrationRequest(username, email, "password123");
        ResponseEntity<UserProfileResponse> response =
                restTemplate.postForEntity("/api/auth/register", request, UserProfileResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private String login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        ResponseEntity<AuthResponse> response =
                restTemplate.postForEntity("/api/auth/login", request, AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().token();
    }

    private <T> HttpEntity<T> authorizedEntity(String token, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }
}
