package com.example.anapo.user.application.bookmark.service;

import com.example.anapo.user.application.bookmark.dto.BookmarkCreateDto;
import com.example.anapo.user.application.bookmark.dto.BookmarkResponseDto;
import com.example.anapo.user.domain.account.entity.Account; // Account 임포트
import com.example.anapo.user.domain.account.repository.AccountRepository; // AccountRepo 임포트
import com.example.anapo.user.domain.bookmark.entity.Bookmark;
import com.example.anapo.user.domain.bookmark.repository.BookmarkRepository;
import com.example.anapo.user.domain.hospital.entity.Hospital;
import com.example.anapo.user.domain.hospital.repository.HospitalRepository;
import com.example.anapo.user.exception.DuplicateBookmarkException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 추가 권장

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용, 쓰기 작업에만 @Transactional 붙임
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final HospitalRepository hospitalRepository;
    private final AccountRepository accountRepository; // [추가] 회원 정보 조회를 위해 필요

    // 즐겨찾기 등록
    @Transactional // 쓰기 작업이므로 트랜잭션 필요
    public BookmarkResponseDto addBookmark(BookmarkCreateDto dto) {

        // 1. 사용자(Account) 정보 조회 (DB에서 객체를 가져옴)
        Account user = accountRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 병원(Hospital) 존재 여부 확인 (DB에서 객체를 가져옴)
        Hospital hos = hospitalRepository.findById(dto.getHospitalId())
                .orElseThrow(() -> new IllegalArgumentException("병원을 찾을 수 없습니다."));

        // 3. 이미 즐겨찾기 되어있는지 확인
        // 주의: Repository 메소드 이름이 findByUserAndHospital 등으로 변경되어야 할 수도 있습니다.
        // 일단 기존 로직 유지를 위해 아래와 같이 작성하지만, Repository에서 에러가 난다면
        // bookmarkRepository.existsByUserAndHospital(user, hos) 등을 사용하세요.
        bookmarkRepository.findByUserIdAndHospitalId(dto.getUserId(), dto.getHospitalId())
                .ifPresent(b -> {
                    throw new DuplicateBookmarkException("이미 즐겨찾기에 등록된 병원입니다.");
                });

        // 4. 저장 (객체 연결)
        Bookmark bookmark = Bookmark.builder()
                .user(user)      // [수정] .userId() -> .user()
                .hospital(hos)   // [수정] .hospitalId() -> .hospital()
                .createdAt(LocalDateTime.now())
                .build();

        Bookmark saved = bookmarkRepository.save(bookmark);

        // 5. 결과 반환
        return BookmarkResponseDto.builder()
                .id(saved.getId())
                .userId(saved.getUser().getId())         // [수정] 객체에서 ID 꺼내기
                .hospitalId(saved.getHospital().getId()) // [수정] 객체에서 ID 꺼내기
                .hosName(hos.getHosName())
                .hosAddress(hos.getHosAddress())
                .build();
    }


    // 즐겨찾기 삭제
    @Transactional
    public void removeBookmark(Long userId, Long hospitalId) {
        Bookmark bookmark = bookmarkRepository.findByUserIdAndHospitalId(userId, hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("즐겨찾기에 존재하지 않는 병원입니다."));

        bookmarkRepository.delete(bookmark);
    }

    // 특정 사용자의 즐겨찾기 리스트 조회
    public List<BookmarkResponseDto> getBookmarks(Long userId) {

        // Repository 메소드 이름을 findByUser_Id(userId) 로 바꾸는 것이 좋습니다.
        List<Bookmark> list = bookmarkRepository.findByUserId(userId);

        return list.stream().map(b -> {
            // [최적화] 이미 Bookmark 안에 Hospital 객체가 연결되어 있으므로,
            // 굳이 hospitalRepository.findById()를 또 호출할 필요가 없습니다.
            Hospital hos = b.getHospital();

            return BookmarkResponseDto.builder()
                    .id(b.getId())
                    .userId(b.getUser().getId())         // [수정]
                    .hospitalId(hos.getId())             // [수정]
                    .hosName(hos.getHosName())
                    .hosAddress(hos.getHosAddress())
                    .createdAt(b.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }
}