package com.enjoyiot.module.member.convert.device;

import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDeviceSleepReportLatestRespVO;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDeviceSleepReportListRespVO;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDeviceSleepReportRespVO;
import com.enjoyiot.module.member.service.device.dto.MemberDeviceSleepReportDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface MemberDeviceSleepReportConvert {

    MemberDeviceSleepReportConvert INSTANCE = Mappers.getMapper(MemberDeviceSleepReportConvert.class);

    AppMemberDeviceSleepReportRespVO convert(MemberDeviceSleepReportDTO report);

    default AppMemberDeviceSleepReportLatestRespVO convertLatest(MemberDeviceSleepReportDTO report) {
        AppMemberDeviceSleepReportLatestRespVO respVO = new AppMemberDeviceSleepReportLatestRespVO();
        respVO.setHasReport(report != null);
        respVO.setReport(convert(report));
        return respVO;
    }

    default AppMemberDeviceSleepReportListRespVO convertList(List<MemberDeviceSleepReportDTO> reports) {
        AppMemberDeviceSleepReportListRespVO respVO = new AppMemberDeviceSleepReportListRespVO();
        if (reports == null || reports.isEmpty()) {
            respVO.setReports(Collections.emptyList());
            return respVO;
        }
        respVO.setReports(reports.stream()
                .map(this::convert)
                .collect(Collectors.toList()));
        return respVO;
    }

}
