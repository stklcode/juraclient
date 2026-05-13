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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test for the {@link Trip} model.
 *
 * @author Stefan Kalscheuer
 */
class TripTest {
    @Test
    void basicConstructorTest() {
        Trip trip = new Trip("sid",
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
            123456789123456789L);
        assertThat(trip.stop().id(), is("sid"));
        assertThat(trip.stop().name(), is("name"));
        assertThat(trip.stop().indicator(), is("indicator"));
        assertThat(trip.stop().state(), is(1));
        assertThat(trip.stop().latitude(), is(2.345));
        assertThat(trip.stop().longitude(), is(6.789));
        assertThat(trip.visitID(), is(123));
        assertThat(trip.lineID(), is("lineid"));
        assertThat(trip.lineName(), is("linename"));
        assertThat(trip.directionID(), is(0));
        assertThat(trip.destinationName(), is("destination name"));
        assertThat(trip.destinationText(), is("destination text"));
        assertThat(trip.vehicleID(), is("vehicle"));
        assertThat(trip.id(), is("id"));
        assertThat(trip.estimatedTime(), is(123456789123456789L));
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

        try {
            Trip trip = Trip.of(raw);
            assertThat(trip.stop().id(), is("stopId"));
            assertThat(trip.stop().name(), is("stopName"));
            assertThat(trip.stop().indicator(), is("stopIndicator"));
            assertThat(trip.stop().state(), is(9));
            assertThat(trip.stop().latitude(), is(8.765));
            assertThat(trip.stop().longitude(), is(43.21));
            assertThat(trip.visitID(), is(123));
            assertThat(trip.lineID(), is("lineid"));
            assertThat(trip.lineName(), is("linename"));
            assertThat(trip.directionID(), is(0));
            assertThat(trip.destinationName(), is("destination name"));
            assertThat(trip.destinationText(), is("destination text"));
            assertThat(trip.vehicleID(), is("vehicle"));
            assertThat(trip.id(), is("9876543210"));
            assertThat(trip.estimatedTime(), is(123456789123456789L));
        } catch (IOException e) {
            fail("Creation of Trip from valid list failed: " + e.getMessage());
        }

        /* Test with V2 style list */
        raw.set(14, "id");
        try {
            Trip trip = Trip.of(raw, "2.0");
            assertThat(trip.id(), is("id"));
        } catch (IOException e) {
            fail("Creation of Trip from valid list failed: " + e.getMessage());
        }
        raw.set(14, 9876543210L);

        /* Excess elements should be ignored */
        raw.add("foo");
        try {
            Trip trip = Trip.of(raw);
            assertThat(trip, is(notNullValue()));
            raw.remove(16);
        } catch (IOException e) {
            fail("Creation of Trip from valid list failed: " + e.getMessage());
        }

        raw.remove(10);
        raw.add(10, 0L);    // Long values are OK.
        try {
            Trip trip = Trip.of(raw);
            assertThat(trip, is(notNullValue()));
            assertThat(trip.directionID(), is(0));
        } catch (IOException e) {
            fail("Creation of Trip from valid list failed: " + e.getMessage());
        }

        raw.remove(10);
        raw.add(10, "0");    // String values are OK.
        try {
            Trip trip = Trip.of(raw);
            assertThat(trip, is(notNullValue()));
            assertThat(trip.directionID(), is(0));
        } catch (IOException e) {
            fail("Creation of Trip from valid list failed: " + e.getMessage());
        }

        /* Test exceptions on invalid data */
        List<Serializable> invalid = new ArrayList<>(raw);
        invalid.remove(7);
        invalid.add(7, "123");
        try {
            Trip.of(invalid);
            fail("Creation of Trip with invalid visitID field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(8);
        invalid.add(8, 25);
        try {
            Trip.of(invalid);
            fail("Creation of Trip with invalid lineID field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(9);
        invalid.add(9, 234L);
        try {
            Trip.of(invalid);
            fail("Creation of Trip with invalid line name field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(10);
        invalid.add(10, "7");   // Strings are generally OK, but 7 is out of range (#2).
        try {
            Trip.of(invalid);
            fail("Creation of Trip with invalid directionID field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(11);
        invalid.add(11, 987);
        try {
            Trip.of(invalid);
            fail("Creation of Trip with invalid destinationName field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(12);
        invalid.add(12, 456.78);
        try {
            Trip.of(invalid);
            fail("Creation of Trip with invalid destinationText field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(13);
        invalid.add(13, 'x');
        try {
            Trip.of(invalid);
            fail("Creation of Trip with invalid vehicleID field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(14);
        invalid.add(14, 1.2);
        try {
            Trip.of(invalid);
            fail("Creation of Trip with invalid id field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(15);
        invalid.add(15, 456);
        try {
            Trip.of(invalid);
            fail("Creation of Trip with invalid estimatedTime field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(15);
        try {
            Trip.of(invalid);
            fail("Creation of Trip with too short list successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.set(10, 3);
        try {
            Trip.of(invalid);
            fail("Creation of Trip with direction ID 3 successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }
    }
}
