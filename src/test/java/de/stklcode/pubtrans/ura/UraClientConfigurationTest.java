package de.stklcode.pubtrans.ura;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link UraClientConfiguration}.
 *
 * @author Stefan Kalscheuer
 */
public class UraClientConfigurationTest {
    @Test
    public void configBuilderTest() {
        final String baseURL = "https://ura.example.com";
        final String instantPath = "/path/to/instant";
        final String streamPath = "/path/to/stream";

        // With Base-URL only.
        UraClientConfiguration config = UraClientConfiguration.forBaseURL(baseURL).build();
        assertEquals(baseURL, config.getBaseURL(), "Unexpected base URL");
        assertEquals("/interfaces/ura/instant_V1", config.getInstantPath(), "Unexpected default instant path");
        assertEquals("/interfaces/ura/stream_V1", config.getStreeamPath(), "Unexpected default stream path");

        // With custom paths.
        config = UraClientConfiguration.forBaseURL(baseURL)
                .withInstantPath(instantPath)
                .withStreamPath(streamPath)
                .build();
        assertEquals(baseURL, config.getBaseURL(), "Unexpected base URL");
        assertEquals(instantPath, config.getInstantPath(), "Unexpected custom instant path");
        assertEquals(streamPath, config.getStreeamPath(), "Unexpected custom stream path");
    }
}
