package de.stklcode.pubtrans.ura;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.stklcode.pubtrans.ura.model.Stop;

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

    private static final String REQUEST_STOP_ID = "StopID";
    private static final String REQUEST_STOP_NAME = "StopPointName";
    private static final String REQUEST_STOP_STATE = "StopPointState";
    private static final String REQUEST_STOP_INDICATOR = "StopPointIndicator";
    private static final String REQUEST_GEOLOCATION = "Latitude,Longitude";

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
     * List available stops.
     *
     * @return the list
     */
    public List<Stop> listStops() {
        List<Stop> stops = new ArrayList<>();
        try (InputStream is = requestInstant(REQUEST_STOP_NAME, REQUEST_STOP_ID, REQUEST_GEOLOCATION);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            boolean first = false;
            while ((line = br.readLine()) != null) {
                if (!first) {
                    first = true;
                    continue;
                }
                stops.add(new Stop(mapper.readValue(line, List.class)));
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
    private InputStream requestInstant(String...returnList) throws IOException {
        URL url = new URL(baseURL + instantURL + "?ReturnList=" + String.join(",", returnList));
        return url.openStream();
    }
}
