claude# Gamergram API: frontend reference

A social network for gamers: profiles, favorite and wishlist games, media posts, likes, comments, follows.
Backend: Spring Boot 3.3.2, Java 17, PostgreSQL, Spring Security (JWT).

The API runs locally at `http://localhost:8080`. Start Postgres with `docker-compose up` (host port 5433).

## Auth flow

1. `POST /api/auth/register` creates an account.
2. `POST /api/auth/login` returns a JWT.
3. All other endpoints (except `/api/auth/**` and `GET /uploads/**`) require this header:
   ```
   Authorization: Bearer <token>
   ```
4. A token lives 24 hours (`jwt.expiration-ms` in `application.yml`). There are no refresh tokens, so log in again after it expires.
5. A protected endpoint returns **401** with an `ErrorResponse` body when the token is missing or invalid. A valid token on a forbidden action (such as deleting another user's post) returns **403**.

Errors share one format:
```json
{ "status": 401, "message": "Authentication required", "timestamp": "2026-08-22T10:00:00" }
```
Validation errors (`400` from `@Valid`) also include a `fieldErrors` map of field name to message.

## Auth

| Method | Path | Request body | Response | Access |
|---|---|---|---|---|
| POST | `/api/auth/register` | `UserRegistrationRequest` | `201` `UserProfileResponse` | public |
| POST | `/api/auth/login` | `LoginRequest` | `200` `AuthResponse` | public |

```ts
UserRegistrationRequest { username: string(3-50), email: string, password: string(8-100) }
LoginRequest           { username: string, password: string }
AuthResponse           { token: string, tokenType: "Bearer" }
UserProfileResponse    { id, username, email, avatarUrl, bio, createdAt }
```

## Users

| Method | Path | Response | Notes |
|---|---|---|---|
| GET | `/api/users/me` | `UserProfileResponse` | current user, with email |
| PATCH | `/api/users/me` | `UserProfileResponse` | body is `UpdateProfileRequest`; `null` keeps a field, an empty string clears it |
| GET | `/api/users/{id}` | `UserPublicProfileResponse` | public profile, no email |
| GET | `/api/users/by-username/{username}` | `UserPublicProfileResponse` | |
| GET | `/api/users?query=&page=&size=` | `Page<UserSummaryResponse>` | username substring search |

```ts
UserSummaryResponse { id, username, avatarUrl }
UserPublicProfileResponse { id, username, avatarUrl, bio, postCount, followerCount, followingCount, followedByMe }
UpdateProfileRequest { avatarUrl?: string(<=255), bio?: string(<=500) }
```

## Posts

| Method | Path | Body / query | Response |
|---|---|---|---|
| POST | `/api/posts` | `CreatePostRequest` | `201` `PostResponse` |
| GET | `/api/posts/{id}` | — | `PostResponse` |
| GET | `/api/posts/user/{userId}?page=&size=` | — | `Page<PostResponse>` |
| GET | `/api/posts/feed?page=&size=` | — | `Page<PostResponse>`: posts by users the current user follows |
| GET | `/api/posts/explore?page=&size=` | — | `Page<PostResponse>`: all posts, newest first |
| GET | `/api/posts/game/{gameId}?page=&size=` | — | `Page<PostResponse>`: posts for a game |
| DELETE | `/api/posts/{id}` | — | `204`, author only |

```ts
CreatePostRequest { mediaUrl: string, mediaType: "PHOTO"|"VIDEO", caption?: string(<=2200), gameId?: number }
PostResponse {
  id, authorId, authorUsername, authorAvatarUrl,
  gameId, gameTitle,        // null if the post has no game
  mediaUrl, mediaType, caption, createdAt,
  likeCount, commentCount, likedByMe
}
```

`Page<T>` is the standard Spring wrapper: `{ content: T[], totalElements, totalPages, number, size, ... }`.

## Media

| Method | Path | Body | Response |
|---|---|---|---|
| POST | `/api/media` | `multipart/form-data`, field `file` | `201` `MediaResponse` |

```ts
MediaResponse { url: string, mediaType: "PHOTO"|"VIDEO" } // relative url: /uploads/<uuid>.<ext>
```

Accepts jpeg, png, webp, gif, mp4 and webm up to 100 MB. Other types return `400`, larger files return `413`. Files are stored in `app.upload-dir` and served by `GET /uploads/**` without a token.

## Comments

| Method | Path | Body / query | Response |
|---|---|---|---|
| POST | `/api/posts/{postId}/comments` | `CreateCommentRequest` | `201` `CommentResponse` |
| GET | `/api/posts/{postId}/comments?page=&size=` | — | `Page<CommentResponse>` |
| DELETE | `/api/posts/{postId}/comments/{commentId}` | — | `204`, author only |

```ts
CreateCommentRequest { body: string(<=1000) }
CommentResponse { id, authorId, authorUsername, authorAvatarUrl, body, createdAt }
```

## Likes

| Method | Path | Response |
|---|---|---|
| POST | `/api/posts/{postId}/likes` | `204` (idempotent: a repeat like is not an error) |
| DELETE | `/api/posts/{postId}/likes` | `204` (idempotent) |
| GET | `/api/posts/{postId}/likes/count` | `200` `number` |
| GET | `/api/posts/{postId}/likes/me` | `200` `boolean`: whether the current user liked the post |

## Follows

| Method | Path | Response |
|---|---|---|
| POST | `/api/users/{userId}/follow` | `204` (idempotent) |
| DELETE | `/api/users/{userId}/follow` | `204` (idempotent) |
| GET | `/api/users/{userId}/followers?page=&size=` | `Page<UserSummaryResponse>` |
| GET | `/api/users/{userId}/following?page=&size=` | `Page<UserSummaryResponse>` |
| GET | `/api/users/{userId}/followers/count` | `number` |
| GET | `/api/users/{userId}/following/count` | `number` |

Following yourself is not allowed (`400`).

## Games

| Method | Path | Body / query | Response |
|---|---|---|---|
| POST | `/api/games` | `CreateGameRequest` | `201` `GameResponse` |
| GET | `/api/games/{id}` | — | `GameResponse` |
| GET | `/api/games?title=` | — | `GameResponse[]`: title substring search |

```ts
CreateGameRequest { title: string(<=150), description?: string(<=2000) }
GameResponse      { id, title, description }
```

## User ↔ Games (favorites / wishlist)

| Method | Path | Body / query | Response |
|---|---|---|---|
| POST | `/api/me/games` | `AddUserGameRequest` | `204`: add a game or move it between lists |
| DELETE | `/api/me/games/{gameId}` | — | `204` |
| GET | `/api/users/{userId}/games?type=FAVORITE\|WISHLIST` | — | `UserGameResponse[]` |

```ts
AddUserGameRequest { gameId: number, relationType: "FAVORITE"|"WISHLIST" }
UserGameResponse   { id, game: GameResponse, relationType }
```

## CORS

Allowed origins come from `app.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS`), configured in `SecurityConfig`. The default is `http://localhost:5173`.
