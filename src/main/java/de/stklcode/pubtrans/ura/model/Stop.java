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

package de.stklcode.pubtrans.ura.model;

import java.io.IOException;
import java.util.List;

/**
 * Entity for a single stop.
 *
 * @author Stefan Kalscheuer
 */
public class Stop {
    private static final int STOP_NAME = 1;
    private static final int STOP_ID = 2;
    private static final int INDICATOR = 3;
    private static final int STATE = 4;
    private static final int LATITUDE = 5;
    private static final int LONGITUDE = 6;
    private static final int NUM_OF_FIELDS = 7;

    private final String id;
    private final String name;
    private final String indicator;
    private final Integer state;
    private final Double latitude;
    private final Double longitude;

    /**
     * Construct Stop object.
     *
     * @param id        Stop ID.
     * @param name      Stop name.
     * @param indicator Stop indicator.
     * @param state     Stop state.
     * @param latitude  Stop geolocation latitude.
     * @param longitude Stop geolocation longitude.
     */
    public Stop(String id, String name, String indicator, Integer state, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.indicator = indicator;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Construct Stop object from raw list of attributes parsed from JSON.
     *
     * @param raw List of attributes from JSON line
     * @throws IOException Thrown on invalid line format.
     */
    public Stop(List raw) throws IOException {
        if (raw == null || raw.size() < NUM_OF_FIELDS) {
            throw new IOException("Invalid number of fields");
        }

        if (raw.get(1) instanceof String) {
            name = (String) raw.get(STOP_NAME);
        } else {
            throw new IOException("Field " + STOP_NAME + " not of expected type String, found "
                    + raw.get(STOP_NAME).getClass().getSimpleName());
        }

        if (raw.get(STOP_ID) instanceof String) {
            id = (String) raw.get(STOP_ID);
        } else {
            throw new IOException("Field " + STOP_ID + " not of expected type String, found "
                    + raw.get(STOP_ID).getClass().getSimpleName());
        }

        if (raw.get(INDICATOR) instanceof String) {
            indicator = (String) raw.get(INDICATOR);
        } else if (raw.get(INDICATOR) == null) {
            indicator = null;
            } else {
            throw new IOException("Field " + INDICATOR + " not of expected type String, found "
                    + raw.get(INDICATOR).getClass().getSimpleName());
        }

        if (raw.get(STATE) instanceof Integer) {
            state = (Integer) raw.get(STATE);
        } else {
            throw new IOException("Field " + STATE + " not of expected type Integer, found "
                    + raw.get(STATE).getClass().getSimpleName());
        }

        if (raw.get(LATITUDE) instanceof Double) {
            latitude = (Double) raw.get(LATITUDE);
        } else {
            throw new IOException("Field " + LATITUDE + " not of expected type Double, found "
                    + raw.get(LATITUDE).getClass().getSimpleName());
        }

        if (raw.get(LONGITUDE) instanceof Double) {
            longitude = (Double) raw.get(LONGITUDE);
        } else {
            throw new IOException("Field " + LONGITUDE + " not of expected type Double, found "
                    + raw.get(LONGITUDE).getClass().getSimpleName());
        }
    }

    /**
     * @return The stop ID.
     */
    public String getId() {
        return id;
    }

    /**
     * @return The stop name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The stop indicator.
     */
    public String getIndicator() {
        return indicator;
    }

    /**
     * @return The stop indicator.
     */
    public Integer getState() {
        return state;
    }

    /**
     * @return The stop geoloaction latitude.
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @return The stop geolocation longitude.
     */
    public Double getLongitude() {
        return longitude;
    }
}
