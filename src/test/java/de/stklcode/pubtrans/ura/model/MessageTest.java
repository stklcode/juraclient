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

;

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
        assertEquals("sid", message.stop().id());
        assertEquals("name", message.stop().name());
        assertEquals("indicator", message.stop().indicator());
        assertEquals(1, message.stop().state());
        assertEquals(2.345, message.stop().latitude());
        assertEquals(6.789, message.stop().longitude());
        assertEquals("msg_uuid", message.uuid());
        assertEquals(1, message.type());
        assertEquals(3, message.priority());
        assertEquals("message text", message.text());
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

        Message message = assertDoesNotThrow(() -> Message.of(raw), "Creation of Message from valid list failed");
        assertEquals("stopId", message.stop().id());
        assertEquals("stopName", message.stop().name());
        assertEquals("stopIndicator", message.stop().indicator());
        assertEquals(9, message.stop().state());
        assertEquals(8.765, message.stop().latitude());
        assertEquals(43.21, message.stop().longitude());
        assertEquals("msg_uuid", message.uuid());
        assertEquals(1, message.type());
        assertEquals(3, message.priority());
        assertEquals("message text", message.text());

        /* Excess elements should be ignored */
        raw.add("foo");
        assertNotNull(assertDoesNotThrow(() -> Message.of(raw), "Creation of Message from valid list failed"));
        raw.remove(11);

        /* Test exceptions on invalid data */
        var invalid1 = new ArrayList<>(raw);
        invalid1.remove(7);
        invalid1.add(7, 123L);
        assertThrows(
            IOException.class,
            () -> Message.of(invalid1),
            "Creation of Message with invalid UUID field successful"
        );

        var invalid2 = new ArrayList<>(raw);
        invalid2.remove(8);
        invalid2.add(8, "abc");
        assertThrows(
            IOException.class,
            () -> Message.of(invalid2),
            "Creation of Message with invalid type field successful"
        );

        var invalid3 = new ArrayList<>(raw);
        invalid3.remove(9);
        invalid3.add(9, "xyz");
        assertThrows(
            IOException.class,
            () -> Message.of(invalid3),
            "Creation of Message with invalid priority field successful"
        );

        var invalid4 = new ArrayList<>(raw);
        invalid4.remove(10);
        invalid4.add(10, 1.23);
        assertThrows(
            IOException.class,
            () -> Message.of(invalid4),
            "Creation of Message with invalid text field successful"
        );

        var invalid5 = new ArrayList<>(raw);
        invalid5.remove(10);
        assertThrows(
            IOException.class,
            () -> Message.of(invalid5),
            "Creation of Message with too short list successful"
        );
    }
}
