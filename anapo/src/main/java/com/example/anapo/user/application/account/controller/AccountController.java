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

    // ✅ [수정됨] 내 정보 불러오기 (role 추가)
    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountInfo(@PathVariable Long id) {
        try {
            Account account = accountService.getAccount(id);

            Map<String, Object> response = new HashMap<>();
            response.put("userName", account.getUserName());
            response.put("userId", account.getUserId());
            response.put("userNumber", account.getUserNumber());
            response.put("birth", account.getBirth());
            response.put("sex", account.getSex());
            // ✅ [추가] 내 정보 볼 때도 역할 확인 가능하도록
            response.put("role", account.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("회원 정보를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    // 회원 정보 수정
    @PatchMapping("/accUpdate/{accId}")
    public ResponseEntity<?> updateAccount(@PathVariable Long accId, @RequestBody AccountUpdateDto dto) {
        try {
            Account updated = accountService.updateAccount(accId, dto);

            return ResponseEntity.ok(Map.of(
                    "message", "회원 정보가 성공적으로 수정되었습니다.",
                    "userName", updated.getUserName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("정보 수정 실패: " + e.getMessage());
        }
    }

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

    // ✅ [수정됨] 로그인 (핵심!)
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
                System.out.println("사용자 권한(Role): " + user.getRole()); // 로그로 확인

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("message", "로그인 성공");
                responseMap.put("id", user.getId());
                responseMap.put("userId", user.getUserId());
                responseMap.put("userName", user.getUserName());

                // ✅ [핵심] 드디어 프론트엔드에게 역할을 알려줍니다!
                // 이게 있어야 프론트에서 if (data.role === "HOSPITAL") 문이 작동합니다.
                responseMap.put("role", user.getRole());

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
}