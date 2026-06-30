package com.enjoyiot.module.eiot.api.device;

import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendQuery;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendResp;
import com.enjoyiot.module.eiot.api.device.dto.DeviceSleepReportPoint;
import com.enjoyiot.module.eiot.api.device.dto.DeviceSleepReportQuery;

import java.util.List;

public interface DeviceHistoryApi {

    DevicePropertyTrendResp getPropertyTrend(DevicePropertyTrendQuery query);

    List<DeviceSleepReportPoint> getSleepReportPoints(DeviceSleepReportQuery query);

}
