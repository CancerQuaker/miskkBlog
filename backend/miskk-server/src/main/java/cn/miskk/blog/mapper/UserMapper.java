package cn.miskk.blog.mapper;

import cn.miskk.blog.entity.User;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper（路由到 miskk_user 库）。
 */
@Mapper
@DS("user")
public interface UserMapper extends BaseMapper<User> {
}
