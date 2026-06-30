package com.enjoyiot.module.member.service.device;

import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendQuery;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendResp;
import com.enjoyiot.module.member.dal.dataobject.device.MemberDeviceBindDO;

import java.util.List;

public interface MemberDeviceBindService {

    /**
     * 绑定设备
     *
     * @param memberUserId 会员用户 ID
     * @param productKey 产品 Key
     * @param dn 设备名称
     * @return 绑定记录 ID
     */
    Long bindDevice(Long memberUserId, String productKey, String dn);

    /**
     * 解绑设备
     *
     * @param memberUserId 会员用户 ID
     * @param deviceId 设备 ID
     */
    void unbindDevice(Long memberUserId, Long deviceId);

    /**
     * 获得会员绑定设备 ID 列表
     *
     * @param memberUserId 会员用户 ID
     * @return 设备 ID 列表
     */
    List<Long> getBindDeviceIdList(Long memberUserId);

    /**
     * 获取会员已绑定设备关系列表
     *
     * @param memberUserId 会员用户 ID
     * @return 设备绑定关系列表
     */
    List<MemberDeviceBindDO> getBindDeviceList(Long memberUserId);

    /**
     * Query property trend for a bound device.
     *
     * @param memberUserId member user ID
     * @param productKey product key
     * @param dn device name
     * @param query trend query
     * @return property trend
     */
    DevicePropertyTrendResp getPropertyTrend(Long memberUserId, String productKey, String dn, DevicePropertyTrendQuery query);

}
