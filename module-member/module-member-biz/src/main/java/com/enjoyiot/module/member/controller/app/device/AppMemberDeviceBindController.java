package com.enjoyiot.module.member.controller.app.device;

import com.enjoyiot.framework.common.pojo.CommonResult;
import com.enjoyiot.module.eiot.api.device.DeviceApi;
import com.enjoyiot.module.eiot.api.device.dto.DeviceInfo;
import com.enjoyiot.module.eiot.api.device.dto.DevicePropertyTrendResp;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDeviceBindReqVO;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDevicePropertyTrendReqVO;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDevicePropertyTrendRespVO;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDeviceRespVO;
import com.enjoyiot.module.member.controller.app.device.vo.AppMemberDeviceUnbindReqVO;
import com.enjoyiot.module.member.convert.device.MemberDeviceBindConvert;
import com.enjoyiot.module.member.dal.dataobject.device.MemberDeviceBindDO;
import com.enjoyiot.module.member.service.device.MemberDeviceBindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.enjoyiot.framework.common.pojo.CommonResult.success;
import static com.enjoyiot.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 设备绑定")
@RestController
@RequestMapping("/member/device")
@Validated
public class AppMemberDeviceBindController {

    @Resource
    private MemberDeviceBindService memberDeviceBindService;

    @Resource
    private DeviceApi deviceApi;

    @PostMapping("/bind")
    @Operation(summary = "绑定设备")
    public CommonResult<Long> bindDevice(@Valid @RequestBody AppMemberDeviceBindReqVO reqVO) {
        return success(memberDeviceBindService.bindDevice(getLoginUserId(), reqVO.getProductKey(), reqVO.getDn()));
    }

    @PostMapping("/unbind")
    @Operation(summary = "解绑设备")
    public CommonResult<Boolean> unbindDevice(@Valid @RequestBody AppMemberDeviceUnbindReqVO reqVO) {
        memberDeviceBindService.unbindDevice(getLoginUserId(), reqVO.getDeviceId());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得已绑定设备 ID 列表")
    public CommonResult<List<Long>> getDeviceList() {
        return success(memberDeviceBindService.getBindDeviceIdList(getLoginUserId()));
    }

    @GetMapping("/detail-list")
    @Operation(summary = "获得已绑定设备详情列表")
    public CommonResult<List<AppMemberDeviceRespVO>> getDeviceDetailList() {
        List<MemberDeviceBindDO> binds = memberDeviceBindService.getBindDeviceList(getLoginUserId());
        List<Long> deviceIds = binds.stream()
                .map(MemberDeviceBindDO::getDeviceId)
                .collect(Collectors.toList());
        List<DeviceInfo> devices = deviceApi.getDeviceInfoList(deviceIds);
        return success(MemberDeviceBindConvert.INSTANCE.convertList(binds, devices));
    }

    @PostMapping("/property-trend")
    @Operation(summary = "获得已绑定设备属性历史趋势")
    public CommonResult<AppMemberDevicePropertyTrendRespVO> getPropertyTrend(
            @Valid @RequestBody AppMemberDevicePropertyTrendReqVO reqVO) {
        DevicePropertyTrendResp trendResp = memberDeviceBindService.getPropertyTrend(getLoginUserId(),
                reqVO.getProductKey(), reqVO.getDn(), MemberDeviceBindConvert.INSTANCE.convert(reqVO));
        return success(MemberDeviceBindConvert.INSTANCE.convert(trendResp, reqVO.getProductKey(), reqVO.getDn()));
    }

}
