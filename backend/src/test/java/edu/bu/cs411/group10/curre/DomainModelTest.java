package edu.bu.cs411.group10.curre;

import edu.bu.cs411.group10.curre.contact.EmergencyContact;
import edu.bu.cs411.group10.curre.contact.EmergencyContactDTO;
import edu.bu.cs411.group10.curre.run.RoutePoint;
import edu.bu.cs411.group10.curre.run.Run;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DomainModelTest {

    @Test
    public void testEmergencyContactGettersAndSetters() {
        EmergencyContact contact = new EmergencyContact();
        contact.setId(1L);
        contact.setName("Test");
        contact.setEmail("test@test.com");
        contact.setPhone("1234567890");

        assertEquals(1L, contact.getId());
        assertEquals("Test", contact.getName());
        assertEquals("test@test.com", contact.getEmail());
        assertEquals("1234567890", contact.getPhone());
    }

    @Test
    public void testEmergencyContactDTO() {
        EmergencyContactDTO dto = new EmergencyContactDTO();
        dto.setId(2L);
        dto.setName("DTO Name");
        dto.setEmail("dto@test.com");
        dto.setPhone("0987654321");

        assertEquals(2L, dto.getId());
        assertEquals("DTO Name", dto.getName());
        assertEquals("dto@test.com", dto.getEmail());
        assertEquals("0987654321", dto.getPhone());
    }

    @Test
    public void testRoutePoint() {
        RoutePoint point = new RoutePoint();
        point.setId(5L);
        point.setLatitude(42.3601);
        point.setLongitude(-71.0589);
        point.setTimestampMillis(123456789L);

        Run parentRun = new Run();
        point.setRun(parentRun);

        assertEquals(5L, point.getId());
        assertEquals(42.3601, point.getLatitude());
        assertEquals(-71.0589, point.getLongitude());
        assertEquals(123456789L, point.getTimestampMillis());
        assertEquals(parentRun, point.getRun());
    }

    @Test
    public void testRunAdditionalCoverage() {
        // Captures any Run getters/setters not hit in RunTest.java
        Run run = new Run();
        run.setStartedAt(100L);
        run.setEndedAt(200L);
        run.setAvgPaceSecsPerMile(500.0);
        run.setCalories(300);
        run.setUserId(99L);

        RoutePoint dummyPoint = new RoutePoint();
        run.setRoutePoints(Collections.singletonList(dummyPoint));

        assertEquals(100L, run.getStartedAt());
        assertEquals(200L, run.getEndedAt());
        assertEquals(500.0, run.getAvgPaceSecsPerMile());
        assertEquals(300, run.getCalories());
        assertEquals(99L, run.getUserId());
        assertEquals(1, run.getRoutePoints().size());
    }
}