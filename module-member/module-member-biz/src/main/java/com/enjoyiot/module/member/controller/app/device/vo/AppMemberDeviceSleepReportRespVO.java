package com.enjoyiot.module.member.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "User App - device sleep report")
@Data
public class AppMemberDeviceSleepReportRespVO {

    @Schema(description = "Report date, based on sleep start date")
    private String date;

    @Schema(description = "Report status: IN_PROGRESS/INCOMPLETE/COMPLETED")
    private String status;

    @Schema(description = "Estimated preparation/session start timestamp in milliseconds")
    private Long prepareStartTime;

    @Schema(description = "Sleep start timestamp in milliseconds")
    private Long sleepStartTime;

    @Schema(description = "Report end timestamp in milliseconds")
    private Long endTime;

    @Schema(description = "Total sleep time in minutes")
    private Integer totalSleepTime;

    @Schema(description = "Deep sleep time in minutes")
    private Integer deepSleepTime;

    @Schema(description = "Light sleep time in minutes")
    private Integer lightSleepTime;

    @Schema(description = "REM sleep time in minutes")
    private Integer remSleepTime;

    @Schema(description = "Awake time in minutes")
    private Integer awakeTime;

    @Schema(description = "Out-of-bed time in minutes")
    private Integer outOfBedTime;

    @Schema(description = "Sleep latency in minutes")
    private Integer sleepLatency;

    @Schema(description = "Wake count")
    private Integer wakeCount;

    @Schema(description = "Sleep cycle count")
    private Integer sleepCycles;

    @Schema(description = "Average heart rate")
    private Integer avgHeartRate;

    @Schema(description = "Average breathing rate")
    private Integer avgBreathingRate;

    @Schema(description = "Apnea count")
    private Integer apneaCount;

    @Schema(description = "Large movement ratio")
    private Double largeMoveRatio;

    @Schema(description = "Small movement ratio")
    private Double smallMoveRatio;

    @Schema(description = "Total sleep score")
    private Double score;

    @Schema(description = "Source point count")
    private Integer sourcePointCount;

}
