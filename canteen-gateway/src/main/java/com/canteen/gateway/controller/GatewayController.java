package com.canteen.gateway.controller;

import com.canteen.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;

/**
 * API网关控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GatewayController {

    private final WebClient webClient;

    /**
     * 用户服务代理
     */
    @RequestMapping(value = "/user/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public Mono<ResponseEntity<String>> proxyUserService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest("http://localhost:8081", request, body);
    }

    /**
     * 商品服务代理
     */
    @RequestMapping(value = "/product/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public Mono<ResponseEntity<String>> proxyProductService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest("http://localhost:8082", request, body);
    }

    /**
     * 订单服务代理
     */
    @RequestMapping(value = "/order/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public Mono<ResponseEntity<String>> proxyOrderService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest("http://localhost:8083", request, body);
    }

    /**
     * 推荐服务代理
     */
    @RequestMapping(value = "/recommend/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public Mono<ResponseEntity<String>> proxyRecommendService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest("http://localhost:8084", request, body);
    }

    /**
     * 通用代理方法
     */
    private Mono<ResponseEntity<String>> proxyRequest(String serviceUrl, HttpServletRequest request, String body) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String token = request.getHeader("Authorization");

        log.info("代理请求: {} {} -> {}", method, path, serviceUrl + path);

        // 对于需要认证的接口进行Token验证
        if (needsAuthentication(path)) {
            if (token == null || !token.startsWith("Bearer ") || !JwtUtils.validateToken(token.substring(7))) {
                return Mono.just(ResponseEntity.status(401)
                        .body("{\"code\":401,\"message\":\"未授权访问\",\"data\":null}"));
            }
        }

        WebClient.RequestHeadersSpec<?> requestSpec = webClient
                .method(org.springframework.http.HttpMethod.valueOf(method))
                .uri(serviceUrl + path);

        // 添加请求头
        if (token != null) {
            requestSpec = requestSpec.header("Authorization", token);
        }

        // 添加请求体
        if (body != null && !body.isEmpty() && 
            ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))) {
            requestSpec = ((WebClient.RequestBodySpec) requestSpec).bodyValue(body);
        }

        return requestSpec
                .retrieve()
                .toEntity(String.class)
                .onErrorReturn(ResponseEntity.status(500)
                        .body("{\"code\":500,\"message\":\"服务调用失败\",\"data\":null}"));
    }

    /**
     * 判断是否需要认证
     */
    private boolean needsAuthentication(String path) {
        // 不需要认证的路径
        String[] publicPaths = {
                "/api/user/register",
                "/api/user/login",
                "/api/user/health",
                "/api/product/list",
                "/api/product/category"
        };

        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"code\":200,\"message\":\"网关服务运行正常\",\"data\":null}");
    }
}