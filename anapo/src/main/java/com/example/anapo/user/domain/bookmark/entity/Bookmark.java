package com.example.anapo.user.domain.bookmark.entity;

import com.example.anapo.user.domain.account.entity.Account; // Account 경로 import 필요
import com.example.anapo.user.domain.hospital.entity.Hospital; // Hospital 경로 import 필요
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 수정 전 (지우세요) ---
    // @Column(nullable = false)
    // private Long userId;

    // @Column(nullable = false)
    // private Long hospitalId;
    // ----------------------

    // --- 수정 후 (추가하세요) ---
    // 사용자 (Account 객체와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Account user;

    // 병원 (Hospital 객체와 연결) -> 이게 있어야 b.hospital 사용 가능!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;
    // ------------------------

    // 즐겨찾기 등록 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;
}