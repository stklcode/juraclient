package de.stklcode.pubtrans.ura.model;

import java.io.IOException;
import java.util.List;

/**
 * Entity for a single trip.
 *
 * @author Stefan Kalscheuer <stefan@stklcode.de>
 */
public class Trip {
    private final Stop stop;
    private final String id;
    private final Integer visitID;
    private final String lineID;
    private final String lineName;
    private final Integer directionID;
    private final String destinationName;
    private final String destinationText;
    private final Long estimatedTime;
    private final String vehicleID;

    public Trip(String stopID, String stopName, String stopIndicator, Integer stopState, Double stopLatitude, Double stopLongitude,
                Integer visitID, String lineID, String lineName, Integer directionID, String destinationName, String destinationText, String vehicleID, String tripID, Long estimatedTime) {
        this(new Stop(stopID, stopName, stopIndicator, stopState, stopLatitude, stopLongitude),
                visitID, lineID, lineName, directionID, destinationName, destinationText, vehicleID, tripID, estimatedTime);
    }

    public Trip(Stop stop, Integer visitID, String lineID, String lineName, Integer directionID, String destinationName, String destinationText, String vehicleID, String tripID, Long estimatedTime) {
        this.stop = stop;
        this.visitID = visitID;
        this.lineID = lineID;
        this.lineName = lineName;
        this.directionID = directionID;
        this.destinationName = destinationName;
        this.destinationText = destinationText;
        this.vehicleID = vehicleID;
        this.id = tripID;
        this.estimatedTime = estimatedTime;
    }

    public Trip(List raw) throws IOException {
        if (raw == null || raw.size() < 16)
            throw new IOException("Invalid number of fields");

        stop = new Stop(raw);

        if (raw.get(7) instanceof Integer)
            visitID = (Integer) raw.get(7);
        else
            throw new IOException("Field 7 not of expected type Integer, found " + raw.get(7).getClass().getSimpleName());
        if (raw.get(8) instanceof String)
            lineID = (String)raw.get(8);
        else
            throw new IOException("Field 8 not of expected type String, found " + raw.get(8).getClass().getSimpleName());
        if (raw.get(9) instanceof String)
            lineName = (String)raw.get(9);
        else
            throw new IOException("Field 9 not of expected type String, found " + raw.get(9).getClass().getSimpleName());
        if (raw.get(10) instanceof Integer)
            directionID = (Integer)raw.get(10);
        else
            throw new IOException("Field 10 not of expected type Integer, found " + raw.get(10).getClass().getSimpleName());
        if (raw.get(11) instanceof String)
            destinationName = (String)raw.get(11);
        else
            throw new IOException("Field 11 not of expected type String, found " + raw.get(11).getClass().getSimpleName());
        if (raw.get(12) instanceof String)
            destinationText = (String)raw.get(12);
        else
            throw new IOException("Field 12 not of expected type String, found " + raw.get(12).getClass().getSimpleName());
        if (raw.get(13) instanceof String)
            vehicleID = (String)raw.get(13);
        else
            throw new IOException("Field 13 not of expected type String, found " + raw.get(13).getClass().getSimpleName());
        if (raw.get(14) instanceof String)
            id = (String)raw.get(14);
        else
            throw new IOException("Field 14 not of expected type String, found " + raw.get(14).getClass().getSimpleName());
        if (raw.get(15) instanceof Long)
            estimatedTime = (Long)raw.get(15);
        else
            throw new IOException("Field 15 not of expected type Long, found " + raw.get(15).getClass().getSimpleName());
    }
}
