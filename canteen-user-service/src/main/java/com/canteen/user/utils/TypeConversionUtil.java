package com.canteen.user.utils;

/**
 * 类型转换工具类
 */
public class TypeConversionUtil {
    
    /**
     * 安全地将对象转换为Long类型
     */
    public static Long toLong(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}