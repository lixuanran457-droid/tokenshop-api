package com.cococlaw.tokenshop.service;

import com.cococlaw.tokenshop.dto.PackageResponse;

import java.util.List;

/**
 * 套餐服务接口
 */
public interface PackageService {

    /**
     * 获取所有上架的套餐列表
     */
    List<PackageResponse> getPackageList();

    /**
     * 获取套餐详情
     */
    PackageResponse getPackageById(Long id);
}
