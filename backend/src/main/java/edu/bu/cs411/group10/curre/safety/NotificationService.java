package edu.bu.cs411.group10.curre.safety;

import java.util.List;

public interface NotificationService {
    void sendRunStartedNotification(String userEmail, List<String> contactEmails, Double lat, Double lng);
    void sendRunEndedNotification(String userEmail, List<String> contactEmails);
    void sendOverdueAlert(String userEmail, List<String> contactEmails, Double lastLat, Double lastLng);
} // END OF INTERFACE NotificationService