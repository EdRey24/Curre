package edu.bu.cs411.group10.curre.run;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Test
    public void testRoutePointsGetterAndSetter() {
        Run run = new Run();

        // Ensure it starts as null (or whatever the default is)
        assertNull(run.getRoutePoints());

        // Create a fake list of points
        List<RoutePoint> fakePoints = new ArrayList<>();
        RoutePoint point1 = new RoutePoint();
        point1.setLatitude(42.3601);
        point1.setLongitude(-71.0589);
        fakePoints.add(point1);

        // Set the points
        run.setRoutePoints(fakePoints);

        // Assert that the getter fetches exactly what we set
        assertNotNull(run.getRoutePoints());
        assertEquals(1, run.getRoutePoints().size());
        assertEquals(42.3601, run.getRoutePoints().get(0).getLatitude());
        assertEquals(-71.0589, run.getRoutePoints().get(0).getLongitude());
    }
}
