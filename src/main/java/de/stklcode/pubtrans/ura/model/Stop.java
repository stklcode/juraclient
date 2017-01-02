/*
 * Copyright 2016 Stefan Kalscheuer
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
 * @author Stefan Kalscheuer <stefan@stklcode.de>
 */
public class Stop {
    private final String id;
    private final String name;
    private final String indicator;
    private final Integer state;
    private final Double latitude;
    private final Double longitude;

    public Stop(String id, String name, String indicator, Integer state, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.indicator = indicator;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Stop(List raw) throws IOException {
        if (raw == null || raw.size() < 7)
            throw new IOException("Invalid number of fields");

        if (raw.get(1) instanceof String)
            name = (String)raw.get(1);
        else
            throw new IOException("Field 1 not of expected type String, found " + raw.get(1).getClass().getSimpleName());
        if (raw.get(2) instanceof String)
            id = (String)raw.get(2);
        else
            throw new IOException("Field 2 not of expected type String, found " + raw.get(2).getClass().getSimpleName());
        if (raw.get(3) instanceof String)
            indicator = (String)raw.get(3);
        else
            throw new IOException("Field 3 not of expected type String, found " + raw.get(3).getClass().getSimpleName());
        if (raw.get(4) instanceof Integer)
            state = (Integer)raw.get(4);
        else
            throw new IOException("Field 4 not of expected type Integer, found " + raw.get(4).getClass().getSimpleName());
        if (raw.get(5) instanceof Double)
            latitude = (Double)raw.get(5);
        else
            throw new IOException("Field 5 not of expected type Double, found " + raw.get(5).getClass().getSimpleName());
        if (raw.get(6) instanceof Double)
            longitude = (Double)raw.get(6);
        else
            throw new IOException("Field 6 not of expected type Double, found " + raw.get(6).getClass().getSimpleName());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIndicator() {
        return indicator;
    }

    public Integer getState() {
        return state;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
