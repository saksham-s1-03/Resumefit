package com.resumeai.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Analysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String fileName;
    private String jobTitle;
    private int matchScore;
    @Column(columnDefinition = "TEXT")
    private String resultJson;
    private Instant createdAt = Instant.now();

    public Analysis() {}
    public Analysis(Long userId, String fileName, String jobTitle, int matchScore, String resultJson) {
        this.userId = userId; this.fileName = fileName; this.jobTitle = jobTitle;
        this.matchScore = matchScore; this.resultJson = resultJson;
    }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getFileName() { return fileName; }
    public String getJobTitle() { return jobTitle; }
    public int getMatchScore() { return matchScore; }
    public String getResultJson() { return resultJson; }
    public Instant getCreatedAt() { return createdAt; }
}
