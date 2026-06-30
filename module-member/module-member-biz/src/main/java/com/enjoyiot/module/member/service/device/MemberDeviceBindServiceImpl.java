package com.enjoyiot.module.member.service.device;

import cn.hutool.core.util.ObjectUtil;
import com.enjoyiot.module.eiot.api.device.DeviceApi;
import com.enjoyiot.module.eiot.api.device.DeviceHistoryApi;
import com.enjoyiot.module.eiot.api.device.dto.DeviceInfo;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendQuery;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendResp;
import com.enjoyiot.module.member.dal.dataobject.device.MemberDeviceBindDO;
import com.enjoyiot.module.member.dal.mysql.device.MemberDeviceBindMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.enjoyiot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enjoyiot.module.member.enums.ErrorCodeConstants.DEVICE_BIND_NOT_EXISTS;
import static com.enjoyiot.module.member.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;

@Service
@Validated
public class MemberDeviceBindServiceImpl implements MemberDeviceBindService {

    @Resource
    private MemberDeviceBindMapper memberDeviceBindMapper;

    @Resource
    private DeviceApi deviceApi;

    @Resource
    private DeviceHistoryApi deviceHistoryApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bindDevice(Long memberUserId, String productKey, String dn) {
        DeviceInfo device = deviceApi.getDeviceByPkDnByCache(productKey, dn);
        if (ObjectUtil.isNull(device)) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        MemberDeviceBindDO bind = memberDeviceBindMapper.selectByMemberUserIdAndDeviceId(memberUserId, device.getId());
        if (ObjectUtil.isNotNull(bind)) {
            return bind.getId();
        }

        bind = new MemberDeviceBindDO()
                .setMemberUserId(memberUserId)
                .setDeviceId(device.getId())
                .setBindTime(LocalDateTime.now());
        memberDeviceBindMapper.insert(bind);
        return bind.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindDevice(Long memberUserId, Long deviceId) {
        MemberDeviceBindDO bind = memberDeviceBindMapper.selectByMemberUserIdAndDeviceId(memberUserId, deviceId);
        if (ObjectUtil.isNull(bind)) {
            throw exception(DEVICE_BIND_NOT_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        memberDeviceBindMapper.unbindByMemberUserIdAndDeviceId(memberUserId, deviceId, now);
    }

    @Override
    public List<Long> getBindDeviceIdList(Long memberUserId) {
        return memberDeviceBindMapper.selectListByMemberUserId(memberUserId)
                .stream()
                .map(MemberDeviceBindDO::getDeviceId)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberDeviceBindDO> getBindDeviceList(Long memberUserId) {
        return memberDeviceBindMapper.selectListByMemberUserId(memberUserId);
    }

    @Override
    public DevicePropertyTrendResp getPropertyTrend(Long memberUserId, String productKey, String dn, DevicePropertyTrendQuery query) {
        DeviceInfo device = deviceApi.getDeviceByPkDnByCache(productKey, dn);
        if (ObjectUtil.isNull(device)) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        MemberDeviceBindDO bind = memberDeviceBindMapper.selectByMemberUserIdAndDeviceId(memberUserId, device.getId());
        if (ObjectUtil.isNull(bind)) {
            throw exception(DEVICE_BIND_NOT_EXISTS);
        }

        query.setDeviceId(device.getId());
        return deviceHistoryApi.getPropertyTrend(query);
    }

}
