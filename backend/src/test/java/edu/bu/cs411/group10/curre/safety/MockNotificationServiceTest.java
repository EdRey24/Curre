package edu.bu.cs411.group10.curre.safety;

import org.junit.jupiter.api.Test;
import java.util.List;

// We can remove this when we implement the real thing
public class MockNotificationServiceTest {

    @Test
    public void testMockNotificationsExecuteWithoutErrors() {
        MockNotificationService service = new MockNotificationService();
        List<String> emails = List.of("contact@test.com");

        service.sendRunStartedNotification("user@test.com", emails, 42.36, -71.05);
        service.sendRunEndedNotification("user@test.com", emails);
        service.sendOverdueAlert("user@test.com", emails, 42.36, -71.05);
    }
}