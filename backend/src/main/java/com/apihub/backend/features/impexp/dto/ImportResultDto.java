package com.apihub.backend.features.impexp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResultDto {
    private Long apiId;
    private String apiName;
    private String slug;
    private Long versionId;
    private String versionLabel;
    private int endpointsImported;
    private String message;
}
