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

package de.stklcode.pubtrans.ura;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.stklcode.pubtrans.ura.model.Stop;
import de.stklcode.pubtrans.ura.model.Trip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for URA based public transport API.
 *
 * @author Stefan Kalscheuer <stefan@stklcode.de>
 */
public class UraClient {
    private static final String DEFAULT_INSTANT_URL = "/interfaces/ura/instant_V2";
    private static final String DEFAULT_STREAM_URL = "/interfaces/ura/stream_V2";

    private static final String PAR_STOP_ID = "StopID";
    private static final String PAR_STOP_NAME = "StopPointName";
    private static final String PAR_STOP_STATE = "StopPointState";
    private static final String PAR_STOP_INDICATOR = "StopPointIndicator";
    private static final String PAR_GEOLOCATION = "Latitude,Longitude";
    private static final String PAR_VISIT_NUMBER = "VisitNumber";
    private static final String PAR_LINE_ID = "LineID";
    private static final String PAR_LINE_NAME = "LineName";
    private static final String PAR_DIR_ID = "DirectionID";
    private static final String PAR_DEST_NAME = "DestinationName";
    private static final String PAR_DEST_TEXT = "DestinationText";
    private static final String PAR_VEHICLE_ID = "VehicleID";
    private static final String PAR_TRIP_ID = "TripID";
    private static final String PAR_ESTTIME = "EstimatedTime";

    private static final Integer RES_TYPE_STOP = 0;
    private static final Integer RES_TYPE_PREDICTION = 1;


    private static final String[] REQUEST_STOP = {PAR_STOP_NAME, PAR_STOP_ID, PAR_STOP_INDICATOR, PAR_STOP_STATE, PAR_GEOLOCATION};
    private static final String[] REQUEST_TRIP = {PAR_STOP_NAME, PAR_STOP_ID, PAR_STOP_INDICATOR, PAR_STOP_STATE, PAR_GEOLOCATION,
            PAR_VISIT_NUMBER, PAR_LINE_ID, PAR_LINE_NAME, PAR_DIR_ID, PAR_DEST_NAME, PAR_DEST_TEXT, PAR_VEHICLE_ID, PAR_TRIP_ID, PAR_ESTTIME};

    private final String baseURL;
    private final String instantURL;
    private final String streamURL;
    private final ObjectMapper mapper;

    /**
     * Constructor with base URL and default API paths.
     *
     * @param baseURL the base URL (with protocol, without trailing slash)
     */
    public UraClient(String baseURL) {
        this(baseURL, DEFAULT_INSTANT_URL, DEFAULT_STREAM_URL);
    }

    /**
     * Constructor with base URL and custom API paths
     *
     * @param baseURL    the base URL (including protocol)
     * @param instantURL the path for instant requests
     * @param streamURL  the path for stream requests
     */
    public UraClient(String baseURL, String instantURL, String streamURL) {
        this.baseURL = baseURL;
        this.instantURL = instantURL;
        this.streamURL = streamURL;
        this.mapper = new ObjectMapper();
    }

    /**
     * Builder pattern to request given stop IDs.
     *
     * @param stops Stop IDs
     * @return the request
     */
    public Query forStops(final String...stops) {
        return new Query().forStops(stops);
    }

    /**
     * Builder pattern to request given stop names.
     *
     * @param stopNames Stop Point Names
     * @return the request
     */
    public Query forStopsByName(final String...stopNames) {
        return new Query().forStopsByName(stopNames);
    }

    /**
     * Builder pattern to request given line IDs.
     *
     * @param lines line IDs
     * @return the request
     */
    public Query forLines(final String...lines) {
        return new Query().forLines(lines);
    }

    /**
     * Builder pattern to request given line names.
     *
     * @param lineNames line names
     * @return the request
     */
    public Query forLinesByName(final String...lineNames) {
        return new Query().forLinesByName(lineNames);
    }

    /**
     * Builder pattern to request given direction.
     *
     * @param direction the direction ID
     * @return the request
     */
    public Query forDirection(final Integer direction) {
        return new Query().forDirection(direction);
    }

    /**
     * Get list of trips.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @return list of trips
     */
    public List<Trip> getTrips() {
        return getTrips(new Query(), null);
    }

    /**
     * Get list of trips with limit.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @return list of trips
     */
    public List<Trip> getTrips(final Integer limit) {
        return getTrips(new Query(), limit);
    }

    /**
     * Get list of trips.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @return list of trips
     */
    public List<Trip> getTrips(Query query) {
        return getTrips(query, null);
    }

    /**
     * Get list of trips for given stopIDs and lineIDs with result limit.
     *
     * @param query the request
     * @param limit   maximum number of results
     * @return list of trips
     */
    public List<Trip> getTrips(final Query query, final Integer limit) {
        List<Trip> trips = new ArrayList<>();
        try (InputStream is = requestInstant(REQUEST_TRIP, query.stopIDs, query.stopNames, query.lineIDs, query.lineNames, query.direction);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null && (limit == null || trips.size() < limit)) {
                List l = mapper.readValue(line, List.class);
                /* Check if result exists and has correct response type */
                if (l != null && l.size() > 0 && l.get(0).equals(RES_TYPE_PREDICTION))
                    trips.add(new Trip(l));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trips;
    }

    /**
     * Get list of stops without filters.
     *
     * @return the list
     */
    public List<Stop> getStops() {
        return getStops(new Query());
    }

    /**
     * List available stopIDs.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @return the list
     */
    public List<Stop> getStops(Query query) {
        List<Stop> stops = new ArrayList<>();
        try (InputStream is = requestInstant(REQUEST_STOP, query.stopIDs, query.stopNames, query.lineIDs, query.lineNames, query.direction);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                List l = mapper.readValue(line, List.class);
                /* Check if result exists and has correct response type */
                if (l != null && l.size() > 0 && l.get(0).equals(RES_TYPE_STOP))
                    stops.add(new Stop(l));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stops;
    }

    /**
     * Issue request to instant endpoint and return input stream.
     *
     * @param returnList fields to fetch
     * @return Input stream of the URL
     * @throws IOException on errors
     */
    private InputStream requestInstant(String[] returnList, String[] stopIDs, String[] stopNames, String[] lineIDs, String[] lineNames, Integer direction) throws IOException {
        String urlStr = baseURL + instantURL + "?ReturnList=" + String.join(",", returnList);
        if (stopIDs != null && stopIDs.length > 0)
            urlStr += "&" + PAR_STOP_ID + "=" + String.join(",", stopIDs);
        if (stopNames != null && stopNames.length > 0)
            urlStr += "&" + PAR_STOP_NAME + "=" + String.join(",", stopNames);
        if (lineIDs != null && lineIDs.length > 0)
            urlStr += "&" + PAR_LINE_ID + "=" + String.join(",", lineIDs);
        if (lineNames != null && lineNames.length > 0)
            urlStr += "&" + PAR_LINE_NAME + "=" + String.join(",", lineNames);
        if (direction != null)
            urlStr += "&" + PAR_DIR_ID + "=" + direction;
        URL url = new URL(urlStr);
        return url.openStream();
    }

    /**
     * Request meta object.
     */
    public class Query {
        private String[] stopIDs;
        private String[] stopNames;
        private String[] lineIDs;
        private String[] lineNames;
        private Integer direction;

        /**
         * Builder pattern to request given line IDs.
         *
         * @param lineIDs line IDs
         * @return the query
         */
        public Query forLines(final String...lineIDs) {
            this.lineIDs = lineIDs;
            return this;
        }

        /**
         * Builder pattern to request given line names.
         *
         * @param lineNames line names
         * @return the query
         */
        public Query forLinesByName(final String...lineNames) {
            this.lineNames = lineNames;
            return this;
        }

        /**
         * Builder pattern to request given stop IDs.
         *
         * @param stopIDs stop IDs
         * @return the query
         */
        public Query forStops(final String...stopIDs) {
            this.stopIDs = stopIDs;
            return this;
        }

        /**
         * Builder pattern to request given stop names.
         *
         * @param stopNames line names
         * @return the query
         */
        public Query forStopsByName(final String...stopNames) {
            this.stopNames = stopNames;
            return this;
        }

        /**
         * Builder pattern to request given direction.
         *
         * @param direction the direction
         * @return the query
         */
        public Query forDirection(final Integer direction) {
            this.direction = direction;
            return this;
        }

        /**
         * Get stops for set filters.
         *
         * @return List of matching trips
         */
        public List<Stop> getStops() {
            return UraClient.this.getStops(this);
        }

        /**
         * Get trips for set filters.
         *
         * @return List of matching trips
         */
        public List<Trip> getTrips() {
            return UraClient.this.getTrips(this);
        }
    }
}
