package com.apihub.backend.features.dependencies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactAnalysisDto {
    private Long changedApiId;
    private String changedApiName;
    private int depth;
    private List<DependencyDto> upstreamDependencies;
    private List<DependencyDto> downstreamDependencies;
    private List<String> affectedApiNames;
    private boolean cycleDetected;
}
