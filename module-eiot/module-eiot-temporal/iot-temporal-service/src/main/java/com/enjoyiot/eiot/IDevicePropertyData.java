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
package com.enjoyiot.eiot;


import com.enjoyiot.module.eiot.api.device.dto.DeviceProperty;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyCache;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendResp;
import com.enjoyiot.module.eiot.api.device.dto.DeviceSleepReportPoint;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 设备属性时序数据接口
 */
public interface IDevicePropertyData {

    /**
     * 按时间范围取设备指定属性的历史数据
     *
     * @param deviceId 设备id
     * @param name     属性名称
     * @param start    开始时间戳
     * @param end      结束时间戳
     * @param size     取时间范围内的数量
     */
    List<DeviceProperty> findDevicePropertyHistory(Long deviceId, String name, long start, long end, int size);

    default DevicePropertyTrendResp findDevicePropertyTrend(Long deviceId, List<String> properties,
                                                            long start, long end, String interval, long intervalMillis) {
        DevicePropertyTrendResp resp = new DevicePropertyTrendResp();
        resp.setDeviceId(deviceId);
        resp.setStartTime(start);
        resp.setEndTime(end);
        resp.setInterval(interval);
        resp.setIntervalMillis(intervalMillis);
        resp.setPoints(Collections.emptyList());
        return resp;
    }

    default List<DeviceSleepReportPoint> findDeviceSleepReportPoints(Long deviceId, long start, long end) {
        return Collections.emptyList();
    }

    /**
     * 添加多个属性
     *
     * @param deviceId   设备ID
     * @param properties 属性
     * @param time       属性上报时间
     */
    void addProperties(Long deviceId, Map<String, DevicePropertyCache> properties, long time);

}
