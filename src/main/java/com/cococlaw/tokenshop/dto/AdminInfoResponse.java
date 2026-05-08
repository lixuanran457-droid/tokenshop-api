package com.cococlaw.tokenshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员信息响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminInfoResponse {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private Integer status;
}
