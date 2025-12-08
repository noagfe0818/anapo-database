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

    // 암호화 도구 (비밀번호를 안전하게 관리)
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final AccountRepository accountRepository;

    // 회원가입
    @Transactional
    public Account join(AccountDto accountDto){
        // 1. 아이디 중복 검사
        if (accountRepository.findByUserId(accountDto.getUserId()).isPresent()) {
            throw new DuplicateUserIdException("이미 존재하는 아이디입니다.");
        }

        // 2. 비밀번호 일치 검사 (프론트에서 한 번 걸러주지만 더블 체크)
        if (!Objects.equals(accountDto.getUserPassword(), accountDto.getUserPassword2())) {
            throw new PasswordMismatchException("비밀번호가 서로 일치하지 않습니다.");
        }

        // 3. 회원 생성 (★ 비밀번호 암호화 적용!)
        Account account = new Account(
                encoder.encode(accountDto.getUserPassword()), // 암호화해서 저장
                accountDto.getUserName(),
                accountDto.getUserId(),
                accountDto.getUserNumber(),
                accountDto.getBirth(),
                accountDto.getSex()
        );

        return accountRepository.save(account);
    }

    // 사용자 조회
    public Account getUser(String userName) {
        return accountRepository.findByUserName(userName)
                .orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 로그인
    public Account login(AccountDto accountDto) {
        // 데이터가 제대로 넘어왔는지 확인
        if (accountDto.getUserId() == null || accountDto.getUserPassword() == null) {
            return null;
        }

        // 1. 아이디로 사용자 찾기
        Optional<Account> userOp = accountRepository.findByUserId(accountDto.getUserId());

        if (userOp.isPresent()) {
            Account found = userOp.get();

            // 2. 비밀번호 비교 (★ 암호화된 비밀번호 비교 matches 사용)
            // encoder.matches(입력한비번, DB에있는암호화된비번)
            if(encoder.matches(accountDto.getUserPassword(), found.getUserPassword())) {
                return found;
            }
        }

        // 로그인 실패 (아이디 없거나 비번 틀림)
        return null;
    }

    // 아이디 중복 방지 확인용
    public boolean existsByUserId(String userId) {
        return accountRepository.existsByUserId(userId);
    }

    // 회원 정보 수정
    @Transactional
    public Account updateAccount(Long accId, AccountUpdateDto dto) {
        Account account = accountRepository.findById(accId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (dto.getUserName() != null) account.setUserName(dto.getUserName());
        if (dto.getUserNumber() != null) account.setUserNumber(dto.getUserNumber());
        if (dto.getBirth() != null) account.setBirth(dto.getBirth());
        if (dto.getSex() != null) account.setSex(dto.getSex());

        return account; // Dirty Checking으로 자동 저장됨
    }
}