package com.enjoyiot.module.eiot.api.device.dto;

import lombok.Data;

@Data
public class DeviceSleepReportQuery {

    private Long deviceId;

    private Long startTime;

    private Long endTime;

}
