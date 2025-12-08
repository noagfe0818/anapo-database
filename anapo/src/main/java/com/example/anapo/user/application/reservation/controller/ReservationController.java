package com.example.anapo.user.application.reservation.controller;

import com.example.anapo.user.application.reservation.dto.ReservationDto;
import com.example.anapo.user.application.reservation.service.ReservationService;
import com.example.anapo.user.domain.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // React 연결 허용
public class ReservationController {

    private final ReservationService reservationService;

    // ✅ 내 예약 내역 가져오기 (마이페이지용)
    @GetMapping("/reservations/user/{accId}")
    public ResponseEntity<?> getUserReservations(@PathVariable Long accId) {
        try {
            // Service의 findAll() 사용
            List<Reservation> allReservations = reservationService.findAll();

            List<Map<String, Object>> resultList = new ArrayList<>();

            for (Reservation r : allReservations) {
                // 🚨 수정된 부분: getAcc() -> getUser()
                // Reservation 엔티티의 변수명이 'user'라서 getUser()가 맞습니다.
                if (r.getUser() != null && r.getUser().getId().equals(accId)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", r.getId());
                    map.put("reserDate", r.getReserDate());
                    map.put("department", r.getDepartment());

                    if (r.getHospital() != null) {
                        map.put("hosName", r.getHospital().getHosName());
                    } else {
                        map.put("hosName", "병원 정보 없음");
                    }
                    resultList.add(map);
                }
            }
            return ResponseEntity.ok(resultList);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // [기존] 예약 등록
    @PostMapping("/reservations")
    public ResponseEntity<?> createReservation(@RequestBody ReservationDto dto) {
        try {
            Reservation saved = reservationService.createReservation(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "예약 등록 성공",
                    "reservationId", saved.getId(),
                    "hospitalId", saved.getHospital().getId(),
                    "department", saved.getDepartment(),
                    "reserDate", saved.getReserDate()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // [기존] 예약 수정
    @PatchMapping("/reservations/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @RequestBody ReservationDto dto) {
        try {
            Reservation updated = reservationService.updateReservation(id, dto);
            return ResponseEntity.ok(Map.of(
                    "message", "예약 수정 성공",
                    "reservationId", updated.getId(),
                    "newReserDate", updated.getReserDate(),
                    "newDepartment", updated.getDepartment()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // [기존] 예약 삭제
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long id) {
        try {
            reservationService.deleteReservation(id);
            return ResponseEntity.ok(Map.of("message", "삭제 성공", "reservationId", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}