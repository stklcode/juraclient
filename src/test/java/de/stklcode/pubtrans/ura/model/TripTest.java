/*
 * Copyright 2016-2026 Stefan Kalscheuer
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

package de.stklcode.pubtrans.ura.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the {@link Trip} model.
 *
 * @author Stefan Kalscheuer
 */
class TripTest {
    @Test
    void basicConstructorTest() {
        Trip trip = new Trip(
            "sid",
            "name",
            "indicator",
            1,
            2.345,
            6.789,
            123,
            "lineid",
            "linename",
            0,
            "destination name",
            "destination text",
            "vehicle",
            "id",
            123456789123456789L
        );
        assertEquals("sid", trip.stop().id());
        assertEquals("name", trip.stop().name());
        assertEquals("indicator", trip.stop().indicator());
        assertEquals(1, trip.stop().state());
        assertEquals(2.345, trip.stop().latitude());
        assertEquals(6.789, trip.stop().longitude());
        assertEquals(123, trip.visitID());
        assertEquals("lineid", trip.lineID());
        assertEquals("linename", trip.lineName());
        assertEquals(0, trip.directionID());
        assertEquals("destination name", trip.destinationName());
        assertEquals("destination text", trip.destinationText());
        assertEquals("vehicle", trip.vehicleID());
        assertEquals("id", trip.id());
        assertEquals(123456789123456789L, trip.estimatedTime());
    }

    @Test
    void listConstructorTest() {
        /* Create valid raw data list */
        List<Serializable> raw = new ArrayList<>();
        raw.add(1);
        raw.add("stopName");
        raw.add("stopId");
        raw.add("stopIndicator");
        raw.add(9);
        raw.add(8.765);
        raw.add(43.21);
        raw.add(123);
        raw.add("lineid");
        raw.add("linename");
        raw.add(0);
        raw.add("destination name");
        raw.add("destination text");
        raw.add("vehicle");
        raw.add(9876543210L);
        raw.add(123456789123456789L);

        Trip trip = assertDoesNotThrow(() -> Trip.of(raw), "Creation of Trip from valid list failed");
        assertEquals("stopId", trip.stop().id());
        assertEquals("stopName", trip.stop().name());
        assertEquals("stopIndicator", trip.stop().indicator());
        assertEquals(9, trip.stop().state());
        assertEquals(8.765, trip.stop().latitude());
        assertEquals(43.21, trip.stop().longitude());
        assertEquals(123, trip.visitID());
        assertEquals("lineid", trip.lineID());
        assertEquals("linename", trip.lineName());
        assertEquals(0, trip.directionID());
        assertEquals("destination name", trip.destinationName());
        assertEquals("destination text", trip.destinationText());
        assertEquals("vehicle", trip.vehicleID());
        assertEquals("9876543210", trip.id());
        assertEquals(123456789123456789L, trip.estimatedTime());

        /* Test with V2 style list */
        raw.set(14, "id");
        trip = assertDoesNotThrow(() -> Trip.of(raw, "2.0"), "Creation of Trip from valid list failed");
        assertEquals("id", trip.id());
        raw.set(14, 9876543210L);

        /* Excess elements should be ignored */
        raw.add("foo");
        trip = assertDoesNotThrow(() -> Trip.of(raw), "Creation of Trip from valid list failed");
        assertNotNull(trip);
        raw.remove(16);

        raw.remove(10);
        raw.add(10, 0L);    // Long values are OK.
        trip = assertDoesNotThrow(() -> Trip.of(raw), "Creation of Trip from valid list failed");
        assertNotNull(trip);
        assertEquals(0, trip.directionID());

        raw.remove(10);
        raw.add(10, "0");    // String values are OK.
        trip = assertDoesNotThrow(() -> Trip.of(raw), "Creation of Trip from valid list failed");
        assertNotNull(trip);
        assertEquals(0, trip.directionID());

        /* Test exceptions on invalid data */
        var invalid1 = new ArrayList<>(raw);
        invalid1.remove(7);
        invalid1.add(7, "123");
        assertThrows(
            IOException.class,
            () -> Trip.of(invalid1),
            "Creation of Trip with invalid visitID field successful"
        );

        var invalid2 = new ArrayList<>(raw);
        invalid2.remove(8);
        invalid2.add(8, 25);
        assertThrows(
            IOException.class,
            () -> Trip.of(invalid2),
            "Creation of Trip with invalid lineID field successful"
        );

        var invalid3 = new ArrayList<>(raw);
        invalid3.remove(9);
        invalid3.add(9, 234L);
        assertThrows(
            IOException.class,
            () -> Trip.of(invalid3),
            "Creation of Trip with invalid lineName field successful"
        );

        var invalid4 = new ArrayList<>(raw);
        invalid4.remove(10);
        invalid4.add(10, "7");   // Strings are generally OK, but 7 is out of range (#2).
        assertThrows(
            IOException.class,
            () -> Trip.of(invalid4),
            "Creation of Trip with invalid directionID field successful"
        );

        var invalid5 = new ArrayList<>(raw);
        invalid5.remove(11);
        invalid5.add(11, 987);
        assertThrows(
            IOException.class,
            () -> Trip.of(invalid5),
            "Creation of Trip with invalid destinationName field successful"
        );

        var invalid6 = new ArrayList<>(raw);
        invalid6.remove(12);
        invalid6.add(12, 456.78);
        assertThrows(
            IOException.class,
            () -> Trip.of(invalid6),
            "Creation of Trip with invalid destinationText field successful"
        );

        var invalid7 = new ArrayList<>(raw);
        invalid7.remove(13);
        invalid7.add(13, 'x');
        assertThrows(
            IOException.class,
            () -> Trip.of(invalid7),
            "Creation of Trip with invalid vehicleID field successful"
        );

        var invalid8 = new ArrayList<>(raw);
        invalid8.remove(14);
        invalid8.add(14, 1.2);
        assertThrows(
            IOException.class,
            () -> Trip.of(invalid8),
            "Creation of Trip with invalid id field successful"
        );

        var invalid9 = new ArrayList<>(raw);
        invalid9.remove(15);
        invalid9.add(15, 456);
        assertThrows(
            IOException.class,
            () -> Trip.of(invalid9),
            "Creation of Trip with invalid estimatedTime field successful"
        );

        var invalid10 = new ArrayList<>(raw);
        invalid10.remove(15);
        assertThrows(
            IOException.class,
            () -> Trip.of(invalid10),
            "Creation of Trip with too short list successful"
        );

        var invalid11 = new ArrayList<>(raw);
        invalid11.set(10, 3);
        assertThrows(
            IOException.class,
            () -> Trip.of(invalid11),
            "Creation of Trip with direction ID 3 successful"
        );
    }
}
