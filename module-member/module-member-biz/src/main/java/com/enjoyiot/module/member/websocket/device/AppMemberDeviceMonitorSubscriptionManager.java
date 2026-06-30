package com.enjoyiot.module.member.websocket.device;

import com.enjoyiot.framework.websocket.core.session.WebSocketSessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * In-memory subscription registry.
 *
 * <p>Status subscriptions are lightweight and scoped to all devices bound to the
 * current member. Monitor subscriptions are heavier and scoped to one selected
 * device per WebSocket session.</p>
 */
@Component
public class AppMemberDeviceMonitorSubscriptionManager {

    private final ConcurrentMap<Long, CopyOnWriteArraySet<String>> deviceSessions = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, AppMemberDeviceMonitorSubscription> sessionSubscriptions = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, AppMemberDeviceStatusSubscription> statusSubscriptions = new ConcurrentHashMap<>();

    @Resource
    private WebSocketSessionManager webSocketSessionManager;

    public void subscribe(AppMemberDeviceMonitorSubscription subscription) {
        unsubscribe(subscription.getSessionId());
        sessionSubscriptions.put(subscription.getSessionId(), subscription);
        deviceSessions.computeIfAbsent(subscription.getDeviceId(), key -> new CopyOnWriteArraySet<>())
                .add(subscription.getSessionId());
    }

    public void unsubscribe(String sessionId) {
        AppMemberDeviceMonitorSubscription removed = sessionSubscriptions.remove(sessionId);
        if (removed == null) {
            return;
        }
        CopyOnWriteArraySet<String> sessions = deviceSessions.get(removed.getDeviceId());
        if (sessions == null) {
            return;
        }
        sessions.remove(sessionId);
        if (sessions.isEmpty()) {
            deviceSessions.remove(removed.getDeviceId(), sessions);
        }
    }

    public void subscribeStatus(AppMemberDeviceStatusSubscription subscription) {
        statusSubscriptions.put(subscription.getSessionId(), subscription);
    }

    public void unsubscribeStatus(String sessionId) {
        statusSubscriptions.remove(sessionId);
    }

    public List<AppMemberDeviceStatusSubscription> getActiveStatusSubscriptions() {
        return statusSubscriptions.keySet().stream()
                .map(this::getActiveStatusSubscription)
                .filter(subscription -> subscription != null)
                .collect(Collectors.toList());
    }

    public List<AppMemberDeviceMonitorSubscription> getActiveSubscriptions(Long deviceId) {
        Collection<String> sessionIds = deviceSessions.get(deviceId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sessionIds.stream()
                .map(this::getActiveSubscription)
                .filter(subscription -> subscription != null)
                .collect(Collectors.toList());
    }

    private AppMemberDeviceMonitorSubscription getActiveSubscription(String sessionId) {
        WebSocketSession session = webSocketSessionManager.getSession(sessionId);
        if (session == null || !session.isOpen()) {
            unsubscribe(sessionId);
            return null;
        }
        return sessionSubscriptions.get(sessionId);
    }

    private AppMemberDeviceStatusSubscription getActiveStatusSubscription(String sessionId) {
        WebSocketSession session = webSocketSessionManager.getSession(sessionId);
        if (session == null || !session.isOpen()) {
            unsubscribeStatus(sessionId);
            return null;
        }
        return statusSubscriptions.get(sessionId);
    }

}
