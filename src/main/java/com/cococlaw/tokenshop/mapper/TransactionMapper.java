package com.cococlaw.tokenshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cococlaw.tokenshop.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 交易记录Mapper
 */
@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

    /**
     * 获取总收入
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction WHERE type IN ('recharge', 'order') AND deleted = 0")
    BigDecimal selectSumAmount();

    /**
     * 获取今日收入
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction WHERE type IN ('recharge', 'order') AND deleted = 0 AND create_time >= CURDATE()")
    BigDecimal selectTodayAmount();
}
