package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.entity.Model;
import com.cococlaw.tokenshop.mapper.ModelMapper;
import com.cococlaw.tokenshop.service.ModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 模型服务实现
 */
@Slf4j
@Service
public class ModelServiceImpl implements ModelService {

    private static final String MODEL_LIST_CACHE_KEY = "model:list:all";
    private static final String MODEL_PROVIDER_CACHE_KEY = "model:list:provider:";

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取所有启用的模型列表
     */
    @Override
    public List<Model> getModelList() {
        // 先从Redis缓存获取
        @SuppressWarnings("unchecked")
        List<Model> cachedList = (List<Model>) redisTemplate.opsForValue().get(MODEL_LIST_CACHE_KEY);
        if (cachedList != null) {
            log.debug("从缓存获取模型列表");
            return cachedList;
        }

        // 从数据库查询
        List<Model> modelList = modelMapper.selectList(
            new LambdaQueryWrapper<Model>()
                .eq(Model::getStatus, 1)
                .orderByAsc(Model::getSortOrder)
        );

        // 存入缓存，10分钟有效
        redisTemplate.opsForValue().set(MODEL_LIST_CACHE_KEY, modelList, 10, TimeUnit.MINUTES);
        log.debug("模型列表已缓存，共{}个模型", modelList.size());

        return modelList;
    }

    /**
     * 按厂商获取模型列表
     */
    @Override
    public List<Model> getModelListByProvider(String provider) {
        String cacheKey = MODEL_PROVIDER_CACHE_KEY + provider;

        // 先从Redis缓存获取
        @SuppressWarnings("unchecked")
        List<Model> cachedList = (List<Model>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedList != null) {
            log.debug("从缓存获取厂商{}模型列表", provider);
            return cachedList;
        }

        // 从数据库查询
        List<Model> modelList = modelMapper.selectList(
            new LambdaQueryWrapper<Model>()
                .eq(Model::getProvider, provider)
                .eq(Model::getStatus, 1)
                .orderByAsc(Model::getSortOrder)
        );

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, modelList, 10, TimeUnit.MINUTES);

        return modelList;
    }

    /**
     * 获取模型详情
     */
    @Override
    public Model getModelById(String modelId) {
        return modelMapper.selectOne(
            new LambdaQueryWrapper<Model>()
                .eq(Model::getModelId, modelId)
                .eq(Model::getStatus, 1)
        );
    }

    /**
     * 启用/禁用模型
     */
    @Override
    public void updateModelStatus(Long id, Integer status) {
        Model model = modelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(ResultCode.MODEL_NOT_EXIST);
        }

        modelMapper.update(null,
            new LambdaUpdateWrapper<Model>()
                .eq(Model::getId, id)
                .set(Model::getStatus, status)
        );

        // 清除缓存
        redisTemplate.delete(MODEL_LIST_CACHE_KEY);
        redisTemplate.delete(MODEL_PROVIDER_CACHE_KEY + model.getProvider());

        log.info("模型{}状态已更新为: {}", model.getModelId(), status == 1 ? "启用" : "禁用");
    }
}
