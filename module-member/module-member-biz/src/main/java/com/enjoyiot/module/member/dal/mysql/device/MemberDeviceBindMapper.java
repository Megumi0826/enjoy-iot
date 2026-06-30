package com.enjoyiot.module.member.dal.mysql.device;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.enjoyiot.framework.mybatis.core.mapper.BaseMapperX;
import com.enjoyiot.module.member.dal.dataobject.device.MemberDeviceBindDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MemberDeviceBindMapper extends BaseMapperX<MemberDeviceBindDO> {

    default MemberDeviceBindDO selectByMemberUserIdAndDeviceId(Long memberUserId, Long deviceId) {
        return selectOne(MemberDeviceBindDO::getMemberUserId, memberUserId,
                MemberDeviceBindDO::getDeviceId, deviceId);
    }

    default List<MemberDeviceBindDO> selectListByMemberUserId(Long memberUserId) {
        return selectList(MemberDeviceBindDO::getMemberUserId, memberUserId);
    }

    default int unbindByMemberUserIdAndDeviceId(Long memberUserId, Long deviceId, LocalDateTime unbindTime) {
        return update(null, new LambdaUpdateWrapper<MemberDeviceBindDO>()
                .eq(MemberDeviceBindDO::getMemberUserId, memberUserId)
                .eq(MemberDeviceBindDO::getDeviceId, deviceId)
                .set(MemberDeviceBindDO::getDeleted, true)
                .set(MemberDeviceBindDO::getDeletedTime, unbindTime)
                .set(MemberDeviceBindDO::getUnbindTime, unbindTime));
    }
}
