package com.apihub.backend.features.release.dto;

public class CreateReleaseRequest {
    private Long apiId;
    private Long versionId;
    private String notes;

    public Long getApiId() { return apiId; }
    public void setApiId(Long apiId) { this.apiId = apiId; }

    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
