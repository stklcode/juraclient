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
 * Entity for a single stop.
 *
 * @param id        Stop identifier.
 * @param name      The name of the bus stop.
 * @param indicator The letter(s) that are displayed on top of the bus stop flag (e.g. SA).
 *                  These are used to help passengers easily identify a bus stop from others in the locality.
 * @param state     The different stop states and their definitions are provided below:
 *                  <ul>
 *                    <li>0: “Open”: Bus stop is being served as usual</li>
 *                    <li>1: “Temporarily Closed” : Vehicles are not serving the stop but may be serving a nearby bus
 *                            stop, predictions may be available</li>
 *                    <li>2: “Closed” : Vehicles are not serving the stop.
 *                           Stop should display the closed message and predictions should not be shown.</li>
 *                    <li>3: “Suspended” : Vehicles are not serving the stop.
 *                           Stop should display the closed message and predictions should not be shown.</li>
 *                  </ul>
 * @param latitude  The latitude of the stop. This is expressed using the WGS84 coordinate system.
 * @param longitude The longitude of the stop. This is expressed using the WGS84 coordinate system.
 * @author Stefan Kalscheuer
 * @since 2.0 record
 */
public record Stop(
    String id,
    String name,
    String indicator,
    int state,
    double latitude,
    double longitude
) implements Model {
    private static final int F_STOP_NAME = 1;
    private static final int F_STOP_ID = 2;
    private static final int F_INDICATOR = 3;
    private static final int F_STATE = 4;
    private static final int F_LATITUDE = 5;
    private static final int F_LONGITUDE = 6;
    private static final int F_NUM_OF_FIELDS = 7;

    /**
     * Construct Stop object from raw list of attributes parsed from JSON.
     *
     * @param raw List of attributes from JSON line
     * @throws IOException Thrown on invalid line format.
     */
    public static Stop of(final List<Serializable> raw) throws IOException {
        if (raw == null || raw.size() < F_NUM_OF_FIELDS) {
            throw new IOException("Invalid number of fields");
        }

        if (!(raw.get(F_STOP_NAME) instanceof String name)) {
            throw Model.typeErrorString(F_STOP_NAME, raw.get(F_STOP_NAME).getClass());
        }

        if (!(raw.get(F_STOP_ID) instanceof String id)) {
            throw Model.typeErrorString(F_STOP_ID, raw.get(F_STOP_ID).getClass());
        }

        String indicator;
        if (raw.get(F_INDICATOR) instanceof String indicatorStr) {
            indicator = indicatorStr;
        } else if (raw.get(F_INDICATOR) == null) {
            indicator = null;
        } else {
            throw Model.typeErrorString(F_INDICATOR, raw.get(F_INDICATOR).getClass());
        }

        if (!(raw.get(F_STATE) instanceof Integer state)) {
            throw Model.typeError(F_STATE, raw.get(F_STATE).getClass(), "Integer");
        }

        if (!(raw.get(F_LATITUDE) instanceof Double latitude)) {
            throw Model.typeError(F_LATITUDE, raw.get(F_LATITUDE).getClass(), "Double");
        }

        if (!(raw.get(F_LONGITUDE) instanceof Double longitude)) {
            throw Model.typeError(F_LONGITUDE, raw.get(F_LONGITUDE).getClass(), "Double");
        }

        return new Stop(id, name, indicator, state, latitude, longitude);
    }
}
