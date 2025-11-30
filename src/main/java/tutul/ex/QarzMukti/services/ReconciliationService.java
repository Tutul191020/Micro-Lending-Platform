package tutul.ex.QarzMukti.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tutul.ex.QarzMukti.common.ValidationException;
import tutul.ex.QarzMukti.entities.PaymentTransaction;
import tutul.ex.QarzMukti.entities.ReconciliationReport;
import tutul.ex.QarzMukti.entities.ReconciliationResult;
import tutul.ex.QarzMukti.repositories.PaymentTransactionRepository;
import tutul.ex.QarzMukti.repositories.ReconciliationReportRepository;
import tutul.ex.QarzMukti.repositories.ReconciliationResultRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final ReconciliationReportRepository reportRepository;
    private final ReconciliationResultRepository resultRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ObjectMapper objectMapper;

    // Configuration for CSV column mapping per provider
    private static final Map<String, Map<String, String>> PROVIDER_CONFIG = Map.of(
            "BKASH", Map.of(
                    "id", "TrxID",
                    "amount", "Amount",
                    "status", "Transaction Status"
            ),
            "NAGAD", Map.of(
                    "id", "TxnID",
                    "amount", "Amount",
                    "status", "Status"
            ),
            "DEFAULT", Map.of(
                    "id", "GatewayTxId",
                    "amount", "Amount",
                    "status", "Status"
            )
    );

    /**
     * Process a daily settlement report from a payment gateway
     * Supports dynamic CSV formats based on provider
     */
    @Transactional
    public ReconciliationReport processSettlementReport(MultipartFile file, String provider, LocalDate reportDate) {
        log.info("Processing settlement report: provider={}, date={}", provider, reportDate);

        // 1. Save the raw report
        ReconciliationReport report = new ReconciliationReport();
        report.setId(UUID.randomUUID());
        report.setReportDate(reportDate);
        report.setProvider(provider);
        report.setCreatedAt(Instant.now());
        
        try {
            String content = new String(file.getBytes());
            Map<String, String> rawData = new HashMap<>();
            rawData.put("filename", file.getOriginalFilename());
            rawData.put("content_preview", content.substring(0, Math.min(content.length(), 1000)));
            report.setRawReport(objectMapper.writeValueAsString(rawData));
        } catch (IOException e) {
            throw new ValidationException("Failed to read report file");
        }

        reportRepository.save(report);

        // 2. Parse CSV and Match Transactions using Dynamic Mapping
        List<ReconciliationResult> results = new ArrayList<>();
        Map<String, String> config = PROVIDER_CONFIG.getOrDefault(provider.toUpperCase(), PROVIDER_CONFIG.get("DEFAULT"));
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) throw new ValidationException("Empty CSV file");
            
            // Map header names to column indices
            String[] headers = headerLine.split(",");
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim(), i);
            }
            
            // Validate required columns exist
            String idCol = config.get("id");
            String amtCol = config.get("amount");
            String statusCol = config.get("status");
            
            if (!headerMap.containsKey(idCol) || !headerMap.containsKey(amtCol) || !headerMap.containsKey(statusCol)) {
                throw new ValidationException("CSV missing required columns for " + provider + 
                        ". Expected: " + idCol + ", " + amtCol + ", " + statusCol);
            }
            
            int idIdx = headerMap.get(idCol);
            int amtIdx = headerMap.get(amtCol);
            int statusIdx = headerMap.get(statusCol);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length <= Math.max(idIdx, Math.max(amtIdx, statusIdx))) continue;

                String gatewayTxId = parts[idIdx].trim();
                String amountStr = parts[amtIdx].trim();
                String status = parts[statusIdx].trim();

                results.add(matchTransaction(report, gatewayTxId, amountStr, status));
            }
        } catch (IOException e) {
            log.error("Error parsing CSV", e);
            throw new ValidationException("Error parsing CSV file");
        }

        resultRepository.saveAll(results);
        log.info("Reconciliation completed. Processed {} records.", results.size());

        return report;
    }

    private ReconciliationResult matchTransaction(ReconciliationReport report, String gatewayTxId, String amountStr, String status) {
        ReconciliationResult result = new ReconciliationResult();
        result.setId(UUID.randomUUID());
        result.setReport(report);
        result.setGatewayTxId(gatewayTxId);
        result.setProcessedAt(Instant.now());

        Optional<PaymentTransaction> txOpt = paymentTransactionRepository.findByGatewayTxId(gatewayTxId);

        if (txOpt.isPresent()) {
            PaymentTransaction tx = txOpt.get();
            result.setMatchedTransaction(tx);

            // Check for mismatches
            boolean amountMatch = tx.getAmount().toString().equals(amountStr); // Simple string comparison for now
            boolean statusMatch = tx.getStatus().equalsIgnoreCase(status) || 
                                  (tx.getStatus().equals("SUCCESS") && status.equalsIgnoreCase("Completed"));

            if (amountMatch && statusMatch) {
                result.setStatus("MATCHED");
            } else {
                result.setStatus("MISMATCH");
                Map<String, String> details = new HashMap<>();
                if (!amountMatch) details.put("amount_mismatch", "System: " + tx.getAmount() + ", Gateway: " + amountStr);
                if (!statusMatch) details.put("status_mismatch", "System: " + tx.getStatus() + ", Gateway: " + status);
                try {
                    result.setDetails(objectMapper.writeValueAsString(details));
                } catch (JsonProcessingException ignored) {}
            }
        } else {
            result.setStatus("NOT_FOUND");
            try {
                result.setDetails(objectMapper.writeValueAsString(Map.of("error", "Transaction not found in system")));
            } catch (JsonProcessingException ignored) {}
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<ReconciliationReport> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public List<ReconciliationResult> getReportResults(UUID reportId) {
        ReconciliationReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ValidationException("Report not found"));
        return resultRepository.findByReport(report);
    }
}
