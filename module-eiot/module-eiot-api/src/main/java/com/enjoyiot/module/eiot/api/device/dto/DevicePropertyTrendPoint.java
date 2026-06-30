package com.enjoyiot.module.eiot.api.device.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DevicePropertyTrendPoint {

    private Long time;

    private Map<String, Double> values;

    private Map<String, Long> counts;

}
