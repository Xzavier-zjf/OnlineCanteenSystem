package com.canteen.merchant.service.impl;

import com.canteen.merchant.service.MerchantOrderService;
import com.canteen.merchant.util.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户订单服务实现
 */
@Slf4j
public class MerchantOrderServiceImpl implements MerchantOrderService {
    
    private static final String BASE_URL = "http://localhost:8083"; // 订单服务地址
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Long getPendingOrderCount(Long merchantId) {
        try {
            String url = BASE_URL + "/api/merchant/orders/pending/count?merchantId=" + merchantId;
            String response = HttpUtil.get(url);
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.get("code").asInt() == 200) {
                return responseNode.get("data").asLong();
            } else {
                log.warn("获取待处理订单数失败：{}", responseNode.get("message").asText());
                return 0L;
            }
        } catch (Exception e) {
            log.error("获取待处理订单数异常：merchantId={}", merchantId, e);
            return 0L;
        }
    }
    
    @Override
    public BigDecimal getTodayRevenue(Long merchantId) {
        try {
            String url = BASE_URL + "/api/merchant/orders/revenue/today?merchantId=" + merchantId;
            String response = HttpUtil.get(url);
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.get("code").asInt() == 200) {
                return new BigDecimal(responseNode.get("data").asText());
            } else {
                log.warn("获取今日营业额失败：{}", responseNode.get("message").asText());
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            log.error("获取今日营业额异常：merchantId={}", merchantId, e);
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public Long getTotalOrderCount(Long merchantId) {
        try {
            String url = BASE_URL + "/api/merchant/orders/count?merchantId=" + merchantId;
            String response = HttpUtil.get(url);
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.get("code").asInt() == 200) {
                return responseNode.get("data").asLong();
            } else {
                log.warn("获取订单总数失败：{}", responseNode.get("message").asText());
                return 0L;
            }
        } catch (Exception e) {
            log.error("获取订单总数异常：merchantId={}", merchantId, e);
            return 0L;
        }
    }
    
    @Override
    public List<Map<String, Object>> getOrderList(Long merchantId, Integer page, Integer size, String status) {
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/api/merchant/orders");
            urlBuilder.append("?merchantId=").append(merchantId);
            urlBuilder.append("&page=").append(page);
            urlBuilder.append("&size=").append(size);
            if (status != null && !status.isEmpty()) {
                urlBuilder.append("&status=").append(status);
            }
            
            String response = HttpUtil.get(urlBuilder.toString());
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.get("code").asInt() == 200) {
                JsonNode dataNode = responseNode.get("data");
                JsonNode recordsNode = dataNode.get("records");
                
                List<Map<String, Object>> orders = new ArrayList<>();
                if (recordsNode.isArray()) {
                    for (JsonNode orderNode : recordsNode) {
                        Map<String, Object> order = objectMapper.convertValue(orderNode, Map.class);
                        orders.add(order);
                    }
                }
                return orders;
            } else {
                log.warn("获取订单列表失败：{}", responseNode.get("message").asText());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("获取订单列表异常：merchantId={}", merchantId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public void acceptOrder(Long orderId, Long merchantId) {
        try {
            String url = BASE_URL + "/api/merchant/orders/" + orderId + "/accept";
            Map<String, Object> data = new HashMap<>();
            data.put("merchantId", merchantId);
            
            String response = HttpUtil.post(url, data);
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.get("code").asInt() != 200) {
                throw new RuntimeException(responseNode.get("message").asText());
            }
            
            log.info("接受订单成功：orderId={}, merchantId={}", orderId, merchantId);
        } catch (Exception e) {
            log.error("接受订单异常：orderId={}, merchantId={}", orderId, merchantId, e);
            throw new RuntimeException("接受订单失败：" + e.getMessage());
        }
    }
    
    @Override
    public void rejectOrder(Long orderId, Long merchantId, String reason) {
        try {
            String url = BASE_URL + "/api/merchant/orders/" + orderId + "/reject";
            Map<String, Object> data = new HashMap<>();
            data.put("merchantId", merchantId);
            data.put("reason", reason);
            
            String response = HttpUtil.post(url, data);
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.get("code").asInt() != 200) {
                throw new RuntimeException(responseNode.get("message").asText());
            }
            
            log.info("拒绝订单成功：orderId={}, merchantId={}, reason={}", orderId, merchantId, reason);
        } catch (Exception e) {
            log.error("拒绝订单异常：orderId={}, merchantId={}", orderId, merchantId, e);
            throw new RuntimeException("拒绝订单失败：" + e.getMessage());
        }
    }
    
    @Override
    public void updateOrderStatus(Long orderId, Long merchantId, String status) {
        try {
            String url = BASE_URL + "/api/merchant/orders/" + orderId + "/status";
            Map<String, Object> data = new HashMap<>();
            data.put("merchantId", merchantId);
            data.put("status", status);
            
            String response = HttpUtil.put(url, data, null);
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.get("code").asInt() != 200) {
                throw new RuntimeException(responseNode.get("message").asText());
            }
            
            log.info("更新订单状态成功：orderId={}, merchantId={}, status={}", orderId, merchantId, status);
        } catch (Exception e) {
            log.error("更新订单状态异常：orderId={}, merchantId={}", orderId, merchantId, e);
            throw new RuntimeException("更新订单状态失败：" + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getOrderDetail(Long orderId, Long merchantId) {
        try {
            String url = BASE_URL + "/api/merchant/orders/" + orderId + "?merchantId=" + merchantId;
            String response = HttpUtil.get(url);
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.get("code").asInt() == 200) {
                return objectMapper.convertValue(responseNode.get("data"), Map.class);
            } else {
                throw new RuntimeException(responseNode.get("message").asText());
            }
        } catch (Exception e) {
            log.error("获取订单详情异常：orderId={}, merchantId={}", orderId, merchantId, e);
            throw new RuntimeException("获取订单详情失败：" + e.getMessage());
        }
    }
}