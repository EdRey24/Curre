package edu.bu.cs411.group10.curre.contact;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// In a real app, userId would be extracted from JWT. For simplicity, we pass as a header.
@RestController
@RequestMapping("/api/contacts")
@CrossOrigin
public class EmergencyContactController {

    private final EmergencyContactService contactService;

    public EmergencyContactController(EmergencyContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<EmergencyContactDTO> addContact(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody EmergencyContactDTO dto) {
        EmergencyContactDTO saved = contactService.addContact(userId, dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    } // END OF METHOD addContact

    @PutMapping("/{contactId}")
    public ResponseEntity<EmergencyContactDTO> updateContact(
            @PathVariable Long contactId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody EmergencyContactDTO dto) {
        EmergencyContactDTO updated = contactService.updateContact(contactId, userId, dto);
        return ResponseEntity.ok(updated);
    } // END OF METHOD updateContact

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long contactId,
            @RequestHeader("X-User-Id") Long userId) {
        contactService.deleteContact(contactId, userId);
        return ResponseEntity.noContent().build();
    } // END OF METHOD deleteContact

    @GetMapping
    public ResponseEntity<List<EmergencyContactDTO>> getContacts(
            @RequestHeader("X-User-Id") Long userId) {
        List<EmergencyContactDTO> contacts = contactService.getContactsForUser(userId);
        return ResponseEntity.ok(contacts);
    } // END OF METHOD getContacts
} // END OF CLASS EmergencyContactController