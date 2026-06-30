package com.enjoyiot.module.member.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "用户 APP - 更新会员资料 Request VO")
@Data
public class AppMemberUserUpdateProfileReqVO {

    @Schema(description = "昵称", example = "小明")
    @Size(max = 30, message = "昵称长度不能超过 30 个字符")
    private String nickname;

    @Schema(description = "姓名", example = "张三")
    @Size(max = 30, message = "姓名长度不能超过 30 个字符")
    private String name;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "生日", example = "1998-01-01")
    private LocalDate birthday;

    @Schema(description = "身高 cm", example = "175")
    @Min(value = 1, message = "身高必须大于 0")
    private Integer height;

    @Schema(description = "体重 kg", example = "65.5")
    @DecimalMin(value = "0.1", message = "体重必须大于 0")
    private BigDecimal weight;

}
