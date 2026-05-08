package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.entity.Model;
import com.cococlaw.tokenshop.service.ModelService;
import com.cococlaw.tokenshop.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理员模型管理控制器
 */
@Api(tags = "管理-模型")
@Slf4j
@RestController
@RequestMapping("/admin/models")
public class AdminModelController {

    @Autowired
    private ModelService modelService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取模型列表
     */
    @ApiOperation("获取模型列表")
    @GetMapping
    public Result<List<Model>> getModelList(
            @RequestParam(required = false) String provider,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        List<Model> models = modelService.getAdminModelList(provider, page, pageSize);
        return Result.success(models);
    }

    /**
     * 获取模型详情
     */
    @ApiOperation("获取模型详情")
    @GetMapping("/{modelId}")
    public Result<Model> getModelDetail(@PathVariable String modelId) {
        Model model = modelService.getModelById(modelId);
        if (model == null) {
            throw new BusinessException(ResultCode.MODEL_NOT_EXIST);
        }
        return Result.success(model);
    }

    /**
     * 创建模型
     */
    @ApiOperation("创建模型")
    @PostMapping
    public Result<Void> createModel(@RequestBody Model model) {
        modelService.createModel(model);
        return Result.success("创建成功", null);
    }

    /**
     * 更新模型
     */
    @ApiOperation("更新模型")
    @PutMapping("/{modelId}")
    public Result<Void> updateModel(
            @PathVariable String modelId,
            @RequestBody Model model) {
        model.setModelId(modelId);
        modelService.updateModel(model);
        return Result.success("更新成功", null);
    }

    /**
     * 删除模型
     */
    @ApiOperation("删除模型")
    @DeleteMapping("/{modelId}")
    public Result<Void> deleteModel(@PathVariable String modelId) {
        modelService.deleteModel(modelId);
        return Result.success("删除成功", null);
    }

    /**
     * 启用/禁用模型
     */
    @ApiOperation("切换模型状态")
    @PostMapping("/{modelId}/status")
    public Result<Void> toggleModelStatus(
            @PathVariable String modelId,
            @RequestParam String status) {
        modelService.toggleModelStatus(modelId, status);
        return Result.success("状态已更新", null);
    }

    /**
     * 从请求中获取管理员ID
     */
    private Long getAdminIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isAdminToken(token)) {
                return jwtUtil.getAdminIdFromToken(token);
            }
        }
        return null;
    }
}
