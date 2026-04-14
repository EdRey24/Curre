package edu.bu.cs411.group10.curre.safety;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MockNotificationService implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(MockNotificationService.class);

    @Override
    public void sendRunStartedNotification(String userEmail, List<String> contactEmails, Double lat, Double lng) {
        log.info("Mock: Run started for user {}, contacts {}, location {} {}", userEmail, contactEmails, lat, lng); // DEBUG
    } // END OF METHOD

    @Override
    public void sendRunEndedNotification(String userEmail, List<String> contactEmails) {
        log.info("Mock: Run ended for user {}, contacts {}", userEmail, contactEmails); // DEBUG
    } // END OF METHOD

    @Override
    public void sendOverdueAlert(String userEmail, List<String> contactEmails, Double lastLat, Double lastLng) {
        log.info("Mock: OVERDUE alert for user {}, contacts {}, location {} {}", userEmail, contactEmails, lastLat, lastLng); // DEBUG
    } // END OF METHOD
} // END OF CLASS MockNotificationService