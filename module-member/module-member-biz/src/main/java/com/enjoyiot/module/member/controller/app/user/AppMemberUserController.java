package com.enjoyiot.module.member.controller.app.user;

import com.enjoyiot.framework.common.pojo.CommonResult;
import com.enjoyiot.module.member.controller.app.user.vo.AppMemberUserRespVO;
import com.enjoyiot.module.member.controller.app.user.vo.AppMemberUserUpdateProfileReqVO;
import com.enjoyiot.module.member.convert.user.UserConvert;
import com.enjoyiot.module.member.dal.dataobject.user.MemberUserDO;
import com.enjoyiot.module.member.service.user.MemberUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.enjoyiot.framework.common.pojo.CommonResult.success;
import static com.enjoyiot.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 会员用户")
@RestController
@RequestMapping("/member/user")
@Validated
public class AppMemberUserController {

    @Resource
    private MemberUserService memberUserService;

    @GetMapping("/get")
    @Operation(summary = "获得当前登录用户信息")
    public CommonResult<AppMemberUserRespVO> getUser() {
        MemberUserDO user = memberUserService.getUser(getLoginUserId());
        return success(UserConvert.INSTANCE.convert(user));
    }

    @PostMapping(value = "/update-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传会员头像")
    public CommonResult<String> updateAvatar(@RequestParam("file") MultipartFile file) throws Exception {
        String avatar = memberUserService.updateUserAvatar(getLoginUserId(), file);
        return success(avatar);
    }

    @PutMapping("/update-profile")
    @Operation(summary = "更新会员资料")
    public CommonResult<Boolean> updateProfile(@Valid @RequestBody AppMemberUserUpdateProfileReqVO reqVO) {
        memberUserService.updateUserProfile(getLoginUserId(), reqVO);
        return success(true);
    }

}
