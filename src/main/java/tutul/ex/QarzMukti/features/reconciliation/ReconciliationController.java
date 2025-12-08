package tutul.ex.QarzMukti.features.reconciliation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tutul.ex.QarzMukti.shared.dto.ApiResponse;
import tutul.ex.QarzMukti.shared.entities.ReconciliationReport;
import tutul.ex.QarzMukti.shared.entities.ReconciliationResult;
import tutul.ex.QarzMukti.features.reconciliation.ReconciliationService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    /**
     * Upload a daily settlement report for reconciliation
     * Admin/Accountant only
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('RECONCILIATION:WRITE', 'LOAN:APPROVE')") // Assuming approvers can reconcile
    public ResponseEntity<ApiResponse<ReconciliationReport>> uploadSettlementReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("provider") String provider,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {

        log.info("Uploading settlement report for provider: {}, date: {}", provider, reportDate);
        
        ReconciliationReport report = reconciliationService.processSettlementReport(file, provider, reportDate);

        return ResponseEntity.ok(ApiResponse.success(report, "Reconciliation report processed successfully"));
    }

    /**
     * Get all reconciliation reports
     */
    @GetMapping("/reports")
    @PreAuthorize("hasAnyAuthority('RECONCILIATION:READ', 'LOAN:READ')")
    public ResponseEntity<ApiResponse<List<ReconciliationReport>>> getAllReports() {
        return ResponseEntity.ok(ApiResponse.success(reconciliationService.getAllReports(), "Reports retrieved successfully"));
    }

    /**
     * Get detailed results for a specific report
     */
    @GetMapping("/reports/{reportId}/results")
    @PreAuthorize("hasAnyAuthority('RECONCILIATION:READ', 'LOAN:READ')")
    public ResponseEntity<ApiResponse<List<ReconciliationResult>>> getReportResults(
            @PathVariable UUID reportId) {
        return ResponseEntity.ok(ApiResponse.success(reconciliationService.getReportResults(reportId), "Report results retrieved successfully"));
    }
}
