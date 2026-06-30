package com.enjoyiot.module.member.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "用户 APP - 绑定设备 Request VO")
@Data
public class AppMemberDeviceBindReqVO {

    @Schema(description = "产品 Key", requiredMode = Schema.RequiredMode.REQUIRED, example = "abc123")
    @NotBlank(message = "产品 Key 不能为空")
    private String productKey;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "device001")
    @NotBlank(message = "设备名称不能为空")
    private String dn;

}
