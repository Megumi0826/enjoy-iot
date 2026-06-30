package com.enjoyiot.module.member.websocket.device;

import com.enjoyiot.framework.common.enums.UserTypeEnum;
import com.enjoyiot.framework.websocket.core.util.WebSocketFrameworkUtils;
import com.enjoyiot.module.eiot.api.device.DeviceApi;
import com.enjoyiot.module.eiot.api.device.dto.DeviceInfo;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyCache;
import com.enjoyiot.module.eiot.api.device.dto.DeviceRealtimeEvent;
import com.enjoyiot.module.infra.api.websocket.WebSocketSenderApi;
import com.enjoyiot.module.member.dal.dataobject.device.MemberDeviceBindDO;
import com.enjoyiot.module.member.dal.mysql.device.MemberDeviceBindMapper;
import com.enjoyiot.module.member.websocket.device.message.AppMemberDeviceMonitorAckMessage;
import com.enjoyiot.module.member.websocket.device.message.AppMemberDeviceMonitorErrorMessage;
import com.enjoyiot.module.member.websocket.device.message.AppMemberDeviceMonitorPropertyMessage;
import com.enjoyiot.module.member.websocket.device.message.AppMemberDeviceMonitorStatusMessage;
import com.enjoyiot.module.member.websocket.device.message.AppMemberDeviceMonitorSubscribeMessage;
import com.enjoyiot.module.member.websocket.device.message.AppMemberDeviceMonitorUnsubscribeMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Member mini program realtime device monitor over WebSocket.
 */
@Service
public class AppMemberDeviceMonitorWebSocketService {

    private static final String ERROR_UNAUTHORIZED = "UNAUTHORIZED";

    private static final String ERROR_DEVICE_NOT_EXISTS = "DEVICE_NOT_EXISTS";

    private static final String ERROR_DEVICE_NOT_BOUND = "DEVICE_NOT_BOUND";

    private static final String ERROR_INVALID_REQUEST = "INVALID_REQUEST";

    @Resource
    private DeviceApi deviceApi;

    @Resource
    private MemberDeviceBindMapper memberDeviceBindMapper;

    @Resource
    private WebSocketSenderApi webSocketSenderApi;

    @Resource
    private AppMemberDeviceMonitorSubscriptionManager subscriptionManager;

    public void subscribe(WebSocketSession session, AppMemberDeviceMonitorSubscribeMessage message) {
        if (!isMemberSession(session)) {
            sendError(session.getId(), ERROR_UNAUTHORIZED, "请先登录小程序账号");
            return;
        }
        if (message == null || isBlank(message.getProductKey()) || isBlank(message.getDn())) {
            sendError(session.getId(), ERROR_INVALID_REQUEST, "设备信息不能为空");
            return;
        }

        DeviceInfo device = deviceApi.getDeviceByPkDnByCache(message.getProductKey(), message.getDn());
        if (device == null) {
            sendError(session.getId(), ERROR_DEVICE_NOT_EXISTS, "设备不存在");
            return;
        }

        Long memberUserId = WebSocketFrameworkUtils.getLoginUserId(session);
        MemberDeviceBindDO bind = memberDeviceBindMapper.selectByMemberUserIdAndDeviceId(memberUserId, device.getId());
        if (bind == null) {
            sendError(session.getId(), ERROR_DEVICE_NOT_BOUND, "设备绑定关系不存在");
            return;
        }

        AppMemberDeviceMonitorSubscription subscription = AppMemberDeviceMonitorSubscription.builder()
                .sessionId(session.getId())
                .memberUserId(memberUserId)
                .deviceId(device.getId())
                .productKey(device.getProductKey())
                .dn(device.getDn())
                .properties(toPropertySet(message.getProperties()))
                .build();
        subscriptionManager.subscribe(subscription);

        sendAck(session.getId(), "subscribe", device);
        sendSnapshot(session.getId(), subscription, device);
        sendStatus(session.getId(), subscription, device);
    }

    public void unsubscribe(WebSocketSession session, AppMemberDeviceMonitorUnsubscribeMessage message) {
        subscriptionManager.unsubscribe(session.getId());
        webSocketSenderApi.sendObject(session.getId(), AppMemberDeviceMonitorMessageType.ACK,
                AppMemberDeviceMonitorAckMessage.builder()
                        .action("unsubscribe")
                        .productKey(message == null ? null : message.getProductKey())
                        .dn(message == null ? null : message.getDn())
                        .build());
    }

    public void subscribeStatus(WebSocketSession session) {
        if (!isMemberSession(session)) {
            sendError(session.getId(), ERROR_UNAUTHORIZED, "请先登录小程序账号");
            return;
        }

        Long memberUserId = WebSocketFrameworkUtils.getLoginUserId(session);
        subscriptionManager.subscribeStatus(AppMemberDeviceStatusSubscription.builder()
                .sessionId(session.getId())
                .memberUserId(memberUserId)
                .build());

        webSocketSenderApi.sendObject(session.getId(), AppMemberDeviceMonitorMessageType.ACK,
                AppMemberDeviceMonitorAckMessage.builder()
                        .action("status-subscribe")
                        .build());
        sendBoundDeviceStatuses(session.getId(), memberUserId);
    }

    public void unsubscribeStatus(WebSocketSession session) {
        subscriptionManager.unsubscribeStatus(session.getId());
        webSocketSenderApi.sendObject(session.getId(), AppMemberDeviceMonitorMessageType.ACK,
                AppMemberDeviceMonitorAckMessage.builder()
                        .action("status-unsubscribe")
                        .build());
    }

    public void pushRealtimeEvent(DeviceRealtimeEvent event) {
        if (event == null || event.getDeviceId() == null) {
            return;
        }
        if (DeviceRealtimeEvent.EVENT_PROPERTY_REPORT.equals(event.getEventType())) {
            pushProperty(event);
            return;
        }
        if (DeviceRealtimeEvent.EVENT_STATUS_CHANGE.equals(event.getEventType())) {
            pushStatus(event);
        }
    }

    private void pushProperty(DeviceRealtimeEvent event) {
        if (event.getProperties() == null || event.getProperties().isEmpty()) {
            return;
        }
        for (AppMemberDeviceMonitorSubscription subscription : subscriptionManager.getActiveSubscriptions(event.getDeviceId())) {
            AppMemberDeviceMonitorPropertyMessage message = buildPropertyMessage(subscription, event);
            if (message.getProperties().isEmpty()) {
                continue;
            }
            webSocketSenderApi.sendObject(subscription.getSessionId(),
                    AppMemberDeviceMonitorMessageType.PROPERTY, message);
        }
    }

    private void pushStatus(DeviceRealtimeEvent event) {
        Set<String> pushedSessions = new HashSet<>();
        for (AppMemberDeviceMonitorSubscription subscription : subscriptionManager.getActiveSubscriptions(event.getDeviceId())) {
            webSocketSenderApi.sendObject(subscription.getSessionId(),
                    AppMemberDeviceMonitorMessageType.STATUS,
                    AppMemberDeviceMonitorStatusMessage.builder()
                            .productKey(subscription.getProductKey())
                            .dn(subscription.getDn())
                            .deviceId(subscription.getDeviceId())
                            .time(event.getTime())
                            .status(event.getStatus())
                            .mqttOnline(DeviceRealtimeEvent.STATUS_ONLINE.equals(event.getStatus()))
                            .build());
            pushedSessions.add(subscription.getSessionId());
        }
        DeviceInfo device = resolveEventDevice(event);
        if (device == null) {
            return;
        }
        for (AppMemberDeviceStatusSubscription subscription : subscriptionManager.getActiveStatusSubscriptions()) {
            if (pushedSessions.contains(subscription.getSessionId())) {
                continue;
            }
            if (memberDeviceBindMapper.selectByMemberUserIdAndDeviceId(subscription.getMemberUserId(), device.getId()) == null) {
                continue;
            }
            sendStatus(subscription.getSessionId(), device, event.getTime(),
                    DeviceRealtimeEvent.STATUS_ONLINE.equals(event.getStatus()));
        }
    }

    private AppMemberDeviceMonitorPropertyMessage buildPropertyMessage(
            AppMemberDeviceMonitorSubscription subscription, DeviceRealtimeEvent event) {
        Map<String, Object> properties = new LinkedHashMap<>();
        Map<String, Long> propertyTimes = new LinkedHashMap<>();
        event.getProperties().forEach((key, value) -> {
            if (!subscription.acceptsProperty(key) || value == null) {
                return;
            }
            properties.put(key, value.getValue());
            propertyTimes.put(key, value.getOccurred());
        });
        return AppMemberDeviceMonitorPropertyMessage.builder()
                .productKey(subscription.getProductKey())
                .dn(subscription.getDn())
                .deviceId(subscription.getDeviceId())
                .time(event.getTime())
                .properties(properties)
                .propertyTimes(propertyTimes)
                .build();
    }

    private void sendSnapshot(String sessionId, AppMemberDeviceMonitorSubscription subscription, DeviceInfo device) {
        Map<String, DevicePropertyCache> cache = deviceApi.getPropertiesFromCache(device.getId());
        if (cache == null || cache.isEmpty()) {
            return;
        }
        DeviceRealtimeEvent snapshot = DeviceRealtimeEvent.builder()
                .eventType(DeviceRealtimeEvent.EVENT_PROPERTY_REPORT)
                .deviceId(device.getId())
                .productKey(device.getProductKey())
                .dn(device.getDn())
                .deviceName(device.getName())
                .time(System.currentTimeMillis())
                .properties(cache)
                .build();
        webSocketSenderApi.sendObject(sessionId, AppMemberDeviceMonitorMessageType.SNAPSHOT,
                buildPropertyMessage(subscription, snapshot));
    }

    private void sendStatus(String sessionId, AppMemberDeviceMonitorSubscription subscription, DeviceInfo device) {
        sendStatus(sessionId, device, System.currentTimeMillis(), device.isOnline());
    }

    private void sendBoundDeviceStatuses(String sessionId, Long memberUserId) {
        List<Long> deviceIds = memberDeviceBindMapper.selectListByMemberUserId(memberUserId)
                .stream()
                .map(MemberDeviceBindDO::getDeviceId)
                .toList();
        if (deviceIds.isEmpty()) {
            return;
        }
        for (DeviceInfo device : deviceApi.getDeviceInfoList(deviceIds)) {
            if (device != null) {
                sendStatus(sessionId, device, System.currentTimeMillis(), device.isOnline());
            }
        }
    }

    private void sendStatus(String sessionId, DeviceInfo device, Long time, boolean online) {
        webSocketSenderApi.sendObject(sessionId, AppMemberDeviceMonitorMessageType.STATUS,
                AppMemberDeviceMonitorStatusMessage.builder()
                        .productKey(device.getProductKey())
                        .dn(device.getDn())
                        .deviceId(device.getId())
                        .time(time == null ? System.currentTimeMillis() : time)
                        .status(online ? DeviceRealtimeEvent.STATUS_ONLINE : DeviceRealtimeEvent.STATUS_OFFLINE)
                        .mqttOnline(online)
                        .build());
    }

    private void sendAck(String sessionId, String action, DeviceInfo device) {
        webSocketSenderApi.sendObject(sessionId, AppMemberDeviceMonitorMessageType.ACK,
                AppMemberDeviceMonitorAckMessage.builder()
                        .action(action)
                        .productKey(device.getProductKey())
                        .dn(device.getDn())
                        .deviceId(device.getId())
                        .build());
    }

    private void sendError(String sessionId, String code, String message) {
        webSocketSenderApi.sendObject(sessionId, AppMemberDeviceMonitorMessageType.ERROR,
                AppMemberDeviceMonitorErrorMessage.builder()
                        .code(code)
                        .message(message)
                        .build());
    }

    private boolean isMemberSession(WebSocketSession session) {
        Integer userType = WebSocketFrameworkUtils.getLoginUserType(session);
        Long userId = WebSocketFrameworkUtils.getLoginUserId(session);
        return userId != null && UserTypeEnum.MEMBER.getValue().equals(userType);
    }

    private DeviceInfo resolveEventDevice(DeviceRealtimeEvent event) {
        if (!isBlank(event.getProductKey()) && !isBlank(event.getDn())) {
            return deviceApi.getDeviceByPkDnByCache(event.getProductKey(), event.getDn());
        }
        return deviceApi.getDeviceInfoFromCache(event.getDeviceId());
    }

    private Set<String> toPropertySet(List<String> properties) {
        if (properties == null || properties.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>();
        for (String property : properties) {
            if (!isBlank(property)) {
                result.add(property);
            }
        }
        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
