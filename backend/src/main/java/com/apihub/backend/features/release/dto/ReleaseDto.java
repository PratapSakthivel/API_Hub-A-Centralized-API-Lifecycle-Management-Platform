package com.apihub.backend.features.release.dto;

import com.apihub.backend.features.release.domain.ReleaseStage;

import java.time.LocalDateTime;

public class ReleaseDto {
    private Long id;
    private Long apiId;
    private String apiName;
    private Long versionId;
    private String versionLabel;
    private ReleaseStage stage;
    private String approvedBy;
    private String notes;
    private LocalDateTime promotedAt;
    private LocalDateTime createdAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApiId() { return apiId; }
    public void setApiId(Long apiId) { this.apiId = apiId; }

    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }

    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }

    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String versionLabel) { this.versionLabel = versionLabel; }

    public ReleaseStage getStage() { return stage; }
    public void setStage(ReleaseStage stage) { this.stage = stage; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getPromotedAt() { return promotedAt; }
    public void setPromotedAt(LocalDateTime promotedAt) { this.promotedAt = promotedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
