package com.enjoyiot.module.eiot.api.device.dto;

import lombok.Data;

import java.util.List;

@Data
public class DevicePropertyTrendResp {

    private Long deviceId;

    private Long startTime;

    private Long endTime;

    private String interval;

    private Long intervalMillis;

    private List<DevicePropertyTrendPoint> points;

}
