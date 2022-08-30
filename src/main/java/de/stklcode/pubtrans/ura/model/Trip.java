/*
 * Copyright 2016-2022 Stefan Kalscheuer
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
 * @author Stefan Kalscheuer
 */
public final class Trip implements Model {
    private static final long serialVersionUID = 7477381188869237381L;

    private static final int VISIT_ID = 7;
    private static final int LINE_ID = 8;
    private static final int LINE_NAME = 9;
    private static final int DIRECTION_ID = 10;
    private static final int DESTINATION_NAME = 11;
    private static final int DESTINATION_TEXT = 12;
    private static final int VEHICLE_ID = 13;
    private static final int TRIP_ID = 14;
    private static final int ESTIMATED_TIME = 15;
    private static final int NUM_OF_FIELDS = 16;

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

    /**
     * Construct Trip object from complete set of data.
     *
     * @param stopID          Stop ID.
     * @param stopName        Stop name.
     * @param stopIndicator   Stop Indicator.
     * @param stopState       Stop state.
     * @param stopLatitude    Stop geolocation latitude.
     * @param stopLongitude   Stop geolocation latitude.
     * @param visitID         Visit ID.
     * @param lineID          Line ID.
     * @param lineName        Line name.
     * @param directionID     Direction ID.
     * @param destinationName Destination name.
     * @param destinationText Destination text.
     * @param vehicleID       Vehicle ID.
     * @param tripID          Trip ID.
     * @param estimatedTime   Estimated time.
     */
    public Trip(final String stopID,
                final String stopName,
                final String stopIndicator,
                final Integer stopState,
                final Double stopLatitude,
                final Double stopLongitude,
                final Integer visitID,
                final String lineID,
                final String lineName,
                final Integer directionID,
                final String destinationName,
                final String destinationText,
                final String vehicleID,
                final String tripID,
                final Long estimatedTime) {
        this(new Stop(stopID,
                        stopName,
                        stopIndicator,
                        stopState,
                        stopLatitude,
                        stopLongitude),
                visitID,
                lineID,
                lineName,
                directionID,
                destinationName,
                destinationText,
                vehicleID,
                tripID,
                estimatedTime);
    }

    /**
     * Construct Trip object from Stop model and set of additional data.
     *
     * @param stop            Stop model
     * @param visitID         Visit ID
     * @param lineID          Line ID
     * @param lineName        Line name
     * @param directionID     Direction ID
     * @param destinationName Destination name
     * @param destinationText Destination text
     * @param vehicleID       Vehicle ID
     * @param tripID          Trip ID
     * @param estimatedTime   Estimated time
     */
    public Trip(final Stop stop,
                final Integer visitID,
                final String lineID,
                final String lineName,
                final Integer directionID,
                final String destinationName,
                final String destinationText,
                final String vehicleID,
                final String tripID,
                final Long estimatedTime) {
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

    /**
     * Construct Trip object from raw list of attributes parsed from JSON.
     *
     * @param raw List of attributes from JSON line
     * @throws IOException Thrown on invalid line format.
     */
    public Trip(final List<?> raw) throws IOException {
        this(raw, null);
    }

    /**
     * Construct Trip object from raw list of attributes parsed from JSON with explicitly specified version.
     *
     * @param raw     List of attributes from JSON line
     * @param version API version
     * @throws IOException Thrown on invalid line format.
     */
    public Trip(final List<?> raw, final String version) throws IOException {
        if (raw == null || raw.size() < NUM_OF_FIELDS) {
            throw new IOException("Invalid number of fields");
        }

        stop = new Stop(raw);

        if (raw.get(VISIT_ID) instanceof Integer) {
            visitID = (Integer) raw.get(VISIT_ID);
        } else {
            throw Model.typeError(VISIT_ID, raw.get(VISIT_ID).getClass(), "Integer");
        }

        if (raw.get(LINE_ID) instanceof String) {
            lineID = (String) raw.get(LINE_ID);
        } else {
            throw Model.typeErrorString(LINE_ID, raw.get(LINE_ID).getClass());
        }

        if (raw.get(LINE_NAME) instanceof String) {
            lineName = (String) raw.get(LINE_NAME);
        } else {
            throw Model.typeErrorString(LINE_NAME, raw.get(LINE_NAME).getClass());
        }

        if (raw.get(DIRECTION_ID) instanceof String                 // Also accept Strings (#2)
                || raw.get(DIRECTION_ID) instanceof Integer
                || raw.get(DIRECTION_ID) instanceof Long) {
            directionID = Integer.valueOf(raw.get(DIRECTION_ID).toString());
            if (directionID < 0 || directionID > 2) {
                throw new IOException("Direction out of range. Expected 1 or 2, found " + directionID);
            }
        } else {
            throw Model.typeError(DIRECTION_ID, raw.get(DIRECTION_ID).getClass(), "String/Long/Integer");
        }

        if (raw.get(DESTINATION_NAME) instanceof String) {
            destinationName = (String) raw.get(DESTINATION_NAME);
        } else {
            throw Model.typeErrorString(DESTINATION_NAME, raw.get(DESTINATION_NAME).getClass());
        }

        if (raw.get(DESTINATION_TEXT) instanceof String) {
            destinationText = (String) raw.get(DESTINATION_TEXT);
        } else {
            throw Model.typeErrorString(DESTINATION_TEXT, raw.get(DESTINATION_TEXT).getClass());
        }

        /* TFL and ASEAG deliver different types with the same API version, so this field is a little more tolerant */
        if (raw.get(VEHICLE_ID) instanceof String
                || raw.get(VEHICLE_ID) instanceof Integer
                || raw.get(VEHICLE_ID) instanceof Long) {
            vehicleID = raw.get(VEHICLE_ID).toString();
        } else if (raw.get(VEHICLE_ID) == null) {   // Only fail of field is not NULL (#3).
            vehicleID = null;
        } else {
            throw Model.typeError(VEHICLE_ID, raw.get(VEHICLE_ID).getClass(), "String/Integer/Long");
        }

        if (raw.get(TRIP_ID) instanceof String
                || raw.get(TRIP_ID) instanceof Integer
                || raw.get(TRIP_ID) instanceof Long) {
            id = raw.get(TRIP_ID).toString();
        } else {
            throw Model.typeError(TRIP_ID, raw.get(TRIP_ID).getClass(), "String/Integer/Long");
        }

        if (raw.get(ESTIMATED_TIME) instanceof Long) {
            estimatedTime = (Long) raw.get(ESTIMATED_TIME);
        } else {
            throw Model.typeError(ESTIMATED_TIME, raw.get(ESTIMATED_TIME).getClass(), "Long");
        }
    }

    /**
     * @return The (starting) stop.
     */
    public Stop getStop() {
        return stop;
    }

    /**
     * @return The trip ID.
     */
    public String getId() {
        return id;
    }

    /**
     * @return The visit ID.
     */
    public Integer getVisitID() {
        return visitID;
    }

    /**
     * @return The line ID.
     */
    public String getLineID() {
        return lineID;
    }

    /**
     * @return The line name.
     */
    public String getLineName() {
        return lineName;
    }

    /**
     * @return The direction ID.
     */
    public Integer getDirectionID() {
        return directionID;
    }

    /**
     * @return The destination name.
     */
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * @return The destination text.
     */
    public String getDestinationText() {
        return destinationText;
    }

    /**
     * @return The estimated departure time.
     */
    public Long getEstimatedTime() {
        return estimatedTime;
    }

    /**
     * @return The vehicle ID or {@code null} if not present.
     */
    public String getVehicleID() {
        return vehicleID;
    }
}
