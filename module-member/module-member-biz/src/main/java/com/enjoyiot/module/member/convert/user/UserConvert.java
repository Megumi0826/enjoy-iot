package com.enjoyiot.module.member.convert.user;

import com.enjoyiot.module.member.controller.app.user.vo.AppMemberUserRespVO;
import com.enjoyiot.module.member.dal.dataobject.user.MemberUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "nickname", target = "username")
    AppMemberUserRespVO convert(MemberUserDO bean);

}
