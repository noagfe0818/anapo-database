package com.example.anapo.user.application.account.controller;

import com.example.anapo.user.application.account.dto.AccountDto;
import com.example.anapo.user.application.account.dto.AccountUpdateDto;
import com.example.anapo.user.application.account.service.AccountService;
import com.example.anapo.user.domain.account.entity.Account;
import com.example.anapo.user.exception.DuplicateUserIdException;
import com.example.anapo.user.exception.PasswordMismatchException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AccountController {

    private final AccountService accountService;

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<?> joinUser(@RequestBody AccountDto accountDto) {
        try {
            return ResponseEntity.ok(accountService.join(accountDto));
        } catch (DuplicateUserIdException | PasswordMismatchException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("회원가입 오류: " + e.getMessage());
        }
    }

    // ★ 로그인 (수정됨: ID를 확실하게 보내주도록 변경)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AccountDto accountDto, HttpServletRequest request) {
        try {
            System.out.println("로그인 시도 중... ID: " + accountDto.getUserId());

            Account user = accountService.login(accountDto);

            if (user != null) {
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) oldSession.invalidate();

                HttpSession newSession = request.getSession(true);
                newSession.setAttribute("loggedInUser", user);
                newSession.setMaxInactiveInterval(1800);

                System.out.println("로그인 성공! User DB ID: " + user.getId());

                // ✅ [핵심 수정] Entity를 바로 보내지 말고, Map에 담아서 보냅니다.
                // 이렇게 해야 프론트엔드에서 data.id 를 확실하게 받을 수 있습니다.
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("message", "로그인 성공");
                responseMap.put("id", user.getId());         // 👈 이 'id'가 프론트엔드에 저장됩니다! (PK)
                responseMap.put("userId", user.getUserId()); // 이메일 아이디
                responseMap.put("userName", user.getUserName()); // 사용자 이름

                return ResponseEntity.ok(responseMap);

            } else {
                System.out.println("로그인 실패: 아이디/비번 불일치");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 잘못되었습니다.");
            }
        } catch (Exception e) {
            System.err.println("!!! 로그인 에러 발생 !!!");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 에러 내용: " + e.toString());
        }
    }

    // 로그아웃
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // 로그인 상태 확인
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 없습니다.");
        }
        try {
            return ResponseEntity.ok((Account) loggedInUser);
        } catch (Exception e) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("세션 오류");
        }
    }

    // 정보 수정
    @PatchMapping("/accUpdate/{accId}")
    public ResponseEntity<?> updateAccount(@PathVariable Long accId, @RequestBody AccountUpdateDto dto) {
        Account updated = accountService.updateAccount(accId, dto);
        return ResponseEntity.ok(Map.of("message", "수정 완료", "userName", updated.getUserName()));
    }
}