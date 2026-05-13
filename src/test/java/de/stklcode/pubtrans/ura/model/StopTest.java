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
 * Unit test for the {@link Stop} model.
 *
 * @author Stefan Kalscheuer
 */
class StopTest {
    @Test
    void basicConstructorTest() {
        Stop stop = new Stop("id", "name", "indicator", 1, 2.345, 6.789);
        assertEquals("id", stop.id());
        assertEquals("name", stop.name());
        assertEquals("indicator", stop.indicator());
        assertEquals(1, stop.state());
        assertEquals(2.345, stop.latitude());
        assertEquals(6.789, stop.longitude());
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
        raw.add(4.321);

        Stop stop = assertDoesNotThrow(() -> Stop.of(raw), "Creation of Stop from valid list failed");
        assertEquals("stopId", stop.id());
        assertEquals("stopName", stop.name());
        assertEquals("stopIndicator", stop.indicator());
        assertEquals(9, stop.state());
        assertEquals(8.765, stop.latitude());
        assertEquals(4.321, stop.longitude());

        /* Excess elements should be ignored */
        raw.add("foo");
        stop = assertDoesNotThrow(() -> Stop.of(raw), "Creation of Stop from valid list failed");
        assertNotNull(stop);
        raw.remove(7);

        /* Test exceptions on invalid data */
        List<Serializable> invalid1 = new ArrayList<>(raw);
        invalid1.remove(1);
        invalid1.add(1, 5);
        assertThrows(
            IOException.class,
            () -> Stop.of(invalid1),
            "Creation of Stop with invalid name field successful"
        );

        var invalid2 = new ArrayList<>(raw);
        invalid2.remove(2);
        invalid2.add(2, 0);
        assertThrows(
            IOException.class,
            () -> Stop.of(invalid2),
            "Creation of Stop with invalid id field successful"
        );

        var invalid3 = new ArrayList<>(raw);
        invalid3.remove(3);
        invalid3.add(3, -1.23);
        assertThrows(
            IOException.class,
            () -> Stop.of(invalid3),
            "Creation of Stop with invalid indicator field successful"
        );

        var invalid4 = new ArrayList<>(raw);
        invalid4.remove(4);
        invalid4.add(4, "foo");
        assertThrows(
            IOException.class,
            () -> Stop.of(invalid4),
            "Creation of Stop with invalid state field successful"
        );

        var invalid5 = new ArrayList<>(raw);
        invalid5.remove(5);
        invalid5.add(5, "123");
        assertThrows(
            IOException.class,
            () -> Stop.of(invalid5),
            "Creation of Stop with invalid latitude field successful"
        );

        var invalid6 = new ArrayList<>(raw);
        invalid6.remove(6);
        invalid6.add(6, 456);
        assertThrows(
            IOException.class,
            () -> Stop.of(invalid6),
            "Creation of Stop with invalid longitude field successful"
        );

        var invalid7 = new ArrayList<>(raw);
        invalid7.remove(6);
        assertThrows(
            IOException.class,
            () -> Stop.of(invalid7),
            "Creation of Stop with too short list successful"
        );
    }
}
