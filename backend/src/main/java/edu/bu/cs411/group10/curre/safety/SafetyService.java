package edu.bu.cs411.group10.curre.safety;

import edu.bu.cs411.group10.curre.contact.EmergencyContact;
import edu.bu.cs411.group10.curre.contact.EmergencyContactRepository;
import edu.bu.cs411.group10.curre.run.Run;
import edu.bu.cs411.group10.curre.run.RunRepository;
import edu.bu.cs411.group10.curre.user.User;
import edu.bu.cs411.group10.curre.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class SafetyService {

    private final SafetySessionRepository sessionRepository;
    private final RunRepository runRepository;
    private final UserRepository userRepository;
    private final EmergencyContactRepository contactRepository;
    private final NotificationService notificationService;

    // In-memory map to store scheduled futures (runId -> ScheduledFuture)
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

    @Transactional
    public void startSafetyMonitoring(Long runId, Long userId, Integer checkInIntervalSeconds) {
        // Verify run exists and belongs to user
        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("Run not found with id: " + runId));
        // For simplicity, assume run has userId column. We'll add that later.
        // In this demo, we skip run ownership check or assume it's passed.

        // Check if user has at least one emergency contact
        List<EmergencyContact> contacts = contactRepository.findByUserId(userId);
        if (contacts.isEmpty()) {
            throw new IllegalStateException("Cannot enable safety: no emergency contacts added");
        }

        // Deactivate any existing active session for this run
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
        session.setActive(true);
        sessionRepository.save(session);

        // Schedule overdue task
        scheduleOverdueCheck(runId, userId, checkInIntervalSeconds);

        // Send "run started" notification
        User user = userRepository.findById(userId).orElseThrow();
        List<String> contactEmails = contacts.stream().map(EmergencyContact::getEmail).collect(Collectors.toList());
        // In real app we would get last known location from run's route points; use null for mock
        notificationService.sendRunStartedNotification(user.getEmail(), contactEmails, null, null);
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

        // Cancel old scheduled task and reschedule
        cancelScheduledTask(runId);
        scheduleOverdueCheck(runId, userId, session.getCheckInIntervalSeconds());
    } // END OF METHOD checkIn

    @Transactional
    public void stopSafetyMonitoring(Long runId, Long userId) {
        SafetySession session = sessionRepository.findByRunIdAndActiveTrue(runId)
                .orElse(null);
        if (session != null && session.getUserId().equals(userId)) {
            session.setActive(false);
            sessionRepository.save(session);
            cancelScheduledTask(runId);

            // Send "run ended" notification
            User user = userRepository.findById(userId).orElseThrow();
            List<EmergencyContact> contacts = contactRepository.findByUserId(userId);
            List<String> contactEmails = contacts.stream().map(EmergencyContact::getEmail).collect(Collectors.toList());
            notificationService.sendRunEndedNotification(user.getEmail(), contactEmails);
        }
    } // END OF METHOD stopSafetyMonitoring

    private void scheduleOverdueCheck(Long runId, Long userId, int delaySeconds) {
        Runnable overdueTask = () -> {
            // Re-fetch session to ensure still active and check-in not refreshed
            SafetySession session = sessionRepository.findByRunIdAndActiveTrue(runId).orElse(null);
            if (session == null || !session.isActive()) return;
            Instant now = Instant.now();
            if (now.isAfter(session.getLastCheckIn().plusSeconds(delaySeconds))) {
                // Overdue: notify contacts
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    List<EmergencyContact> contacts = contactRepository.findByUserId(userId);
                    List<String> contactEmails = contacts.stream().map(EmergencyContact::getEmail).collect(Collectors.toList());
                    // Use last known location from run's route points if available; null for now
                    notificationService.sendOverdueAlert(user.getEmail(), contactEmails, null, null);
                }
                // Optionally deactivate session after alert to avoid repeated alerts
                if (session != null) {
                    session.setActive(false);
                    sessionRepository.save(session);
                }
                cancelScheduledTask(runId);
            } else {
                // Not overdue yet, reschedule? Actually the schedule is one-shot, so we rely on check-in to reschedule.
                // If this task runs early due to clock skew, we could reschedule, but for simplicity we do nothing.
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