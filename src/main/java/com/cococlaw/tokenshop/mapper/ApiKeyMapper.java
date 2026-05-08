package com.cococlaw.tokenshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cococlaw.tokenshop.entity.ApiKey;
import org.apache.ibatis.annotations.Mapper;

/**
 * API密钥Mapper
 */
@Mapper
public interface ApiKeyMapper extends BaseMapper<ApiKey> {
}
