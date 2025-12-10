package com.example.anapo.user.domain.report.repository;

import com.example.anapo.user.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

}
