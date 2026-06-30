package com.enjoyiot.module.member.dal.dataobject.user;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enjoyiot.framework.common.enums.CommonStatusEnum;
import com.enjoyiot.framework.tenant.core.db.TenantBaseDO;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员用户 DO
 *
 * @author Megumi0826
 *
 */
@TableName("member_user")
@KeySequence("member_user_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MemberUserDO extends TenantBaseDO {

    /**
     * 会员编号
     */
    @TableId
    private Long id;

    /**
     * 手机号
     * 未绑定手机号时为 null
     */
    private String mobile;

    /**
     * 密码
     * 未设置密码时为空字符串
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 身高 cm
     */
    private Integer height;

    /**
     * 体重 kg
     */
    private BigDecimal weight;

    /**
     * 状态
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 注册 IP
     */
    private String registerIp;

    /**
     * 注册终端
     */
    private Integer registerTerminal;

    /**
     * 最后登录 IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private LocalDateTime loginDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除时间
     * 未删除时为 1970-01-01 00:00:00
     */
    private LocalDateTime deletedTime;

}
