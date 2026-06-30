package com.enjoyiot.module.member.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "User App - device property trend response")
@Data
public class AppMemberDevicePropertyTrendRespVO {

    @Schema(description = "Device ID")
    private Long deviceId;

    @Schema(description = "Product Key")
    private String productKey;

    @Schema(description = "Device Name")
    private String dn;

    @Schema(description = "Start timestamp in milliseconds")
    private Long startTime;

    @Schema(description = "End timestamp in milliseconds")
    private Long endTime;

    @Schema(description = "TDengine interval, for example 30s/1m/5m")
    private String interval;

    @Schema(description = "Interval milliseconds")
    private Long intervalMillis;

    @Schema(description = "Trend points")
    private List<Point> points;

    @Data
    public static class Point {

        @Schema(description = "Window start timestamp in milliseconds")
        private Long time;

        @Schema(description = "Average values keyed by property identifier")
        private Map<String, Double> values;

        @Schema(description = "Valid sample counts keyed by property identifier")
        private Map<String, Long> counts;

    }

}
