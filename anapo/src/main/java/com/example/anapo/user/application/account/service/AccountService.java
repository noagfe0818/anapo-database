package com.example.anapo.user.application.account.service;

import com.example.anapo.user.application.account.dto.AccountUpdateDto;
import com.example.anapo.user.exception.DataNotFoundException;
import com.example.anapo.user.application.account.dto.AccountDto;
import com.example.anapo.user.domain.account.entity.Account;
import com.example.anapo.user.domain.account.repository.AccountRepository;
import com.example.anapo.user.exception.DuplicateUserIdException;
import com.example.anapo.user.exception.PasswordMismatchException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final AccountRepository accountRepository;

    // 내 정보 가져오기
    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }

    // ✅ [수정됨] 회원가입 (Builder 패턴 사용으로 안전하게 변경)
    @Transactional
    public Account join(AccountDto accountDto){
        // 1. 아이디 중복 검사
        if (accountRepository.findByUserId(accountDto.getUserId()).isPresent()) {
            throw new DuplicateUserIdException("이미 존재하는 아이디입니다.");
        }

        // 2. 비밀번호 일치 검사
        if (!Objects.equals(accountDto.getUserPassword(), accountDto.getUserPassword2())) {
            throw new PasswordMismatchException("비밀번호가 서로 일치하지 않습니다.");
        }

        // 3. 회원 생성 (Builder 사용 -> role 기본값 USER 자동 적용됨)
        Account account = Account.builder()
                .userId(accountDto.getUserId())
                .userPassword(encoder.encode(accountDto.getUserPassword())) // 암호화
                .userName(accountDto.getUserName())
                .userNumber(accountDto.getUserNumber())
                .birth(accountDto.getBirth())
                .sex(accountDto.getSex())
                // .role(AccountRole.USER) // 엔티티의 @Builder.Default 덕분에 생략해도 자동으로 USER가 됨
                .build();

        return accountRepository.save(account);
    }

    // 사용자 조회
    public Account getUser(String userName) {
        return accountRepository.findByUserName(userName)
                .orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // ✅ 로그인 (수정 없음 - 엔티티를 리턴하면 role 정보도 같이 나갑니다!)
    public Account login(AccountDto accountDto) {
        if (accountDto.getUserId() == null || accountDto.getUserPassword() == null) {
            return null;
        }

        Optional<Account> userOp = accountRepository.findByUserId(accountDto.getUserId());

        if (userOp.isPresent()) {
            Account found = userOp.get();
            if(encoder.matches(accountDto.getUserPassword(), found.getUserPassword())) {
                return found; // 여기서 role이 포함된 Account 객체가 리턴됨
            }
        }
        return null;
    }

    // 아이디 중복 확인
    public boolean existsByUserId(String userId) {
        return accountRepository.existsByUserId(userId);
    }

    // 회원 정보 수정
    @Transactional
    public Account updateAccount(Long accId, AccountUpdateDto dto) {
        Account account = accountRepository.findById(accId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (dto.getUserName() != null && !dto.getUserName().isEmpty()) {
            account.setUserName(dto.getUserName());
        }
        if (dto.getUserNumber() != null && !dto.getUserNumber().isEmpty()) {
            account.setUserNumber(dto.getUserNumber());
        }
        if (dto.getUserPassword() != null && !dto.getUserPassword().isEmpty()) {
            String encodedPwd = encoder.encode(dto.getUserPassword());
            account.setUserPassword(encodedPwd);
        }
        if (dto.getBirth() != null) account.setBirth(dto.getBirth());
        if (dto.getSex() != null) account.setSex(dto.getSex());

        return account;
    }
}