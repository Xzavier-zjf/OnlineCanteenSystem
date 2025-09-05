package com.canteen.merchant;

import com.canteen.merchant.ui.LoginFrame;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * 商户客户端主程序
 */
@Slf4j
public class MerchantClientApplication {

    public static void main(String[] args) {
        try {
            // 设置系统外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // 启动登录界面
            SwingUtilities.invokeLater(() -> {
                try {
                    new LoginFrame().setVisible(true);
                } catch (Exception e) {
                    log.error("启动商户客户端失败", e);
                    JOptionPane.showMessageDialog(null, 
                        "启动失败：" + e.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            
        } catch (Exception e) {
            log.error("初始化商户客户端失败", e);
            System.exit(1);
        }
    }
}