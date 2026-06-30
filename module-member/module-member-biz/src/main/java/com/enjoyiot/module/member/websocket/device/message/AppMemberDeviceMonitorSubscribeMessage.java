package com.enjoyiot.module.member.websocket.device.message;

import lombok.Data;

import java.util.List;

/**
 * Subscribe to realtime data of one bound device.
 */
@Data
public class AppMemberDeviceMonitorSubscribeMessage {

    private String productKey;

    private String dn;

    /**
     * Optional property whitelist. Empty means all reported properties.
     */
    private List<String> properties;

}
