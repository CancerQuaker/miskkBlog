package cn.miskk.blog.user.service;

import cn.miskk.blog.common.dto.UserCardVO;
import cn.miskk.blog.common.dto.UserStatsVO;
import cn.miskk.blog.common.dto.LoginDTO;
import cn.miskk.blog.common.dto.RegisterDTO;
import cn.miskk.blog.common.dto.UpdatePasswordDTO;
import cn.miskk.blog.common.dto.UpdateProfileDTO;
import cn.miskk.blog.common.vo.LoginVO;
import cn.miskk.blog.common.vo.UserVO;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口。
 */
public interface UserService {

    /** 发送注册验证码，debug 模式返回验证码明文 */
    String sendEmailCode(String email);

    UserVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    LoginVO githubLogin(String code);

    LoginVO qqLogin(String code);

    LoginVO wechatLogin(String code);

    UserVO me(Long userId);

    UserVO updateProfile(Long userId, UpdateProfileDTO dto);

    void updatePassword(Long userId, UpdatePasswordDTO dto);

    /** 公开的用户主页信息 */
    UserVO getUserPublic(Long id);

    Map<Long, UserCardVO> getUserCards(List<Long> ids);

    UserStatsVO statsOverview();
}
