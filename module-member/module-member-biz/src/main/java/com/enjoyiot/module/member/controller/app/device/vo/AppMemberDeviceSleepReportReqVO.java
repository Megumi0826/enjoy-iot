package com.enjoyiot.module.member.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "User App - device sleep report request")
@Data
public class AppMemberDeviceSleepReportReqVO {

    @Schema(description = "Product Key", requiredMode = Schema.RequiredMode.REQUIRED, example = "dEkr5BkkXTFZFBdR")
    @NotBlank(message = "productKey cannot be blank")
    private String productKey;

    @Schema(description = "Device Name", requiredMode = Schema.RequiredMode.REQUIRED, example = "58E6C55A3AA8")
    @NotBlank(message = "dn cannot be blank")
    private String dn;

}
