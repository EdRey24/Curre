package edu.bu.cs411.group10.curre.safety;

import edu.bu.cs411.group10.curre.user.User;
import edu.bu.cs411.group10.curre.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/safety")
@CrossOrigin
public class SafetyController {

    private final SafetyService safetyService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public SafetyController(SafetyService safetyService, UserRepository userRepository, NotificationService notificationService) {
        this.safetyService = safetyService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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

    @PostMapping("/test-notification")
    public ResponseEntity<Void> sendTestNotification(@RequestHeader("X-User-Id") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        notificationService.sendTestNotification(user.getEmail(), null);
        return ResponseEntity.ok().build();
    }
} // END OF CLASS SafetyController