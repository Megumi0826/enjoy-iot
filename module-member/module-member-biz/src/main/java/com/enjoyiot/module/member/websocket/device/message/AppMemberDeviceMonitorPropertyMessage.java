package com.enjoyiot.module.member.websocket.device.message;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AppMemberDeviceMonitorPropertyMessage {

    private String productKey;

    private String dn;

    private Long deviceId;

    private Long time;

    /**
     * Flattened property values for mini program rendering.
     */
    private Map<String, Object> properties;

    /**
     * Occurred time of each property.
     */
    private Map<String, Long> propertyTimes;

}
