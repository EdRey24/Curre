package edu.bu.cs411.group10.curre.contact;

import edu.bu.cs411.group10.curre.user.User;
import edu.bu.cs411.group10.curre.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * The system has exactly one valid user: Demo with password Password1.
 * This is reflected in the test data below (user email "demo@curre.com").
 */
@ExtendWith(MockitoExtension.class)
public class EmergencyContactServiceTest {

    @Mock
    private EmergencyContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmergencyContactService service;

    private User testUser;
    private EmergencyContactDTO testDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("demo@curre.com"); // only valid user

        testDTO = new EmergencyContactDTO();
        testDTO.setName("Mom");
        testDTO.setEmail("mom@example.com");
        testDTO.setPhone("1234567890");

        lenient().when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(testUser));
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            if (savedUser.getId() == null) {
                savedUser.setId(1L); // We give it a fake DB ID
            }
            return savedUser;
        });
    }

    @Test
    public void testAddContactSuccess() {
        System.out.println("Starting test: Adding contacts works (tested with valid user)");
        EmergencyContact savedContact = new EmergencyContact();
        savedContact.setId(100L);
        savedContact.setName(testDTO.getName());
        savedContact.setEmail(testDTO.getEmail());
        savedContact.setPhone(testDTO.getPhone());
        savedContact.setUser(testUser);
        when(contactRepository.save(any(EmergencyContact.class))).thenReturn(savedContact);

        EmergencyContactDTO result = service.addContact(1L, testDTO);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Mom", result.getName());
        verify(contactRepository, times(1)).save(any(EmergencyContact.class));
        System.out.println("PASSED: Adding contacts works (tested with valid user)");
    } // END OF TEST testAddContactSuccess

    @Test
    public void testUpdateContactSuccess() {
        System.out.println("Starting test: Update existing contact with valid data");
        EmergencyContact existing = new EmergencyContact();
        existing.setId(200L);
        existing.setName("Old Name");
        existing.setEmail("old@example.com");
        existing.setUser(testUser);
        when(contactRepository.findById(200L)).thenReturn(Optional.of(existing));
        when(contactRepository.save(any(EmergencyContact.class))).thenReturn(existing);

        testDTO.setName("Updated Mom");
        EmergencyContactDTO updated = service.updateContact(200L, 1L, testDTO);

        assertEquals("Updated Mom", updated.getName());
        verify(contactRepository).save(existing);
        System.out.println("PASSED: Update existing contact with valid data");
    } // END OF TEST testUpdateContactSuccess

    @Test
    public void testUpdateContactWrongUser() {
        System.out.println("Starting test: Prevent update of another user's contact (security check)");
        EmergencyContact existing = new EmergencyContact();
        existing.setId(200L);
        User otherUser = new User();
        otherUser.setId(2L);
        existing.setUser(otherUser);
        when(contactRepository.findById(200L)).thenReturn(Optional.of(existing));

        assertThrows(SecurityException.class, () -> service.updateContact(200L, 1L, testDTO));
        verify(contactRepository, never()).save(any());
        System.out.println("PASSED: Prevent update of another user's contact (security check)");
    } // END OF TEST testUpdateContactWrongUser

    @Test
    public void testDeleteContactSuccess() {
        System.out.println("Starting test: Delete an existing contact");
        EmergencyContact existing = new EmergencyContact();
        existing.setId(300L);
        existing.setUser(testUser);
        when(contactRepository.findById(300L)).thenReturn(Optional.of(existing));

        service.deleteContact(300L, 1L);

        verify(contactRepository).delete(existing);
        System.out.println("PASSED: Delete an existing contact");
    } // END OF TEST testDeleteContactSuccess

    @Test
    public void testGetContactsForUser() {
        System.out.println("Starting test: Retrieve all contacts for a user");
        EmergencyContact c1 = new EmergencyContact();
        c1.setId(1L);
        c1.setName("A");
        EmergencyContact c2 = new EmergencyContact();
        c2.setId(2L);
        c2.setName("B");
        when(contactRepository.findByUserId(1L)).thenReturn(List.of(c1, c2));

        List<EmergencyContactDTO> contacts = service.getContactsForUser(1L);

        assertEquals(2, contacts.size());
        assertEquals("A", contacts.get(0).getName());
        System.out.println("PASSED: Retrieve all contacts for a user");
    } // END OF TEST testGetContactsForUser
} // END OF CLASS EmergencyContactServiceTest