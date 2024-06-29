package de.stklcode.pubtrans.ura;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit test for {@link UraClientConfiguration}.
 *
 * @author Stefan Kalscheuer
 */
class UraClientConfigurationTest {
    @Test
    void configBuilderTest() {
        final String baseURL = "https://ura.example.com";
        final String instantPath = "/path/to/instant";
        final String streamPath = "/path/to/stream";
        final Duration timeout = Duration.ofSeconds(2);
        final Duration conTimeout = Duration.ofSeconds(41);

        // With Base-URL only.
        UraClientConfiguration config = UraClientConfiguration.forBaseURL(baseURL).build();
        assertEquals(baseURL, config.getBaseURL(), "Unexpected base URL");
        assertEquals("/interfaces/ura/instant_V1", config.getInstantPath(), "Unexpected default instant path");
        assertEquals("/interfaces/ura/stream_V1", config.getStreeamPath(), "Unexpected default stream path");
        assertNull(config.getConnectTimeout(), "No default connection timeout expected");
        assertNull(config.getTimeout(), "No default timeout expected");

        // With custom paths.
        config = UraClientConfiguration.forBaseURL(baseURL)
                .withInstantPath(instantPath)
                .withStreamPath(streamPath)
                .build();
        assertEquals(baseURL, config.getBaseURL(), "Unexpected base URL");
        assertEquals(instantPath, config.getInstantPath(), "Unexpected custom instant path");
        assertEquals(streamPath, config.getStreeamPath(), "Unexpected custom stream path");

        // With timeouts. (#14)
        config = UraClientConfiguration.forBaseURL(baseURL)
                .withConnectTimeout(conTimeout)
                .withTimeout(timeout)
                .build();
        assertEquals(conTimeout, config.getConnectTimeout(), "Unexpected connection timeout value");
        assertEquals(timeout, config.getTimeout(), "Unexpected timeout value");
    }
}
