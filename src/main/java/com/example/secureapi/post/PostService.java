package com.example.secureapi.post;

import com.example.secureapi.user.User;
import com.example.secureapi.user.UserRepository;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public List<PostResponse> findAll() {
        return postRepository.findAllByOrderByIdDesc().stream().map(PostResponse::from).toList();
    }

    public PostResponse create(CreatePostRequest request, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String safeTitle = Jsoup.clean(request.title(), Safelist.none());
        String safeContent = Jsoup.clean(request.content(), Safelist.none());
        Post post = postRepository.save(new Post(safeTitle, safeContent, author));
        return PostResponse.from(post);
    }
}