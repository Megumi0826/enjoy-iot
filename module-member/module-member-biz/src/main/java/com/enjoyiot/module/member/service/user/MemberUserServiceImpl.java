package com.enjoyiot.module.member.service.user;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.enjoyiot.framework.common.enums.CommonStatusEnum;
import com.enjoyiot.module.infra.api.file.FileApi;
import com.enjoyiot.module.member.controller.app.user.vo.AppMemberUserUpdateProfileReqVO;
import com.enjoyiot.module.member.dal.dataobject.user.MemberUserDO;
import com.enjoyiot.module.member.dal.mysql.user.MemberUserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static com.enjoyiot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enjoyiot.module.member.enums.ErrorCodeConstants.*;

@Service
@Validated
public class MemberUserServiceImpl implements MemberUserService {

    @Resource
    private MemberUserMapper memberUserMapper;

    @Resource
    private FileApi fileApi;

    @Override
    public MemberUserDO getUser(Long id) {
        return memberUserMapper.selectById(id);
    }

    @Override
    public MemberUserDO getUserByMobile(String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return null;
        }
        return memberUserMapper.selectByMobile(mobile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberUserDO createUser(String nickname, String avatar, String registerIp, Integer terminal) {
        return createUser(null, nickname, avatar, registerIp, terminal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberUserDO createUserIfAbsent(String mobile, String registerIp, Integer terminal) {
        if (StrUtil.isNotBlank(mobile)) {
            MemberUserDO user = memberUserMapper.selectByMobile(mobile);
            if (user != null) {
                return user;
            }
        }
        return createUser(mobile, null, null, registerIp, terminal);
    }

    private MemberUserDO createUser(String mobile, String nickname, String avatar,
                                    String registerIp, Integer terminal) {
        MemberUserDO user = new MemberUserDO()
                .setMobile(StrUtil.blankToDefault(mobile, null))
                .setPassword("")
                .setNickname(StrUtil.blankToDefault(nickname, "用户" + RandomUtil.randomNumbers(6)))
                .setAvatar(StrUtil.blankToDefault(avatar, ""))
                .setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setRegisterIp(registerIp)
                .setRegisterTerminal(terminal);

        memberUserMapper.insert(user);
        return user;
    }

    @Override
    public void updateUserLogin(Long id, String loginIp) {
        memberUserMapper.updateById(new MemberUserDO()
                .setId(id)
                .setLoginIp(loginIp)
                .setLoginDate(LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateUserAvatar(Long userId, MultipartFile file) throws Exception {
        MemberUserDO user = getUser(userId);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        if (file == null || file.isEmpty()) {
            throw exception(USER_AVATAR_IS_EMPTY);
        }

        byte[] content = IoUtil.readBytes(file.getInputStream());
        if (content.length > 5 * 1024 * 1024) {
            throw exception(USER_AVATAR_SIZE_EXCEED);
        }

        String extName = getAvatarExtName(file.getContentType());
        if (StrUtil.isBlank(extName)) {
            throw exception(USER_AVATAR_TYPE_NOT_SUPPORT);
        }

        String originalFilename = StrUtil.blankToDefault(file.getOriginalFilename(), "avatar." + extName);
        String path = "member/avatar/" + userId + "/" + IdUtil.fastSimpleUUID() + "." + extName;
        String avatar = fileApi.createFile(originalFilename, path, content);

        memberUserMapper.updateById(new MemberUserDO()
                .setId(userId)
                .setAvatar(avatar));
        return avatar;
    }

    private String getAvatarExtName(String contentType) {
        if ("image/jpeg".equals(contentType)) {
            return "jpg";
        }
        if ("image/png".equals(contentType)) {
            return "png";
        }
        if ("image/webp".equals(contentType)) {
            return "webp";
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserProfile(Long userId, AppMemberUserUpdateProfileReqVO reqVO) {
        MemberUserDO user = getUser(userId);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        if (reqVO.getNickname() != null && StrUtil.isBlank(reqVO.getNickname())) {
            throw exception(USER_NICKNAME_IS_EMPTY);
        }

        MemberUserDO updateObj = new MemberUserDO().setId(userId);
        if (reqVO.getNickname() != null) {
            updateObj.setNickname(reqVO.getNickname());
        }
        if (reqVO.getName() != null) {
            updateObj.setName(reqVO.getName());
        }
        if (reqVO.getSex() != null) {
            updateObj.setSex(reqVO.getSex());
        }
        if (reqVO.getBirthday() != null) {
            updateObj.setBirthday(reqVO.getBirthday());
        }
        if (reqVO.getHeight() != null) {
            updateObj.setHeight(reqVO.getHeight());
        }
        if (reqVO.getWeight() != null) {
            updateObj.setWeight(reqVO.getWeight());
        }
        memberUserMapper.updateById(updateObj);
    }

}
