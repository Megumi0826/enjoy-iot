package com.enjoyiot.module.eiot.websocket;

import com.enjoyiot.framework.common.enums.UserTypeEnum;
import com.enjoyiot.module.eiot.api.device.dto.DeviceRealtimeEvent;
import com.enjoyiot.module.infra.api.websocket.WebSocketSenderApi;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class DashboardWebSocketPushListener {

    public static final String MESSAGE_TYPE_DEVICE_REALTIME = "eiot-device-realtime";

    @Resource
    private WebSocketSenderApi webSocketSenderApi;

    @Async
    @EventListener
    public void onDeviceRealtime(DeviceRealtimeEvent event) {
        webSocketSenderApi.sendObject(
                UserTypeEnum.ADMIN.getValue(),
                MESSAGE_TYPE_DEVICE_REALTIME,
                event);
    }

}
