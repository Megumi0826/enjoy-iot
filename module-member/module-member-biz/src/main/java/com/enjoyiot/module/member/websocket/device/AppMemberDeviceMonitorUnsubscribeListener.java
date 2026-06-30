package com.enjoyiot.module.member.websocket.device;

import com.enjoyiot.framework.websocket.core.listener.WebSocketMessageListener;
import com.enjoyiot.module.member.websocket.device.message.AppMemberDeviceMonitorUnsubscribeMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import jakarta.annotation.Resource;

@Component
public class AppMemberDeviceMonitorUnsubscribeListener implements WebSocketMessageListener<AppMemberDeviceMonitorUnsubscribeMessage> {

    @Resource
    private AppMemberDeviceMonitorWebSocketService monitorWebSocketService;

    @Override
    public void onMessage(WebSocketSession session, AppMemberDeviceMonitorUnsubscribeMessage message) {
        monitorWebSocketService.unsubscribe(session, message);
    }

    @Override
    public String getType() {
        return AppMemberDeviceMonitorMessageType.UNSUBSCRIBE;
    }

}
