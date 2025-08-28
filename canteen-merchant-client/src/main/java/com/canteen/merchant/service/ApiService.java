package com.canteen.merchant.service;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * API服务类 - 处理与后端的HTTP通信
 */
public class ApiService {
    
    private final OkHttpClient client;
    private final String baseUrl;
    
    public ApiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.baseUrl = "http://localhost:8082"; // 餐品服务地址
    }
    
    /**
     * GET请求
     */
    public String get(String path) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            return response.body().string();
        }
    }
    
    /**
     * POST请求
     */
    public String post(String path, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(
                jsonBody, MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            return response.body().string();
        }
    }
    
    /**
     * PUT请求
     */
    public String put(String path, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(
                jsonBody, MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .put(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            return response.body().string();
        }
    }
    
    /**
     * DELETE请求
     */
    public String delete(String path) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .delete()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            return response.body().string();
        }
    }
}