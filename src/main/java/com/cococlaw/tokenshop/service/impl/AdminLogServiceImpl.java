package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cococlaw.tokenshop.entity.AdminLog;
import com.cococlaw.tokenshop.mapper.AdminLogMapper;
import com.cococlaw.tokenshop.service.AdminLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AdminLogServiceImpl extends ServiceImpl<AdminLogMapper, AdminLog> implements AdminLogService {

    @Override
    @Async
    public void log(Long adminId, String username, String action, String targetType, 
                   String targetId, String description, String params) {
        logSuccess(adminId, username, action, targetType, targetId, description, params);
    }

    @Override
    @Async
    public void logSuccess(Long adminId, String username, String action, String targetType, 
                          String targetId, String description, String params) {
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setUsername(username);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDescription(description);
        log.setParams(params);
        log.setResult(1);
        fillRequestInfo(log);
        this.save(log);
    }

    @Override
    @Async
    public void logFail(Long adminId, String username, String action, String description, 
                       String errorMsg, String params) {
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setUsername(username);
        log.setAction(action);
        log.setDescription(description);
        log.setParams(params);
        log.setResult(0);
        log.setErrorMsg(errorMsg);
        fillRequestInfo(log);
        this.save(log);
    }

    private void fillRequestInfo(AdminLog log) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIp(getClientIp(request));
                log.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
