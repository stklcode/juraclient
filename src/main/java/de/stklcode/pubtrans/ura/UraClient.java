/*
 * Copyright 2016-2021 Stefan Kalscheuer
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
import de.stklcode.pubtrans.ura.exception.UraClientConfigurationException;
import de.stklcode.pubtrans.ura.exception.UraClientException;
import de.stklcode.pubtrans.ura.model.Message;
import de.stklcode.pubtrans.ura.model.Stop;
import de.stklcode.pubtrans.ura.model.Trip;
import de.stklcode.pubtrans.ura.reader.AsyncUraTripReader;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Client for URA based public transport API.
 * <p>
 * This client features builder pattern style query functionality to obtain {@link Trip} and {@link Stop} information.
 *
 * @author Stefan Kalscheuer
 */
public class UraClient implements Serializable {
    private static final long serialVersionUID = -1183740075816686611L;

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
    private static final String PAR_MSG_UUID = "MessageUUID";
    private static final String PAR_MSG_TYPE = "MessageType";
    private static final String PAR_MSG_PRIORITY = "MessagePriority";
    private static final String PAR_MSG_TEXT = "MessageText";

    private static final Integer RES_TYPE_STOP = 0;
    private static final Integer RES_TYPE_PREDICTION = 1;
    private static final Integer RES_TYPE_FLEX_MESSAGE = 2;
    private static final Integer RES_TYPE_URA_VERSION = 4;

    private static final String[] REQUEST_STOP = {PAR_STOP_NAME, PAR_STOP_ID, PAR_STOP_INDICATOR, PAR_STOP_STATE, PAR_GEOLOCATION};
    private static final String[] REQUEST_TRIP = {PAR_STOP_NAME, PAR_STOP_ID, PAR_STOP_INDICATOR, PAR_STOP_STATE, PAR_GEOLOCATION,
            PAR_VISIT_NUMBER, PAR_LINE_ID, PAR_LINE_NAME, PAR_DIR_ID, PAR_DEST_NAME, PAR_DEST_TEXT, PAR_VEHICLE_ID, PAR_TRIP_ID, PAR_ESTTIME};
    private static final String[] REQUEST_MESSAGE = {PAR_STOP_NAME, PAR_STOP_ID, PAR_STOP_INDICATOR, PAR_STOP_STATE, PAR_GEOLOCATION,
            PAR_MSG_UUID, PAR_MSG_TYPE, PAR_MSG_PRIORITY, PAR_MSG_TEXT};

    private final UraClientConfiguration config;
    private final ObjectMapper mapper;

    /**
     * Constructor from {@link UraClientConfiguration}.
     *
     * @param config The configuration.
     * @since 2.0
     */
    public UraClient(final UraClientConfiguration config) {
        this.config = config;
        this.mapper = new ObjectMapper();
    }

    /**
     * Constructor with base URL and default API paths.
     *
     * @param baseURL The base URL (with protocol, without trailing slash).
     */
    public UraClient(final String baseURL) {
        this(UraClientConfiguration.forBaseURL(baseURL).build());
    }

    /**
     * Constructor with base URL and custom API paths.
     *
     * @param baseURL     The base URL (including protocol).
     * @param instantPath The path for instant requests.
     * @param streamPath  The path for stream requests.
     */
    public UraClient(final String baseURL, final String instantPath, final String streamPath) {
        this(
                UraClientConfiguration.forBaseURL(baseURL)
                        .withInstantPath(instantPath)
                        .withStreamPath(streamPath)
                        .build()
        );
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
     * @throws UraClientException Error with API communication.
     * @since 1.0
     * @since 2.0 Throws {@link UraClientException}.
     */
    public List<Trip> getTrips() throws UraClientException {
        return getTrips(new Query(), null);
    }

    /**
     * Get list of trips with limit.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @param limit Maximum number of results.
     * @return List of trips.
     * @throws UraClientException Error with API communication.
     * @since 1.0
     * @since 2.0 Throws {@link UraClientException}.
     */
    public List<Trip> getTrips(final Integer limit) throws UraClientException {
        return getTrips(new Query(), limit);
    }

    /**
     * Get list of trips.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @param query The query.
     * @return List of trips.
     * @throws UraClientException Error with API communication.
     * @throws UraClientException Error with API communication.
     * @since 1.0
     * @since 2.0 Throws {@link UraClientException}.
     */
    public List<Trip> getTrips(final Query query) throws UraClientException {
        return getTrips(query, null);
    }

    /**
     * Get list of trips for given stopIDs and lineIDs with result limit.
     *
     * @param query The query.
     * @param limit Maximum number of results.
     * @return List of trips.
     * @throws UraClientException Error with API communication.
     * @since 1.0
     * @since 2.0 Throws {@link UraClientException}.
     */
    public List<Trip> getTrips(final Query query, final Integer limit) throws UraClientException {
        List<Trip> trips = new ArrayList<>();
        try (InputStream is = requestInstant(REQUEST_TRIP, query);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String version = null;
            String line = br.readLine();
            while (line != null && (limit == null || trips.size() < limit)) {
                List<Serializable> l = mapper.readValue(line, mapper.getTypeFactory().constructCollectionType(List.class, Serializable.class));
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
            throw new UraClientException("Failed to read trips from API", e);
        }
        return trips;
    }

    /**
     * Get trips for given stopIDs and lineIDs using stream API and pass each result to given consumer.
     *
     * @param query    The query.
     * @param consumer Consumer(s) for single trips.
     * @return Trip reader.
     * @throws UraClientConfigurationException Error reading response.
     * @see #getTripsStream(Query, List)
     * @since 1.2
     */
    public AsyncUraTripReader getTripsStream(final Query query, final Consumer<Trip> consumer) throws UraClientConfigurationException {
        return getTripsStream(query, Collections.singletonList(consumer));
    }

    /**
     * Get trips for given stopIDs and lineIDs using stream API and pass each result to given consumers.
     *
     * @param query     The query.
     * @param consumers Consumer(s) for single trips.
     * @return Trip reader.
     * @throws UraClientConfigurationException Error retrieving stream response.
     * @since 1.2
     * @since 2.0 Throws {@link UraClientConfigurationException}.
     */
    public AsyncUraTripReader getTripsStream(final Query query, final List<Consumer<Trip>> consumers) throws UraClientConfigurationException {
        // Create the reader.
        try {
            AsyncUraTripReader reader = new AsyncUraTripReader(
                    URI.create(requestURL(config.getBaseURL() + config.getStreeamPath(), REQUEST_TRIP, query)),
                    config,
                    consumers
            );

            // Open the reader, i.e. start reading from API.
            reader.open();

            return reader;
        } catch (IllegalArgumentException e) {
            throw new UraClientConfigurationException("Invalid API URL, check client configuration.", e);
        }
    }

    /**
     * Get list of stops without filters.
     *
     * @return The list of stops.
     * @throws UraClientException Error with API communication.
     * @since 1.0
     * @since 2.0 Throws {@link UraClientException}.
     */
    public List<Stop> getStops() throws UraClientException {
        return getStops(new Query());
    }

    /**
     * List available stopIDs.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @param query The query.
     * @return The list.
     * @throws UraClientException Error with API communication.
     * @since 1.0
     * @since 2.0 Throws {@link UraClientException}.
     */
    public List<Stop> getStops(final Query query) throws UraClientException {
        List<Stop> stops = new ArrayList<>();
        try (InputStream is = requestInstant(REQUEST_STOP, query);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<Serializable> l = mapper.readValue(line, mapper.getTypeFactory().constructCollectionType(List.class, Serializable.class));
                /* Check if result exists and has correct response type */
                if (l != null && !l.isEmpty() && l.get(0).equals(RES_TYPE_STOP)) {
                    stops.add(new Stop(l));
                }
            }
        } catch (IOException e) {
            throw new UraClientException("Failed to read stops from API", e);
        }
        return stops;
    }

    /**
     * Get list of messages.
     *
     * @return List of messages.
     * @throws UraClientException Error with API communication.
     * @since 1.3
     * @since 2.0 Throw {@link UraClientException}.
     */
    public List<Message> getMessages() throws UraClientException {
        return getMessages(new Query(), null);
    }


    /**
     * Get list of messages.
     * If forStops() has been called, those will be used as filter.
     *
     * @param query The query.
     * @return List of trips.
     * @throws UraClientException Error with API communication.
     * @since 1.3
     * @since 2.0 Throw {@link UraClientException}.
     */
    public List<Message> getMessages(final Query query) throws UraClientException {
        return getMessages(query, null);
    }

    /**
     * Get list of messages for given stopIDs with result limit.
     *
     * @param query The query.
     * @param limit Maximum number of results.
     * @return List of trips.
     * @throws UraClientException Error with API communication.
     * @since 1.3
     * @since 2.0 Throw {@link UraClientException}.
     */
    public List<Message> getMessages(final Query query, final Integer limit) throws UraClientException {
        List<Message> messages = new ArrayList<>();
        try (InputStream is = requestInstant(REQUEST_MESSAGE, query);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String version = null;
            String line = br.readLine();
            while (line != null && (limit == null || messages.size() < limit)) {
                List<Serializable> l = mapper.readValue(line, mapper.getTypeFactory().constructCollectionType(List.class, Serializable.class));
                /* Check if result exists and has correct response type */
                if (l != null && !l.isEmpty()) {
                    if (l.get(0).equals(RES_TYPE_URA_VERSION)) {
                        version = l.get(1).toString();
                    } else if (l.get(0).equals(RES_TYPE_FLEX_MESSAGE)) {
                        messages.add(new Message(l, version));
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            throw new UraClientException("Failed to read messages from API", e);
        }
        return messages;
    }

    /**
     * Issue request to instant endpoint and return input stream.
     *
     * @param returnList Fields to fetch.
     * @param query      The query.
     * @return Response {@link InputStream}.
     * @throws IOException on errors
     */
    private InputStream requestInstant(final String[] returnList, final Query query) throws IOException {
        return request(requestURL(config.getBaseURL() + config.getInstantPath(), returnList, query));
    }

    /**
     * Build request URL from given parameters.
     *
     * @param endpointURL Endpoint URL.
     * @param returnList  Fields to fetch.
     * @param query       The query.
     * @return The URL
     * @since 1.2
     * @since 2.0 Does not throw exception anymore.
     */
    private String requestURL(final String endpointURL, final String[] returnList, final Query query) {
        StringBuilder urlStr = new StringBuilder(endpointURL)
                .append("?ReturnList=")
                .append(String.join(",", returnList));

        addParameterArray(urlStr, PAR_STOP_ID, query.stopIDs);
        addParameterArray(urlStr, PAR_STOP_NAME, query.stopNames);
        addParameterArray(urlStr, PAR_LINE_ID, query.lineIDs);
        addParameterArray(urlStr, PAR_LINE_NAME, query.lineNames);
        if (query.direction != null) {
            urlStr.append("&").append(PAR_DIR_ID).append("=").append(query.direction);
        }
        addParameterArray(urlStr, PAR_DEST_NAME, query.destinationNames);
        addParameterArray(urlStr, PAR_TOWARDS, query.towards);
        if (query.circle != null) {
            urlStr.append("&").append(PAR_CIRCLE).append("=").append(URLEncoder.encode(query.circle, UTF_8));
        }

        return urlStr.toString();
    }

    /**
     * Open given URL as InputStream.
     *
     * @param url The URL.
     * @return Response {@link InputStream}.
     * @throws IOException Error opening connection or reading data.
     */
    private InputStream request(String url) throws IOException {
        try {
            var clientBuilder = HttpClient.newBuilder();
            if (config.getConnectTimeout() != null) {
                clientBuilder.connectTimeout(config.getConnectTimeout());
            }

            var reqBuilder = HttpRequest.newBuilder(URI.create(url)).GET();
            if (config.getTimeout() != null) {
                reqBuilder.timeout(config.getTimeout());
            }

            return clientBuilder.build().send(reqBuilder.build(), HttpResponse.BodyHandlers.ofInputStream()).body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("API request interrupted", e);
        }
    }

    /**
     * Add a URL parameter with list of values, if filled.
     *
     * @param urlBuilder StringBuilder holding the current URL.
     * @param parameter  Parameter key.
     * @param values     List of parameter values (might be {@code null} or empty)
     */
    private static void addParameterArray(StringBuilder urlBuilder, String parameter, String[] values) {
        if (values != null && values.length > 0) {
            urlBuilder.append("&").append(parameter)
                    .append("=").append(URLEncoder.encode(String.join(",", values), UTF_8));
        }
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
         * @throws UraClientException Error with API communication.
         * @since 1.0
         * @since 2.0 Throws {@link UraClientException}.
         */
        public List<Stop> getStops() throws UraClientException {
            return UraClient.this.getStops(this);
        }

        /**
         * Get trips for set filters.
         *
         * @return List of matching trips.
         * @throws UraClientException Error with API communication.
         * @since 1.0
         * @since 2.0 Throws {@link UraClientException}.
         */
        public List<Trip> getTrips() throws UraClientException {
            return UraClient.this.getTrips(this);
        }

        /**
         * Get trips for set filters.
         *
         * @param consumer Consumer for single trips.
         * @return Trip reader.
         * @throws UraClientConfigurationException Error reading response.
         * @see #getTripsStream(List)
         * @since 1.2
         */
        public AsyncUraTripReader getTripsStream(Consumer<Trip> consumer) throws UraClientConfigurationException {
            return UraClient.this.getTripsStream(this, consumer);
        }

        /**
         * Get trips for set filters.
         *
         * @param consumers Consumers for single trips.
         * @return Trip reader.
         * @throws UraClientConfigurationException Errors retrieving stream response.
         * @since 1.2
         */
        public AsyncUraTripReader getTripsStream(List<Consumer<Trip>> consumers) throws UraClientConfigurationException {
            return UraClient.this.getTripsStream(this, consumers);
        }

        /**
         * Get trips for set filters.
         *
         * @return List of matching messages.
         * @throws UraClientException Error with API communication.
         * @since 1.3
         * @since 2.0 Throws {@link UraClientException}.
         */
        public List<Message> getMessages() throws UraClientException {
            return UraClient.this.getMessages(this);
        }
    }
}
