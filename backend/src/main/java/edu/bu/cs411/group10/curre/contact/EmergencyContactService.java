package edu.bu.cs411.group10.curre.contact;

import edu.bu.cs411.group10.curre.user.User;
import edu.bu.cs411.group10.curre.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmergencyContactService {

    private static final Logger log = LoggerFactory.getLogger(EmergencyContactService.class);
    private final EmergencyContactRepository contactRepository;
    private final UserRepository userRepository;

    public EmergencyContactService(EmergencyContactRepository contactRepository,
                                   UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves or creates a user with the given ID.
     * Used to auto‑provision the default user when the frontend sends a userId.
     */
    private final Object userCreationLock = new Object();

    private User getOrCreateUser(Long userId) {
        // First try to find by ID (for real authenticated users)
        Optional<User> byId = userRepository.findById(userId);
        if (byId.isPresent()) {
            return byId.get();
        }
        // Fallback: look up by email pattern (for legacy/test users)
        String email = "user" + userId + "@curre.com";
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }
        // Synchronize to avoid duplicate inserts from concurrent requests
        synchronized (userCreationLock) {
            // Double-check after acquiring lock
            Optional<User> againById = userRepository.findById(userId);
            if (againById.isPresent()) {
                return againById.get();
            }
            Optional<User> againByEmail = userRepository.findByEmail(email);
            if (againByEmail.isPresent()) {
                return againByEmail.get();
            }
            User newUser = new User();
            newUser.setFirstName("Test");
            newUser.setLastName("User");
            newUser.setEmail(email);
            newUser.setPassword("default");
            User saved = userRepository.save(newUser);
            log.info("Auto‑created user with email {} and ID {}", email, saved.getId()); // DEBUG
            return saved;
        }
    } // END OF METHOD getOrCreateUser

    @Transactional
    public EmergencyContactDTO addContact(Long userId, EmergencyContactDTO dto) {
        User user = getOrCreateUser(userId);
        EmergencyContact contact = new EmergencyContact();
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contact.setUser(user);
        EmergencyContact saved = contactRepository.save(contact);
        log.info("Added emergency contact ID {} for user ID {}", saved.getId(), userId); // DEBUG
        return convertToDTO(saved);
    } // END OF METHOD addContact

    @Transactional
    public EmergencyContactDTO updateContact(Long contactId, Long userId, EmergencyContactDTO dto) {
        // Ensure user exists (auto‑create if needed)
        User user = getOrCreateUser(userId);
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new EntityNotFoundException("Contact not found with id: " + contactId));
        if (!contact.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Contact does not belong to this user");
        }
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        EmergencyContact updated = contactRepository.save(contact);
        log.info("Updated emergency contact ID {}", contactId); // DEBUG
        return convertToDTO(updated);
    } // END OF METHOD updateContact

    @Transactional
    public void deleteContact(Long contactId, Long userId) {
        // Ensure user exists (auto‑create if needed)
        User user = getOrCreateUser(userId);
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new EntityNotFoundException("Contact not found with id: " + contactId));
        if (!contact.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Contact does not belong to this user");
        }
        contactRepository.delete(contact);
        log.info("Deleted emergency contact ID {}", contactId); // DEBUG
    } // END OF METHOD deleteContact

    public List<EmergencyContactDTO> getContactsForUser(Long userId) {
        // Ensure user exists (auto‑create if needed)
        User user = getOrCreateUser(userId);
        List<EmergencyContactDTO> contacts = contactRepository.findByUserId(user.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        log.info("Returning {} contacts for user ID {}", contacts.size(), user.getId()); // DEBUG
        return contacts;
    } // END OF METHOD getContactsForUser

    private EmergencyContactDTO convertToDTO(EmergencyContact contact) {
        EmergencyContactDTO dto = new EmergencyContactDTO();
        dto.setId(contact.getId());
        dto.setName(contact.getName());
        dto.setEmail(contact.getEmail());
        dto.setPhone(contact.getPhone());
        return dto;
    } // END OF METHOD convertToDTO
} // END OF CLASS EmergencyContactService