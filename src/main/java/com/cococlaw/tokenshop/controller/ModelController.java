package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.entity.Model;
import com.cococlaw.tokenshop.service.ModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型控制器
 */
@Api(tags = "模型接口")
@Slf4j
@RestController
@RequestMapping("/models")
public class ModelController {

    @Autowired
    private ModelService modelService;

    /**
     * 获取模型列表
     */
    @ApiOperation("获取模型列表")
    @GetMapping
    public Result<List<Model>> getModelList() {
        List<Model> models = modelService.getModelList();
        return Result.success(models);
    }

    /**
     * 按厂商获取模型
     */
    @ApiOperation("按厂商获取模型")
    @GetMapping("/{provider}")
    public Result<List<Model>> getModelListByProvider(@PathVariable String provider) {
        List<Model> models = modelService.getModelListByProvider(provider);
        return Result.success(models);
    }

    /**
     * 获取模型详情
     */
    @ApiOperation("获取模型详情")
    @GetMapping("/detail/{modelId}")
    public Result<Model> getModelDetail(@PathVariable String modelId) {
        Model model = modelService.getModelById(modelId);
        return Result.success(model);
    }
}
