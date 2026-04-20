package edu.bu.cs411.group10.curre.safety;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import edu.bu.cs411.group10.curre.contact.EmergencyContact;

@Service
public class MockNotificationService implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(MockNotificationService.class);

    @Override
    public void sendRunStartedNotification(String userEmail, List<EmergencyContact> contacts, Double lat, Double lng) {
        String names = getContactNames(contacts);
        log.info("Mock: Run started for user {}, contacts {}, location {} {}", userEmail, names, lat, lng); // DEBUG
    } // END OF METHOD

    @Override
    public void sendRunEndedNotification(String userEmail, List<EmergencyContact> contacts) {
        String names = getContactNames(contacts);
        log.info("Mock: Run ended for user {}, contacts {}", userEmail, names); // DEBUG
    } // END OF METHOD

    @Override
    public void sendOverdueAlert(String userEmail, List<EmergencyContact> contacts, Double lastLat, Double lastLng) {
        String names = getContactNames(contacts);
        log.info("Mock: OVERDUE alert for user {}, contacts {}, location {} {}", userEmail, names, lastLat, lastLng); // DEBUG
    } // END OF METHOD

    @Override
    public void sendTestNotification(String userEmail, String phone){
        log.info("Mock: Test notification sent to email {} and phone {}", userEmail, phone);
    }

    private String getContactNames(List<EmergencyContact> contacts) {
        return contacts.stream()
                .map(c -> c.getName() + " (" + c.getEmail() + ")")
                .collect(Collectors.joining(", "));
    }
} // END OF CLASS MockNotificationService