package cn.miskk.blog.mapper;

import cn.miskk.blog.common.entity.FriendLink;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * FriendLink Mapper。
 */
@Mapper
@DS("friend")
public interface FriendLinkMapper extends BaseMapper<FriendLink> {
}
