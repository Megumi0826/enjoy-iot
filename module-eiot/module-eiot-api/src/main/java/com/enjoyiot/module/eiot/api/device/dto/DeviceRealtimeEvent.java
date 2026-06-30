package com.enjoyiot.module.eiot.api.device.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Device realtime event published by the rule engine.
 *
 * <p>The event source is platform-wide. Consumers decide how to route it, for example
 * admin dashboard broadcast or member app device subscription push.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRealtimeEvent implements Serializable {

    public static final String EVENT_PROPERTY_REPORT = "property_report";
    public static final String EVENT_STATUS_CHANGE = "status_change";

    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_OFFLINE = "offline";

    /**
     * Event type, such as property report or status change.
     */
    private String eventType;

    /**
     * Platform internal device ID.
     */
    private Long deviceId;

    /**
     * Product key.
     */
    private String productKey;

    /**
     * Device name used by MQTT identity.
     */
    private String dn;

    /**
     * Display name of the device.
     */
    private String deviceName;

    /**
     * Online status for status change events.
     */
    private String status;

    /**
     * Event time in milliseconds.
     */
    private Long time;

    /**
     * Changed properties for property report events.
     */
    private Map<String, DevicePropertyCache> properties;

}
