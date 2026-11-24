package com.apihub.backend.features.release.dto;

import com.apihub.backend.features.release.domain.ReleaseStage;

public class PromoteReleaseRequest {
    private ReleaseStage targetStage;
    private String approvedBy;
    private String notes;

    public ReleaseStage getTargetStage() { return targetStage; }
    public void setTargetStage(ReleaseStage targetStage) { this.targetStage = targetStage; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
