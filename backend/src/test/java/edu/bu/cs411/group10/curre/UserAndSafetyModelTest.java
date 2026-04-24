package edu.bu.cs411.group10.curre;

import edu.bu.cs411.group10.curre.safety.SafetySession;
import edu.bu.cs411.group10.curre.user.User;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class UserAndSafetyModelTest {

    @Test
    public void testUserGettersAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@curre.com");
        user.setPassword("securePassword123");
        user.setEmergencyContacts(new ArrayList<>());

        assertEquals(1L, user.getId());
        assertEquals("test@curre.com", user.getEmail());
        assertEquals("securePassword123", user.getPassword());
        assertNotNull(user.getEmergencyContacts());
    }

    @Test
    public void testSafetySessionGettersAndSetters() {
        SafetySession session = new SafetySession();
        session.setId(5L);
        session.setRunId(10L);
        session.setUserId(20L);
        session.setCheckInIntervalSeconds(900);

        Instant now = Instant.now();
        session.setLastCheckIn(now);
        session.setActive(true);

        assertEquals(5L, session.getId());
        assertEquals(10L, session.getRunId());
        assertEquals(20L, session.getUserId());
        assertEquals(900, session.getCheckInIntervalSeconds());
        assertEquals(now, session.getLastCheckIn());
        assertTrue(session.isActive());
    }
}