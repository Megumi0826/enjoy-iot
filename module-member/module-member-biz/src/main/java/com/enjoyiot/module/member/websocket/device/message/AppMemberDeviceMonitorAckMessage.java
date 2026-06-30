package com.enjoyiot.module.member.websocket.device.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppMemberDeviceMonitorAckMessage {

    private String action;

    private String productKey;

    private String dn;

    private Long deviceId;

}
