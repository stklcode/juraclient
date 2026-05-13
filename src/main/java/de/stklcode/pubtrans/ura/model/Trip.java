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
 * Entity for a single trip.
 *
 * @param stop            The starting stop.
 * @param id              The identifier of the specific trip that the prediction is for.
 * @param visitID         Visit identifier.
 * @param lineID          The identifier of a route. This is an internal identifier and is not equal to the route number
 *                        displayed on the front of the bus. It should not be displayed to the public.
 * @param lineName        This is the route number that is displayed on the front of the bus and on any publicity
 *                        advertising the route.
 * @param directionID     This identifies the direction of the trip that the vehicle is on.
 *                        It indicates whether the vehicle is on an outbound or inbound trip.
 * @param destinationName The full length destination name of the trip the vehicle is on.
 *                        The destination name is based on the route and end point of the trip.
 * @param destinationText The abbreviated destination name of the trip the vehicle is on.
 *                        The destination text is based on the route and end point of the trip.
 * @param estimatedTime   This is the predicted time of arrival for the vehicle at a specific stop.
 *                        It is an absolute time in UTC as per Unix epoch (in milliseconds).
 * @param vehicleID       The unique identifier of the vehicle. This is an internal identifier and should not be
 *                        displayed to the public.
 * @author Stefan Kalscheuer
 * @since 2.0 record
 */
public record Trip(
    Stop stop,
    String id,
    int visitID,
    String lineID,
    String lineName,
    int directionID,
    String destinationName,
    String destinationText,
    long estimatedTime,
    String vehicleID
) implements Model {
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
            tripID,
            visitID,
            lineID,
            lineName,
            directionID,
            destinationName,
            destinationText,
            estimatedTime,
            vehicleID);
    }


    /**
     * Construct Trip object from raw list of attributes parsed from JSON.
     *
     * @param raw List of attributes from JSON line
     * @throws IOException Thrown on invalid line format.
     */
    public static Trip of(final List<Serializable> raw) throws IOException {
        return of(raw, null);
    }

    /**
     * Construct Trip object from raw list of attributes parsed from JSON with explicitly specified version.
     *
     * @param raw     List of attributes from JSON line
     * @param version API version
     * @throws IOException Thrown on invalid line format.
     */
    public static Trip of(final List<Serializable> raw, final String version) throws IOException {
        if (raw == null || raw.size() < NUM_OF_FIELDS) {
            throw new IOException("Invalid number of fields");
        }

        var stop = Stop.of(raw);

        if (!(raw.get(VISIT_ID) instanceof Integer visitID)) {
            throw Model.typeError(VISIT_ID, raw.get(VISIT_ID).getClass(), "Integer");
        }

        if (!(raw.get(LINE_ID) instanceof String lineID)) {
            throw Model.typeErrorString(LINE_ID, raw.get(LINE_ID).getClass());
        }

        if (!(raw.get(LINE_NAME) instanceof String lineName)) {
            throw Model.typeErrorString(LINE_NAME, raw.get(LINE_NAME).getClass());
        }

        int directionID;
        if (raw.get(DIRECTION_ID) instanceof String                 // Also accept Strings (#2)
            || raw.get(DIRECTION_ID) instanceof Integer
            || raw.get(DIRECTION_ID) instanceof Long) {
            directionID = Integer.parseInt(raw.get(DIRECTION_ID).toString());
            if (directionID < 0 || directionID > 2) {
                throw new IOException("Direction out of range. Expected 1 or 2, found " + directionID);
            }
        } else {
            throw Model.typeError(DIRECTION_ID, raw.get(DIRECTION_ID).getClass(), "String/Long/Integer");
        }

        if (!(raw.get(DESTINATION_NAME) instanceof String destinationName)) {
            throw Model.typeErrorString(DESTINATION_NAME, raw.get(DESTINATION_NAME).getClass());
        }

        if (!(raw.get(DESTINATION_TEXT) instanceof String destinationText)) {
            throw Model.typeErrorString(DESTINATION_TEXT, raw.get(DESTINATION_TEXT).getClass());
        }

        /* TFL and ASEAG deliver different types with the same API version, so this field is a little more tolerant */
        String vehicleID;
        if (raw.get(VEHICLE_ID) instanceof String
            || raw.get(VEHICLE_ID) instanceof Integer
            || raw.get(VEHICLE_ID) instanceof Long) {
            vehicleID = raw.get(VEHICLE_ID).toString();
        } else if (raw.get(VEHICLE_ID) == null) {   // Only fail of field is not NULL (#3).
            vehicleID = null;
        } else {
            throw Model.typeError(VEHICLE_ID, raw.get(VEHICLE_ID).getClass(), "String/Integer/Long");
        }

        String id;
        if (raw.get(TRIP_ID) instanceof String
            || raw.get(TRIP_ID) instanceof Integer
            || raw.get(TRIP_ID) instanceof Long) {
            id = raw.get(TRIP_ID).toString();
        } else {
            throw Model.typeError(TRIP_ID, raw.get(TRIP_ID).getClass(), "String/Integer/Long");
        }

        if (!(raw.get(ESTIMATED_TIME) instanceof Long estimatedTime)) {
            throw Model.typeError(ESTIMATED_TIME, raw.get(ESTIMATED_TIME).getClass(), "Long");
        }

        return new Trip(stop, id, visitID, lineID, lineName, directionID, destinationName, destinationText, estimatedTime, vehicleID);
    }
}
