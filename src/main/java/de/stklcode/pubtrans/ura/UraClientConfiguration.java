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

package de.stklcode.pubtrans.ura;

import java.io.Serializable;
import java.time.Duration;

/**
 * Configuration Object for the {@link UraClient}.
 *
 * @param baseURL        API base URL.
 * @param instantPath    Path to instant API endpoint.
 * @param streamPath     Path to stream API endpoint.
 * @param connectTimeout Optional connection timeout.
 * @param timeout        Optional read timeout.
 * @author Stefan Kalscheuer
 * @since 2.0
 * @since 3.0 record
 */
public record UraClientConfiguration(
    String baseURL,
    String instantPath,
    String streamPath,
    Duration connectTimeout,
    Duration timeout
) implements Serializable {

    private static final String DEFAULT_INSTANT_PATH = "/interfaces/ura/instant_V1";
    private static final String DEFAULT_STREAM_PATH = "/interfaces/ura/stream_V1";

    /**
     * Get new configuration {@link Builder} for given base URL.
     * This URL is the only option required.
     *
     * @param baseURL The base URL (with protocol, without trailing slash).
     * @return Configuration Builder instance.
     */
    public static Builder forBaseURL(final String baseURL) {
        return new Builder(baseURL);
    }


    /**
     * Builder for {@link UraClientConfiguration} objects.
     */
    public static class Builder {
        private final String baseURL;
        private String instantPath;
        private String streamPath;
        private Duration connectTimeout;
        private Duration timeout;

        /**
         * Initialize the builder with mandatory base URL.
         * Use {@link UraClientConfiguration#forBaseURL(String)} to get a builder instance.
         *
         * @param baseURL The base URL.
         */
        private Builder(String baseURL) {
            this.baseURL = baseURL;
            this.instantPath = DEFAULT_INSTANT_PATH;
            this.streamPath = DEFAULT_STREAM_PATH;
            this.connectTimeout = null;
            this.timeout = null;
        }

        /**
         * Specify a custom path to the instant API.
         *
         * @param instantPath Instant endpoint path.
         * @return The builder.
         */
        public Builder withInstantPath(String instantPath) {
            this.instantPath = instantPath;
            return this;
        }

        /**
         * Specify a custom path to the stream API.
         *
         * @param streamPath Stream endpoint path.
         * @return The builder.
         */
        public Builder withStreamPath(String streamPath) {
            this.streamPath = streamPath;
            return this;
        }

        /**
         * Specify a custom connection timeout duration.
         *
         * @param connectTimeout Timeout duration.
         * @return The builder.
         */
        public Builder withConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Specify a custom timeout duration.
         *
         * @param timeout Timeout duration.
         * @return The builder.
         */
        public Builder withTimeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Finally build the configuration object.
         *
         * @return The configuration.
         */
        public UraClientConfiguration build() {
            return new UraClientConfiguration(baseURL, instantPath, streamPath, connectTimeout, timeout);
        }
    }
}
