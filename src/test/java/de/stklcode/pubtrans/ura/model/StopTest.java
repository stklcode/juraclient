/*
 * Copyright 2016-2019 Stefan Kalscheuer
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test for the Stop metamodel.
 *
 * @author Stefan Kalscheuer
 */
public class StopTest {
    @Test
    public void basicConstructorTest() {
        Stop stop = new Stop("id", "name", "indicator", 1, 2.345, 6.789);
        assertThat(stop.getId(), is("id"));
        assertThat(stop.getName(), is("name"));
        assertThat(stop.getIndicator(), is("indicator"));
        assertThat(stop.getState(), is(1));
        assertThat(stop.getLatitude(), is(2.345));
        assertThat(stop.getLongitude(), is(6.789));
    }

    @Test
    public void listConstructorTest() {
        /* Create valid raw data list */
        List<Object> raw = new ArrayList<>();
        raw.add(1);
        raw.add("stopName");
        raw.add("stopId");
        raw.add("stopIndicator");
        raw.add(9);
        raw.add(8.765);
        raw.add(4.321);

        try {
            Stop stop = new Stop(raw);
            assertThat(stop.getId(), is("stopId"));
            assertThat(stop.getName(), is("stopName"));
            assertThat(stop.getIndicator(), is("stopIndicator"));
            assertThat(stop.getState(), is(9));
            assertThat(stop.getLatitude(), is(8.765));
            assertThat(stop.getLongitude(), is(4.321));
        } catch (IOException e) {
            fail("Creation of Stop from valid list failed: " + e.getMessage());
        }

        /* Excess elements should be ignored */
        raw.add("foo");
        try {
            Stop stop = new Stop(raw);
            assertThat(stop, is(notNullValue()));
            raw.remove(7);
        } catch (IOException e) {
            fail("Creation of Stop from valid list failed: " + e.getMessage());
        }

        /* Test exceptions on invalid data */
        List<Object> invalid = new ArrayList<>(raw);
        invalid.remove(1);
        invalid.add(1, 5);
        try {
            new Stop(invalid);
            fail("Creation of Stop with invalid name field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(2);
        invalid.add(2, 0);
        try {
            new Stop(invalid);
            fail("Creation of Stop with invalid id field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(3);
        invalid.add(3, -1.23);
        try {
            new Stop(invalid);
            fail("Creation of Stop with invalid indicator field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(4);
        invalid.add(4, "foo");
        try {
            new Stop(invalid);
            fail("Creation of Stop with invalid state field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(5);
        invalid.add(5, "123");
        try {
            new Stop(invalid);
            fail("Creation of Stop with invalid latitude field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(6);
        invalid.add(6, 456);
        try {
            new Stop(invalid);
            fail("Creation of Stop with invalid longitude field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(6);
        try {
            new Stop(invalid);
            fail("Creation of Stop with too short list successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }
    }
}
