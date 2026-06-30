package com.enjoyiot.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 APP - 微信小程序登录 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthWeixinMiniLoginReqVO {

    @Schema(description = "登录 code，小程序通过 uni.login / wx.login 获取", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "code不能为空")
    private String code;

}
