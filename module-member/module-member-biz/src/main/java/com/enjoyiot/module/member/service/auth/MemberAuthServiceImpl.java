package com.enjoyiot.module.member.service.auth;

import com.enjoyiot.framework.common.enums.TerminalEnum;
import com.enjoyiot.framework.common.enums.UserTypeEnum;
import com.enjoyiot.framework.common.util.servlet.ServletUtils;
import com.enjoyiot.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import com.enjoyiot.module.member.controller.app.auth.vo.AppAuthWeixinMiniLoginReqVO;
import com.enjoyiot.module.member.convert.auth.AuthConvert;
import com.enjoyiot.module.member.dal.dataobject.user.MemberUserDO;
import com.enjoyiot.module.member.service.user.MemberUserService;
import com.enjoyiot.module.system.api.oauth2.OAuth2TokenApi;
import com.enjoyiot.module.system.api.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import com.enjoyiot.module.system.api.oauth2.dto.OAuth2AccessTokenRespDTO;
import com.enjoyiot.module.system.api.social.SocialUserApi;
import com.enjoyiot.module.system.api.social.dto.SocialUserBindReqDTO;
import com.enjoyiot.module.system.api.social.dto.SocialUserRespDTO;
import com.enjoyiot.module.system.enums.oauth2.OAuth2ClientConstants;
import com.enjoyiot.module.system.enums.social.SocialTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class MemberAuthServiceImpl implements MemberAuthService {

    private static final String DEFAULT_SOCIAL_STATE = "default";

    @Resource
    private MemberUserService memberUserService;
    @Resource
    private SocialUserApi socialUserApi;
    @Resource
    private OAuth2TokenApi oauth2TokenApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppAuthLoginRespVO weixinMiniLogin(AppAuthWeixinMiniLoginReqVO reqVO) {
        SocialUserRespDTO socialUser = socialUserApi.getSocialUserByCode(
                UserTypeEnum.MEMBER.getValue(),
                SocialTypeEnum.WECHAT_MINI_APP.getType(),
                reqVO.getCode(),
                DEFAULT_SOCIAL_STATE);

        MemberUserDO memberUser;
        if (socialUser.getUserId() != null) {
            memberUser = memberUserService.getUser(socialUser.getUserId());
        } else {
            memberUser = memberUserService.createUser(socialUser.getNickname(), socialUser.getAvatar(),
                    ServletUtils.getClientIP(), TerminalEnum.WECHAT_MINI_PROGRAM.getTerminal());
            socialUserApi.bindSocialUser(new SocialUserBindReqDTO(
                    memberUser.getId(),
                    UserTypeEnum.MEMBER.getValue(),
                    SocialTypeEnum.WECHAT_MINI_APP.getType(),
                    reqVO.getCode(),
                    DEFAULT_SOCIAL_STATE));
        }

        OAuth2AccessTokenRespDTO token = createAccessToken(memberUser.getId());
        return AuthConvert.INSTANCE.convert(token, socialUser.getOpenid());
    }

    @Override
    public AppAuthLoginRespVO refreshToken(String refreshToken) {
        OAuth2AccessTokenRespDTO token = oauth2TokenApi.refreshAccessToken(refreshToken,
                OAuth2ClientConstants.CLIENT_ID_DEFAULT);
        return AuthConvert.INSTANCE.convert(token, null);
    }

    @Override
    public void logout(String accessToken) {
        oauth2TokenApi.removeAccessToken(accessToken);
    }

    private OAuth2AccessTokenRespDTO createAccessToken(Long userId) {
        OAuth2AccessTokenCreateReqDTO reqDTO = new OAuth2AccessTokenCreateReqDTO();
        reqDTO.setUserId(userId);
        reqDTO.setUserType(UserTypeEnum.MEMBER.getValue());
        reqDTO.setClientId(OAuth2ClientConstants.CLIENT_ID_DEFAULT);
        return oauth2TokenApi.createAccessToken(reqDTO);
    }

}
