package edu.bu.cs411.group10.curre.run;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;

@Entity
@Table(name = "runs")
public class Run {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private Long startedAt;

    @Column(name = "ended_at", nullable = false)
    private Long endedAt;

    @Column(name = "distance_miles", nullable = false)
    private Double distanceMiles;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "avg_pace_secs_per_mile", nullable = false)
    private Double avgPaceSecsPerMile;

    @Column(name = "calories", nullable = false)
    private Integer calories;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RoutePoint> routePoints;

    public Run(){}

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public Long getStartedAt(){
        return startedAt;
    }

    public void setStartedAt(Long startedAt){
        this.startedAt = startedAt;
    }

    public Long getEndedAt(){
        return endedAt;
    }

    public void setEndedAt(Long endedAt){
        this.endedAt = endedAt;
    }

    public Double getDistanceMiles(){
        return distanceMiles;
    }

    public void setDistanceMiles(Double distanceMiles){
        this.distanceMiles = distanceMiles;
    }

    public Integer getDurationSeconds(){
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds){
        this.durationSeconds = durationSeconds;
    }

    public Double getAvgPaceSecsPerMile(){
        return avgPaceSecsPerMile;
    }

    public void setAvgPaceSecsPerMile(Double avgPaceSecsPerMile){
        this.avgPaceSecsPerMile = avgPaceSecsPerMile;
    }

    public Integer getCalories(){
        return calories;
    }

    public void setCalories(Integer calories){
        this.calories = calories;
    }

    public List<RoutePoint> getRoutePoints() {return routePoints;}

    public void setRoutePoints(List<RoutePoint> routePoints){
        this.routePoints = routePoints;
        if(routePoints != null){
            for(RoutePoint point : routePoints){
                point.setRun(this);
            }
        }
    }
}
