package com.enjoyiot.module.member.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "User App - latest device sleep report response")
@Data
public class AppMemberDeviceSleepReportLatestRespVO {

    @Schema(description = "Whether a sleep report was found")
    private Boolean hasReport;

    @Schema(description = "Latest sleep report")
    private AppMemberDeviceSleepReportRespVO report;

}
