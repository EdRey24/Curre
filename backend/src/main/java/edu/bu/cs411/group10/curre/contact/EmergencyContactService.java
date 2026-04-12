package edu.bu.cs411.group10.curre.contact;

import edu.bu.cs411.group10.curre.user.User;
import edu.bu.cs411.group10.curre.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmergencyContactService {

    private final EmergencyContactRepository contactRepository;
    private final UserRepository userRepository;

    public EmergencyContactService(EmergencyContactRepository contactRepository,
                                   UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EmergencyContactDTO addContact(Long userId, EmergencyContactDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        EmergencyContact contact = new EmergencyContact();
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contact.setUser(user);
        EmergencyContact saved = contactRepository.save(contact);
        return convertToDTO(saved);
    } // END OF METHOD addContact

    @Transactional
    public EmergencyContactDTO updateContact(Long contactId, Long userId, EmergencyContactDTO dto) {
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new EntityNotFoundException("Contact not found with id: " + contactId));
        if (!contact.getUser().getId().equals(userId)) {
            throw new SecurityException("Contact does not belong to this user");
        }
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        EmergencyContact updated = contactRepository.save(contact);
        return convertToDTO(updated);
    } // END OF METHOD updateContact

    @Transactional
    public void deleteContact(Long contactId, Long userId) {
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new EntityNotFoundException("Contact not found with id: " + contactId));
        if (!contact.getUser().getId().equals(userId)) {
            throw new SecurityException("Contact does not belong to this user");
        }
        contactRepository.delete(contact);
    } // END OF METHOD deleteContact

    public List<EmergencyContactDTO> getContactsForUser(Long userId) {
        return contactRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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