package com.canteen.merchant.service.impl;

import com.canteen.merchant.dto.MerchantUser;
import com.canteen.merchant.service.AuthService;
import com.canteen.merchant.util.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现
 */
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private static final String BASE_URL = "http://localhost:8081"; // 用户服务地址
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MerchantUser currentUser;
    
    @Override
    public boolean login(String username, String password) {
        try {
            Map<String, Object> loginData = new HashMap<>();
            loginData.put("username", username);
            loginData.put("password", password);
            
            String response = HttpUtil.post(BASE_URL + "/api/auth/login", loginData);
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.get("code").asInt() == 200) {
                JsonNode dataNode = responseNode.get("data");
                
                // 检查用户角色是否为商户
                String role = dataNode.get("role").asText();
                if (!"MERCHANT".equals(role)) {
                    log.warn("非商户用户尝试登录商户端：username={}, role={}", username, role);
                    return false;
                }
                
                // 构建用户信息
                currentUser = new MerchantUser();
                currentUser.setId(dataNode.get("id").asLong());
                currentUser.setUsername(dataNode.get("username").asText());
                currentUser.setEmail(dataNode.get("email").asText());
                currentUser.setPhone(dataNode.get("phone").asText());
                currentUser.setRole(role);
                currentUser.setStatus(dataNode.get("status").asText());
                currentUser.setToken(dataNode.get("token").asText());
                
                // 设置商户信息
                currentUser.setMerchantName(username + "的店铺");
                
                log.info("商户登录成功：username={}", username);
                return true;
            } else {
                log.warn("登录失败：{}", responseNode.get("message").asText());
                return false;
            }
        } catch (Exception e) {
            log.error("登录异常：username={}", username, e);
            return false;
        }
    }
    
    @Override
    public MerchantUser getCurrentUser() {
        return currentUser;
    }
    
    @Override
    public void logout() {
        currentUser = null;
        log.info("用户已退出登录");
    }
    
    @Override
    public boolean isLoggedIn() {
        return currentUser != null && currentUser.getToken() != null;
    }
}