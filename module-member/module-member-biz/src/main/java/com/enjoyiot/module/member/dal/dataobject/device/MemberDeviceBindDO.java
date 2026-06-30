package com.enjoyiot.module.member.dal.dataobject.device;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enjoyiot.framework.tenant.core.db.TenantBaseDO;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@TableName("member_device_bind")
@KeySequence("member_device_bind_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MemberDeviceBindDO extends TenantBaseDO {

    @TableId
    private Long id;

    private Long memberUserId;

    private Long deviceId;

    private LocalDateTime bindTime;

    private LocalDateTime unbindTime;

    /**
     * 删除时间
     * 未删除时为 1970-01-01 00:00:00
     */
    private LocalDateTime deletedTime;

}
