package com.enjoyiot.module.member.service.device;

import cn.hutool.core.util.ObjectUtil;
import com.enjoyiot.module.eiot.api.device.DeviceApi;
import com.enjoyiot.module.eiot.api.device.DeviceHistoryApi;
import com.enjoyiot.module.eiot.api.device.dto.DeviceInfo;
import com.enjoyiot.module.eiot.api.device.dto.DeviceSleepReportPoint;
import com.enjoyiot.module.eiot.api.device.dto.DeviceSleepReportQuery;
import com.enjoyiot.module.member.dal.dataobject.device.MemberDeviceBindDO;
import com.enjoyiot.module.member.dal.mysql.device.MemberDeviceBindMapper;
import com.enjoyiot.module.member.service.device.dto.MemberDeviceSleepReportDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.enjoyiot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enjoyiot.module.member.enums.ErrorCodeConstants.DEVICE_BIND_NOT_EXISTS;
import static com.enjoyiot.module.member.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;

@Service
@Validated
public class MemberDeviceSleepReportServiceImpl implements MemberDeviceSleepReportService {

    private static final ZoneId REPORT_ZONE = ZoneId.of("Asia/Shanghai");

    private static final int DEFAULT_DAYS = 30;
    private static final int MAX_DAYS = 30;
    private static final long DAY_MILLIS = 24L * 60 * 60 * 1000;
    private static final long LATEST_QUERY_RANGE_MILLIS = 7L * DAY_MILLIS;
    private static final long ACTIVE_GAP_MILLIS = 3L * 60 * 60 * 1000;
    private static final long NON_SLEEP_CLOSE_GAP_MILLIS = 90L * 60 * 1000;
    private static final long MIN_SLEEP_DURATION_MILLIS = 15L * 60 * 1000;
    private static final int MIN_SLEEP_POINT_COUNT = 5;
    private static final int MIN_TOTAL_SLEEP_MINUTES = 15;
    private static final double RESET_TOLERANCE_MINUTES = 5.0;

    @Resource
    private MemberDeviceBindMapper memberDeviceBindMapper;

    @Resource
    private DeviceApi deviceApi;

    @Resource
    private DeviceHistoryApi deviceHistoryApi;

    @Override
    public MemberDeviceSleepReportDTO getLatestReport(Long memberUserId, String productKey, String dn) {
        DeviceInfo device = validateBoundDevice(memberUserId, productKey, dn);
        long endTime = System.currentTimeMillis();
        long startTime = endTime - LATEST_QUERY_RANGE_MILLIS;

        return buildReports(device.getId(), startTime, endTime, endTime).stream()
                .max(Comparator.comparing(MemberDeviceSleepReportDTO::getSleepStartTime))
                .orElse(null);
    }

    @Override
    public List<MemberDeviceSleepReportDTO> getReportList(Long memberUserId, String productKey, String dn, Integer days) {
        DeviceInfo device = validateBoundDevice(memberUserId, productKey, dn);
        int normalizedDays = normalizeDays(days);
        long endTime = System.currentTimeMillis();
        long reportStartTime = endTime - normalizedDays * DAY_MILLIS;
        long queryStartTime = reportStartTime - DAY_MILLIS;

        return buildReports(device.getId(), queryStartTime, endTime, endTime).stream()
                .filter(report -> report.getSleepStartTime() != null && report.getSleepStartTime() >= reportStartTime)
                .sorted(Comparator.comparing(MemberDeviceSleepReportDTO::getSleepStartTime).reversed())
                .collect(Collectors.toList());
    }

    private DeviceInfo validateBoundDevice(Long memberUserId, String productKey, String dn) {
        DeviceInfo device = deviceApi.getDeviceByPkDnByCache(productKey, dn);
        if (ObjectUtil.isNull(device)) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        MemberDeviceBindDO bind = memberDeviceBindMapper.selectByMemberUserIdAndDeviceId(memberUserId, device.getId());
        if (ObjectUtil.isNull(bind)) {
            throw exception(DEVICE_BIND_NOT_EXISTS);
        }
        return device;
    }

    private int normalizeDays(Integer days) {
        if (days == null) {
            return DEFAULT_DAYS;
        }
        return Math.max(1, Math.min(MAX_DAYS, days));
    }

    private List<MemberDeviceSleepReportDTO> buildReports(Long deviceId, long startTime, long endTime, long nowTime) {
        DeviceSleepReportQuery query = new DeviceSleepReportQuery();
        query.setDeviceId(deviceId);
        query.setStartTime(startTime);
        query.setEndTime(endTime);

        List<DeviceSleepReportPoint> points = deviceHistoryApi.getSleepReportPoints(query).stream()
                .filter(point -> point.getTime() != null)
                .sorted(Comparator.comparing(DeviceSleepReportPoint::getTime))
                .collect(Collectors.toList());

        return detectSessions(points, nowTime).stream()
                .map(session -> buildReport(session, nowTime))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<SleepSession> detectSessions(List<DeviceSleepReportPoint> points, long nowTime) {
        List<SleepSession> sessions = new ArrayList<>();
        SleepSession current = null;
        DeviceSleepReportPoint previousPoint = null;
        DeviceSleepReportPoint previousActivePoint = null;

        for (DeviceSleepReportPoint point : points) {
            if (isRealSleepPoint(point, previousPoint)) {
                if (current == null || shouldStartNewSession(previousActivePoint, point)) {
                    if (current != null && current.hasEnoughSleep()) {
                        sessions.add(current);
                    }
                    current = new SleepSession();
                }
                current.addActivePoint(point);
                previousPoint = point;
                previousActivePoint = point;
                continue;
            }

            if (current == null) {
                previousPoint = point;
                continue;
            }

            if (isSessionEndPoint(point)) {
                if (hasSleepSummary(point)) {
                    current.addActivePoint(point);
                    previousActivePoint = point;
                }
                current.markExplicitEnd(point.getTime());
                if (current.hasEnoughSleep()) {
                    sessions.add(current);
                }
                current = null;
                previousPoint = point;
                previousActivePoint = null;
                continue;
            }

            if (shouldCloseByNonSleep(current, point)) {
                if (point.getTime() - current.getLastActiveTime() > ACTIVE_GAP_MILLIS) {
                    current.markDataInterrupted();
                } else {
                    current.markNaturalEnd(current.getLastActiveTime());
                }
                if (current.hasEnoughSleep()) {
                    sessions.add(current);
                }
                current = null;
                previousActivePoint = null;
            }
            previousPoint = point;
        }

        if (current != null && current.hasEnoughSleep()) {
            if (nowTime - current.getLastActiveTime() > NON_SLEEP_CLOSE_GAP_MILLIS) {
                current.markDataInterrupted();
            }
            sessions.add(current);
        }
        return sessions;
    }

    private boolean shouldStartNewSession(DeviceSleepReportPoint previousActivePoint, DeviceSleepReportPoint point) {
        if (previousActivePoint == null) {
            return false;
        }
        long gap = point.getTime() - previousActivePoint.getTime();
        if (gap > ACTIVE_GAP_MILLIS) {
            return true;
        }
        return positive(point.getAlgoTotalSleepTime())
                && positive(previousActivePoint.getAlgoTotalSleepTime())
                && point.getAlgoTotalSleepTime() + RESET_TOLERANCE_MINUTES < previousActivePoint.getAlgoTotalSleepTime();
    }

    private boolean shouldCloseByNonSleep(SleepSession session, DeviceSleepReportPoint point) {
        return !isSessionCandidatePoint(point)
                && point.getTime() - session.getLastActiveTime() > NON_SLEEP_CLOSE_GAP_MILLIS;
    }

    private MemberDeviceSleepReportDTO buildReport(SleepSession session, long nowTime) {
        List<DeviceSleepReportPoint> points = session.getActivePoints();
        if (points.isEmpty()) {
            return null;
        }

        DeviceSleepReportPoint firstPoint = points.get(0);
        long sleepStartTime = firstPoint.getTime();
        int sleepLatency = maxInt(points, DeviceSleepReportPoint::getAlgoSleepLatency);
        long prepareStartTime = estimatePrepareStartTime(sleepStartTime, sleepLatency);

        MemberDeviceSleepReportDTO report = new MemberDeviceSleepReportDTO();
        report.setDate(toReportDate(sleepStartTime));
        report.setStatus(resolveStatus(session, nowTime));
        report.setPrepareStartTime(prepareStartTime);
        report.setSleepStartTime(sleepStartTime);
        report.setEndTime(Optional.ofNullable(session.getEndTime()).orElse(session.getLastActiveTime()));
        report.setTotalSleepTime(maxInt(points, DeviceSleepReportPoint::getAlgoTotalSleepTime));
        report.setDeepSleepTime(maxInt(points, DeviceSleepReportPoint::getAlgoDeepSleepTime));
        report.setLightSleepTime(maxInt(points, DeviceSleepReportPoint::getAlgoLightSleepTime));
        report.setRemSleepTime(maxInt(points, DeviceSleepReportPoint::getAlgoRemSleepTime));
        report.setAwakeTime(maxInt(points, DeviceSleepReportPoint::getAlgoAwakeTime));
        report.setOutOfBedTime(maxInt(points, DeviceSleepReportPoint::getAlgoOutOfBedTime));
        report.setSleepLatency(sleepLatency);
        report.setWakeCount(maxInt(points, DeviceSleepReportPoint::getAlgoWakeCount));
        report.setSleepCycles(maxInt(points, DeviceSleepReportPoint::getAlgoSleepCycles));
        report.setAvgHeartRate(lastInt(points, DeviceSleepReportPoint::getAvgHeartRate));
        report.setAvgBreathingRate(lastInt(points, DeviceSleepReportPoint::getAvgBreathingRate));
        report.setApneaCount(maxInt(points, DeviceSleepReportPoint::getApneaCount));
        report.setLargeMoveRatio(lastDouble(points, DeviceSleepReportPoint::getLargeMoveRatio));
        report.setSmallMoveRatio(lastDouble(points, DeviceSleepReportPoint::getSmallMoveRatio));
        report.setScore(lastPositive(points, DeviceSleepReportPoint::getAlgoTotalScore));
        report.setSourcePointCount(points.size());
        return report;
    }

    private long estimatePrepareStartTime(long sleepStartTime, int sleepLatency) {
        if (sleepLatency > 0 && sleepLatency <= 12 * 60) {
            return sleepStartTime - sleepLatency * 60_000L;
        }
        return sleepStartTime;
    }

    private String resolveStatus(SleepSession session, long nowTime) {
        if (session.hasExplicitEnd() || session.hasNaturalEnd()) {
            return "COMPLETED";
        }
        if (nowTime - session.getLastActiveTime() <= NON_SLEEP_CLOSE_GAP_MILLIS) {
            return "IN_PROGRESS";
        }
        return "INCOMPLETE";
    }

    private String toReportDate(long sleepStartTime) {
        LocalDate date = Instant.ofEpochMilli(sleepStartTime).atZone(REPORT_ZONE).toLocalDate();
        return date.toString();
    }

    private boolean isRealSleepPoint(DeviceSleepReportPoint point, DeviceSleepReportPoint previousPoint) {
        Integer state = toInt(point.getAlgorithmState());
        if (state != null && (state == 0 || state == 8)) {
            return false;
        }

        boolean sleepStage = isSleepStage(state);
        boolean activeState = isActiveState(state);
        boolean summaryStarted = hasSleepSummary(point) && (previousPoint == null || !hasSleepSummary(previousPoint));
        boolean summaryIncreased = hasSleepSummaryIncreased(point, previousPoint);

        if (sleepStage && (summaryStarted || summaryIncreased)) {
            return true;
        }

        if (summaryIncreased && (activeState || state == null)) {
            return true;
        }

        return false;
    }

    private boolean isSessionCandidatePoint(DeviceSleepReportPoint point) {
        Integer state = toInt(point.getAlgorithmState());
        if (state != null && state >= 1 && state <= 7) {
            return true;
        }
        if (valueOrZero(point.getSleepProgress()) >= 50) {
            return true;
        }
        return state == null && positive(point.getAlgoTotalSleepTime());
    }

    private boolean isSessionEndPoint(DeviceSleepReportPoint point) {
        Integer state = toInt(point.getAlgorithmState());
        return state != null && state == 8;
    }

    private boolean isSleepStage(Integer state) {
        return state != null && state >= 3 && state <= 5;
    }

    private boolean isActiveState(Integer state) {
        return state != null && state >= 1 && state <= 7;
    }

    private int maxInt(List<DeviceSleepReportPoint> points, PointValueGetter getter) {
        return (int) Math.round(points.stream()
                .map(getter::get)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0));
    }

    private Integer lastInt(List<DeviceSleepReportPoint> points, PointValueGetter getter) {
        Double value = lastNonNull(points, getter);
        return value == null ? null : (int) Math.round(value);
    }

    private Double lastDouble(List<DeviceSleepReportPoint> points, PointValueGetter getter) {
        Double value = lastNonNull(points, getter);
        return value == null ? null : Math.round(value * 100.0) / 100.0;
    }

    private Double lastNonNull(List<DeviceSleepReportPoint> points, PointValueGetter getter) {
        for (int i = points.size() - 1; i >= 0; i--) {
            Double value = getter.get(points.get(i));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Double lastPositive(List<DeviceSleepReportPoint> points, PointValueGetter getter) {
        for (int i = points.size() - 1; i >= 0; i--) {
            Double value = getter.get(points.get(i));
            if (positive(value)) {
                return Math.round(value * 10.0) / 10.0;
            }
        }
        return null;
    }

    private Integer toInt(Double value) {
        return value == null ? null : (int) Math.round(value);
    }

    private boolean positive(Double value) {
        return value != null && value > 0;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0 : value;
    }

    private boolean hasSleepSummary(DeviceSleepReportPoint point) {
        return positive(point.getAlgoTotalSleepTime())
                || positive(point.getAlgoDeepSleepTime())
                || positive(point.getAlgoLightSleepTime())
                || positive(point.getAlgoRemSleepTime())
                || positive(point.getAlgoAwakeTime())
                || positive(point.getAlgoOutOfBedTime())
                || positive(point.getAlgoTotalScore());
    }

    private boolean hasSleepSummaryIncreased(DeviceSleepReportPoint point, DeviceSleepReportPoint previousPoint) {
        if (previousPoint == null) {
            return false;
        }

        return increased(point.getAlgoTotalSleepTime(), previousPoint.getAlgoTotalSleepTime())
                || increased(point.getAlgoDeepSleepTime(), previousPoint.getAlgoDeepSleepTime())
                || increased(point.getAlgoLightSleepTime(), previousPoint.getAlgoLightSleepTime())
                || increased(point.getAlgoRemSleepTime(), previousPoint.getAlgoRemSleepTime())
                || increased(point.getAlgoAwakeTime(), previousPoint.getAlgoAwakeTime())
                || increased(point.getAlgoOutOfBedTime(), previousPoint.getAlgoOutOfBedTime())
                || increased(point.getAlgoSleepLatency(), previousPoint.getAlgoSleepLatency())
                || increased(point.getAlgoWakeCount(), previousPoint.getAlgoWakeCount())
                || increased(point.getAlgoSleepCycles(), previousPoint.getAlgoSleepCycles())
                || increased(point.getAlgoTotalScore(), previousPoint.getAlgoTotalScore());
    }

    private boolean increased(Double current, Double previous) {
        return current != null && current > 0 && (previous == null || current > previous);
    }

    @FunctionalInterface
    private interface PointValueGetter {
        Double get(DeviceSleepReportPoint point);
    }

    private static class SleepSession {

        private final List<DeviceSleepReportPoint> activePoints = new ArrayList<>();

        private Long endTime;

        private boolean explicitEnd;

        private boolean naturalEnd;

        private boolean dataInterrupted;

        private void addActivePoint(DeviceSleepReportPoint point) {
            activePoints.add(point);
        }

        private void markExplicitEnd(Long endTime) {
            this.endTime = endTime;
            this.explicitEnd = true;
        }

        private void markNaturalEnd(Long endTime) {
            this.endTime = endTime;
            this.naturalEnd = true;
        }

        private void markDataInterrupted() {
            this.dataInterrupted = true;
        }

        private boolean hasEnoughSleep() {
            if (activePoints.size() < MIN_SLEEP_POINT_COUNT) {
                return false;
            }

            long duration = getLastActiveTime() - activePoints.get(0).getTime();
            return duration >= MIN_SLEEP_DURATION_MILLIS
                    || activePoints.stream().anyMatch(point -> point.getAlgoTotalSleepTime() != null
                    && point.getAlgoTotalSleepTime() >= MIN_TOTAL_SLEEP_MINUTES);
        }

        private Long getLastActiveTime() {
            return activePoints.get(activePoints.size() - 1).getTime();
        }

        private List<DeviceSleepReportPoint> getActivePoints() {
            return activePoints;
        }

        private Long getEndTime() {
            return endTime;
        }

        private boolean hasExplicitEnd() {
            return explicitEnd;
        }

        private boolean hasNaturalEnd() {
            return naturalEnd && !dataInterrupted;
        }
    }

}
