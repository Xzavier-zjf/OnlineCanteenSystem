package com.canteen.merchant;

import com.formdev.flatlaf.FlatLightLaf;
import com.canteen.merchant.ui.MainFrame;

import javax.swing.*;

/**
 * 商户端桌面应用主类
 */
public class MerchantClientApp {
    
    public static void main(String[] args) {
        // 设置外观
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("设置外观失败: " + e.getMessage());
        }
        
        // 设置系统属性
        System.setProperty("java.awt.headless", "false");
        
        // 启动主窗口
        SwingUtilities.invokeLater(() -> {
            try {
                new com.canteen.merchant.ui.LoginFrame().setVisible(true);
                System.out.println("=================================");
                System.out.println("商户端桌面应用启动成功！");
                System.out.println("=================================");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "应用启动失败: " + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}