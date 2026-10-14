package cn.miskk.blog.mapper;

import cn.miskk.blog.common.entity.FriendApply;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * FriendApply Mapper。
 */
@Mapper
@DS("friend")
public interface FriendApplyMapper extends BaseMapper<FriendApply> {
}
