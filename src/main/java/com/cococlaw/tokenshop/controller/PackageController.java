package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.dto.PackageResponse;
import com.cococlaw.tokenshop.service.PackageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐控制器
 */
@Api(tags = "套餐接口")
@Slf4j
@RestController
@RequestMapping("/packages")
public class PackageController {

    @Autowired
    private PackageService packageService;

    /**
     * 获取套餐列表
     */
    @ApiOperation("获取套餐列表")
    @GetMapping
    public Result<List<PackageResponse>> getPackageList() {
        List<PackageResponse> packages = packageService.getPackageList();
        return Result.success(packages);
    }

    /**
     * 获取套餐详情
     */
    @ApiOperation("获取套餐详情")
    @GetMapping("/{id}")
    public Result<PackageResponse> getPackageDetail(@PathVariable Long id) {
        PackageResponse pkg = packageService.getPackageById(id);
        return Result.success(pkg);
    }
}
