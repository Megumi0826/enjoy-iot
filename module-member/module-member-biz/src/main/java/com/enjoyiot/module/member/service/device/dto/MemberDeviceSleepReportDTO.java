package com.enjoyiot.module.member.service.device.dto;

import lombok.Data;

@Data
public class MemberDeviceSleepReportDTO {

    private String date;

    private String status;

    private Long prepareStartTime;

    private Long sleepStartTime;

    private Long endTime;

    private Integer totalSleepTime;

    private Integer deepSleepTime;

    private Integer lightSleepTime;

    private Integer remSleepTime;

    private Integer awakeTime;

    private Integer outOfBedTime;

    private Integer sleepLatency;

    private Integer wakeCount;

    private Integer sleepCycles;

    private Integer avgHeartRate;

    private Integer avgBreathingRate;

    private Integer apneaCount;

    private Double largeMoveRatio;

    private Double smallMoveRatio;

    private Double score;

    private Integer sourcePointCount;

}
