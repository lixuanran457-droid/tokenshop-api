package com.cococlaw.tokenshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cococlaw.tokenshop.entity.AdminLog;

public interface AdminLogService extends IService<AdminLog> {
    
    void log(Long adminId, String username, String action, String targetType, 
             String targetId, String description, String params);
    
    void logSuccess(Long adminId, String username, String action, String targetType, 
                   String targetId, String description, String params);
    
    void logFail(Long adminId, String username, String action, String description, 
                String errorMsg, String params);
}
