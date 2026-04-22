package edu.bu.cs411.group10.curre.safety;

import edu.bu.cs411.group10.curre.contact.EmergencyContact;
import edu.bu.cs411.group10.curre.contact.EmergencyContactRepository;
import edu.bu.cs411.group10.curre.user.User;
import edu.bu.cs411.group10.curre.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/safety")
@CrossOrigin
public class SafetyController {

    private final SafetyService safetyService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmergencyContactRepository contactRepository;

    public SafetyController(SafetyService safetyService, UserRepository userRepository, NotificationService notificationService, EmergencyContactRepository contactRepository) {
        this.safetyService = safetyService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.contactRepository = contactRepository;
    }

    @PostMapping("/start")
    public ResponseEntity<Void> startSafety(@RequestHeader("X-User-Id") Long userId,
                                            @RequestBody Map<String, Object> payload) {
        Long runId = Long.valueOf(payload.get("runId").toString());
        Integer intervalSeconds = payload.containsKey("intervalSeconds") ?
                Integer.parseInt(payload.get("intervalSeconds").toString()) : 900; // default 15 min
        Double lat = payload.containsKey("lat") && payload.get("lat") != null ?
                Double.valueOf(payload.get("lat").toString()) : null;
        Double lng = payload.containsKey("lng") && payload.get("lng") != null ?
                Double.valueOf(payload.get("lng").toString()) : null;
        safetyService.startSafetyMonitoring(runId, userId, intervalSeconds, lat, lng);
        return ResponseEntity.ok().build();
    } // END OF METHOD startSafety

    @PostMapping("/checkin/{runId}")
    public ResponseEntity<Void> checkIn(@PathVariable Long runId,
                                        @RequestHeader("X-User-Id") Long userId,
                                        @RequestBody(required = false) Map<String, Object> payload) {
        Double lat = null;
        Double lng = null;
        if (payload != null) {
            lat = payload.containsKey("lat") && payload.get("lat") != null ? Double.valueOf(payload.get("lat").toString()) : null;
            lng = payload.containsKey("lng") && payload.get("lng") != null ? Double.valueOf(payload.get("lng").toString()) : null;
        }
        safetyService.checkIn(runId, userId, lat, lng);
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