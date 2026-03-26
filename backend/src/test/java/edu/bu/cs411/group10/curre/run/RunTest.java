package edu.bu.cs411.group10.curre.run;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
public class RunTest {
    @Test
    public void testRunGettersAndSetters(){
        Run run = new Run();

        assertNull(run.getId());

        run.setDistanceMiles(3.1);
        run.setDurationSeconds(1200);

        assertEquals(3.1, run.getDistanceMiles());
        assertEquals(1200, run.getDurationSeconds());
    }
}
