package de.stklcode.pubtrans.ura.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonAppend;

import java.util.List;
import java.util.StringJoiner;

/**
 * Created by stefan on 24.12.16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stop {
    private final String id;
    private final String name;
    private final Double latitude;
    private final Double longitude;

    public Stop(String id, String name, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Stop(List raw) {
        if (raw.get(1) instanceof String)
            name = (String)raw.get(1);
        else
            throw new UnsupportedOperationException("Field 1 not of expected Type String");
        if (raw.get(2) instanceof String)
            id = (String)raw.get(2);
        else
            throw new UnsupportedOperationException("Field 2 not of expected Type String");
        if (raw.get(3) instanceof Double)
            latitude = (Double)raw.get(3);
        else
            throw new UnsupportedOperationException("Field 3 not of expected Type Double");
        if (raw.get(4) instanceof Double)
            longitude = (Double)raw.get(4);
        else
            throw new UnsupportedOperationException("Field 4 not of expected Type Double");

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
