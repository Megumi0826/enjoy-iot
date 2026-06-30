package com.enjoyiot.module.member.websocket.device.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppMemberDeviceMonitorStatusMessage {

    private String productKey;

    private String dn;

    private Long deviceId;

    private Long time;

    private String status;

    private Boolean mqttOnline;

}
