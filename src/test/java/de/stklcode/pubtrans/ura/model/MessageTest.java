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
 * Unit test for the {@link Message} model.
 *
 * @author Stefan Kalscheuer
 */
class MessageTest {
    @Test
    void basicConstructorTest() {
        Message message = new Message(
            "sid",
            "name",
            "indicator",
            1,
            2.345,
            6.789,
            "msg_uuid",
            1,
            3,
            "message text"
        );
        assertThat(message.stop().id(), is("sid"));
        assertThat(message.stop().name(), is("name"));
        assertThat(message.stop().indicator(), is("indicator"));
        assertThat(message.stop().state(), is(1));
        assertThat(message.stop().latitude(), is(2.345));
        assertThat(message.stop().longitude(), is(6.789));
        assertThat(message.uuid(), is("msg_uuid"));
        assertThat(message.type(), is(1));
        assertThat(message.priority(), is(3));
        assertThat(message.text(), is("message text"));
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
        raw.add("msg_uuid");
        raw.add(1);
        raw.add(3);
        raw.add("message text");

        try {
            Message message = Message.of(raw);
            assertThat(message.stop().id(), is("stopId"));
            assertThat(message.stop().name(), is("stopName"));
            assertThat(message.stop().indicator(), is("stopIndicator"));
            assertThat(message.stop().state(), is(9));
            assertThat(message.stop().latitude(), is(8.765));
            assertThat(message.stop().longitude(), is(43.21));
            assertThat(message.uuid(), is("msg_uuid"));
            assertThat(message.type(), is(1));
            assertThat(message.priority(), is(3));
            assertThat(message.text(), is("message text"));
        } catch (IOException e) {
            fail("Creation of Message from valid list failed: " + e.getMessage());
        }

        /* Excess elements should be ignored */
        raw.add("foo");
        try {
            Message message = Message.of(raw);
            assertThat(message, is(notNullValue()));
            raw.remove(11);
        } catch (IOException e) {
            fail("Creation of Message from valid list failed: " + e.getMessage());
        }

        /* Test exceptions on invalid data */
        List<Serializable> invalid = new ArrayList<>(raw);
        invalid.remove(7);
        invalid.add(7, 123L);
        try {
            Message.of(invalid);
            fail("Creation of Message with invalid UUID field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(8);
        invalid.add(8, "abc");
        try {
            Message.of(invalid);
            fail("Creation of Message with invalid type field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(9);
        invalid.add(9, "xyz");
        try {
            Message.of(invalid);
            fail("Creation of Message with invalid priority field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(10);
        invalid.add(10, 1.23);
        try {
            Message.of(invalid);
            fail("Creation of Message with invalid text field successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(10);
        try {
            Message.of(invalid);
            fail("Creation of Message with too short list successful");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }
    }
}
