package com.enjoyiot.module.member.websocket.device;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppMemberDeviceStatusSubscription {

    private String sessionId;

    private Long memberUserId;

}
