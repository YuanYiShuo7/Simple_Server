package io.yys.server.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.yys.server.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}