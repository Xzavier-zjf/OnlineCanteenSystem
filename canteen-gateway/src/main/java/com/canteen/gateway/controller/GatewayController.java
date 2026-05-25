package com.canteen.gateway.controller;

import com.canteen.common.utils.AuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * API网关控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowCredentials = "true")
public class GatewayController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${canteen.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${canteen.product-service.url:http://localhost:8082}")
    private String productServiceUrl;

    @Value("${canteen.order-service.url:http://localhost:8083}")
    private String orderServiceUrl;

    @Value("${canteen.recommend-service.url:http://localhost:8084}")
    private String recommendServiceUrl;

    /**
     * 文件上传专用代理
     */
    @PostMapping("/upload/**")
    public ResponseEntity<String> proxyFileUpload(HttpServletRequest request) {
        return proxyFileUploadRequest(userServiceUrl, request);
    }

    /**
     * 用户服务代理 - 包括管理员API
     */
    @RequestMapping(value = {"/users/**", "/admin/**", "/merchant/**"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyUserService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest(userServiceUrl, request, body);
    }

    /**
     * 商品服务代理 - 包括管理员商品API
     */
    @RequestMapping(value = {"/products/**"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyProductService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest(productServiceUrl, request, body);
    }

    /**
     * 订单服务代理 - 包括管理员订单API
     */
    @RequestMapping(value = {"/orders/**"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyOrderService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest(orderServiceUrl, request, body);
    }

    /**
     * 推荐服务代理
     */
    @RequestMapping(value = "/recommend/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyRecommendService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest(recommendServiceUrl, request, body);
    }

    /**
     * 文件上传代理方法
     */
    private ResponseEntity<String> proxyFileUploadRequest(String serviceUrl, HttpServletRequest request) {
        String path = request.getRequestURI();
        String token = request.getHeader("Authorization");
        
        log.info("代理文件上传请求: POST {} -> {}{}", path, serviceUrl, path);

        try {
            AuthContext auth = AuthContext.from(request);
            auth.requireRole("USER", "MERCHANT", "ADMIN");

            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                
                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                if (token != null) {
                    headers.set("Authorization", token);
                }

                // 构建multipart请求体
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                
                // 处理文件
                for (String paramName : multipartRequest.getFileMap().keySet()) {
                    MultipartFile file = multipartRequest.getFile(paramName);
                    if (file != null && !file.isEmpty()) {
                        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                            @Override
                            public String getFilename() {
                                return file.getOriginalFilename();
                            }
                        };
                        body.add(paramName, resource);
                    }
                }
                
                // 处理其他参数
                for (String paramName : multipartRequest.getParameterMap().keySet()) {
                    String[] values = multipartRequest.getParameterValues(paramName);
                    if (values != null) {
                        for (String value : values) {
                            body.add(paramName, value);
                        }
                    }
                }

                // 创建请求实体
                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

                // 发送请求
                return restTemplate.exchange(
                    buildTargetUrl(serviceUrl, request),
                    HttpMethod.POST,
                    entity,
                    String.class
                );
            } else {
                return ResponseEntity.status(400)
                        .body("{\"code\":400,\"message\":\"不是multipart请求\",\"data\":null}");
            }
        } catch (HttpStatusCodeException e) {
            return downstreamError(e);
        } catch (Exception e) {
            log.error("文件上传代理请求失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body("{\"code\":500,\"message\":\"文件上传失败\",\"data\":null}");
        }
    }

    /**
     * 通用代理方法
     */
    private ResponseEntity<String> proxyRequest(String serviceUrl, HttpServletRequest request, String body) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String token = request.getHeader("Authorization");
        String targetUrl = buildTargetUrl(serviceUrl, request);

        log.info("代理请求: {} {} -> {}", method, path, targetUrl);

        if (needsAuthentication(path, method)) {
            try {
                AuthContext.from(request);
            } catch (SecurityException e) {
                return ResponseEntity.status(401)
                        .body("{\"code\":401,\"message\":\"未授权访问\",\"data\":null}");
            }
        }

        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (token != null) {
                headers.set("Authorization", token);
            }

            // 创建请求实体
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // 发送请求
            return restTemplate.exchange(
                targetUrl,
                HttpMethod.valueOf(method),
                entity,
                String.class
            );
        } catch (HttpStatusCodeException e) {
            return downstreamError(e);
        } catch (Exception e) {
            log.error("代理请求失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body("{\"code\":500,\"message\":\"服务调用失败\",\"data\":null}");
        }
    }

    /**
     * 判断是否需要认证
     */
    private boolean needsAuthentication(String path, String method) {
        if (RequestMethod.OPTIONS.name().equalsIgnoreCase(method)) {
            return false;
        }

        if (RequestMethod.GET.name().equalsIgnoreCase(method)) {
            String[] publicExactGetPaths = {
                    "/api/products",
                    "/api/products/categories",
                    "/api/products/hot",
                    "/api/health",
                    "/api/users/health",
                    "/api/orders/health",
                    "/api/recommend"
            };
            for (String publicPath : publicExactGetPaths) {
                if (path.equals(publicPath)) {
                    return false;
                }
            }

            String[] publicGetPrefixes = {
                    "/api/products/category/",
                    "/api/products/hot",
                    "/api/recommend/"
            };
            for (String publicPath : publicGetPrefixes) {
                if (path.startsWith(publicPath)) {
                    return false;
                }
            }
        }

        // 不需要认证的路径
        String[] publicPaths = {
                "/api/users/register",
                "/api/users/login",
                "/api/merchant/login",
                "/api/merchant/register",
                "/api/health"
        };

        for (String publicPath : publicPaths) {
            if (path.equals(publicPath)) {
                return false;
            }
        }
        return true;
    }

    private String buildTargetUrl(String serviceUrl, HttpServletRequest request) {
        StringBuilder targetUrl = new StringBuilder(serviceUrl).append(request.getRequestURI());
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isBlank()) {
            targetUrl.append('?').append(queryString);
        }
        return targetUrl.toString();
    }

    private ResponseEntity<String> downstreamError(HttpStatusCodeException e) {
        log.warn("下游服务返回错误: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
        HttpHeaders headers = new HttpHeaders();
        MediaType contentType = e.getResponseHeaders() == null ? null : e.getResponseHeaders().getContentType();
        headers.setContentType(contentType == null ? MediaType.APPLICATION_JSON : contentType);
        return ResponseEntity.status(e.getStatusCode())
                .headers(headers)
                .body(e.getResponseBodyAsString());
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"code\":200,\"message\":\"网关服务运行正常\",\"data\":null}");
    }
}
