package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.dto.PackageResponse;
import com.cococlaw.tokenshop.entity.Package;
import com.cococlaw.tokenshop.mapper.PackageMapper;
import com.cococlaw.tokenshop.service.PackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 套餐服务实现
 */
@Slf4j
@Service
public class PackageServiceImpl implements PackageService {

    private static final String PACKAGE_LIST_CACHE_KEY = "package:list";

    @Autowired
    private PackageMapper packageMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取所有上架的套餐列表
     */
    @Override
    public List<PackageResponse> getPackageList() {
        // 先从Redis缓存获取
        @SuppressWarnings("unchecked")
        List<PackageResponse> cachedList = (List<PackageResponse>) redisTemplate.opsForValue().get(PACKAGE_LIST_CACHE_KEY);
        if (cachedList != null) {
            log.debug("从缓存获取套餐列表");
            return cachedList;
        }

        // 从数据库查询
        List<Package> packages = packageMapper.selectList(
            new LambdaQueryWrapper<Package>()
                .eq(Package::getStatus, 1)
                .orderByAsc(Package::getSortOrder)
        );

        List<PackageResponse> packageList = packages.stream()
                .map(pkg -> PackageResponse.builder()
                        .id(pkg.getId())
                        .name(pkg.getName())
                        .description(pkg.getDescription())
                        .price(pkg.getPrice())
                        .tokens(pkg.getTokens())
                        .validityDays(pkg.getValidityDays())
                        .bonus(pkg.getBonus())
                        .isPopular(pkg.getIsPopular())
                        .build())
                .collect(Collectors.toList());

        // 存入缓存，30分钟有效
        redisTemplate.opsForValue().set(PACKAGE_LIST_CACHE_KEY, packageList, 30, TimeUnit.MINUTES);
        log.debug("套餐列表已缓存，共{}个套餐", packageList.size());

        return packageList;
    }

    /**
     * 获取套餐详情
     */
    @Override
    public PackageResponse getPackageById(Long id) {
        Package pkg = packageMapper.selectById(id);
        if (pkg == null || pkg.getStatus() == 0) {
            throw new BusinessException(ResultCode.PACKAGE_NOT_EXIST);
        }

        return PackageResponse.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .price(pkg.getPrice())
                .tokens(pkg.getTokens())
                .validityDays(pkg.getValidityDays())
                .bonus(pkg.getBonus())
                .isPopular(pkg.getIsPopular())
                .build();
    }
}
