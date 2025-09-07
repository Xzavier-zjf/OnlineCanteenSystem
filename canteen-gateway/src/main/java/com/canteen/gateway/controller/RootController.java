package com.canteen.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 根路径控制器 - 处理根路径请求
 */
@RestController
public class RootController {

    @GetMapping("/")
    public String root() {
        return "API网关服务运行正常 - 端口8080";
    }
    
    @GetMapping("/status")
    public String status() {
        return "{\"status\":\"running\",\"service\":\"canteen-gateway\",\"port\":8080}";
    }
}