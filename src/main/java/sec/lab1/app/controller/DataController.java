package sec.lab1.app.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import sec.lab1.app.model.LabPost;
import sec.lab1.app.repository.LabPostRepository;
import sec.lab1.app.repository.LabUserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

record CreatePostRequest(@NotBlank String content) {}

record PostResponse(
        Long id,
        String content,
        Instant createdAt,
        String authorUsername
) { }

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class DataController {
    private final LabPostRepository posts;
    private final LabUserRepository users;

    @GetMapping("/data")
    public List<PostResponse> allPosts() {
        return posts.findAll().stream()
                .map(p -> new PostResponse(
                        p.getId(),
                        p.getContent(),
                        p.getCreatedAt(),
                        p.getAuthor().getUsername()
                ))
                .toList();
    }

    @PostMapping("/posts")
    public PostResponse create(@RequestBody @Validated CreatePostRequest req, Authentication auth) {
        var username = auth.getName();
        var user = users.findByUsername(username).orElseThrow();

        String normalized = req.content().strip();

        LabPost p = LabPost.builder()
                .content(normalized)
                .createdAt(Instant.now())
                .author(user)
                .build();
                
        posts.save(p);

        return new PostResponse(
                p.getId(),
                p.getContent(),
                p.getCreatedAt(),
                p.getAuthor().getUsername()
        );
    }
}
