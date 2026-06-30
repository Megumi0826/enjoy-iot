package com.enjoyiot.module.member.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "User App - device sleep report list response")
@Data
public class AppMemberDeviceSleepReportListRespVO {

    @Schema(description = "Sleep reports")
    private List<AppMemberDeviceSleepReportRespVO> reports;

}
