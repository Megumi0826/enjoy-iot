/*
 *
 *  * | Licensed 未经许可不能去掉「Enjoy-iot」相关版权
 *  * +----------------------------------------------------------------------
 *  * | Author: xw2sy@163.com
 *  * +----------------------------------------------------------------------
 *
 *  Copyright [2025] [Enjoy-iot]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */
package com.enjoyiot.eiot.temporal.td.service;

import com.enjoyiot.eiot.IDevicePropertyData;
import com.enjoyiot.eiot.temporal.td.config.Constants;
import com.enjoyiot.eiot.temporal.td.dao.TdTemplate;
import com.enjoyiot.eiot.temporal.td.model.TbDeviceProperty;
import com.enjoyiot.framework.common.util.json.JsonUtils;
import com.enjoyiot.module.eiot.api.device.DeviceApi;
import com.enjoyiot.module.eiot.api.device.dto.DeviceInfo;
import com.enjoyiot.module.eiot.api.device.dto.DeviceProperty;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyCache;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendPoint;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendResp;
import com.enjoyiot.module.eiot.api.device.dto.DeviceSleepReportPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DevicePropertyDataImpl implements IDevicePropertyData {

    @Autowired
    private TdTemplate tdTemplate;

    @Resource
    private DeviceApi deviceApi;

    @Override
    public List<DeviceProperty> findDevicePropertyHistory(Long deviceId, String name, long start, long end, int size) {
        DeviceInfo device = deviceApi.getDeviceInfoFromCache(deviceId);
        if (device == null) {
            return new ArrayList<>();
        }

        String tableName = Constants.getProductPropertySTableName(device.getProductKey());
        String fieldName = name.contains(".") ? "`" + name + "`" : name;
        List<TbDeviceProperty> deviceProperties = tdTemplate.query(String.format(
                        "select time,%s as `value`,device_id from %s where device_id=? and time>=? and time<=? order by time asc limit 0,%d",
                        fieldName.toLowerCase(), tableName, size),
                new BeanPropertyRowMapper<>(TbDeviceProperty.class),
                deviceId, start, end);
        return deviceProperties.stream().map(property -> new DeviceProperty(
                        property.getTime().toString(),
                        property.getDeviceId(),
                        name,
                        property.getValue(),
                        property.getTime()))
                .collect(Collectors.toList());
    }

    @Override
    public DevicePropertyTrendResp findDevicePropertyTrend(Long deviceId, List<String> properties,
                                                           long start, long end, String interval, long intervalMillis) {
        DevicePropertyTrendResp resp = createTrendResp(deviceId, start, end, interval, intervalMillis);
        List<String> trendProperties = normalizeProperties(properties);
        Map<Long, DevicePropertyTrendPoint> pointMap = createEmptyPoints(trendProperties, start, end, intervalMillis);
        if (trendProperties.isEmpty() || intervalMillis <= 0 || start >= end) {
            resp.setPoints(toSortedPoints(pointMap));
            return resp;
        }

        DeviceInfo device = deviceApi.getDeviceInfoFromCache(deviceId);
        if (device == null) {
            resp.setPoints(toSortedPoints(pointMap));
            return resp;
        }

        String tableName = Constants.getProductPropertySTableName(device.getProductKey());
        String selectFields = trendProperties.stream()
                .map(this::buildAggregateSelect)
                .collect(Collectors.joining(","));
        String sql = String.format(
                "select _wstart as time,%s from %s where device_id=? and time>=? and time<? interval(%s) fill(NULL) order by time asc",
                selectFields, tableName, interval);

        List<Map<String, Object>> rows = tdTemplate.queryForList(sql, deviceId, start, end);
        for (Map<String, Object> row : rows) {
            Long time = toEpochMillis(getIgnoreCase(row, "time"));
            if (time == null) {
                continue;
            }
            DevicePropertyTrendPoint point = pointMap.computeIfAbsent(time,
                    key -> createEmptyPoint(trendProperties, key));
            for (String property : trendProperties) {
                point.getValues().put(property, toDouble(getIgnoreCase(row, property)));
                point.getCounts().put(property, toLong(getIgnoreCase(row, property + "_count"), 0L));
            }
        }

        resp.setPoints(toSortedPoints(pointMap));
        return resp;
    }

    @Override
    public List<DeviceSleepReportPoint> findDeviceSleepReportPoints(Long deviceId, long start, long end) {
        DeviceInfo device = deviceApi.getDeviceInfoFromCache(deviceId);
        if (device == null || start >= end) {
            return new ArrayList<>();
        }

        String tableName = Constants.getProductPropertySTableName(device.getProductKey());
        String sql = String.format(
                "select _wstart as time,"
                        + " last(algorithmstate) as algorithmstate,"
                        + " avg(sleepprogress) as sleepprogress,"
                        + " avg(avgbreathingrate) as avgbreathingrate,"
                        + " avg(avgheartrate) as avgheartrate,"
                        + " max(apneacount) as apneacount,"
                        + " avg(largemoveratio) as largemoveratio,"
                        + " avg(smallmoveratio) as smallmoveratio,"
                        + " max(algototalsleeptime) as algototalsleeptime,"
                        + " max(algodeepsleeptime) as algodeepsleeptime,"
                        + " max(algolightsleeptime) as algolightsleeptime,"
                        + " max(algoremsleeptime) as algoremsleeptime,"
                        + " max(algoawaketime) as algoawaketime,"
                        + " max(algooutofbedtime) as algooutofbedtime,"
                        + " max(algosleeplatency) as algosleeplatency,"
                        + " max(algowakecount) as algowakecount,"
                        + " max(algosleepcycles) as algosleepcycles,"
                        + " max(algototalscore) as algototalscore"
                        + " from %s"
                        + " where device_id=? and time>=? and time<?"
                        + " interval(1m)"
                        + " order by time asc",
                tableName);

        List<Map<String, Object>> rows = tdTemplate.queryForList(sql, deviceId, start, end);
        return rows.stream()
                .map(this::toSleepReportPoint)
                .collect(Collectors.toList());
    }

    @Override
    public void addProperties(Long deviceId, Map<String, DevicePropertyCache> properties, long time) {
        DeviceInfo device = deviceApi.getDeviceInfoFromCache(deviceId);
        if (device == null) {
            return;
        }

        Map<String, DevicePropertyCache> oldProperties = deviceApi.getPropertiesFromCache(deviceId);
        oldProperties.putAll(properties);

        StringBuilder fieldNames = new StringBuilder();
        StringBuilder fieldPlaceholders = new StringBuilder();
        List<Object> args = new ArrayList<>();
        args.add(time);

        oldProperties.forEach((key, value) -> {
            String fieldName = key.contains(".") ? "`" + key + "`" : key;
            fieldNames.append(fieldName).append(",");
            fieldPlaceholders.append("?,");
            args.add(stringifyValue(value.getValue()));
        });
        fieldNames.deleteCharAt(fieldNames.length() - 1);
        fieldPlaceholders.deleteCharAt(fieldPlaceholders.length() - 1);

        String sql = String.format("INSERT INTO %s (time,%s) USING %s TAGS ('%s') VALUES (?,%s);",
                Constants.getDevicePropertyTableName(deviceId),
                fieldNames,
                Constants.getProductPropertySTableName(device.getProductKey()),
                deviceId,
                fieldPlaceholders);

        tdTemplate.update(sql, args.toArray());
    }

    private Object stringifyValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            // TDengine TINYINT expects numeric values for bool columns.
            return (Boolean) value ? 1 : 0;
        }
        if (value instanceof String) {
            String stringValue = (String) value;
            if ("true".equalsIgnoreCase(stringValue)) {
                return 1;
            }
            if ("false".equalsIgnoreCase(stringValue)) {
                return 0;
            }
        }
        if (value instanceof Map || value instanceof List || value.getClass().isArray()) {
            return JsonUtils.toJsonString(value);
        }
        return value;
    }

    private DeviceSleepReportPoint toSleepReportPoint(Map<String, Object> row) {
        DeviceSleepReportPoint point = new DeviceSleepReportPoint();
        point.setTime(toEpochMillis(getIgnoreCase(row, "time")));
        point.setAlgorithmState(toDouble(getIgnoreCase(row, "algorithmstate")));
        point.setSleepProgress(toDouble(getIgnoreCase(row, "sleepprogress")));
        point.setAvgBreathingRate(toDouble(getIgnoreCase(row, "avgbreathingrate")));
        point.setAvgHeartRate(toDouble(getIgnoreCase(row, "avgheartrate")));
        point.setApneaCount(toDouble(getIgnoreCase(row, "apneacount")));
        point.setLargeMoveRatio(toDouble(getIgnoreCase(row, "largemoveratio")));
        point.setSmallMoveRatio(toDouble(getIgnoreCase(row, "smallmoveratio")));
        point.setAlgoTotalSleepTime(toDouble(getIgnoreCase(row, "algototalsleeptime")));
        point.setAlgoDeepSleepTime(toDouble(getIgnoreCase(row, "algodeepsleeptime")));
        point.setAlgoLightSleepTime(toDouble(getIgnoreCase(row, "algolightsleeptime")));
        point.setAlgoRemSleepTime(toDouble(getIgnoreCase(row, "algoremsleeptime")));
        point.setAlgoAwakeTime(toDouble(getIgnoreCase(row, "algoawaketime")));
        point.setAlgoOutOfBedTime(toDouble(getIgnoreCase(row, "algooutofbedtime")));
        point.setAlgoSleepLatency(toDouble(getIgnoreCase(row, "algosleeplatency")));
        point.setAlgoWakeCount(toDouble(getIgnoreCase(row, "algowakecount")));
        point.setAlgoSleepCycles(toDouble(getIgnoreCase(row, "algosleepcycles")));
        point.setAlgoTotalScore(toDouble(getIgnoreCase(row, "algototalscore")));
        return point;
    }

    private DevicePropertyTrendResp createTrendResp(Long deviceId, long start, long end, String interval, long intervalMillis) {
        DevicePropertyTrendResp resp = new DevicePropertyTrendResp();
        resp.setDeviceId(deviceId);
        resp.setStartTime(start);
        resp.setEndTime(end);
        resp.setInterval(interval);
        resp.setIntervalMillis(intervalMillis);
        resp.setPoints(new ArrayList<>());
        return resp;
    }

    private List<String> normalizeProperties(List<String> properties) {
        if (properties == null) {
            return new ArrayList<>();
        }
        return properties.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(property -> !property.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private String buildAggregateSelect(String property) {
        String fieldName = toTdFieldName(property);
        String valueAlias = toTdAlias(property);
        String countAlias = toTdAlias(property + "_count");
        return String.format("avg(%s) as %s,count(%s) as %s", fieldName, valueAlias, fieldName, countAlias);
    }

    private String toTdFieldName(String property) {
        String fieldName = property.toLowerCase();
        return fieldName.contains(".") ? "`" + fieldName + "`" : fieldName;
    }

    private String toTdAlias(String alias) {
        return "`" + alias + "`";
    }

    private Map<Long, DevicePropertyTrendPoint> createEmptyPoints(List<String> properties,
                                                                  long start, long end, long intervalMillis) {
        Map<Long, DevicePropertyTrendPoint> points = new LinkedHashMap<>();
        if (intervalMillis <= 0 || start >= end) {
            return points;
        }
        long alignedStart = alignDown(start, intervalMillis);
        long alignedEnd = alignUp(end, intervalMillis);
        for (long time = alignedStart; time < alignedEnd; time += intervalMillis) {
            points.put(time, createEmptyPoint(properties, time));
        }
        return points;
    }

    private DevicePropertyTrendPoint createEmptyPoint(List<String> properties, long time) {
        DevicePropertyTrendPoint point = new DevicePropertyTrendPoint();
        point.setTime(time);
        Map<String, Double> values = new LinkedHashMap<>();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String property : properties) {
            values.put(property, null);
            counts.put(property, 0L);
        }
        point.setValues(values);
        point.setCounts(counts);
        return point;
    }

    private List<DevicePropertyTrendPoint> toSortedPoints(Map<Long, DevicePropertyTrendPoint> pointMap) {
        return pointMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private long alignDown(long time, long intervalMillis) {
        return time - Math.floorMod(time, intervalMillis);
    }

    private long alignUp(long time, long intervalMillis) {
        long aligned = alignDown(time, intervalMillis);
        return aligned == time ? time : aligned + intervalMillis;
    }

    private Object getIgnoreCase(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Long toEpochMillis(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).getTime();
        }
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        return toLong(value, null);
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long toLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
