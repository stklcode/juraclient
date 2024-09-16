/*
 * Copyright 2016-2024 Stefan Kalscheuer
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
        assertEquals("/interfaces/ura/stream_V1", config.getStreamPath(), "Unexpected default stream path");
        assertNull(config.getConnectTimeout(), "No default connection timeout expected");
        assertNull(config.getTimeout(), "No default timeout expected");

        // With custom paths.
        config = UraClientConfiguration.forBaseURL(baseURL)
                .withInstantPath(instantPath)
                .withStreamPath(streamPath)
                .build();
        assertEquals(baseURL, config.getBaseURL(), "Unexpected base URL");
        assertEquals(instantPath, config.getInstantPath(), "Unexpected custom instant path");
        assertEquals(streamPath, config.getStreamPath(), "Unexpected custom stream path");

        // With timeouts. (#14)
        config = UraClientConfiguration.forBaseURL(baseURL)
                .withConnectTimeout(conTimeout)
                .withTimeout(timeout)
                .build();
        assertEquals(conTimeout, config.getConnectTimeout(), "Unexpected connection timeout value");
        assertEquals(timeout, config.getTimeout(), "Unexpected timeout value");
    }
}
