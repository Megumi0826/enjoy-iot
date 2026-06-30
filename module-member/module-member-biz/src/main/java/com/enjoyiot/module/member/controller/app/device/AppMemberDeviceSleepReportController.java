package com.enjoyiot.module.member.controller.app.device;

import com.enjoyiot.framework.common.pojo.CommonResult;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDeviceSleepReportLatestRespVO;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDeviceSleepReportListReqVO;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDeviceSleepReportListRespVO;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDeviceSleepReportReqVO;
import com.enjoyiot.module.member.convert.device.MemberDeviceSleepReportConvert;
import com.enjoyiot.module.member.service.device.MemberDeviceSleepReportService;
import com.enjoyiot.module.member.service.device.dto.MemberDeviceSleepReportDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.enjoyiot.framework.common.pojo.CommonResult.success;
import static com.enjoyiot.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "User App - Device Sleep Report")
@RestController
@RequestMapping("/member/device/sleep-report")
@Validated
public class AppMemberDeviceSleepReportController {

    @Resource
    private MemberDeviceSleepReportService memberDeviceSleepReportService;

    @GetMapping("/latest")
    @Operation(summary = "Get latest sleep report")
    public CommonResult<AppMemberDeviceSleepReportLatestRespVO> getLatestSleepReport(
            @Valid AppMemberDeviceSleepReportReqVO reqVO) {
        MemberDeviceSleepReportDTO report = memberDeviceSleepReportService.getLatestReport(getLoginUserId(),
                reqVO.getProductKey(), reqVO.getDn());
        return success(MemberDeviceSleepReportConvert.INSTANCE.convertLatest(report));
    }

    @GetMapping("/list")
    @Operation(summary = "Get sleep report list in recent 30 days")
    public CommonResult<AppMemberDeviceSleepReportListRespVO> getSleepReportList(
            @Valid AppMemberDeviceSleepReportListReqVO reqVO) {
        List<MemberDeviceSleepReportDTO> reports = memberDeviceSleepReportService.getReportList(getLoginUserId(),
                reqVO.getProductKey(), reqVO.getDn(), reqVO.getDays());
        return success(MemberDeviceSleepReportConvert.INSTANCE.convertList(reports));
    }

}
