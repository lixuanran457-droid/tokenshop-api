package com.cococlaw.tokenshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cococlaw.tokenshop.entity.UsageLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 使用记录Mapper
 */
@Mapper
public interface UsageLogMapper extends BaseMapper<UsageLog> {
}
