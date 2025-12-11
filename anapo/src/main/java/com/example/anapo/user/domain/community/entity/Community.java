package com.example.anapo.user.domain.community.entity;

import com.example.anapo.user.domain.account.entity.Account;
import jakarta.persistence.*;
import lombok.*; // ✅ [중요] 여기에 Setter가 들어있습니다!
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter // ✅ 이제 에러 없이 작동합니다!
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String writer;

    private String category;

    private int views;
    private int likes;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @CreationTimestamp
    private LocalDateTime updatedAt;
}