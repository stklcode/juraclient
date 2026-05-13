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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Entity for a message.
 *
 * @param stop     The stop, the message is targeted.
 * @param uuid     Message's unique identifier.
 * @param type     Messages are assigned a type.
 *                 This is predominantly in order to define how they should be displayed on on-street signs, however can
 *                 be used to alter display on other devices.
 *                 <ul>
 *                   <li>0: “Normal”</li>
 *                   <li>1: “Special”</li>
 *                   <li>2: “Full Matrix” – Stop is temporarily out of service and predictions should not be presented</li>
 *                 </ul>
 * @param priority Messages are assigned a priority in order for them to be ranked.
 *                 Since it is possible for a stop to be assigned multiple messages it is important to ensure priority
 *                 is given.* Priorities are between 1 and 10 (where 1 is the highest priority). By default, the message
 *                 priority is set to 3.
 * @param text     The text of the message. This should be displayed to the public.
 * @author Stefan Kalscheuer
 * @since 1.3
 * @since 3.0 record
 */
public record Message(Stop stop, String uuid, int type, int priority, String text) implements Model {

    private static final int MSG_UUID = 7;
    private static final int MSG_TYPE = 8;
    private static final int MSG_PRIORITY = 9;
    private static final int MSG_TEXT = 10;
    private static final int NUM_OF_FIELDS = 11;

    /**
     * Construct Message object from complete set of data.
     *
     * @param stopID        Stop ID.
     * @param stopName      Stop name.
     * @param stopIndicator Stop Indicator.
     * @param stopState     Stop state.
     * @param stopLatitude  Stop geolocation latitude.
     * @param stopLongitude Stop geolocation latitude.
     * @param msgUUID       Message UUID.
     * @param msgType       Message type.
     * @param msgPriority   Message priority.
     * @param msgText       Message text.
     */
    public Message(final String stopID,
                   final String stopName,
                   final String stopIndicator,
                   final Integer stopState,
                   final Double stopLatitude,
                   final Double stopLongitude,
                   final String msgUUID,
                   final Integer msgType,
                   final Integer msgPriority,
                   final String msgText) {
        this(new Stop(stopID,
                stopName,
                stopIndicator,
                stopState,
                stopLatitude,
                stopLongitude),
            msgUUID,
            msgType,
            msgPriority,
            msgText);
    }

    /**
     * Construct Message object from raw list of attributes parsed from JSON.
     *
     * @param raw List of attributes from JSON line
     * @throws IOException Thrown on invalid line format.
     */
    public static Message of(final List<Serializable> raw) throws IOException {
        return of(raw, null);
    }

    /**
     * Construct Message object from raw list of attributes parsed from JSON with explicitly specified version.
     *
     * @param raw     List of attributes from JSON line
     * @param version API version
     * @throws IOException Thrown on invalid line format.
     */
    public static Message of(final List<Serializable> raw, final String version) throws IOException {
        if (raw == null || raw.size() < NUM_OF_FIELDS) {
            throw new IOException("Invalid number of fields");
        }

        var stop = Stop.of(raw);

        if (!(raw.get(MSG_UUID) instanceof String uuid)) {
            throw Model.typeErrorString(MSG_UUID, raw.get(MSG_UUID).getClass());
        }

        if (!(raw.get(MSG_TYPE) instanceof Integer type)) {
            throw Model.typeError(MSG_TYPE, raw.get(MSG_TYPE).getClass(), "Integer");
        }

        if (!(raw.get(MSG_PRIORITY) instanceof Integer priority)) {
            throw Model.typeError(MSG_PRIORITY, raw.get(MSG_PRIORITY).getClass(), "Integer");
        }

        if (!(raw.get(MSG_TEXT) instanceof String text)) {
            throw Model.typeErrorString(MSG_TEXT, raw.get(MSG_TEXT).getClass());
        }

        return new Message(stop, uuid, type, priority, text);
    }
}
