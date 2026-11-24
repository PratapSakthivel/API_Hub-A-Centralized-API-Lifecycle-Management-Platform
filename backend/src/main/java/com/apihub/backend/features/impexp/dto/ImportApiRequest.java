package com.apihub.backend.features.impexp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportApiRequest {
    private Long projectId;
    private String slug;
    private String specContent;
}
