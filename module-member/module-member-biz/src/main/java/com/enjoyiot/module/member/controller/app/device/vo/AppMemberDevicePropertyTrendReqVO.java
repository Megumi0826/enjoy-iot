package com.enjoyiot.module.member.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "User App - device property trend request")
@Data
public class AppMemberDevicePropertyTrendReqVO {

    @Schema(description = "Product Key", requiredMode = Schema.RequiredMode.REQUIRED, example = "dEkr5BkkXTFZFBdR")
    @NotBlank(message = "productKey cannot be blank")
    private String productKey;

    @Schema(description = "Device Name", requiredMode = Schema.RequiredMode.REQUIRED, example = "58E6C55A3AA8")
    @NotBlank(message = "dn cannot be blank")
    private String dn;

    @Schema(description = "Property identifiers", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"heartRate\",\"breathingRate\"]")
    @NotEmpty(message = "properties cannot be empty")
    private List<String> properties;

    @Schema(description = "Start timestamp in milliseconds", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "startTime cannot be null")
    private Long startTime;

    @Schema(description = "End timestamp in milliseconds", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "endTime cannot be null")
    private Long endTime;

    @Schema(description = "Maximum chart points. Defaults to 300, max 500.", example = "300")
    @Min(value = 30, message = "maxPoints must be greater than or equal to 30")
    @Max(value = 500, message = "maxPoints must be less than or equal to 500")
    private Integer maxPoints;

}
