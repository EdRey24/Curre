package edu.bu.cs411.group10.curre.contact;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmergencyContactController.class)
public class EmergencyContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmergencyContactService contactService;

    @Test
    public void testAddContact() throws Exception {
        EmergencyContactDTO mockDto = new EmergencyContactDTO();
        mockDto.setId(1L);
        mockDto.setName("Mom");
        mockDto.setEmail("mom@example.com");

        Mockito.when(contactService.addContact(eq(1L), any(EmergencyContactDTO.class)))
                .thenReturn(mockDto);

        mockMvc.perform(post("/api/contacts")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mom"));
    }

    @Test
    public void testUpdateContact() throws Exception {
        EmergencyContactDTO mockDto = new EmergencyContactDTO();
        mockDto.setId(1L);
        mockDto.setName("Dad");
        mockDto.setEmail("dad@example.com");

        Mockito.when(contactService.updateContact(eq(1L), eq(1L), any(EmergencyContactDTO.class)))
                .thenReturn(mockDto);

        mockMvc.perform(put("/api/contacts/1")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dad"));
    }

    @Test
    public void testDeleteContact() throws Exception {
        mockMvc.perform(delete("/api/contacts/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());

        Mockito.verify(contactService).deleteContact(1L, 1L);
    }

    @Test
    public void testGetContacts() throws Exception {
        EmergencyContactDTO dto = new EmergencyContactDTO();
        dto.setId(10L);
        dto.setName("Brother");
        dto.setEmail("brother@example.com");

        Mockito.when(contactService.getContactsForUser(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/contacts")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Brother"));
    }
}