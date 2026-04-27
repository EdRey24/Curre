package edu.bu.cs411.group10.curre.safety;

import edu.bu.cs411.group10.curre.contact.EmergencyContact;
import java.util.List;

public interface NotificationService {
    void sendRunStartedNotification(String runnerName, List<EmergencyContact> contacts, Double lat, Double lng);
    void sendRunEndedNotification(String runnerName, List<EmergencyContact> contacts);
    void sendOverdueAlert(String runnerName, List<EmergencyContact> contacts, Double lastLat, Double lastLng);
    void sendTestNotification(String runnerName, List<EmergencyContact> contacts);
} // END OF INTERFACE NotificationService