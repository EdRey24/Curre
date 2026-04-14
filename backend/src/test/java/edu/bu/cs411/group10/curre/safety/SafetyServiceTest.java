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
} // END OF CLASS SafetyServiceTest