package com.enjoyiot.module.eiot.api.device.dto;

import lombok.Data;

import java.util.List;

@Data
public class DevicePropertyTrendQuery {

    private Long deviceId;

    private List<String> properties;

    private Long startTime;

    private Long endTime;

    private Integer maxPoints;

}
