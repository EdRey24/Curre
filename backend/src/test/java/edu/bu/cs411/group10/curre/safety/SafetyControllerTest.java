package edu.bu.cs411.group10.curre.safety;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SafetyController.class)
public class SafetyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SafetyService safetyService;

    @Test
    public void testStartSafetyWithInterval() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("runId", 100);
        payload.put("intervalSeconds", 1200);

        mockMvc.perform(post("/api/safety/start")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // Verify the controller successfully parsed the JSON and called the service
        Mockito.verify(safetyService).startSafetyMonitoring(100L, 1L, 1200);
    }

    @Test
    public void testStartSafetyWithDefaultInterval() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("runId", 100);

        mockMvc.perform(post("/api/safety/start")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // Verify it defaulted to 900 seconds (15 mins)
        Mockito.verify(safetyService).startSafetyMonitoring(100L, 1L, 900);
    }

    @Test
    public void testCheckIn() throws Exception {
        mockMvc.perform(post("/api/safety/checkin/100")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk());

        Mockito.verify(safetyService).checkIn(100L, 1L);
    }

    @Test
    public void testStopSafety() throws Exception {
        mockMvc.perform(post("/api/safety/stop/100")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk());

        Mockito.verify(safetyService).stopSafetyMonitoring(100L, 1L);
    }
}