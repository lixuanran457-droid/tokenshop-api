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

    // ==================== 管理员方法 ====================

    /**
     * 获取模型列表（管理员）
     */
    List<Model> getAdminModelList(String provider, Integer page, Integer pageSize);

    /**
     * 创建模型
     */
    void createModel(Model model);

    /**
     * 更新模型
     */
    void updateModel(Model model);

    /**
     * 删除模型
     */
    void deleteModel(String modelId);

    /**
     * 切换模型状态
     */
    void toggleModelStatus(String modelId, String status);
}
