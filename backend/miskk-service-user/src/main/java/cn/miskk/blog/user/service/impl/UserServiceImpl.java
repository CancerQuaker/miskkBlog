package cn.miskk.blog.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.miskk.blog.common.constant.RedisKeys;
import cn.miskk.blog.common.constant.SecurityConstants;
import cn.miskk.blog.common.dto.UserCardVO;
import cn.miskk.blog.common.dto.UserStatsVO;
import cn.miskk.blog.common.exception.BizException;
import cn.miskk.blog.common.auth.JwtUtil;
import cn.miskk.blog.common.dto.LoginDTO;
import cn.miskk.blog.common.dto.RegisterDTO;
import cn.miskk.blog.common.dto.UpdatePasswordDTO;
import cn.miskk.blog.common.dto.UpdateProfileDTO;
import cn.miskk.blog.user.entity.User;
import cn.miskk.blog.user.mapper.UserMapper;
import cn.miskk.blog.user.service.MailService;
import cn.miskk.blog.user.service.UserService;
import cn.miskk.blog.common.vo.LoginVO;
import cn.miskk.blog.common.vo.UserVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final MailService mailService;

    @Value("${miskk.mail.debug:true}")
    private boolean mailDebug;

    @Value("${miskk.oauth.github.client-id:}")
    private String githubClientId;

    @Value("${miskk.oauth.github.client-secret:}")
    private String githubClientSecret;

    // ==================== 验证码 / 注册 ====================

    @Override
    public String sendEmailCode(String email) {
        // 60 秒内不允许重复发送
        String limitKey = RedisKeys.EMAIL_CODE + email + ":limit";
        Boolean first = redisTemplate.opsForValue().setIfAbsent(limitKey, "1", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(first)) {
            throw new BizException("发送太频繁啦，请 1 分钟后再试");
        }

        String code = RandomUtil.randomNumbers(6);
        mailService.sendVerifyCode(email, code);
        redisTemplate.opsForValue().set(RedisKeys.EMAIL_CODE + email, code, 5, TimeUnit.MINUTES);
        return mailDebug ? code : null;
    }

    @Override
    public UserVO register(RegisterDTO dto) {
        String codeKey = RedisKeys.EMAIL_CODE + dto.getEmail();
        String cached = redisTemplate.opsForValue().get(codeKey);
        if (cached == null || !cached.equals(dto.getCode())) {
            throw new BizException("验证码错误或已过期");
        }
        redisTemplate.delete(codeKey);

        Long count = userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getEmail, dto.getEmail()));
        if (count != null && count > 0) {
            throw new BizException("该邮箱已被注册，请直接登录");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername("user_" + RandomUtil.randomString(8));
        user.setNickname(StringUtils.hasText(dto.getNickname())
                ? dto.getNickname() : "喵友" + RandomUtil.randomNumbers(4));
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setRole(SecurityConstants.ROLE_USER);
        user.setStatus(0);
        user.setAvatar(null);
        userMapper.insert(user);
        return toVO(user);
    }

    // ==================== 登录 ====================

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getEmail, dto.getAccount())
                .or()
                .eq(User::getUsername, dto.getAccount())
                .last("LIMIT 1"));
        if (user == null || !StringUtils.hasText(user.getPassword())
                || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BizException("账号或密码不正确");
        }
        if (user.getStatus() != null && user.getStatus() != 0) {
            throw new BizException("账号已被封禁，请联系管理员");
        }
        return doLogin(user);
    }

    @Override
    public LoginVO githubLogin(String code) {
        if (!StringUtils.hasText(githubClientId) || !StringUtils.hasText(githubClientSecret)) {
            throw new BizException("GitHub 登录暂未配置，请使用邮箱注册登录");
        }
        Map<String, Object> tokenForm = new HashMap<>();
        tokenForm.put("client_id", githubClientId);
        tokenForm.put("client_secret", githubClientSecret);
        tokenForm.put("code", code);
        String tokenResp = HttpUtil.createPost("https://github.com/login/oauth/access_token")
                .header("Accept", "application/json")
                .form(tokenForm)
                .timeout(8000)
                .execute().body();
        JSONObject tokenJson = JSONUtil.parseObj(tokenResp);
        String accessToken = tokenJson.getStr("access_token");
        if (!StringUtils.hasText(accessToken)) {
            log.warn("GitHub 换取 token 失败: {}", tokenResp);
            throw new BizException("GitHub 登录失败，请重试");
        }

        String userResp = HttpRequest.get("https://api.github.com/user")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "miskk-blog")
                .timeout(8000)
                .execute().body();
        JSONObject ghUser = JSONUtil.parseObj(userResp);
        String ghLogin = ghUser.getStr("login");
        if (!StringUtils.hasText(ghLogin)) {
            throw new BizException("GitHub 用户信息获取失败");
        }

        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getGithub, ghLogin));
        if (user == null) {
            user = new User();
            user.setGithub(ghLogin);
            user.setUsername("gh_" + ghLogin);
            String name = ghUser.getStr("name");
            user.setNickname(StringUtils.hasText(name) ? name : ghLogin);
            user.setAvatar(ghUser.getStr("avatar_url"));
            user.setEmail(ghUser.getStr("email"));
            user.setBio(ghUser.getStr("bio"));
            user.setRole(SecurityConstants.ROLE_USER);
            user.setStatus(0);
            userMapper.insert(user);
        }
        return doLogin(user);
    }

    @Override
    public LoginVO qqLogin(String code) {
        throw new BizException("QQ 登录暂未开放（需要 QQ 互联审核），请使用邮箱或 GitHub 登录");
    }

    @Override
    public LoginVO wechatLogin(String code) {
        throw new BizException("微信登录暂未开放（需要开放平台认证），请使用邮箱或 GitHub 登录");
    }

    private LoginVO doLogin(User user) {
        User update = new User();
        update.setId(user.getId());
        update.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(update);

        String token = jwtUtil.createToken(user.getId(), user.getRole());
        return new LoginVO(token, toVO(user));
    }

    // ==================== 资料 ====================

    @Override
    public UserVO me(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return toVO(user);
    }

    @Override
    public UserVO updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getQq() != null) {
            user.setQq(dto.getQq());
        }
        if (dto.getWechat() != null) {
            user.setWechat(dto.getWechat());
        }
        if (dto.getGithub() != null) {
            user.setGithub(dto.getGithub());
        }
        if (StringUtils.hasText(dto.getEmail()) && !dto.getEmail().equals(user.getEmail())) {
            Long count = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                    .eq(User::getEmail, dto.getEmail()).ne(User::getId, userId));
            if (count != null && count > 0) {
                throw new BizException("该邮箱已被其他账号绑定");
            }
            user.setEmail(dto.getEmail());
        }
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (StringUtils.hasText(user.getPassword())
                && !BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        User update = new User();
        update.setId(userId);
        update.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        userMapper.updateById(update);
    }

    @Override
    public UserVO getUserPublic(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return toVO(user);
    }

    @Override
    public Map<Long, UserCardVO> getUserCards(List<Long> ids) {
        Map<Long, UserCardVO> cards = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return cards;
        }
        List<User> users = userMapper.selectBatchIds(ids);
        for (User u : users) {
            cards.put(u.getId(), new UserCardVO(u.getId(),
                    StringUtils.hasText(u.getNickname()) ? u.getNickname() : "喵友" + u.getId(), u.getAvatar()));
        }
        return cards;
    }

    @Override
    public UserStatsVO statsOverview() {
        UserStatsVO vo = new UserStatsVO();
        vo.setTotalUsers(userMapper.selectCount(null));
        YearMonth now = YearMonth.now();
        vo.setNewUsersThisMonth(userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .ge(User::getCreatedAt, now.atDay(1).atStartOfDay())
                .lt(User::getCreatedAt, now.plusMonths(1).atDay(1).atStartOfDay())));
        return vo;
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setEmail(user.getEmail());
        vo.setQq(user.getQq());
        vo.setWechat(user.getWechat());
        vo.setGithub(user.getGithub());
        vo.setRole(user.getRole());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setLastLoginAt(user.getLastLoginAt());
        return vo;
    }
}
