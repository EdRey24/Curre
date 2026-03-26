package edu.bu.cs411.group10.curre.run;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RunController.class)
public class RunControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RunRepository runRepository;

    @Test
    public void testCreateRun() throws Exception {
        Run savedRun = new Run();
        savedRun.setId(1L);
        savedRun.setDistanceMiles(1.5);

        Mockito.when(runRepository.save(Mockito.any(Run.class))).thenReturn(savedRun);
        String runJson = """
                {
                    "startedAt": 1711234567000,
                    "endedAt": 1711235167000,
                    "distanceMiles": 1.5,
                    "durationSeconds": 900,
                    "avgPaceSecsPerMile": 600.0,
                    "calories": 5
                }
                """;

        mockMvc.perform(post("/api/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(runJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.distanceMiles").value(1.5));
    }

    @Test
    public void testGetAllRuns() throws Exception {
        Run run = new Run();
        run.setId(1L);
        run.setDistanceMiles(3.1);

        Mockito.when(runRepository.findAll()).thenReturn(List.of(run));

        mockMvc.perform(get("/api/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].distanceMiles").value(3.1));
    }
}
