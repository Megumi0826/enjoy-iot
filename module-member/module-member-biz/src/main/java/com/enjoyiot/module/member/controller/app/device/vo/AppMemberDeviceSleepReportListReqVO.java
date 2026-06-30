package com.enjoyiot.module.member.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "User App - device sleep report list request")
@Data
public class AppMemberDeviceSleepReportListReqVO {

    @Schema(description = "Product Key", requiredMode = Schema.RequiredMode.REQUIRED, example = "dEkr5BkkXTFZFBdR")
    @NotBlank(message = "productKey cannot be blank")
    private String productKey;

    @Schema(description = "Device Name", requiredMode = Schema.RequiredMode.REQUIRED, example = "58E6C55A3AA8")
    @NotBlank(message = "dn cannot be blank")
    private String dn;

    @Schema(description = "Query days, max 30", example = "30")
    @Min(value = 1, message = "days must be greater than or equal to 1")
    @Max(value = 30, message = "days must be less than or equal to 30")
    private Integer days = 30;

}
