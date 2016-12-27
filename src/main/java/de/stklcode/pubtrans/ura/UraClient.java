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

    private static final String FILTER_LINE = "LineID";
    private static final String FILTER_STOP = "StopID";

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

    private String[] stops;
    private String[] lines;

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
     * Builder pattern to request given stops.
     *
     * @param stops Stop IDs
     * @return the client
     */
    public UraClient forStops(final String...stops) {
        this.stops = stops;
        return this;
    }

    /**
     * Builder pattern to request given stops.
     *
     * @param lines line IDs
     * @return the client
     */
    public UraClient forLines(final String...lines) {
        this.lines = lines;
        return this;
    }

    /**
     * Get list of trips.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @return list of trips
     */
    public List<Trip> getTrips() {
        return getTrips(stops, lines, null);
    }

    /**
     * Get list of trips with limit.
     * If forStops() and/or forLines() has been called, those will be used as filter.
     *
     * @return list of trips
     */
    public List<Trip> getTrips(final Integer limit) {
        return getTrips(stops, lines, limit);
    }

    /**
     * Get list of trips for given stops and lines.
     *
     * @param stops the stops
     * @param lines the lines
     * @return list of trips
     */
    public List<Trip> getTrips(final String[] stops, final String[] lines) {
        return getTrips(stops, lines, null);
    }

    /**
     * Get list of trips for given stops and lines with result limit.
     *
     * @param stops the stops
     * @param lines the lines
     * @param limit maximum number of results
     * @return list of trips
     */
    public List<Trip> getTrips(final String[] stops, final String[] lines, final Integer limit) {
        List<Trip> trips = new ArrayList<>();
        try (InputStream is = requestInstant(REQUEST_TRIP, stops, lines);
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
     * List available stops.
     *
     * @return the list
     */
    public List<Stop> listStops() {
        List<Stop> stops = new ArrayList<>();
        try (InputStream is = requestInstant(REQUEST_STOP, null, null);
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
    private InputStream requestInstant(String[] returnList, String[] stops, String[] lines) throws IOException {
        String urlStr = baseURL + instantURL + "?ReturnList=" + String.join(",", returnList);
        if (stops != null && stops.length > 0)
            urlStr += "&" + FILTER_STOP + "=" + String.join(",", stops);
        if (lines != null && lines.length > 0)
            urlStr += "&" + FILTER_LINE + "=" + String.join(",", lines);
        URL url = new URL(urlStr);
        return url.openStream();
    }
}
