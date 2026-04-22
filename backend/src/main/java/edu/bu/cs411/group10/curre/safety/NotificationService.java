package edu.bu.cs411.group10.curre.safety;

import edu.bu.cs411.group10.curre.contact.EmergencyContact;
import java.util.List;

public interface NotificationService {
    void sendRunStartedNotification(String userEmail, List<EmergencyContact> contacts, Double lat, Double lng);
    void sendRunEndedNotification(String userEmail, List<EmergencyContact> contacts);
    void sendOverdueAlert(String userEmail, List<EmergencyContact> contacts, Double lastLat, Double lastLng);
    void sendTestNotification(String userEmail, List<EmergencyContact> contacts);
} // END OF INTERFACE NotificationService