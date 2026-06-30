package com.enjoyiot.module.member.convert.device;

import com.enjoyiot.module.eiot.api.device.dto.DeviceInfo;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendPoint;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendQuery;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendResp;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDevicePropertyTrendReqVO;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDevicePropertyTrendRespVO;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDeviceRespVO;
import com.enjoyiot.module.member.dal.dataobject.device.MemberDeviceBindDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper
public interface MemberDeviceBindConvert {

    MemberDeviceBindConvert INSTANCE = Mappers.getMapper(MemberDeviceBindConvert.class);

    default List<AppMemberDeviceRespVO> convertList(List<MemberDeviceBindDO> binds, List<DeviceInfo> devices) {
        if (binds == null || binds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, DeviceInfo> deviceMap = devices == null
                ? Collections.emptyMap()
                : devices.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(DeviceInfo::getId, Function.identity(), (first, second) -> first));

        return binds.stream()
                .map(bind -> convert(bind, deviceMap.get(bind.getDeviceId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    default AppMemberDeviceRespVO convert(MemberDeviceBindDO bind, DeviceInfo device) {
        if (bind == null || device == null) {
            return null;
        }

        AppMemberDeviceRespVO respVO = new AppMemberDeviceRespVO();
        respVO.setBindId(bind.getId());
        respVO.setDeviceId(bind.getDeviceId());
        respVO.setProductKey(device.getProductKey());
        respVO.setDn(device.getDn());
        respVO.setName(device.getName());
        respVO.setModel(device.getModel());
        respVO.setFirmVersion(device.getFirmVersion());
        respVO.setSerialNo(device.getSerialNo());
        respVO.setAddr(device.getAddr());
        respVO.setState(device.getState());
        respVO.setOnline(device.isOnline());
        respVO.setOnlineTime(device.getOnlineTime());
        respVO.setOfflineTime(device.getOfflineTime());
        respVO.setNodeType(device.getNodeType());
        respVO.setBindTime(bind.getBindTime());
        return respVO;
    }

    default DevicePropertyTrendQuery convert(AppMemberDevicePropertyTrendReqVO reqVO) {
        if (reqVO == null) {
            return null;
        }
        DevicePropertyTrendQuery query = new DevicePropertyTrendQuery();
        query.setProperties(reqVO.getProperties());
        query.setStartTime(reqVO.getStartTime());
        query.setEndTime(reqVO.getEndTime());
        query.setMaxPoints(reqVO.getMaxPoints());
        return query;
    }

    default AppMemberDevicePropertyTrendRespVO convert(DevicePropertyTrendResp trendResp, String productKey, String dn) {
        if (trendResp == null) {
            return null;
        }
        AppMemberDevicePropertyTrendRespVO respVO = new AppMemberDevicePropertyTrendRespVO();
        respVO.setDeviceId(trendResp.getDeviceId());
        respVO.setProductKey(productKey);
        respVO.setDn(dn);
        respVO.setStartTime(trendResp.getStartTime());
        respVO.setEndTime(trendResp.getEndTime());
        respVO.setInterval(trendResp.getInterval());
        respVO.setIntervalMillis(trendResp.getIntervalMillis());
        respVO.setPoints(convertTrendPoints(trendResp.getPoints()));
        return respVO;
    }

    default List<AppMemberDevicePropertyTrendRespVO.Point> convertTrendPoints(List<DevicePropertyTrendPoint> points) {
        if (points == null || points.isEmpty()) {
            return Collections.emptyList();
        }
        return points.stream()
                .map(this::convertTrendPoint)
                .collect(Collectors.toList());
    }

    default AppMemberDevicePropertyTrendRespVO.Point convertTrendPoint(DevicePropertyTrendPoint point) {
        if (point == null) {
            return null;
        }
        AppMemberDevicePropertyTrendRespVO.Point respPoint = new AppMemberDevicePropertyTrendRespVO.Point();
        respPoint.setTime(point.getTime());
        respPoint.setValues(point.getValues());
        respPoint.setCounts(point.getCounts());
        return respPoint;
    }

}
