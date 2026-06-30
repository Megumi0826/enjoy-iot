package com.enjoyiot.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 APP - 登录 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthLoginRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "访问令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "happy")
    private String accessToken;

    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "nice")
    private String refreshToken;

    @Schema(description = "访问令牌有效期，单位：秒", requiredMode = Schema.RequiredMode.REQUIRED, example = "1800")
    private Long accessExpiresIn;

    @Schema(description = "刷新令牌有效期，单位：秒", requiredMode = Schema.RequiredMode.REQUIRED, example = "2592000")
    private Long refreshExpiresIn;

    @Schema(description = "社交用户 openid，仅微信登录时返回", example = "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o")
    private String openid;

}
