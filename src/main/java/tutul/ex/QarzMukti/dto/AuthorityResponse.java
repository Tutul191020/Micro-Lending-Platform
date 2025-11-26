package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityResponse {
    private Integer id;
    private String name;
    private String description;
    private String resource;
    private String action;
    private OffsetDateTime createdAt;
}