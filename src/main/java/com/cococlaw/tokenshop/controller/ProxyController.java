package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.entity.ApiKey;
import com.cococlaw.tokenshop.entity.Model;
import com.cococlaw.tokenshop.mapper.ApiKeyMapper;
import com.cococlaw.tokenshop.service.ModelService;
import com.cococlaw.tokenshop.service.UserService;
import com.cococlaw.tokenshop.service.UsageService;
import com.cococlaw.tokenshop.utils.JwtUtil;
import com.cococlaw.tokenshop.utils.PasswordUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * API代理控制器 - 处理实际的AI模型调用
 */
@Api(tags = "代理接口")
@Slf4j
@RestController
@RequestMapping("/v1")
public class ProxyController {

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    @Autowired
    private ModelService modelService;

    @Autowired
    private UserService userService;

    @Autowired
    private UsageService usageService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 聊天补全接口 - OpenAI兼容格式
     */
    @ApiOperation("聊天补全")
    @PostMapping("/chat/completions")
    public ResponseEntity<?> chatCompletions(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        long startTime = System.currentTimeMillis();
        String clientIp = getClientIp(httpRequest);
        
        try {
            // 1. 验证API密钥
            String apiKey = extractApiKey(auth);
            ApiKey keyEntity = validateApiKey(apiKey);
            Long userId = keyEntity.getUserId();

            // 2. 获取请求参数
            String modelId = (String) request.get("model");
            JsonNode messages = objectMapper.valueToTree(request.get("messages"));

            // 3. 验证模型
            Model model = modelService.getModelById(modelId);
            if (model == null) {
                throw new BusinessException(ResultCode.MODEL_NOT_EXIST);
            }

            // 4. 检查余额
            var userInfo = userService.getUserInfo(userId);
            if (userInfo.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", Map.of("code", "insufficient_balance", "message", "余额不足")));
            }

            // 5. 调用实际的上游API (这里需要根据不同模型厂商调用不同的API)
            JsonNode response = callUpstreamApi(model, request);

            // 6. 计算Token消耗和费用
            int inputTokens = calculateInputTokens(messages.toString());
            int outputTokens = calculateOutputTokens(response.toString());
            BigDecimal cost = calculateCost(model, inputTokens, outputTokens);

            // 7. 扣除余额
            if (cost.compareTo(BigDecimal.ZERO) > 0) {
                userService.deductBalance(userId, cost);
            }

            // 8. 记录使用
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            usageService.recordUsage(userId, keyEntity.getId(), apiKey, modelId, model.getName(),
                    inputTokens, outputTokens, cost, latencyMs, 200, null, clientIp);

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            usageService.recordUsage(null, null, auth != null ? auth : "", 
                    request.get("model") != null ? request.get("model").toString() : "",
                    "", 0, 0, BigDecimal.ZERO, latencyMs, 
                    e.getCode(), e.getMessage(), clientIp);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", Map.of("code", e.getCode().toString(), "message", e.getMessage())));
        } catch (Exception e) {
            log.error("处理聊天补全请求失败", e);
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            usageService.recordUsage(null, null, auth != null ? auth : "",
                    request.get("model") != null ? request.get("model").toString() : "",
                    "", 0, 0, BigDecimal.ZERO, latencyMs,
                    500, e.getMessage(), clientIp);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", Map.of("code", "internal_error", "message", "服务器内部错误")));
        }
    }

    /**
     * 提取API密钥
     */
    private String extractApiKey(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return auth.substring(7);
    }

    /**
     * 验证API密钥
     */
    private ApiKey validateApiKey(String apiKey) {
        // 先检查Redis缓存
        String cacheKey = "apikey:valid:" + apiKey;
        Long cachedUserId = (Long) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedUserId != null) {
            ApiKey key = new ApiKey();
            key.setUserId(cachedUserId);
            return key;
        }

        ApiKey keyEntity = apiKeyMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ApiKey>()
                .eq(ApiKey::getApiKey, apiKey)
                .eq(ApiKey::getDeleted, 0)
        );

        if (keyEntity == null) {
            throw new BusinessException(ResultCode.API_KEY_NOT_EXIST);
        }

        if (keyEntity.getStatus() == 0) {
            throw new BusinessException(ResultCode.API_KEY_DISABLED);
        }

        // 缓存验证结果，10分钟有效
        redisTemplate.opsForValue().set(cacheKey, keyEntity.getUserId(), 10, TimeUnit.MINUTES);

        // 更新最后使用时间
        apiKeyMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ApiKey>()
                .eq(ApiKey::getId, keyEntity.getId())
                .set(ApiKey::getLastUsedTime, LocalDateTime.now())
        );

        return keyEntity;
    }

    /**
     * 调用上游API (需要根据不同模型厂商实现)
     */
    private JsonNode callUpstreamApi(Model model, Map<String, Object> request) {
        // TODO: 根据不同模型厂商调用不同的API
        // 这里返回模拟响应，实际生产需要实现真实的API调用
        
        // OpenAI兼容响应格式
        Map<String, Object> response = new HashMap<>();
        response.put("id", "chatcmpl-" + System.currentTimeMillis());
        response.put("object", "chat.completion");
        response.put("created", System.currentTimeMillis() / 1000);
        response.put("model", request.get("model"));
        
        Map<String, Object> choice = new HashMap<>();
        choice.put("index", 0);
        
        Map<String, Object> message = new HashMap<>();
        message.put("role", "assistant");
        message.put("content", "这是来自COCO CLAW的模拟响应。实际调用时请配置上游API密钥。");
        choice.put("message", message);
        
        choice.put("finish_reason", "stop");
        
        response.put("choices", new Object[]{choice});
        response.put("usage", Map.of(
            "prompt_tokens", 100,
            "completion_tokens", 50,
            "total_tokens", 150
        ));
        
        return objectMapper.valueToTree(response);
    }

    /**
     * 计算输入Token数 (简化估算)
     */
    private int calculateInputTokens(String text) {
        // 简单按字符数估算，实际应使用tokenizer
        return (int) Math.ceil(text.length() / 4.0);
    }

    /**
     * 计算输出Token数 (简化估算)
     */
    private int calculateOutputTokens(String text) {
        return (int) Math.ceil(text.length() / 4.0);
    }

    /**
     * 计算费用
     */
    private BigDecimal calculateCost(Model model, int inputTokens, int outputTokens) {
        BigDecimal inputCost = model.getInputPrice().multiply(BigDecimal.valueOf(inputTokens / 1000.0));
        BigDecimal outputCost = model.getOutputPrice().multiply(BigDecimal.valueOf(outputTokens / 1000.0));
        return inputCost.add(outputCost);
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
