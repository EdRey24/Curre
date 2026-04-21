package edu.bu.cs411.group10.curre.safety;

import edu.bu.cs411.group10.curre.contact.EmergencyContact;
import edu.bu.cs411.group10.curre.contact.EmergencyContactRepository;
import edu.bu.cs411.group10.curre.run.Run;
import edu.bu.cs411.group10.curre.run.RunRepository;
import edu.bu.cs411.group10.curre.user.User;
import edu.bu.cs411.group10.curre.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class SafetyService {

    private static final Logger log = LoggerFactory.getLogger(SafetyService.class);
    private final SafetySessionRepository sessionRepository;
    private final RunRepository runRepository;
    private final UserRepository userRepository;
    private final EmergencyContactRepository contactRepository;
    private final NotificationService notificationService;

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SafetyService(SafetySessionRepository sessionRepository,
                         RunRepository runRepository,
                         UserRepository userRepository,
                         EmergencyContactRepository contactRepository,
                         NotificationService notificationService) {
        this.sessionRepository = sessionRepository;
        this.runRepository = runRepository;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.notificationService = notificationService;
    }

    private User getOrCreateUser(Long userId) {
        String email = "user" + userId + "@curre.com";
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    // Do NOT set ID; let database auto‑generate
                    newUser.setEmail(email);
                    newUser.setPassword("default");
                    User saved = userRepository.save(newUser);
                    log.info("SafetyService: Auto‑created user with email {} and ID {}", email, saved.getId()); // DEBUG
                    return saved;
                });
    } // END OF METHOD getOrCreateUser

    @Transactional
    public void startSafetyMonitoring(Long runId, Long userId, Integer checkInIntervalSeconds, Double lat, Double lng) {
        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("Run not found with id: " + runId));

        List<EmergencyContact> contacts = contactRepository.findByUserId(userId);
        if (contacts.isEmpty()) {
            throw new IllegalStateException("Cannot enable safety: no emergency contacts added");
        }

        sessionRepository.findByRunIdAndActiveTrue(runId).ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
            cancelScheduledTask(runId);
        });

        SafetySession session = new SafetySession();
        session.setRunId(runId);
        session.setUserId(userId);
        session.setCheckInIntervalSeconds(checkInIntervalSeconds);
        session.setLastCheckIn(Instant.now());
        session.setLastLat(lat);
        session.setLastLng(lng);
        session.setAlertCount(0);
        session.setActive(true);
        sessionRepository.save(session);

        scheduleOverdueCheck(runId, userId, checkInIntervalSeconds);

        User user = getOrCreateUser(userId);
        notificationService.sendRunStartedNotification(user.getEmail(), contacts, lat, lng);
        log.info("Started safety monitoring for run {} user {}", runId, userId); // DEBUG
    } // END OF METHOD startSafetyMonitoring

    @Transactional
    public void checkIn(Long runId, Long userId) {
        SafetySession session = sessionRepository.findByRunIdAndActiveTrue(runId)
                .orElseThrow(() -> new EntityNotFoundException("No active safety session for run " + runId));
        if (!session.getUserId().equals(userId)) {
            throw new SecurityException("Run does not belong to this user");
        }
        session.setLastCheckIn(Instant.now());
        sessionRepository.save(session);

        cancelScheduledTask(runId);
        scheduleOverdueCheck(runId, userId, session.getCheckInIntervalSeconds());
        log.info("Check‑in received for run {} user {}", runId, userId); // DEBUG
    } // END OF METHOD checkIn

    @Transactional
    public void stopSafetyMonitoring(Long runId, Long userId) {
        SafetySession session = sessionRepository.findByRunIdAndActiveTrue(runId).orElse(null);
        if (session != null && session.getUserId().equals(userId)) {
            session.setActive(false);
            sessionRepository.save(session);
            cancelScheduledTask(runId);

            User user = getOrCreateUser(userId);
            List<EmergencyContact> contacts = contactRepository.findByUserId(userId);
            notificationService.sendRunEndedNotification(user.getEmail(), contacts);
            log.info("Stopped safety monitoring for run {} user {}", runId, userId); // DEBUG
        }
    } // END OF METHOD stopSafetyMonitoring

    private void scheduleOverdueCheck(Long runId, Long userId, int delaySeconds) {
        Runnable overdueTask = () -> {
            SafetySession session = sessionRepository.findByRunIdAndActiveTrue(runId).orElse(null);
            if (session == null || !session.isActive()) return;
            Instant now = Instant.now();
            if (now.isAfter(session.getLastCheckIn().plusSeconds(delaySeconds))) {
                User user = getOrCreateUser(userId);
                List<EmergencyContact> contacts = contactRepository.findByUserId(userId);
                Run run = runRepository.findById(runId).orElse(null);
                Double lastLat = null;
                Double lastLng = null;
                if(run != null && run.getRoutePoints() != null && !run.getRoutePoints().isEmpty()) {
                    var points = run.getRoutePoints();
                    var lastPoint = points.getLast();
                    lastLat = lastPoint.getLatitude();
                    lastLng = lastPoint.getLongitude();
                }
                notificationService.sendOverdueAlert(user.getEmail(), contacts, lastLat, lastLng);
                session.setActive(false);
                sessionRepository.save(session);
                cancelScheduledTask(runId);
                log.warn("Overdue alert sent for run {} user {}", runId, userId); // DEBUG
            }
        };
        ScheduledFuture<?> future = scheduler.schedule(overdueTask, delaySeconds, TimeUnit.SECONDS);
        scheduledTasks.put(runId, future);
    } // END OF METHOD scheduleOverdueCheck

    private void cancelScheduledTask(Long runId) {
        ScheduledFuture<?> future = scheduledTasks.remove(runId);
        if (future != null) {
            future.cancel(false);
        }
    } // END OF METHOD cancelScheduledTask
} // END OF CLASS SafetyService