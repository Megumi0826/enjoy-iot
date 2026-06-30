package com.enjoyiot.module.member.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 APP - 解绑设备 Request VO")
@Data
public class AppMemberDeviceUnbindReqVO {

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1800000000000000000")
    @NotNull(message = "设备 ID 不能为空")
    private Long deviceId;

}
