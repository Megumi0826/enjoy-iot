package com.enjoyiot.module.member.websocket.device;

import com.enjoyiot.framework.websocket.core.listener.WebSocketMessageListener;
import com.enjoyiot.module.member.websocket.device.message.AppMemberDeviceStatusUnsubscribeMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import jakarta.annotation.Resource;

@Component
public class AppMemberDeviceStatusUnsubscribeListener implements WebSocketMessageListener<AppMemberDeviceStatusUnsubscribeMessage> {

    @Resource
    private AppMemberDeviceMonitorWebSocketService monitorWebSocketService;

    @Override
    public void onMessage(WebSocketSession session, AppMemberDeviceStatusUnsubscribeMessage message) {
        monitorWebSocketService.unsubscribeStatus(session);
    }

    @Override
    public String getType() {
        return AppMemberDeviceMonitorMessageType.STATUS_UNSUBSCRIBE;
    }

}
