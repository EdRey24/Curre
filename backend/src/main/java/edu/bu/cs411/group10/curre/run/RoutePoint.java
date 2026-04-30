package edu.bu.cs411.group10.curre.run;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "route_points")
public class RoutePoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    @JsonBackReference
    private Run run;

    @Column(name = "segment_index", nullable = false)
    private Integer segmentIndex = 0;

    private Double latitude;
    private Double longitude;
    private Long timestampMillis;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public Run getRun() {return run;}
    public void setRun(Run run) {this.run = run;}
    public Double getLatitude() {return latitude;}
    public void setLatitude(Double latitude) {this.latitude = latitude;}
    public Double getLongitude() {return longitude;}
    public void setLongitude(Double longitude) {this.longitude = longitude;}
    public Long getTimestampMillis(){return timestampMillis;}
    public void setTimestampMillis(Long timestampMillis) {this.timestampMillis = timestampMillis;}

    public Integer getSegmentIndex() { return segmentIndex; }
    public void setSegmentIndex(Integer segmentIndex) { this.segmentIndex = segmentIndex; }
}
