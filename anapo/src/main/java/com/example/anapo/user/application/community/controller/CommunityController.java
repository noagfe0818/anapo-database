package com.example.anapo.user.application.community.controller;

import com.example.anapo.user.application.community.dto.CommunityRequestDto;
import com.example.anapo.user.application.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // 필수!
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CommunityRequestDto dto) {
        return ResponseEntity.ok(communityService.create(dto));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(communityService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.get(id));
    }

    // ✅ [추가됨] 좋아요 기능
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.like(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CommunityRequestDto dto) {
        return ResponseEntity.ok(communityService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        communityService.delete(id);
        return ResponseEntity.ok("삭제 완료!");
    }

    @GetMapping("/search/title")
    public ResponseEntity<?> searchByTitle(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(communityService.searchByTitle(keyword, page, size));
    }

    @GetMapping("/search/writer")
    public ResponseEntity<?> searchByWriter(
            @RequestParam String writer,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(communityService.searchByWriter(writer, page, size));
    }
}