package com.enjoyiot.module.member.websocket.device;

import com.enjoyiot.framework.websocket.core.listener.WebSocketMessageListener;
import com.enjoyiot.module.member.websocket.device.message.AppMemberDeviceMonitorSubscribeMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import jakarta.annotation.Resource;

@Component
public class AppMemberDeviceMonitorSubscribeListener implements WebSocketMessageListener<AppMemberDeviceMonitorSubscribeMessage> {

    @Resource
    private AppMemberDeviceMonitorWebSocketService monitorWebSocketService;

    @Override
    public void onMessage(WebSocketSession session, AppMemberDeviceMonitorSubscribeMessage message) {
        monitorWebSocketService.subscribe(session, message);
    }

    @Override
    public String getType() {
        return AppMemberDeviceMonitorMessageType.SUBSCRIBE;
    }

}
