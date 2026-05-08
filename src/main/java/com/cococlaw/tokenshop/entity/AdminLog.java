package com.cococlaw.tokenshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("admin_log")
public class AdminLog implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long adminId;
    
    private String username;
    
    private String action;
    
    private String targetType;
    
    private String targetId;
    
    private String description;
    
    private String ip;
    
    private String userAgent;
    
    private String params;
    
    private Integer result;
    
    private String errorMsg;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
