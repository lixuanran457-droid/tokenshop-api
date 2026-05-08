package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.entity.Model;
import com.cococlaw.tokenshop.mapper.ModelMapper;
import com.cococlaw.tokenshop.service.ModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
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
        clearModelCache();

        log.info("模型{}状态已更新为: {}", model.getModelId(), status == 1 ? "启用" : "禁用");
    }

    // ==================== 管理员方法 ====================

    @Override
    public List<Model> getAdminModelList(String provider, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(provider)) {
            wrapper.eq(Model::getProvider, provider);
        }

        wrapper.orderByAsc(Model::getSortOrder);

        IPage<Model> result = modelMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return result.getRecords();
    }

    @Override
    @Transactional
    public void createModel(Model model) {
        // 生成模型ID
        if (!StringUtils.hasText(model.getModelId())) {
            model.setModelId(UUID.randomUUID().toString().substring(0, 8));
        }

        // 设置默认值
        if (model.getStatus() == null) {
            model.setStatus(1);
        }
        if (model.getInputPrice() == null) {
            model.setInputPrice(java.math.BigDecimal.ZERO);
        }
        if (model.getOutputPrice() == null) {
            model.setOutputPrice(java.math.BigDecimal.ZERO);
        }
        if (model.getSortOrder() == null) {
            model.setSortOrder(0);
        }

        modelMapper.insert(model);

        // 清除缓存
        clearModelCache();

        log.info("创建模型: {}", model.getModelId());
    }

    @Override
    @Transactional
    public void updateModel(Model model) {
        Model existing = modelMapper.selectOne(
            new LambdaQueryWrapper<Model>()
                .eq(Model::getModelId, model.getModelId())
        );

        if (existing == null) {
            throw new BusinessException(ResultCode.MODEL_NOT_EXIST);
        }

        modelMapper.update(null,
            new LambdaUpdateWrapper<Model>()
                .eq(Model::getModelId, model.getModelId())
                .set(StringUtils.hasText(model.getName()), Model::getName, model.getName())
                .set(StringUtils.hasText(model.getProvider()), Model::getProvider, model.getProvider())
                .set(model.getInputPrice() != null, Model::getInputPrice, model.getInputPrice())
                .set(model.getOutputPrice() != null, Model::getOutputPrice, model.getOutputPrice())
                .set(model.getStatus() != null, Model::getStatus, model.getStatus())
                .set(model.getSortOrder() != null, Model::getSortOrder, model.getSortOrder())
                .set(StringUtils.hasText(model.getDescription()), Model::getDescription, model.getDescription())
        );

        // 清除缓存
        clearModelCache();

        log.info("更新模型: {}", model.getModelId());
    }

    @Override
    @Transactional
    public void deleteModel(String modelId) {
        modelMapper.delete(
            new LambdaQueryWrapper<Model>()
                .eq(Model::getModelId, modelId)
        );

        // 清除缓存
        clearModelCache();

        log.info("删除模型: {}", modelId);
    }

    @Override
    @Transactional
    public void toggleModelStatus(String modelId, String status) {
        Model model = modelMapper.selectOne(
            new LambdaQueryWrapper<Model>()
                .eq(Model::getModelId, modelId)
        );

        if (model == null) {
            throw new BusinessException(ResultCode.MODEL_NOT_EXIST);
        }

        Integer newStatus = "enabled".equals(status) ? 1 : 0;

        modelMapper.update(null,
            new LambdaUpdateWrapper<Model>()
                .eq(Model::getModelId, modelId)
                .set(Model::getStatus, newStatus)
        );

        // 清除缓存
        clearModelCache();

        log.info("切换模型{}状态为: {}", modelId, status);
    }

    /**
     * 清除模型缓存
     */
    private void clearModelCache() {
        redisTemplate.delete(MODEL_LIST_CACHE_KEY);
        // 清除所有厂商缓存
        redisTemplate.delete(redisTemplate.keys(MODEL_PROVIDER_CACHE_KEY + "*"));
    }
}
