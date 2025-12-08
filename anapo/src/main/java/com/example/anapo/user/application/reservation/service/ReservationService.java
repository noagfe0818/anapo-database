package com.example.anapo.user.application.reservation.service;

import com.example.anapo.user.application.reservation.dto.ReservationDto;
import com.example.anapo.user.domain.account.entity.Account;
import com.example.anapo.user.domain.account.repository.AccountRepository;
import com.example.anapo.user.domain.hospital.entity.Hospital;
import com.example.anapo.user.domain.hospital.repository.HospitalDepartmentRepository;
import com.example.anapo.user.domain.hospital.repository.HospitalRepository;
import com.example.anapo.user.domain.reservation.entity.Reservation;
import com.example.anapo.user.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final AccountRepository accountRepository;
    private final HospitalRepository hospitalRepository;
    private final HospitalDepartmentRepository hospitalDepartmentRepository;

    // ✅ [필수] 모든 예약 조회 (Controller에서 사용)
    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    // 예약 등록
    public Reservation createReservation(ReservationDto dto) {
        // 1. 유효성 검사
        if (dto.getReserDate() == null || dto.getDepartment() == null || dto.getAcc() == null || dto.getHos() == null) {
            throw new IllegalArgumentException("필수 정보가 부족합니다.");
        }
        boolean exists = hospitalDepartmentRepository.existsByHospital_IdAndDepartment_DeptName(
                dto.getHos(), dto.getDepartment()
        );
        if (!exists) throw new IllegalArgumentException("해당 진료과 미제공");

        long count = reservationRepository.countReservationsAtSameTime(
                dto.getHos().longValue(), dto.getReserDate(), dto.getDepartment()
        );
        if (count >= 3) throw new IllegalStateException("예약 마감");

        // 2. 조회
        Account user = accountRepository.findById(dto.getAcc())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Hospital hospital = hospitalRepository.findById(dto.getHos())
                .orElseThrow(() -> new IllegalArgumentException("병원 없음"));

        // 3. 생성 (Builder 대신 생성자 사용)
        Reservation reservation = new Reservation(
                dto.getReserDate(), dto.getDepartment(), true, user, hospital
        );

        return reservationRepository.save(reservation);
    }

    // 예약 수정
    public Reservation updateReservation(Long reservationId, ReservationDto dto) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 없음"));

        LocalDateTime oldDate = reservation.getReserDate();
        String oldDept = reservation.getDepartment();
        Long hosId = reservation.getHospital().getId();

        LocalDateTime newDate = dto.getReserDate() != null ? dto.getReserDate() : oldDate;
        String newDept = dto.getDepartment() != null ? dto.getDepartment() : oldDept;

        // 시간/과 변경 시 인원 체크
        if (!newDate.equals(oldDate) || !newDept.equals(oldDept)) {
            long count = reservationRepository.countReservationsAtSameTime(hosId, newDate, newDept);
            if (count >= 3) throw new IllegalStateException("해당 시간대는 마감되었습니다.");
        }

        // 🚨 수정된 부분: Builder 대신 changeReservation 메서드 사용 (Reservation.java에 있는 기능!)
        reservation.changeReservation(newDate, newDept);

        return reservationRepository.save(reservation);
    }

    // 예약 삭제
    public void deleteReservation(Long reservationId) {
        if (!reservationRepository.existsById(reservationId)) {
            throw new IllegalArgumentException("예약 없음");
        }
        reservationRepository.deleteById(reservationId);
    }
}