package com.example.anapo.user.application.report.controller;

import com.example.anapo.user.application.report.dto.ReportCreateDto;
import com.example.anapo.user.application.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ReportCreateDto dto) {
        return ResponseEntity.ok(reportService.createReport(dto));
    }
}
