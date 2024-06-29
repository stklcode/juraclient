package de.stklcode.pubtrans.ura;

import java.io.Serializable;
import java.time.Duration;

/**
 * Configuration Object for the {@link UraClient}.
 *
 * @author Stefan Kalscheuer
 * @since 2.0
 */
public class UraClientConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_INSTANT_PATH = "/interfaces/ura/instant_V1";
    private static final String DEFAULT_STREAM_PATH = "/interfaces/ura/stream_V1";

    /**
     * API base URL.
     */
    private final String baseURL;

    /**
     * Path to instant API endpoint.
     */
    private final String instantPath;

    /**
     * Path to stream API endpoint.
     */
    private final String streamPath;

    /**
     * Optional connection timeout.
     */
    private final Duration connectTimeout;

    /**
     * Optional read timeout.
     */
    private final Duration timeout;

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
     * Construct new configuration object from Builder.
     *
     * @param builder The builder instance.
     */
    private UraClientConfiguration(Builder builder) {
        this.baseURL = builder.baseURL;
        this.instantPath = builder.instantPath;
        this.streamPath = builder.streamPath;
        this.connectTimeout = builder.connectTimeout;
        this.timeout = builder.timeout;
    }

    /**
     * Get the API base URL.
     *
     * @return Base URL.
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Get the API instant endpoint path.
     *
     * @return Instant endpoint path.
     */
    public String getInstantPath() {
        return this.instantPath;
    }

    /**
     * Get the API stream endpoint path.
     *
     * @return Stream endpoint path.
     */
    public String getStreamPath() {
        return this.streamPath;
    }

    /**
     * Get the connection timeout, if any.
     *
     * @return Timeout duration or {@code null} if none specified.
     */
    public Duration getConnectTimeout() {
        return this.connectTimeout;
    }

    /**
     * Get the response timeout, if any.
     *
     * @return Timeout duration or {@code null} if none specified.
     */
    public Duration getTimeout() {
        return this.timeout;
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
            return new UraClientConfiguration(this);
        }
    }
}
