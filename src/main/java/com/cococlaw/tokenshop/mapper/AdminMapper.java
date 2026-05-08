package com.cococlaw.tokenshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cococlaw.tokenshop.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员Mapper
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
