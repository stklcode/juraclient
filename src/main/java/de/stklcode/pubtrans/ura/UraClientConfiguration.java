package de.stklcode.pubtrans.ura;

import java.io.Serializable;

/**
 * Configurstion Object for the {@link UraClient}.
 *
 * @author Stefan Kalscheuer
 * @since 2.0
 */
public class UraClientConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_INSTANT_PATH = "/interfaces/ura/instant_V1";
    private static final String DEFAULT_STREAM_PATH = "/interfaces/ura/stream_V1";

    private final String baseURL;
    private final String instantPath;
    private final String streamPath;

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
    public String getStreeamPath() {
        return this.streamPath;
    }

    /**
     * Builder for {@link UraClientConfiguration} objects.
     */
    public static class Builder {
        private final String baseURL;
        private String instantPath;
        private String streamPath;

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
         * Finally build the configuration object.
         *
         * @return The configuration.
         */
        public UraClientConfiguration build() {
            return new UraClientConfiguration(this);
        }
    }
}
