/*
 * Copyright 2016-2018 Stefan Kalscheuer
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
import de.stklcode.pubtrans.ura.reader.AsyncUraTripReader;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Client for URA based public transport API.
 * <p>
 * This client features builder pattern style query functionality to obtain {@link Trip} and {@link Stop} information.
 *
 * @author Stefan Kalscheuer
 */
public class UraClient implements Serializable {
    private static final String DEFAULT_INSTANT_URL = "/interfaces/ura/instant_V1";
    private static final String DEFAULT_STREAM_URL = "/interfaces/ura/stream_V1";

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
    private static final String PAR_TOWARDS = "Towards";
    private static final String PAR_CIRCLE = "Circle";

    private static final Integer RES_TYPE_STOP = 0;
    private static final Integer RES_TYPE_PREDICTION = 1;
    private static final Integer RES_TYPE_URA_VERSION = 4;

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
     * @param baseURL The base URL (with protocol, without trailing slash).
     */
    public UraClient(final String baseURL) {
        this(baseURL, DEFAULT_INSTANT_URL, DEFAULT_STREAM_URL);
    }

    /**
     * Constructor with base URL and custom API paths.
     *
     * @param baseURL    The base URL (including protocol).
     * @param instantURL The path for instant requests.
     * @param streamURL  The path for stream requests.
     */
    public UraClient(final String baseURL, final String instantURL, final String streamURL) {
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
    public final Query forStops(final String... stops) {
        return new Query().forStops(stops);
    }

    /**
     * Builder pattern to request given stop names.
     *
     * @param stopNames Stop Point Names
     * @return the request
     */
    public final Query forStopsByName(final String... stopNames) {
        return new Query().forStopsByName(stopNames);
    }

    /**
     * Builder pattern to request given line IDs.
     *
     * @param lines Line IDs.
     * @return The request.
     */
    public final Query forLines(final String... lines) {
        return new Query().forLines(lines);
    }

    /**
     * Builder pattern to request given line names.
     *
     * @param lineNames Line names.
     * @return The request.
     */
    public final Query forLinesByName(final String... lineNames) {
        return new Query().forLinesByName(lineNames);
    }

    /**
     * Builder pattern to request given direction.
     *
     * @param direction The direction ID.
     * @return The request.
     */
    public final Query forDirection(final Integer direction) {
        return new Query().forDirection(direction);
    }

    /**
     * Builder pattern to request given destination names.
     *
     * @param destinationNames Destination names.
     * @return The request.
     * @since 1.1.0
     */
    public final Query forDestinationNames(final String... destinationNames) {
        return new Query().forDestinationNames(destinationNames);
    }

    /**
     * Builder pattern to request given direction defined by stop point name.
     *
     * @param towards Towards stop point names.
     * @return The request.
     * @since 1.1.0
     */
    public final Query towards(final String... towards) {
        return new Query().towards(towards);
    }

    /**
     * Builder pattern to request given destination names.
     *
     * @param latitude  Latitude (WGS84).
     * @param longitude Longitude (WGS84).
     * @param radius    Search radius (meters).
     * @return The request.
     * @since 1.1.0
     */
    public final Query forPosition(final Double latitude, final Double longitude, final Integer radius) {
        return new Query().forPosition(latitude, longitude, radius);
    }

    /**
     * Get list of trips.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @return List of trips.
     */
    public List<Trip> getTrips() {
        return getTrips(new Query(), null);
    }

    /**
     * Get list of trips with limit.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @param limit Maximum number of results.
     * @return List of trips.
     */
    public List<Trip> getTrips(final Integer limit) {
        return getTrips(new Query(), limit);
    }

    /**
     * Get list of trips.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @param query The query.
     * @return List of trips.
     */
    public List<Trip> getTrips(final Query query) {
        return getTrips(query, null);
    }

    /**
     * Get list of trips for given stopIDs and lineIDs with result limit.
     *
     * @param query The query.
     * @param limit Maximum number of results.
     * @return List of trips.
     */
    public List<Trip> getTrips(final Query query, final Integer limit) {
        List<Trip> trips = new ArrayList<>();
        try (InputStream is = requestInstant(REQUEST_TRIP, query);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String version = null;
            String line = br.readLine();
            while (line != null && (limit == null || trips.size() < limit)) {
                List l = mapper.readValue(line, List.class);
                /* Check if result exists and has correct response type */
                if (l != null && !l.isEmpty()) {
                    if (l.get(0).equals(RES_TYPE_URA_VERSION)) {
                        version = l.get(1).toString();
                    } else if (l.get(0).equals(RES_TYPE_PREDICTION)) {
                        trips.add(new Trip(l, version));
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read from API", e);
        }
        return trips;
    }

    /**
     * Get trips for given stopIDs and lineIDs using stream API and pass each result to given consumer.
     *
     * @param query    The query.
     * @param consumer Consumer(s) for single trips.
     * @see #getTripsStream(Query, List)
     * @since 1.2.0
     */
    public AsyncUraTripReader getTripsStream(final Query query, final Consumer<Trip> consumer) throws IOException {
        return getTripsStream(query, Collections.singletonList(consumer));
    }

    /**
     * Get trips for given stopIDs and lineIDs using stream API and pass each result to given consumers.
     *
     * @param query     The query.
     * @param consumers Consumer(s) for single trips.
     * @since 1.2.0
     */
    public AsyncUraTripReader getTripsStream(final Query query, final List<Consumer<Trip>> consumers) throws IOException {
        // Create the reader.
        AsyncUraTripReader reader = new AsyncUraTripReader(
                new URL(requestURL(baseURL + streamURL, REQUEST_TRIP, query)),
                consumers
        );

        // Open the reader, i.e. start reading from API.
        reader.open();

        return reader;
    }

    /**
     * Get list of stops without filters.
     *
     * @return Lhe list.
     */
    public List<Stop> getStops() {
        return getStops(new Query());
    }

    /**
     * List available stopIDs.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @param query The query.
     * @return The list.
     */
    public List<Stop> getStops(final Query query) {
        List<Stop> stops = new ArrayList<>();
        try (InputStream is = requestInstant(REQUEST_STOP, query);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                List l = mapper.readValue(line, List.class);
                /* Check if result exists and has correct response type */
                if (l != null && !l.isEmpty() && l.get(0).equals(RES_TYPE_STOP)) {
                    stops.add(new Stop(l));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read from API", e);
        }
        return stops;
    }

    /**
     * Issue request to instant endpoint and return input stream.
     *
     * @param returnList Fields to fetch.
     * @param query      The query.
     * @return Input stream of the URL
     * @throws IOException on errors
     */
    private InputStream requestInstant(final String[] returnList, final Query query) throws IOException {
        return request(requestURL(baseURL + instantURL, returnList, query));
    }

    /**
     * Build request URL from given parameters.
     *
     * @param endpointURL Endpoint URL.
     * @param returnList  Fields to fetch.
     * @param query       The query.
     * @return The URL
     * @since 1.2.0
     */
    private String requestURL(final String endpointURL, final String[] returnList, final Query query) {
        String urlStr = endpointURL + "?ReturnList=" + String.join(",", returnList);

        if (query.stopIDs != null && query.stopIDs.length > 0) {
            urlStr += "&" + PAR_STOP_ID + "=" + String.join(",", query.stopIDs);
        }
        if (query.stopNames != null && query.stopNames.length > 0) {
            urlStr += "&" + PAR_STOP_NAME + "=" + String.join(",", query.stopNames);
        }
        if (query.lineIDs != null && query.lineIDs.length > 0) {
            urlStr += "&" + PAR_LINE_ID + "=" + String.join(",", query.lineIDs);
        }
        if (query.lineNames != null && query.lineNames.length > 0) {
            urlStr += "&" + PAR_LINE_NAME + "=" + String.join(",", query.lineNames);
        }
        if (query.direction != null) {
            urlStr += "&" + PAR_DIR_ID + "=" + query.direction;
        }
        if (query.destinationNames != null) {
            urlStr += "&" + PAR_DEST_NAME + "=" + String.join(",", query.destinationNames);
        }
        if (query.towards != null) {
            urlStr += "&" + PAR_TOWARDS + "=" + String.join(",", query.towards);
        }
        if (query.circle != null) {
            urlStr += "&" + PAR_CIRCLE + "=" + String.join(",", query.circle);
        }

        return urlStr;
    }

    /**
     * Open given URL as InputStream.
     *
     * @param url The URL.
     * @return Input Stream of results.
     * @throws IOException Error opening conneciton or reading data.
     */
    private InputStream request(String url) throws IOException {
        return new URL(url).openStream();
    }

    /**
     * Request meta object.
     */
    public final class Query {
        private String[] stopIDs;
        private String[] stopNames;
        private String[] lineIDs;
        private String[] lineNames;
        private Integer direction;
        private String[] destinationNames;
        private String[] towards;
        private String circle;

        /**
         * Builder pattern to request given line IDs.
         *
         * @param lineIDs Line IDs.
         * @return The query.
         */
        public Query forLines(final String... lineIDs) {
            this.lineIDs = lineIDs;
            return this;
        }

        /**
         * Builder pattern to request given line names.
         *
         * @param lineNames Line names.
         * @return The query.
         */
        public Query forLinesByName(final String... lineNames) {
            this.lineNames = lineNames;
            return this;
        }

        /**
         * Builder pattern to request given stop IDs.
         *
         * @param stopIDs Stop IDs.
         * @return The query.
         */
        public Query forStops(final String... stopIDs) {
            this.stopIDs = stopIDs;
            return this;
        }

        /**
         * Builder pattern to request given stop names.
         *
         * @param stopNames Line names.
         * @return The query.
         */
        public Query forStopsByName(final String... stopNames) {
            this.stopNames = stopNames;
            return this;
        }

        /**
         * Builder pattern to request given direction.
         *
         * @param direction The direction.
         * @return The query.
         */
        public Query forDirection(final Integer direction) {
            this.direction = direction;
            return this;
        }

        /**
         * Builder pattern to request given destination names.
         *
         * @param destinationNames Names of destinations.
         * @return The query.
         * @since 1.1.0
         */
        public Query forDestinationNames(final String... destinationNames) {
            this.destinationNames = destinationNames;
            return this;
        }

        /**
         * Builder pattern to request given direction defined by stop point name.
         *
         * @param towards Towards stop point names.
         * @return The request.
         * @since 1.1.0
         */
        public Query towards(final String... towards) {
            this.towards = towards;
            return this;
        }

        /**
         * Builder pattern to request given position and radius.
         *
         * @param latitude  Latitude (WGS84).
         * @param longitude Longitude (WGS84).
         * @param radius    Search radius (meters).
         * @return The query.
         * @since 1.1.0
         */
        public Query forPosition(final Double latitude, final Double longitude, final Integer radius) {
            this.circle = latitude.toString() + "," + longitude.toString() + "," + radius.toString();
            return this;
        }

        /**
         * Get stops for set filters.
         *
         * @return List of matching trips.
         */
        public List<Stop> getStops() {
            return UraClient.this.getStops(this);
        }

        /**
         * Get trips for set filters.
         *
         * @return List of matching trips.
         */
        public List<Trip> getTrips() {
            return UraClient.this.getTrips(this);
        }

        /**
         * Get trips for set filters.
         *
         * @param consumer Consumer for single trips.
         * @see #getTripsStream(List)
         * @since 1.2.0
         */
        public AsyncUraTripReader getTripsStream(Consumer<Trip> consumer) throws IOException {
            return UraClient.this.getTripsStream(this, consumer);
        }

        /**
         * Get trips for set filters.
         *
         * @param consumers Consumers for single trips.
         * @since 1.2.0
         */
        public AsyncUraTripReader getTripsStream(List<Consumer<Trip>> consumers) throws IOException {
            return UraClient.this.getTripsStream(this, consumers);
        }
    }
}
