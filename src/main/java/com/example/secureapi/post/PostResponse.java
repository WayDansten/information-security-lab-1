package com.example.secureapi.post;

public record PostResponse(Long id, String title, String content, String author) {
    public static PostResponse from(Post post) {
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getAuthor().getUsername());
    }
}