package com.enjoyiot.module.eiot.api.device;

import com.enjoyiot.eiot.IDevicePropertyData;
import com.enjoyiot.framework.common.exception.ServiceException;
import com.enjoyiot.framework.tenant.core.util.TenantUtils;
import com.enjoyiot.module.eiot.api.device.dto.DeviceInfo;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendQuery;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendResp;
import com.enjoyiot.module.eiot.api.device.dto.DeviceSleepReportPoint;
import com.enjoyiot.module.eiot.api.device.dto.DeviceSleepReportQuery;
import com.enjoyiot.module.eiot.api.thingmodel.dto.ThingModel;
import com.enjoyiot.module.eiot.service.device.DeviceInfoService;
import com.enjoyiot.module.eiot.service.product.ThingModelService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DeviceHistoryApiImpl implements DeviceHistoryApi {

    private static final int DEFAULT_MAX_POINTS = 300;
    private static final int MIN_MAX_POINTS = 30;
    private static final int MAX_MAX_POINTS = 500;
    private static final long MAX_QUERY_RANGE_MILLIS = 90L * 24 * 60 * 60 * 1000;

    private static final List<IntervalOption> INTERVAL_OPTIONS = Arrays.asList(
            new IntervalOption("10s", 10_000L),
            new IntervalOption("30s", 30_000L),
            new IntervalOption("1m", 60_000L),
            new IntervalOption("5m", 5 * 60_000L),
            new IntervalOption("10m", 10 * 60_000L),
            new IntervalOption("30m", 30 * 60_000L),
            new IntervalOption("1h", 60 * 60_000L),
            new IntervalOption("3h", 3 * 60 * 60_000L),
            new IntervalOption("6h", 6 * 60 * 60_000L),
            new IntervalOption("12h", 12 * 60 * 60_000L),
            new IntervalOption("1d", 24 * 60 * 60_000L)
    );

    private static final Set<String> NUMERIC_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ThingModel.DataType.TYPE_BOOL,
            ThingModel.DataType.TYPE_ENUM,
            ThingModel.DataType.TYPE_INT32,
            ThingModel.DataType.TYPE_INT64,
            ThingModel.DataType.TYPE_FLOAT,
            ThingModel.DataType.TYPE_DOUBLE
    )));

    @Resource
    private DeviceInfoService deviceInfoService;

    @Resource
    private ThingModelService thingModelService;

    @Lazy
    @Resource
    private IDevicePropertyData devicePropertyData;

    @Override
    public DevicePropertyTrendResp getPropertyTrend(DevicePropertyTrendQuery query) {
        return TenantUtils.executeIgnoreResult(() -> doGetPropertyTrend(query));
    }

    @Override
    public List<DeviceSleepReportPoint> getSleepReportPoints(DeviceSleepReportQuery query) {
        return TenantUtils.executeIgnoreResult(() -> doGetSleepReportPoints(query));
    }

    private DevicePropertyTrendResp doGetPropertyTrend(DevicePropertyTrendQuery query) {
        validateTrendQuery(query);

        DeviceInfo device = deviceInfoService.getDeviceInfo(query.getDeviceId());
        if (device == null) {
            throw new ServiceException(400, "Device does not exist");
        }

        List<String> properties = validateAndNormalizeProperties(device.getProductKey(), query.getProperties());
        IntervalOption interval = chooseInterval(query.getStartTime(), query.getEndTime(),
                normalizeMaxPoints(query.getMaxPoints()));
        return devicePropertyData.findDevicePropertyTrend(device.getId(), properties,
                query.getStartTime(), query.getEndTime(), interval.value(), interval.millis());
    }

    private List<DeviceSleepReportPoint> doGetSleepReportPoints(DeviceSleepReportQuery query) {
        validateSleepReportQuery(query);

        DeviceInfo device = deviceInfoService.getDeviceInfo(query.getDeviceId());
        if (device == null) {
            throw new ServiceException(400, "Device does not exist");
        }
        return devicePropertyData.findDeviceSleepReportPoints(device.getId(), query.getStartTime(), query.getEndTime());
    }

    private void validateTrendQuery(DevicePropertyTrendQuery query) {
        validateTimeRange(query == null ? null : query.getDeviceId(),
                query == null ? null : query.getStartTime(),
                query == null ? null : query.getEndTime());
        if (query.getProperties() == null || query.getProperties().isEmpty()) {
            throw new ServiceException(400, "Properties cannot be empty");
        }
    }

    private void validateSleepReportQuery(DeviceSleepReportQuery query) {
        validateTimeRange(query == null ? null : query.getDeviceId(),
                query == null ? null : query.getStartTime(),
                query == null ? null : query.getEndTime());
    }

    private void validateTimeRange(Long deviceId, Long startTime, Long endTime) {
        if (deviceId == null) {
            throw new ServiceException(400, "Device cannot be empty");
        }
        if (startTime == null || endTime == null || startTime >= endTime) {
            throw new ServiceException(400, "Invalid time range");
        }
        if (endTime - startTime > MAX_QUERY_RANGE_MILLIS) {
            throw new ServiceException(400, "Time range cannot exceed 90 days");
        }
    }

    private List<String> validateAndNormalizeProperties(String productKey, List<String> requestedProperties) {
        ThingModel thingModel = thingModelService.getThingModelByProductKeyFromCache(productKey);
        if (thingModel == null || thingModel.getModel() == null || thingModel.getModel().getProperties() == null) {
            throw new ServiceException(400, "Thing model does not exist");
        }

        Map<String, ThingModel.Property> propertyMap = thingModel.getModel().getProperties().stream()
                .filter(Objects::nonNull)
                .filter(property -> property.getIdentifier() != null)
                .collect(Collectors.toMap(ThingModel.Property::getIdentifier, property -> property,
                        (left, right) -> left, LinkedHashMap::new));

        List<String> properties = requestedProperties.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(property -> !property.isEmpty())
                .distinct()
                .map(property -> validateProperty(propertyMap, property))
                .collect(Collectors.toList());
        if (properties.isEmpty()) {
            throw new ServiceException(400, "Properties cannot be empty");
        }
        return properties;
    }

    private String validateProperty(Map<String, ThingModel.Property> propertyMap, String propertyName) {
        ThingModel.Property property = propertyMap.get(propertyName);
        if (property == null) {
            throw new ServiceException(400, "Property does not exist: " + propertyName);
        }
        ThingModel.DataType dataType = property.getDataType();
        if (dataType == null || !NUMERIC_TYPES.contains(dataType.normalizedType())) {
            throw new ServiceException(400, "Only numeric properties are supported: " + propertyName);
        }
        return property.getIdentifier();
    }

    private int normalizeMaxPoints(Integer maxPoints) {
        if (maxPoints == null) {
            return DEFAULT_MAX_POINTS;
        }
        return Math.max(MIN_MAX_POINTS, Math.min(MAX_MAX_POINTS, maxPoints));
    }

    private IntervalOption chooseInterval(long startTime, long endTime, int maxPoints) {
        long duration = endTime - startTime;
        for (IntervalOption option : INTERVAL_OPTIONS) {
            long pointCount = (long) Math.ceil((double) duration / option.millis());
            if (pointCount <= maxPoints) {
                return option;
            }
        }
        return INTERVAL_OPTIONS.get(INTERVAL_OPTIONS.size() - 1);
    }

    private static class IntervalOption {

        private final String value;

        private final long millis;

        private IntervalOption(String value, long millis) {
            this.value = value;
            this.millis = millis;
        }

        private String value() {
            return value;
        }

        private long millis() {
            return millis;
        }
    }

}
