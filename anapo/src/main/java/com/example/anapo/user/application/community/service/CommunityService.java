package com.example.anapo.user.application.community.service;

import com.example.anapo.user.application.community.dto.CommunityRequestDto;
import com.example.anapo.user.application.community.dto.CommunityResponseDto;
import com.example.anapo.user.domain.account.entity.Account;
import com.example.anapo.user.domain.account.repository.AccountRepository;
import com.example.anapo.user.domain.community.entity.Community;
import com.example.anapo.user.domain.community.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 추가

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // ✅ 데이터 변경 감지를 위해 필수!
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final AccountRepository accountRepository;

    // 게시글 생성
    public CommunityResponseDto create(CommunityRequestDto dto) {
        Account writerAccount = null;
        if (dto.getAccId() != null) {
            writerAccount = accountRepository.findById(dto.getAccId()).orElse(null);
        }

        Community post = Community.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .writer(dto.getWriter())
                .category(dto.getCategory())
                .account(writerAccount)
                .views(0) // 초기값 0
                .likes(0) // 초기값 0
                .build();

        Community saved = communityRepository.save(post);
        return convertToDto(saved);
    }

    // ✅ [수정됨] 게시글 단건 조회 + 조회수 증가
    public CommunityResponseDto get(Long id) {
        Community post = communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 조회수 1 증가 (Dirty Checking으로 자동 저장됨)
        post.setViews(post.getViews() + 1);

        return convertToDto(post);
    }

    // ✅ [추가됨] 좋아요 증가
    public CommunityResponseDto like(Long id) {
        Community post = communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 좋아요 1 증가
        post.setLikes(post.getLikes() + 1);

        return convertToDto(post);
    }

    // 전체 게시글
    public List<CommunityResponseDto> getAll() {
        return communityRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 수정
    public CommunityResponseDto update(Long id, CommunityRequestDto dto) {
        Community post = communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        post = Community.builder()
                .id(post.getId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .writer(dto.getWriter())
                .category(dto.getCategory())
                .account(post.getAccount())
                .views(post.getViews()) // 기존 값 유지
                .likes(post.getLikes()) // 기존 값 유지
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();

        Community updated = communityRepository.save(post);
        return convertToDto(updated);
    }

    // 삭제
    public void delete(Long id) {
        communityRepository.deleteById(id);
    }

    // 제목 검색
    public Page<CommunityResponseDto> searchByTitle(String keyword, int page, int size) {
        return communityRepository.findByTitleContaining(keyword, PageRequest.of(page, size))
                .map(this::convertToDto);
    }

    // 작성자 검색
    public Page<CommunityResponseDto> searchByWriter(String writer, int page, int size) {
        return communityRepository.findByWriterContaining(writer, PageRequest.of(page, size))
                .map(this::convertToDto);
    }

    // DTO 변환
    private CommunityResponseDto convertToDto(Community post) {
        return CommunityResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getWriter())
                .category(post.getCategory())
                .views(post.getViews()) // ✅ 조회수 전달
                .likes(post.getLikes()) // ✅ 좋아요 전달
                .createdAt(post.getCreatedAt().toString())
                .updatedAt(post.getUpdatedAt().toString())
                .build();
    }
}