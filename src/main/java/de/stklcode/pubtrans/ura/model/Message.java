package de.stklcode.pubtrans.ura.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Entity for a message.
 *
 * @author Stefan Kalscheuer
 * @since 1.3
 */
public class Message implements Model {
    private static final long serialVersionUID = 5233610751062774273L;

    private static final int MSG_UUID = 7;
    private static final int MSG_TYPE = 8;
    private static final int MSG_PRIORITY = 9;
    private static final int MSG_TEXT = 10;
    private static final int NUM_OF_FIELDS = 11;

    /**
     * Corresponding stop.
     */
    private final Stop stop;

    /**
     * Message UUID.
     */
    private final String uuid;

    /**
     * Message type.
     */
    private final Integer type;

    /**
     * Message priority.
     */
    private final Integer priority;

    /**
     * Message text.
     */
    private final String text;

    /**
     * Construct Message object from complete set of data.
     *
     * @param stopID        Stop ID.
     * @param stopName      Stop name.
     * @param stopIndicator Stop Indicator.
     * @param stopState     Stop state.
     * @param stopLatitude  Stop geolocation latitude.
     * @param stopLongitude Stop geolocation latitude.
     * @param msgUUID       Message UUID.
     * @param msgType       Message type.
     * @param msgPriority   Message priority.
     * @param msgText       Message text.
     */
    public Message(final String stopID,
                   final String stopName,
                   final String stopIndicator,
                   final Integer stopState,
                   final Double stopLatitude,
                   final Double stopLongitude,
                   final String msgUUID,
                   final Integer msgType,
                   final Integer msgPriority,
                   final String msgText) {
        this(new Stop(stopID,
                        stopName,
                        stopIndicator,
                        stopState,
                        stopLatitude,
                        stopLongitude),
                msgUUID,
                msgType,
                msgPriority,
                msgText);
    }

    /**
     * Construct Message object from Stop model and set of additional data.
     *
     * @param stop        Stop model
     * @param msgUUID     Message UUID.
     * @param msgType     Message type.
     * @param msgPriority Message priority.
     * @param msgText     Message text.
     */
    public Message(final Stop stop,
                   final String msgUUID,
                   final Integer msgType,
                   final Integer msgPriority,
                   final String msgText) {
        this.stop = stop;
        this.uuid = msgUUID;
        this.type = msgType;
        this.priority = msgPriority;
        this.text = msgText;
    }

    /**
     * Construct Message object from raw list of attributes parsed from JSON.
     *
     * @param raw List of attributes from JSON line
     * @throws IOException Thrown on invalid line format.
     */
    public Message(final List<Serializable> raw) throws IOException {
        this(raw, null);
    }

    /**
     * Construct Message object from raw list of attributes parsed from JSON with explicitly specified version.
     *
     * @param raw     List of attributes from JSON line
     * @param version API version
     * @throws IOException Thrown on invalid line format.
     */
    public Message(final List<Serializable> raw, final String version) throws IOException {
        if (raw == null || raw.size() < NUM_OF_FIELDS) {
            throw new IOException("Invalid number of fields");
        }

        stop = new Stop(raw);

        if (raw.get(MSG_UUID) instanceof String) {
            uuid = (String) raw.get(MSG_UUID);
        } else {
            throw Model.typeErrorString(MSG_UUID, raw.get(MSG_UUID).getClass());
        }

        if (raw.get(MSG_TYPE) instanceof Integer) {
            type = (Integer) raw.get(MSG_TYPE);
        } else {
            throw Model.typeError(MSG_TYPE, raw.get(MSG_TYPE).getClass(), "Integer");
        }

        if (raw.get(MSG_PRIORITY) instanceof Integer) {
            priority = (Integer) raw.get(MSG_PRIORITY);
        } else {
            throw Model.typeError(MSG_PRIORITY, raw.get(MSG_PRIORITY).getClass(), "Integer");
        }

        if (raw.get(MSG_TEXT) instanceof String) {
            text = (String) raw.get(MSG_TEXT);
        } else {
            throw Model.typeErrorString(MSG_TEXT, raw.get(MSG_TEXT).getClass());
        }
    }

    /**
     * The stop, the message is targeted.
     *
     * @return The affected stop.
     */
    public Stop getStop() {
        return stop;
    }

    /**
     * This is the unique identifier of the flexible message.
     *
     * @return Message's unique identifier.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Messages are assigned a type.
     * This is predominantly in order to define how they should be displayed on on-street signs, however can be used to
     * alter display on other devices.
     * <ul>
     * <li>0: “Normal”</li>
     * <li>1: “Special”</li>
     * <li>2: “Full Matrix” – Stop is temporarily out of service and predictions should not be presented</li>
     * </ul>
     *
     * @return Message type.
     */
    public Integer getType() {
        return type;
    }

    /**
     * Messages are assigned a priority in order for them to be ranked.
     * Since it is possible for a stop to be assigned multiple messages it is important to ensure priority is given.
     * Priorities are between 1 and 10 (where 1 is the highest priority). By default, the message priority is set to 3.
     *
     * @return Message priority.
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * The text of the message. This should be displayed to the public.
     *
     * @return Message text.
     */
    public String getText() {
        return text;
    }
}
