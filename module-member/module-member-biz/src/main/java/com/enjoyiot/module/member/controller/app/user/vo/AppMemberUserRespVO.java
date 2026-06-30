package com.enjoyiot.module.member.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "用户 APP - 会员用户 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppMemberUserRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "用户名", example = "15601691399")
    private String username;

    @Schema(description = "手机号", example = "15601691399")
    private String mobile;


    @Schema(description = "昵称", example = "用户123456")
    private String nickname;

    @Schema(description = "头像", example = "https://example.com/avatar.png")
    private String avatar;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "生日", example = "1998-01-01")
    private LocalDate birthday;

    @Schema(description = "身高 cm", example = "175")
    private Integer height;

    @Schema(description = "体重 kg", example = "65.5")
    private BigDecimal weight;


}
