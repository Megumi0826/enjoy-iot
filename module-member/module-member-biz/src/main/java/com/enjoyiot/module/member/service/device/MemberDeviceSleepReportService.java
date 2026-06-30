package com.enjoyiot.module.member.service.device;

import com.enjoyiot.module.member.service.device.dto.MemberDeviceSleepReportDTO;

import java.util.List;

public interface MemberDeviceSleepReportService {

    MemberDeviceSleepReportDTO getLatestReport(Long memberUserId, String productKey, String dn);

    List<MemberDeviceSleepReportDTO> getReportList(Long memberUserId, String productKey, String dn, Integer days);

}
