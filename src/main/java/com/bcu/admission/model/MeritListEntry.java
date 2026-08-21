package com.bcu.admission.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "merit_list_entries")
public class MeritListEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meritListEntryId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "admission_id")
    private Admission admission;

    private String courseName;
    private Integer rankPosition;
    private Boolean selected;
    private LocalDateTime releasedAt;

    public Long getMeritListEntryId() {
        return meritListEntryId;
    }

    public void setMeritListEntryId(Long meritListEntryId) {
        this.meritListEntryId = meritListEntryId;
    }

    public Admission getAdmission() {
        return admission;
    }

    public void setAdmission(Admission admission) {
        this.admission = admission;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }
}
