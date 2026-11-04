package com.example.secureapi.post;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/data")
    public ResponseEntity<List<PostResponse>> data() {
        return ResponseEntity.ok(postService.findAll());
    }

    @PostMapping("/posts")
    public ResponseEntity<PostResponse> create(
            @Valid @RequestBody CreatePostRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(request, principal.getName()));
    }
}