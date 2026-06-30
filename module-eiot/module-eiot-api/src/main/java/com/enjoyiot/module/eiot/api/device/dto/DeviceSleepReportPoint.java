package com.enjoyiot.module.eiot.api.device.dto;

import lombok.Data;

@Data
public class DeviceSleepReportPoint {

    private Long time;

    private Double algorithmState;

    private Double sleepProgress;

    private Double avgBreathingRate;

    private Double avgHeartRate;

    private Double apneaCount;

    private Double largeMoveRatio;

    private Double smallMoveRatio;

    private Double algoTotalSleepTime;

    private Double algoDeepSleepTime;

    private Double algoLightSleepTime;

    private Double algoRemSleepTime;

    private Double algoAwakeTime;

    private Double algoOutOfBedTime;

    private Double algoSleepLatency;

    private Double algoWakeCount;

    private Double algoSleepCycles;

    private Double algoTotalScore;

}
