package com.apihub.backend.features.versioning.dto;

import com.apihub.backend.features.documentation.dto.EndpointDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionComparisonDto {
    private Long fromVersionId;
    private String fromVersionLabel;
    private Long toVersionId;
    private String toVersionLabel;
    private List<EndpointDto> addedEndpoints;
    private List<EndpointDto> removedEndpoints;
    private List<EndpointDto> modifiedEndpoints;
}
