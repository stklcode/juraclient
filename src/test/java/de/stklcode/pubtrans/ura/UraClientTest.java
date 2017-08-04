/*
 * Copyright 2016-2017 Stefan Kalscheuer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.stklcode.pubtrans.ura;

import de.stklcode.pubtrans.ura.model.Stop;
import de.stklcode.pubtrans.ura.model.Trip;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * Unit test for the URA Client.
 * Tests run against mocked data collected from ASEAG API (http://ivu.aseag.de) and
 *  TFL API (http://http://countdown.api.tfl.gov.uk)
 *
 * @author Stefan Kalscheuer
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ UraClient.class, URL.class })
public class UraClientTest {
    @Test
    public void getStopsTest() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V2_stops.txt"));

        /* List stops and verify some values */
        List<Stop> stops = new UraClient("mocked").getStops();
        assertThat(stops, hasSize(10));
        assertThat(stops.get(0).getId(), is("100210"));
        assertThat(stops.get(1).getName(), is("Brockenberg"));
        assertThat(stops.get(2).getState(), is(0));;
        assertThat(stops.get(3).getLatitude(), is(50.7578775));
        assertThat(stops.get(4).getLongitude(), is(6.0708663));

        /* Test exception handling */
        PowerMockito.when(mockURL.openStream()).thenReturn(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Provoked exception 1.");
            }
        });
        assertThat(new UraClient("mocked").getStops(), hasSize(0));
        PowerMockito.when(mockURL.openStream()).thenThrow(new IOException("Provoked exception 2."));
        assertThat(new UraClient("mocked").getStops(), hasSize(0));
    }

    @Test
    public void getStopsForLineTest() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V2_stops_line.txt"));

        /* List stops and verify some values */
        List<Stop> stops = new UraClient("mocked").forLines("33").getStops();
        assertThat(stops, hasSize(47));
        assertThat(stops.get(0).getId(), is("100000"));
        assertThat(stops.get(1).getName(), is("Kuckelkorn"));
        assertThat(stops.get(2).getState(), is(0));;
        assertThat(stops.get(3).getLatitude(), is(50.7690688));
        assertThat(stops.get(4).getIndicator(), is("H.1"));
        assertThat(stops.get(5).getLongitude(), is(6.2314072));
    }

    @Test
    public void getStopsForPositionTest() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_stops_circle.txt"));

        /* List stops and verify some values */
        List<Stop> stops = new UraClient("mocked")
                .forPosition(51.51009, -0.1345734, 200)
                .getStops();
        assertThat(stops, hasSize(13));
        assertThat(stops.get(0).getId(), is("156"));
        assertThat(stops.get(1).getName(), is("Piccadilly Circus"));
        assertThat(stops.get(2).getState(), is(0));;
        assertThat(stops.get(3).getLatitude(), is(51.509154));
        assertThat(stops.get(4).getLongitude(), is(-0.134172));
        assertThat(stops.get(5).getIndicator(), is(nullValue()));

        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_stops_circle_name.txt"));
        stops = new UraClient("mocked")
                .forStopsByName("Piccadilly Circus")
                .forPosition(51.51009, -0.1345734, 200)
                .getStops();
        assertThat(stops, hasSize(7));
        assertThat(stops.stream().filter(t -> !t.getName().equals("Piccadilly Circus")).findAny(), is(Optional.empty()));
    }

    @Test
    public void getTripsForDestinationNamesTest() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_destination.txt"));

        /* List stops and verify some values */
        List<Trip> trips = new UraClient("mocked").forDestinationNames("Piccadilly Circus").getTrips();
        assertThat(trips, hasSize(9));
        assertThat(trips.stream().filter(t -> !t.getDestinationName().equals("Piccadilly Cir")).findAny(),
                is(Optional.empty()));

        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_stop_destination.txt"));
        trips = new UraClient("mocked")
                .forStops("156")
                .forDestinationNames("Marble Arch")
                .getTrips();
        assertThat(trips, hasSize(5));
        assertThat(trips.stream().filter(t -> !t.getStop().getId().equals("156")).findAny(),
                is(Optional.empty()));
        assertThat(trips.stream().filter(t -> !t.getDestinationName().equals("Marble Arch")).findAny(),
                is(Optional.empty()));
    }

    @Test
    public void getTripsTowardsTest() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_towards.txt"));

        /* List stops and verify some values */
        List<Trip> trips = new UraClient("mocked").towards("Marble Arch").getTrips();
        assertThat(trips, hasSize(10));

        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_stop_towards.txt"));
        trips = new UraClient("mocked").forStops("156").towards("Marble Arch").getTrips();
        assertThat(trips, hasSize(17));
        assertThat(trips.stream().filter(t -> !t.getStop().getId().equals("156")).findAny(), is(Optional.empty()));
    }

    @Test
    public void getTripsTest() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_all.txt"));

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

        /* Repeat test for API V2 */
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V2_trips_all.txt"));
        /* Get trips without filters and verify some values */
        trips = new UraClient("mocked").getTrips();
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

        /* Get limited number of trips */
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_all.txt"));
        trips = new UraClient("mocked").getTrips(5);
        assertThat(trips, hasSize(5));

        /* Test exception handling */
        PowerMockito.when(mockURL.openStream()).thenReturn(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Provoked exception 1.");
            }
        });
        assertThat(new UraClient("mocked").getTrips(), hasSize(0));
        PowerMockito.when(mockURL.openStream()).thenThrow(new IOException("Provoked exception 2."));
        assertThat(new UraClient("mocked").getTrips(), hasSize(0));
    }

    @Test
    public void getTripsForStopTest() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_stop.txt"));

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

        /* Get trips for stop name "Uniklinik" and verify some values */
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_stop_name.txt"));
        trips = new UraClient("mocked")
                .forStopsByName("Uniklinik")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getStop().getName().equals("Uniklinik")).findAny(),
                is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("92000043013001"));
        assertThat(trips.get(1).getLineID(), is("5"));
        assertThat(trips.get(2).getVehicleID(), is("317"));;
        assertThat(trips.get(3).getDirectionID(), is(1));
    }

    @Test
    public void getTripsForLine() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_line.txt"));

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

        /* Get trips for line name "3.A" and verify some values */
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_line_name.txt"));
        trips = new UraClient("mocked")
                .forLinesByName("3.A")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineName().equals("3.A")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("92000288014001"));
        assertThat(trips.get(1).getLineID(), is("3"));
        assertThat(trips.get(2).getLineName(), is("3.A"));;
        assertThat(trips.get(3).getStop().getName(), is("Aachen Gartenstraße"));

        /* Get trips for line 3 with direction 1 and verify some values */
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_line_direction.txt"));
        trips = new UraClient("mocked")
                .forLines("412")
                .forDirection(2)
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("412")).findAny(), is(Optional.empty()));
        assertThat(trips.stream().filter(t -> !t.getDirectionID().equals(2)).findAny(), is(Optional.empty()));

        /* Test lineID and direction in different order */
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_line_direction.txt"));
        trips = new UraClient("mocked")
                .forDirection(2)
                .forLines("412")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("412")).findAny(), is(Optional.empty()));
        assertThat(trips.stream().filter(t -> !t.getDirectionID().equals(2)).findAny(), is(Optional.empty()));
    }

    @Test
    public void getTripsForStopAndLine() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream())
                .thenReturn(getClass().getResourceAsStream("instant_V1_trips_stop_line.txt"));

        /* Get trips for line ID 25 and 25 at stop 100000 and verify some values */
        List<Trip> trips = new UraClient("mocked")
                .forLines("25", "35")
                .forStops("100000")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("25") && !t.getLineID().equals("35")).findAny(),
                is(Optional.empty()));
        assertThat(trips.stream().filter(t -> !t.getStop().getId().equals("100000")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("27000078014001"));
        assertThat(trips.get(1).getLineID(), is("25"));
        assertThat(trips.get(3).getLineName(), is("35"));;
        assertThat(trips.get(5).getStop().getIndicator(), is("H.12"));
    }
}
