package de.stklcode.pubtrans.ura;

import de.stklcode.pubtrans.ura.model.Stop;
import de.stklcode.pubtrans.ura.model.Trip;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

/**
 * Unit test for the URA Client.
 * Tests run against mocked data collected from hte ASEAG API (http://ivu.aseag.de/)
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ UraClient.class, URL.class })
public class UraClientTest {
    private static final String BASE_ASEAG = "http://ivu.aseag.de";

    @Test
    public void listStopsTest() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream()).thenReturn(getClass().getResourceAsStream("instant_stops.txt"));

        /* List stops and verify some values */
        List<Stop> stops = new UraClient("mocked").listStops();
        assertThat(stops, hasSize(10));
        assertThat(stops.get(0).getId(), is("100210"));
        assertThat(stops.get(1).getName(), is("Brockenberg"));
        assertThat(stops.get(2).getState(), is(0));;
        assertThat(stops.get(3).getLatitude(), is(50.7578775));
        assertThat(stops.get(4).getLongitude(), is(6.0708663));
    }

    @Test
    public void getTripsTest() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream()).thenReturn(getClass().getResourceAsStream("instant_trips_all.txt"));

        /* Get trips without filters and verify some values */
        List<Trip> trips = new UraClient("mocked").getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.get(0).getId(), is("27000165015001"));
        assertThat(trips.get(1).getLineID(), is("55"));
        assertThat(trips.get(2).getLineName(), is("28"));;
        assertThat(trips.get(3).getDirectionID(), is(1));
        assertThat(trips.get(4).getDestinationName(), is("Verlautenheide Endstr."));
        assertThat(trips.get(5).getDestinationText(), is("Aachen Bushof"));
        assertThat(trips.get(6).getVehicleID(), is("247"));
        assertThat(trips.get(7).getEstimatedTime(), is(1482854580000L));
        assertThat(trips.get(8).getVisitID(), is(30));
        assertThat(trips.get(9).getStop().getId(), is("100002"));
    }

    @Test
    public void getTripsForStopTest() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream()).thenReturn(getClass().getResourceAsStream("instant_trips_stop.txt"));

        /* Get trips for stop ID 100000 (Aachen Bushof) and verify some values */
        List<Trip> trips = new UraClient("mocked")
                .forStops("100000")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getStop().getId().equals("100000")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("27000158010001"));
        assertThat(trips.get(1).getLineID(), is("7"));
        assertThat(trips.get(2).getLineName(), is("25"));;
        assertThat(trips.get(3).getStop().getIndicator(), is("H.15"));
    }

    @Test
    public void getTripsForLine() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream()).thenReturn(getClass().getResourceAsStream("instant_trips_line.txt"));

        /* Get trips for line ID 3 and verify some values */
        List<Trip> trips = new UraClient("mocked")
                .forLines("3")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("3")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("27000154004001"));
        assertThat(trips.get(1).getLineID(), is("3"));
        assertThat(trips.get(2).getLineName(), is("3.A"));;
        assertThat(trips.get(3).getStop().getIndicator(), is("H.4 (Pontwall)"));
    }

    @Test
    public void getTripsForStopAndLine() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream()).thenReturn(getClass().getResourceAsStream("instant_trips_stop_line.txt"));

        /* Get trips for line ID 25 and 25 at stop 100000 and verify some values */
        List<Trip> trips = new UraClient("mocked")
                .forLines("25", "35")
                .forStops("100000")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("25") && !t.getLineID().equals("35")).findAny(), is(Optional.empty()));
        assertThat(trips.stream().filter(t -> !t.getStop().getId().equals("100000")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("27000078014001"));
        assertThat(trips.get(1).getLineID(), is("25"));
        assertThat(trips.get(3).getLineName(), is("35"));;
        assertThat(trips.get(5).getStop().getIndicator(), is("H.12"));
    }
}
