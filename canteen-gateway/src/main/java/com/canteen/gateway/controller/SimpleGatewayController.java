package com.canteen.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

/**
 * 简化的API网关控制器 - 用于调试
 */
@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SimpleGatewayController {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 用户服务代理
     */
    @RequestMapping(value = "/users/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyUserService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest("http://localhost:8081", request, body);
    }

    /**
     * 商品服务代理
     */
    @RequestMapping(value = "/products/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyProductService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest("http://localhost:8082", request, body);
    }

    /**
     * 订单服务代理
     */
    @RequestMapping(value = "/orders/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyOrderService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest("http://localhost:8083", request, body);
    }

    /**
     * 简化的代理方法
     */
    private ResponseEntity<String> proxyRequest(String serviceUrl, HttpServletRequest request, String body) {
        try {
            String path = request.getRequestURI();
            String method = request.getMethod();
            
            log.info("简化代理: {} {} -> {}{}", method, path, serviceUrl, path);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 创建请求实体
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                serviceUrl + path,
                HttpMethod.valueOf(method),
                entity,
                String.class
            );
            
            log.info("代理成功: {} {}", response.getStatusCode(), path);
            return response;
            
        } catch (Exception e) {
            log.error("代理失败: {} - {}", request.getRequestURI(), e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("{\"code\":500,\"message\":\"代理请求失败: " + e.getMessage() + "\",\"data\":null}");
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"code\":200,\"message\":\"简化网关运行正常\",\"data\":null}");
    }
}