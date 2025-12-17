package com.example.anapo.user.application.account.dto;

import com.example.anapo.user.domain.account.entity.AccountRole; // ✅ Import 추가
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Getter
@Setter
public class AccountDto {
    @NotEmpty(message = "비밀번호은 필수항목입니다.")
    private String userPassword;

    @NotEmpty(message = "비밀번호 확인은 필수항목입니다.")
    private String userPassword2;

    @Size(min = 3, max = 25, message = "아이디는 3자 이상 25자 이하로 입력해주세요.")
    @NotEmpty(message = "아이디는 필수항목입니다.")
    private String userId;

    @NotEmpty(message = "전화번호는 필수항목입니다.")
    @Pattern(regexp = "\\d{10,11}", message = "전화번호 형식은 01012345678이어야 합니다.")
    private String userNumber;

    @NotEmpty(message = "이름은 필수항목입니다.")
    private String userName;

    @NotEmpty(message = "성별은 필수항목입니다.")
    private String sex;

    @NotEmpty(message = "생년월일은 필수항목입니다.")
    private String birth;

    // ✅ [추가됨] 역할 정보 (USER 또는 HOSPITAL)
    private AccountRole role;

    // 생년월일을 LocalDate로 반환하는 메서드
    public LocalDate getBirthdateAsLocalDate() {
        if (birth != null && !birth.isEmpty()) {
            try {
                return LocalDate.parse(birth);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("생년월일을 올바른 형식으로 입력해주세요.");
            }
        }
        return null;
    }
}