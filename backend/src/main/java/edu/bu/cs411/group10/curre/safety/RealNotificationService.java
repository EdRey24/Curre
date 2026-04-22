package edu.bu.cs411.group10.curre.safety;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import edu.bu.cs411.group10.curre.contact.EmergencyContact;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
@Primary
public class RealNotificationService implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(RealNotificationService.class);

    @Value("${sendgrid.api.key}") private String sendGridApiKey;
    @Value("${sendgrid.from.email}") private String fromEmail;
    @Value("${twilio.account.sid}") private String twilioSid;
    @Value("${twilio.auth.token}") private String twilioToken;
    @Value("${twilio.from.phone}") private String fromPhone;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(twilioSid, twilioToken);
    }

    @Override
    public void sendRunStartedNotification(String userEmail, List<EmergencyContact> contact, Double lat, Double lng){
        String msg = "Curre: " + userEmail + " started a run. Location: " + formatMapLink(lat, lng);
        broadcast(contact, "Curre: Run Started", msg);
    }

    @Override
    public void sendRunEndedNotification(String userEmail, List<EmergencyContact> contacts) {
        String msg = "Curre: " + userEmail + " finished their run safely.";
        broadcast(contacts, "Curre: Run Ended", msg);
    }

    @Override
    public void sendOverdueAlert(String userEmail, List<EmergencyContact> contacts, Double lastLat, Double lastLng) {
        String msg = "URGENT: " + userEmail + " missed a check-in! Last location: " + formatMapLink(lastLat, lastLng);
        broadcast(contacts, "URGENT: Runner Overdue", msg);
    }

    @Override
    public void sendTestNotification(String userEmail, List<EmergencyContact> contacts) {
        String msg = "Curre Test: If you recieve this, emergency alerts are working correctly!";
        broadcast(contacts, "Curre: Test Alert", msg);
    }

    private void broadcast(List<EmergencyContact> contacts, String subject, String body){
        for(EmergencyContact contact : contacts){
            sendEmail(contact.getEmail(), subject, body);
            if(contact.getPhone() != null && !contact.getPhone().isBlank()) {
                sendSms(contact.getPhone(), body);
            }
        }
    }

    private void sendEmail(String to, String subject, String text) {
        Mail mail = new Mail(new Email(fromEmail), subject, new Email(to), new Content("text/plain", text));
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
            log.info("Email sent to {}", to);
        } catch (IOException ex) {
            log.error("Email failed to {}", to, ex);
        }
    }

    private void sendSms(String to, String body) {
        try {
            Message.creator(new PhoneNumber(to), new PhoneNumber(fromPhone), body).create();
            log.info("SMS sent to {}", to);
        } catch (Exception e) {
            log.error("SMS failed to {}", to, e);
        }
    }

    private String formatMapLink(Double lat, Double lng) {
        return (lat != null && lng != null) ? "https://www.google.com/maps?q=" + lat + "," + lng : "Unknown";
    }
}
