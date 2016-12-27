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
 * Entity for a single trip.
 *
 * @author Stefan Kalscheuer <stefan@stklcode.de>
 */
public class Trip {
    private final Stop stop;
    private final String id;
    private final Integer visitID;
    private final String lineID;
    private final String lineName;
    private final Integer directionID;
    private final String destinationName;
    private final String destinationText;
    private final Long estimatedTime;
    private final String vehicleID;

    public Trip(String stopID, String stopName, String stopIndicator, Integer stopState, Double stopLatitude, Double stopLongitude,
                Integer visitID, String lineID, String lineName, Integer directionID, String destinationName, String destinationText, String vehicleID, String tripID, Long estimatedTime) {
        this(new Stop(stopID, stopName, stopIndicator, stopState, stopLatitude, stopLongitude),
                visitID, lineID, lineName, directionID, destinationName, destinationText, vehicleID, tripID, estimatedTime);
    }

    public Trip(Stop stop, Integer visitID, String lineID, String lineName, Integer directionID, String destinationName, String destinationText, String vehicleID, String tripID, Long estimatedTime) {
        this.stop = stop;
        this.visitID = visitID;
        this.lineID = lineID;
        this.lineName = lineName;
        this.directionID = directionID;
        this.destinationName = destinationName;
        this.destinationText = destinationText;
        this.vehicleID = vehicleID;
        this.id = tripID;
        this.estimatedTime = estimatedTime;
    }

    public Trip(List raw) throws IOException {
        if (raw == null || raw.size() < 16)
            throw new IOException("Invalid number of fields");

        stop = new Stop(raw);

        if (raw.get(7) instanceof Integer)
            visitID = (Integer) raw.get(7);
        else
            throw new IOException("Field 7 not of expected type Integer, found " + raw.get(7).getClass().getSimpleName());
        if (raw.get(8) instanceof String)
            lineID = (String)raw.get(8);
        else
            throw new IOException("Field 8 not of expected type String, found " + raw.get(8).getClass().getSimpleName());
        if (raw.get(9) instanceof String)
            lineName = (String)raw.get(9);
        else
            throw new IOException("Field 9 not of expected type String, found " + raw.get(9).getClass().getSimpleName());
        if (raw.get(10) instanceof Integer)
            directionID = (Integer)raw.get(10);
        else
            throw new IOException("Field 10 not of expected type Integer, found " + raw.get(10).getClass().getSimpleName());
        if (raw.get(11) instanceof String)
            destinationName = (String)raw.get(11);
        else
            throw new IOException("Field 11 not of expected type String, found " + raw.get(11).getClass().getSimpleName());
        if (raw.get(12) instanceof String)
            destinationText = (String)raw.get(12);
        else
            throw new IOException("Field 12 not of expected type String, found " + raw.get(12).getClass().getSimpleName());
        if (raw.get(13) instanceof String)
            vehicleID = (String)raw.get(13);
        else
            throw new IOException("Field 13 not of expected type String, found " + raw.get(13).getClass().getSimpleName());
        if (raw.get(14) instanceof String)
            id = (String)raw.get(14);
        else
            throw new IOException("Field 14 not of expected type String, found " + raw.get(14).getClass().getSimpleName());
        if (raw.get(15) instanceof Long)
            estimatedTime = (Long)raw.get(15);
        else
            throw new IOException("Field 15 not of expected type Long, found " + raw.get(15).getClass().getSimpleName());
    }

    public Stop getStop() {
        return stop;
    }

    public String getId() {
        return id;
    }

    public Integer getVisitID() {
        return visitID;
    }

    public String getLineID() {
        return lineID;
    }

    public String getLineName() {
        return lineName;
    }

    public Integer getDirectionID() {
        return directionID;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getDestinationText() {
        return destinationText;
    }

    public Long getEstimatedTime() {
        return estimatedTime;
    }

    public String getVehicleID() {
        return vehicleID;
    }
}
