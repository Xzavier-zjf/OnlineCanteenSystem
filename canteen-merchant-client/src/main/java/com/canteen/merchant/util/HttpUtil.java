package com.canteen.merchant.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * HTTP工具类
 */
@Slf4j
public class HttpUtil {
    
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 发送GET请求
     */
    public static String get(String url) throws IOException, InterruptedException {
        return get(url, null);
    }
    
    /**
     * 发送带认证头的GET请求
     */
    public static String get(String url, String token) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET();
        
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        
        HttpRequest request = builder.build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        log.debug("GET {} -> {}", url, response.statusCode());
        return response.body();
    }
    
    /**
     * 发送POST请求
     */
    public static String post(String url, Map<String, Object> data) throws IOException, InterruptedException {
        return post(url, data, null);
    }
    
    /**
     * 发送带认证头的POST请求
     */
    public static String post(String url, Map<String, Object> data, String token) throws IOException, InterruptedException {
        String jsonData = objectMapper.writeValueAsString(data);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonData));
        
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        
        HttpRequest request = builder.build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        log.debug("POST {} -> {}", url, response.statusCode());
        return response.body();
    }
    
    /**
     * 发送PUT请求
     */
    public static String put(String url, Map<String, Object> data, String token) throws IOException, InterruptedException {
        String jsonData = objectMapper.writeValueAsString(data);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonData));
        
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        
        HttpRequest request = builder.build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        log.debug("PUT {} -> {}", url, response.statusCode());
        return response.body();
    }
    
    /**
     * 发送DELETE请求
     */
    public static String delete(String url, String token) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .DELETE();
        
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        
        HttpRequest request = builder.build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        log.debug("DELETE {} -> {}", url, response.statusCode());
        return response.body();
    }
}