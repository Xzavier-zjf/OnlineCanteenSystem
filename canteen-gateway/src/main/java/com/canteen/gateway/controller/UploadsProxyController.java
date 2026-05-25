package com.canteen.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

/**
 * 上传文件读取代理，供生产环境前端直接访问 /uploads/**。
 */
@Slf4j
@RestController
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
public class UploadsProxyController {

    private static final String USER_SERVICE_URL = "http://localhost:8081";

    private final RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(value = "/uploads/**", method = RequestMethod.GET)
    public ResponseEntity<byte[]> proxyUploads(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.info("代理上传文件读取请求: GET {} -> {}{}", path, USER_SERVICE_URL, path);

        return restTemplate.exchange(
                USER_SERVICE_URL + path,
                HttpMethod.GET,
                null,
                byte[].class
        );
    }
}
