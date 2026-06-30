package com.enjoyiot.module.member.service.user;

import com.enjoyiot.module.member.controller.app.user.vo.AppMemberUserUpdateProfileReqVO;
import com.enjoyiot.module.member.dal.dataobject.user.MemberUserDO;
import org.springframework.web.multipart.MultipartFile;

public interface MemberUserService {

    MemberUserDO getUser(Long id);

    MemberUserDO getUserByMobile(String mobile);

    MemberUserDO createUser(String nickname, String avatar, String registerIp, Integer terminal);

    MemberUserDO createUserIfAbsent(String mobile, String registerIp, Integer terminal);

    void updateUserLogin(Long id, String loginIp);

    /**
     * 更新会员头像
     *
     * @param userId 用户编号
     * @param file 头像文件
     * @return 头像 URL
     */
    String updateUserAvatar(Long userId, MultipartFile file) throws Exception;

    /**
     * 更新会员资料
     *
     * @param userId 用户编号
     * @param reqVO 资料信息
     */
    void updateUserProfile(Long userId, AppMemberUserUpdateProfileReqVO reqVO);

}
