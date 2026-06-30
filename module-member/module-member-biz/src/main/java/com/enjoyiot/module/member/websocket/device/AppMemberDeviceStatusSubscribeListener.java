package com.enjoyiot.module.member.websocket.device;

import com.enjoyiot.framework.websocket.core.listener.WebSocketMessageListener;
import com.enjoyiot.module.member.websocket.device.message.AppMemberDeviceStatusSubscribeMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import jakarta.annotation.Resource;

@Component
public class AppMemberDeviceStatusSubscribeListener implements WebSocketMessageListener<AppMemberDeviceStatusSubscribeMessage> {

    @Resource
    private AppMemberDeviceMonitorWebSocketService monitorWebSocketService;

    @Override
    public void onMessage(WebSocketSession session, AppMemberDeviceStatusSubscribeMessage message) {
        monitorWebSocketService.subscribeStatus(session);
    }

    @Override
    public String getType() {
        return AppMemberDeviceMonitorMessageType.STATUS_SUBSCRIBE;
    }

}
