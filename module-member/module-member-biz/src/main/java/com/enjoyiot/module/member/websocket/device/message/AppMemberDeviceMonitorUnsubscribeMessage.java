package com.enjoyiot.module.member.websocket.device.message;

import lombok.Data;

/**
 * Unsubscribe from realtime data of the current device.
 */
@Data
public class AppMemberDeviceMonitorUnsubscribeMessage {

    private String productKey;

    private String dn;

}
