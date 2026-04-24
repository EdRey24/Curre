package edu.bu.cs411.group10.curre.safety;

import edu.bu.cs411.group10.curre.contact.EmergencyContact;
import edu.bu.cs411.group10.curre.contact.EmergencyContactRepository;
import edu.bu.cs411.group10.curre.run.Run;
import edu.bu.cs411.group10.curre.run.RunRepository;
import edu.bu.cs411.group10.curre.user.User;
import edu.bu.cs411.group10.curre.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * The system has exactly one valid user: Demo with password Password1.
 * This is reflected in the test data below (user email "demo@curre.com").
 */
public class SafetyServiceTest {

    @Mock
    private SafetySessionRepository sessionRepository;
    @Mock
    private RunRepository runRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmergencyContactRepository contactRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SafetyService safetyService;

    private User testUser;
    private Run testRun;
    private EmergencyContact testContact;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("demo@curre.com"); // only valid user

        testRun = new Run();
        testRun.setId(100L);
        testRun.setUserId(1L);

        testContact = new EmergencyContact();
        testContact.setId(10L);
        testContact.setEmail("contact@example.com");
        testContact.setUser(testUser);

        lenient().when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(testUser));
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            if (savedUser.getId() == null) {
                savedUser.setId(1L); // Give it a fake DB ID
            }
            return savedUser;
        });
    }

    @Test
    public void testStartSafetyMonitoringSendsStartNotification() {
        System.out.println("Starting test: Start safety monitoring sends 'run started' notification");
        when(runRepository.findById(100L)).thenReturn(Optional.of(testRun));
        when(contactRepository.findByUserId(1L)).thenReturn(List.of(testContact));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sessionRepository.save(any(SafetySession.class))).thenAnswer(inv -> inv.getArgument(0));

        safetyService.startSafetyMonitoring(100L, 1L, 900);

        verify(notificationService, times(1))
                .sendRunStartedNotification(eq("demo@curre.com"), anyList(), isNull(), isNull());
        verify(notificationService, never())
                .sendOverdueAlert(any(), anyList(), isNull(), isNull());
        System.out.println("PASSED: Start safety monitoring sends 'run started' notification");
    }

    @Test
    public void testCheckInUpdatesLastCheckIn() {
        System.out.println("Starting test: Check‑in updates the last check‑in timestamp");
        SafetySession session = new SafetySession();
        session.setRunId(100L);
        session.setUserId(1L);
        session.setCheckInIntervalSeconds(900);
        session.setLastCheckIn(Instant.now().minusSeconds(100));
        session.setActive(true);

        when(sessionRepository.findByRunIdAndActiveTrue(100L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(SafetySession.class))).thenAnswer(inv -> inv.getArgument(0));

        safetyService.checkIn(100L, 1L);

        verify(sessionRepository).save(session);
        assertTrue(session.getLastCheckIn().isAfter(Instant.now().minusSeconds(5)));
        System.out.println("PASSED: Check‑in updates the last check‑in timestamp");
    }

    @Test
    public void testStartSafetyNoContacts() {
        System.out.println("Starting test: Start safety fails when no emergency contacts exist");
        when(runRepository.findById(100L)).thenReturn(Optional.of(testRun));
        when(contactRepository.findByUserId(1L)).thenReturn(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> safetyService.startSafetyMonitoring(100L, 1L, 900));
        assertEquals("Cannot enable safety: no emergency contacts added", ex.getMessage());
        verify(sessionRepository, never()).save(any());
        System.out.println("PASSED: Start safety fails when no emergency contacts exist");
    }

    @Test
    public void testStartSafetyRunNotFound() {
        System.out.println("Starting test: Start safety fails when run does not exist");
        when(runRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> safetyService.startSafetyMonitoring(999L, 1L, 900));
        verify(sessionRepository, never()).save(any());
        System.out.println("PASSED: Start safety fails when run does not exist");
    }

    @Test
    public void testStopSafetyMonitoring() {
        System.out.println("Starting test: Stop safety monitoring sends 'run ended' notification and deactivates session");
        SafetySession session = new SafetySession();
        session.setRunId(100L);
        session.setUserId(1L);
        session.setActive(true);
        when(sessionRepository.findByRunIdAndActiveTrue(100L)).thenReturn(Optional.of(session));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(contactRepository.findByUserId(1L)).thenReturn(List.of(testContact));

        safetyService.stopSafetyMonitoring(100L, 1L);

        verify(sessionRepository).save(session);
        assertFalse(session.isActive());
        verify(notificationService).sendRunEndedNotification(eq("demo@curre.com"), anyList());
        System.out.println("PASSED: Stop safety monitoring sends 'run ended' notification and deactivates session");
    }

    @Test
    public void testCheckInOnNonExistentSession() {
        System.out.println("Starting test: Check‑in on non‑existent active session throws exception");
        when(sessionRepository.findByRunIdAndActiveTrue(200L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> safetyService.checkIn(200L, 1L));
        System.out.println("PASSED: Check‑in on non‑existent active session throws exception");
    }

    @Test
    public void testStartSafetyMonitoringWithExistingActiveSession() {
        System.out.println("Starting test: Start safety deactivates existing active session");
        when(runRepository.findById(100L)).thenReturn(Optional.of(testRun));
        when(contactRepository.findByUserId(1L)).thenReturn(List.of(testContact));

        SafetySession existingSession = new SafetySession();
        existingSession.setRunId(100L);
        existingSession.setActive(true);
        when(sessionRepository.findByRunIdAndActiveTrue(100L)).thenReturn(Optional.of(existingSession));

        safetyService.startSafetyMonitoring(100L, 1L, 900);

        // Verifies the ifPresent lambda executed to deactivate the old session
        assertFalse(existingSession.isActive());
        verify(sessionRepository, atLeastOnce()).save(existingSession);
        System.out.println("PASSED: Start safety deactivates existing active session");
    }

    @Test
    public void testCheckInWrongUser() {
        System.out.println("Starting test: Check-in with wrong user ID throws SecurityException");
        SafetySession session = new SafetySession();
        session.setRunId(100L);
        session.setUserId(2L); // A different user
        session.setActive(true);

        when(sessionRepository.findByRunIdAndActiveTrue(100L)).thenReturn(Optional.of(session));

        SecurityException ex = assertThrows(SecurityException.class, () -> safetyService.checkIn(100L, 1L));
        assertEquals("Run does not belong to this user", ex.getMessage());
        System.out.println("PASSED: Check-in with wrong user ID throws SecurityException");
    }

    @Test
    public void testStopSafetyMonitoringNoActiveSession() {
        System.out.println("Starting test: Stop safety with no active session ignores gracefully");
        when(sessionRepository.findByRunIdAndActiveTrue(100L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> safetyService.stopSafetyMonitoring(100L, 1L));
        verify(notificationService, never()).sendRunEndedNotification(anyString(), anyList());
        System.out.println("PASSED: Stop safety with no active session ignores gracefully");
    }

    @Test
    public void testStopSafetyMonitoringWrongUser() {
        System.out.println("Starting test: Stop safety with wrong user ID ignores gracefully");
        SafetySession session = new SafetySession();
        session.setRunId(100L);
        session.setUserId(2L); // Different user
        session.setActive(true);

        when(sessionRepository.findByRunIdAndActiveTrue(100L)).thenReturn(Optional.of(session));

        safetyService.stopSafetyMonitoring(100L, 1L);

        assertTrue(session.isActive()); // Session should remain active
        verify(notificationService, never()).sendRunEndedNotification(anyString(), anyList());
        System.out.println("PASSED: Stop safety with wrong user ID ignores gracefully");
    }

    @Test
    public void testGetOrCreateUserAutoCreatesUser() {
        System.out.println("Starting test: Auto-create user when user does not exist");
        // Override the lenient setup to return empty, triggering the orElseGet branch
        when(userRepository.findByEmail("user99@curre.com")).thenReturn(Optional.empty());

        SafetySession session = new SafetySession();
        session.setRunId(100L);
        session.setUserId(99L);
        session.setActive(true);
        when(sessionRepository.findByRunIdAndActiveTrue(100L)).thenReturn(Optional.of(session));
        when(contactRepository.findByUserId(99L)).thenReturn(List.of(testContact));

        // Calling stop triggers getOrCreateUser(99L) under the hood
        safetyService.stopSafetyMonitoring(100L, 99L);

        // Verify the lambda logic executed and saved a new user
        verify(userRepository).save(argThat(u ->
                "user99@curre.com".equals(u.getEmail()) && "default".equals(u.getPassword())
        ));
        System.out.println("PASSED: Auto-create user when user does not exist");
    }

    @Test
    public void testOverdueCheckAlertTriggered() throws InterruptedException {
        System.out.println("Starting test: Overdue check triggers alert");
        when(runRepository.findById(100L)).thenReturn(Optional.of(testRun));
        when(contactRepository.findByUserId(1L)).thenReturn(List.of(testContact));

        // Ensure the user repository mock is set up so getOrCreateUser doesn't fail in the background thread
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Create a session heavily in the past so the delay logic evaluates to true
        SafetySession overdueSession = new SafetySession();
        overdueSession.setRunId(100L);
        overdueSession.setUserId(1L);
        overdueSession.setActive(true);
        overdueSession.setLastCheckIn(Instant.now().minusSeconds(50));

        // THE FIX: Chain the return values!
        when(sessionRepository.findByRunIdAndActiveTrue(100L))
                .thenReturn(Optional.empty())               // 1st call: startSafetyMonitoring checks for existing session
                .thenReturn(Optional.of(overdueSession));   // 2nd call: scheduled background task evaluates the session

        // Trigger with a 0 second delay so the scheduled task executes immediately
        safetyService.startSafetyMonitoring(100L, 1L, 0);

        // Allow the SingleThreadScheduledExecutor a brief moment to process the Runnable
        Thread.sleep(100);

        verify(notificationService, atLeastOnce()).sendOverdueAlert(eq("demo@curre.com"), anyList(), isNull(), isNull());
        assertFalse(overdueSession.isActive());
        System.out.println("PASSED: Overdue check triggers alert");
    }

    @Test
    public void testOverdueCheckNotTriggeredWhenInactive() throws InterruptedException {
        System.out.println("Starting test: Overdue check ignores inactive sessions");
        when(runRepository.findById(100L)).thenReturn(Optional.of(testRun));
        when(contactRepository.findByUserId(1L)).thenReturn(List.of(testContact));

        SafetySession inactiveSession = new SafetySession();
        inactiveSession.setActive(false); // Covers the `if (session == null || !session.isActive()) return;` branch

        when(sessionRepository.findByRunIdAndActiveTrue(100L)).thenReturn(Optional.of(inactiveSession));

        safetyService.startSafetyMonitoring(100L, 1L, 0);
        Thread.sleep(100);

        verify(notificationService, never()).sendOverdueAlert(anyString(), anyList(), any(), any());
        System.out.println("PASSED: Overdue check ignores inactive sessions");
    }
} // END OF CLASS SafetyServiceTest