package edu.bu.cs411.group10.curre.safety;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/safety")
@CrossOrigin
public class SafetyController {

    private final SafetyService safetyService;

    public SafetyController(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    @PostMapping("/start")
    public ResponseEntity<Void> startSafety(@RequestHeader("X-User-Id") Long userId,
                                            @RequestBody Map<String, Object> payload) {
        Long runId = Long.valueOf(payload.get("runId").toString());
        Integer intervalSeconds = payload.containsKey("intervalSeconds") ?
                Integer.valueOf(payload.get("intervalSeconds").toString()) : 900; // default 15 min
        safetyService.startSafetyMonitoring(runId, userId, intervalSeconds);
        return ResponseEntity.ok().build();
    } // END OF METHOD startSafety

    @PostMapping("/checkin/{runId}")
    public ResponseEntity<Void> checkIn(@PathVariable Long runId,
                                        @RequestHeader("X-User-Id") Long userId) {
        safetyService.checkIn(runId, userId);
        return ResponseEntity.ok().build();
    } // END OF METHOD checkIn

    @PostMapping("/stop/{runId}")
    public ResponseEntity<Void> stopSafety(@PathVariable Long runId,
                                           @RequestHeader("X-User-Id") Long userId) {
        safetyService.stopSafetyMonitoring(runId, userId);
        return ResponseEntity.ok().build();
    } // END OF METHOD stopSafety
} // END OF CLASS SafetyController