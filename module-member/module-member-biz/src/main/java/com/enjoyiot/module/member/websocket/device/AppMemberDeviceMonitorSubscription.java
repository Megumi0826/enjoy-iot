package com.enjoyiot.module.member.websocket.device;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AppMemberDeviceMonitorSubscription {

    private String sessionId;

    private Long memberUserId;

    private Long deviceId;

    private String productKey;

    private String dn;

    private Set<String> properties;

    public boolean acceptsProperty(String property) {
        return properties == null || properties.isEmpty() || properties.contains(property);
    }

}
