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
import java.util.Optional;
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
        // First try to find by ID (for real authenticated users)
        Optional<User> byId = userRepository.findById(userId);
        if (byId.isPresent()) {
            return byId.get();
        }
        // Fallback: look up by email pattern (for legacy/test users)
        String email = "user" + userId + "@curre.com";
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setFirstName("Test");
                    newUser.setLastName("User");
                    newUser.setEmail(email);
                    newUser.setPassword("default");
                    User saved = userRepository.save(newUser);
                    log.info("SafetyService: Auto‑created user with email {} and ID {}", email, saved.getId()); // DEBUG
                    return saved;
                });
    } // END OF METHOD getOrCreateUser

    @Transactional
    public void startSafetyMonitoring(Long runId, Long userId, Integer checkInIntervalSeconds) {
        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("Run not found with id: " + runId));

        User user = getOrCreateUser(userId);
        List<EmergencyContact> contacts = contactRepository.findByUserId(user.getId());
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
        session.setUserId(user.getId());
        session.setCheckInIntervalSeconds(checkInIntervalSeconds);
        session.setLastCheckIn(Instant.now());
        session.setActive(true);
        sessionRepository.save(session);

        scheduleOverdueCheck(runId, user.getId(), checkInIntervalSeconds);

        List<String> contactEmails = contacts.stream().map(EmergencyContact::getEmail).collect(Collectors.toList());
        notificationService.sendRunStartedNotification(user.getEmail(), contactEmails, null, null);
        log.info("Started safety monitoring for run {} user {}", runId, user.getId()); // DEBUG
    } // END OF METHOD startSafetyMonitoring

    @Transactional
    public void checkIn(Long runId, Long userId) {
        User user = getOrCreateUser(userId);
        SafetySession session = sessionRepository.findByRunIdAndActiveTrue(runId)
                .orElseThrow(() -> new EntityNotFoundException("No active safety session for run " + runId));
        if (!session.getUserId().equals(user.getId())) {
            throw new SecurityException("Run does not belong to this user");
        }
        session.setLastCheckIn(Instant.now());
        sessionRepository.save(session);

        cancelScheduledTask(runId);
        scheduleOverdueCheck(runId, user.getId(), session.getCheckInIntervalSeconds());
        log.info("Check‑in received for run {} user {}", runId, user.getId()); // DEBUG
    } // END OF METHOD checkIn

    @Transactional
    public void stopSafetyMonitoring(Long runId, Long userId) {
        User user = getOrCreateUser(userId);
        SafetySession session = sessionRepository.findByRunIdAndActiveTrue(runId).orElse(null);
        if (session != null && session.getUserId().equals(user.getId())) {
            session.setActive(false);
            sessionRepository.save(session);
            cancelScheduledTask(runId);

            List<EmergencyContact> contacts = contactRepository.findByUserId(user.getId());
            List<String> contactEmails = contacts.stream().map(EmergencyContact::getEmail).collect(Collectors.toList());
            notificationService.sendRunEndedNotification(user.getEmail(), contactEmails);
            log.info("Stopped safety monitoring for run {} user {}", runId, user.getId()); // DEBUG
        }
    } // END OF METHOD stopSafetyMonitoring

    private void scheduleOverdueCheck(Long runId, Long userId, int delaySeconds) {
        Runnable overdueTask = () -> {
            SafetySession session = sessionRepository.findByRunIdAndActiveTrue(runId).orElse(null);
            if (session == null || !session.isActive()) return;
            Instant now = Instant.now();
            if (now.isAfter(session.getLastCheckIn().plusSeconds(delaySeconds))) {
                User user = getOrCreateUser(userId);
                List<EmergencyContact> contacts = contactRepository.findByUserId(user.getId());
                List<String> contactEmails = contacts.stream().map(EmergencyContact::getEmail).collect(Collectors.toList());
                notificationService.sendOverdueAlert(user.getEmail(), contactEmails, null, null);
                if (session != null) {
                    session.setActive(false);
                    sessionRepository.save(session);
                }
                cancelScheduledTask(runId);
                log.warn("Overdue alert sent for run {} user {}", runId, user.getId()); // DEBUG
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