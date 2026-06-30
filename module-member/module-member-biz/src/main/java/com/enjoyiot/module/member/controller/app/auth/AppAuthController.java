package com.enjoyiot.module.member.controller.app.auth;

import cn.hutool.core.util.StrUtil;
import com.enjoyiot.framework.common.pojo.CommonResult;
import com.enjoyiot.framework.security.config.SecurityProperties;
import com.enjoyiot.framework.security.core.util.SecurityFrameworkUtils;
import com.enjoyiot.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import com.enjoyiot.module.member.controller.app.auth.vo.AppAuthRefreshTokenReqVO;
import com.enjoyiot.module.member.controller.app.auth.vo.AppAuthWeixinMiniLoginReqVO;
import com.enjoyiot.module.member.service.auth.MemberAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.enjoyiot.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 认证")
@RestController
@RequestMapping("/member/auth")
@Validated
public class AppAuthController {

    @Resource
    private MemberAuthService authService;

    @Resource
    private SecurityProperties securityProperties;

    @PostMapping("/weixin-mini-login")
    @Operation(summary = "微信小程序一键登录")
    @PermitAll
    public CommonResult<AppAuthLoginRespVO> weixinMiniLogin(
            @RequestBody @Valid AppAuthWeixinMiniLoginReqVO reqVO) {
        return success(authService.weixinMiniLogin(reqVO));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌")
    @PermitAll
    public CommonResult<AppAuthLoginRespVO> refreshToken(@RequestBody @Valid AppAuthRefreshTokenReqVO reqVO) {
        return success(authService.refreshToken(reqVO.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    @PermitAll
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(
                request,
                securityProperties.getTokenHeader(),
                securityProperties.getTokenParameter()
        );
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token);
        }
        return success(true);
    }

}
