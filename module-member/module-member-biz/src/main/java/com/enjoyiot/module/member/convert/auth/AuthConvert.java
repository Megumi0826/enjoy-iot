package com.enjoyiot.module.member.convert.auth;

import com.enjoyiot.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import com.enjoyiot.module.system.api.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Duration;
import java.time.LocalDateTime;

@Mapper
public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    default AppAuthLoginRespVO convert(OAuth2AccessTokenRespDTO token, String openid) {
        LocalDateTime now = LocalDateTime.now();
        return AppAuthLoginRespVO.builder()
                .userId(token.getUserId())
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .accessExpiresIn(getExpiresIn(now, token.getExpiresTime()))
                .refreshExpiresIn(getExpiresIn(now, token.getRefreshExpiresTime()))
                .openid(openid)
                .build();
    }

    default Long getExpiresIn(LocalDateTime now, LocalDateTime expiresTime) {
        if (expiresTime == null) {
            return 0L;
        }
        return Math.max(Duration.between(now, expiresTime).getSeconds(), 0L);
    }

}
