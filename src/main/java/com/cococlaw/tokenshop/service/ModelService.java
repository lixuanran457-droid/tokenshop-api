package com.cococlaw.tokenshop.service;

import com.cococlaw.tokenshop.entity.Model;

import java.util.List;

/**
 * 模型服务接口
 */
public interface ModelService {

    /**
     * 获取所有启用的模型列表
     */
    List<Model> getModelList();

    /**
     * 按厂商获取模型列表
     */
    List<Model> getModelListByProvider(String provider);

    /**
     * 获取模型详情
     */
    Model getModelById(String modelId);

    /**
     * 启用/禁用模型
     */
    void updateModelStatus(Long id, Integer status);
}
