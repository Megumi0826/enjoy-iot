package com.enjoyiot.module.member.websocket.device;

/**
 * WebSocket message types used by the member mini program device monitor.
 */
public interface AppMemberDeviceMonitorMessageType {

    String SUBSCRIBE = "member-device-monitor-subscribe";

    String UNSUBSCRIBE = "member-device-monitor-unsubscribe";

    String STATUS_SUBSCRIBE = "member-device-status-subscribe";

    String STATUS_UNSUBSCRIBE = "member-device-status-unsubscribe";

    String ACK = "member-device-monitor-ack";

    String ERROR = "member-device-monitor-error";

    String SNAPSHOT = "member-device-monitor-snapshot";

    String PROPERTY = "member-device-monitor-property";

    String STATUS = "member-device-monitor-status";

}
