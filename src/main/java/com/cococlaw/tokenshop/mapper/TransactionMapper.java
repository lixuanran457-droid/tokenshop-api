package com.cococlaw.tokenshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cococlaw.tokenshop.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易记录Mapper
 */
@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {
}
