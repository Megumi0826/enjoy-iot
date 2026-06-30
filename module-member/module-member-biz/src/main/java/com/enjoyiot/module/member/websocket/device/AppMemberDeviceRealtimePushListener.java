package com.enjoyiot.module.member.websocket.device;

import com.enjoyiot.module.eiot.api.device.dto.DeviceRealtimeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class AppMemberDeviceRealtimePushListener {

    @Resource
    private AppMemberDeviceMonitorWebSocketService monitorWebSocketService;

    @Async
    @EventListener
    public void onDeviceRealtime(DeviceRealtimeEvent event) {
        monitorWebSocketService.pushRealtimeEvent(event);
    }

}
