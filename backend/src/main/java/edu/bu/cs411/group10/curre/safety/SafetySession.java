package edu.bu.cs411.group10.curre.safety;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "safety_sessions")
public class SafetySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long runId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer checkInIntervalSeconds; // e.g., 900 (15 min)

    @Column(nullable = false)
    private Instant lastCheckIn;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_lat")
    private Double lastLat;

    @Column(name = "last_lng")
    private Double lastLng;

    @Column(name = "alert_count")
    private Integer alertCount = 0;

    public SafetySession() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRunId() { return runId; }
    public void setRunId(Long runId) { this.runId = runId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getCheckInIntervalSeconds() { return checkInIntervalSeconds; }
    public void setCheckInIntervalSeconds(Integer checkInIntervalSeconds) { this.checkInIntervalSeconds = checkInIntervalSeconds; }

    public Instant getLastCheckIn() { return lastCheckIn; }
    public void setLastCheckIn(Instant lastCheckIn) { this.lastCheckIn = lastCheckIn; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Double getLastLat() { return lastLat; }
    public void setLastLat(Double lastLat) { this.lastLat = lastLat; }

    public Double getLastLng() { return lastLng; }
    public void setLastLng(Double lastLng) { this.lastLng = lastLng; }

    public Integer getAlertCount() { return alertCount; }
    public void setAlertCount(Integer alertCount) { this.alertCount = alertCount; }
} // END OF CLASS SafetySession