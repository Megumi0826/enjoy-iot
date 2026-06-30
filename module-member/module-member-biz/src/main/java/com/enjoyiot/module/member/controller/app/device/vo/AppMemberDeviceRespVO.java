package com.enjoyiot.module.member.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 APP - 已绑定设备 Response VO")
@Data
public class AppMemberDeviceRespVO {

    @Schema(description = "绑定记录 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1800000000000000000")
    private Long bindId;

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3442")
    private Long deviceId;

    @Schema(description = "产品 Key", requiredMode = Schema.RequiredMode.REQUIRED, example = "dEkr5BkkXTFZFBdR")
    private String productKey;

    @Schema(description = "设备唯一标识/设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "58E6C55AEDB0")
    private String dn;

    @Schema(description = "设备别名", example = "卧室雷达")
    private String name;

    @Schema(description = "设备型号", example = "radar")
    private String model;

    @Schema(description = "固件版本", example = "2.1.0")
    private String firmVersion;

    @Schema(description = "设备序列号", example = "SN001")
    private String serialNo;

    @Schema(description = "位置备注", example = "卧室")
    private String addr;

    @Schema(description = "云端状态：0 离线，1 在线，2 未激活，3 禁用", example = "1")
    private Integer state;

    @Schema(description = "是否在线", example = "true")
    private Boolean online;

    @Schema(description = "上线时间戳")
    private Long onlineTime;

    @Schema(description = "离线时间戳")
    private Long offlineTime;

    @Schema(description = "节点类型")
    private Integer nodeType;

    @Schema(description = "绑定时间")
    private LocalDateTime bindTime;

}
