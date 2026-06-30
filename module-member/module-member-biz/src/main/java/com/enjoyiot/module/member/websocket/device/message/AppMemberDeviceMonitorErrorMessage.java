package com.enjoyiot.module.member.websocket.device.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppMemberDeviceMonitorErrorMessage {

    private String code;

    private String message;

}
