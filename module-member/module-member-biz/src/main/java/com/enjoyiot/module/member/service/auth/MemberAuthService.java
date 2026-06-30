package com.enjoyiot.module.member.service.auth;

import com.enjoyiot.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import com.enjoyiot.module.member.controller.app.auth.vo.AppAuthWeixinMiniLoginReqVO;

import jakarta.validation.Valid;

public interface MemberAuthService {

    AppAuthLoginRespVO weixinMiniLogin(@Valid AppAuthWeixinMiniLoginReqVO reqVO);

    AppAuthLoginRespVO refreshToken(String refreshToken);

    void logout(String accessToken);

}
