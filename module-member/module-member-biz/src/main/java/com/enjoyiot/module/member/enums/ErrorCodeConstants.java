package com.enjoyiot.module.member.enums;

import com.enjoyiot.framework.common.exception.ErrorCode;

/**
 * Member 错误码枚举类
 *
 * member 系统，使用 1-004-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 会员用户 1-004-001-000 ==========
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1_004_001_000, "会员用户不存在");
    ErrorCode USER_AVATAR_IS_EMPTY = new ErrorCode(1_004_001_001, "头像文件不能为空");
    ErrorCode USER_AVATAR_SIZE_EXCEED = new ErrorCode(1_004_001_002, "头像文件不能超过 5MB");
    ErrorCode USER_AVATAR_TYPE_NOT_SUPPORT = new ErrorCode(1_004_001_003, "头像仅支持 jpg、png、webp 格式");
    ErrorCode USER_NICKNAME_IS_EMPTY = new ErrorCode(1_004_001_004, "昵称不能为空");

    // ========== 会员设备绑定 1-004-002-000 ==========
    ErrorCode DEVICE_NOT_EXISTS = new ErrorCode(1_004_002_000, "设备不存在或未注册");
    ErrorCode DEVICE_BIND_NOT_EXISTS = new ErrorCode(1_004_002_001, "设备绑定关系不存在");

}
