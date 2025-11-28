package tutul.ex.QarzMukti.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Standardized API Response wrapper.
 * Supports both simple and paginated responses with metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private Object errors;
    private MetaData metaData;

    /**
     * Metadata for API responses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetaData {
        private String requestId;
        private String transactionId;
        private String eventTime;
        private Boolean status;
    }

    /**
     * Paginated response wrapper.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageResponse<T> {
        private java.util.List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean empty;
    }

    // ============ Simple Success Response ============

    /**
     * Create a simple success response.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metaData(createDefaultMetaData())
                .build();
    }

    // ============ Paginated Success Response ============

    /**
     * Create a paginated success response.
     * Wraps Spring Page into a clean PageResponse.
     */
    public static <T> ApiResponse<PageResponse<T>> successPage(Page<T> page, String message) {
        PageResponse<T> pageResponse = PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();

        return ApiResponse.<PageResponse<T>>builder()
                .success(true)
                .message(message)
                .data(pageResponse)
                .metaData(createDefaultMetaData())
                .build();
    }

    /**
     * Create a paginated success response with custom metadata.
     */
    public static <T> ApiResponse<PageResponse<T>> successPage(Page<T> page, String message, MetaData metaData) {
        PageResponse<T> pageResponse = PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();

        return ApiResponse.<PageResponse<T>>builder()
                .success(true)
                .message(message)
                .data(pageResponse)
                .metaData(metaData != null ? metaData : createDefaultMetaData())
                .build();
    }

    // ============ Error Response ============

    /**
     * Create an error response.
     */
    public static <T> ApiResponse<T> error(String message, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .metaData(createDefaultMetaData())
                .build();
    }

    /**
     * Create an error response without error details.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .metaData(createDefaultMetaData())
                .build();
    }

    // ============ Helper Methods ============

    /**
     * Create default metadata.
     */
    private static MetaData createDefaultMetaData() {
        return MetaData.builder()
                .requestId(UUID.randomUUID().toString())
                .transactionId(UUID.randomUUID().toString())
                .eventTime(LocalDateTime.now().toString())
                .status(true)
                .build();
    }

    /**
     * Create metadata with custom request ID.
     */
    public static MetaData createMetaData(String requestId) {
        return MetaData.builder()
                .requestId(requestId != null ? requestId : "No req Id, permit for all.")
                .transactionId(UUID.randomUUID().toString())
                .eventTime(LocalDateTime.now().toString())
                .status(true)
                .build();
    }

    // ============ Legacy Support (Optional) ============

    /**
     * Create response with custom body structure (legacy support).
     */
    public static ApiResponse<Map<String, Object>> successWithCustomBody(String key, Object value, String message) {
        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put(key, value);
        
        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message(message)
                .data(responseData)
                .metaData(createDefaultMetaData())
                .build();
    }
}