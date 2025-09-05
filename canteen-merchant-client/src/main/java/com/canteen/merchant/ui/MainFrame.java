package com.canteen.merchant.ui;

import com.canteen.merchant.dto.MerchantUser;
import com.canteen.merchant.ui.panel.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 商户主界面
 */
@Slf4j
public class MainFrame extends JFrame {

    private final MerchantUser currentUser;
    private JTabbedPane tabbedPane;
    
    // 各功能面板
    private DashboardPanel dashboardPanel;
    private ProductManagementPanel productPanel;
    private OrderManagementPanel orderPanel;
    private FinancePanel financePanel;
    private ProfilePanel profilePanel;

    public MainFrame(MerchantUser user) {
        this.currentUser = user;
        initComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initComponents() {
        setTitle("食堂管理系统 - 商户管理端 [" + currentUser.getUsername() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 创建各功能面板
        dashboardPanel = new DashboardPanel(currentUser);
        productPanel = new ProductManagementPanel(currentUser);
        orderPanel = new OrderManagementPanel(currentUser);
        financePanel = new FinancePanel(currentUser);
        profilePanel = new ProfilePanel(currentUser);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 顶部工具栏
        JToolBar toolBar = createToolBar();
        add(toolBar, BorderLayout.NORTH);

        // 添加选项卡
        tabbedPane.addTab("首页概览", new ImageIcon(), dashboardPanel, "查看今日营业概况");
        tabbedPane.addTab("菜品管理", new ImageIcon(), productPanel, "管理菜品信息");
        tabbedPane.addTab("订单管理", new ImageIcon(), orderPanel, "处理订单");
        tabbedPane.addTab("财务统计", new ImageIcon(), financePanel, "查看收入统计");
        tabbedPane.addTab("商户信息", new ImageIcon(), profilePanel, "管理商户资料");

        add(tabbedPane, BorderLayout.CENTER);

        // 底部状态栏
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // 刷新按钮
        JButton refreshButton = new JButton("刷新");
        refreshButton.setIcon(new ImageIcon()); // 可以添加图标
        refreshButton.addActionListener(e -> refreshCurrentPanel());
        toolBar.add(refreshButton);

        toolBar.addSeparator();

        // 设置按钮
        JButton settingsButton = new JButton("设置");
        settingsButton.addActionListener(e -> showSettings());
        toolBar.add(settingsButton);

        toolBar.addSeparator();

        // 退出按钮
        JButton logoutButton = new JButton("退出登录");
        logoutButton.addActionListener(e -> logout());
        toolBar.add(logoutButton);

        return toolBar;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

        JLabel statusLabel = new JLabel("就绪");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        statusBar.add(statusLabel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel();
        timeLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        statusBar.add(timeLabel, BorderLayout.EAST);

        // 定时更新时间
        Timer timer = new Timer(1000, e -> {
            timeLabel.setText(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });
        timer.start();

        return statusBar;
    }

    private void setupEventHandlers() {
        // 选项卡切换事件
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            refreshPanelByIndex(selectedIndex);
        });
    }

    private void refreshCurrentPanel() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        refreshPanelByIndex(selectedIndex);
    }

    private void refreshPanelByIndex(int index) {
        try {
            switch (index) {
                case 0:
                    dashboardPanel.refresh();
                    break;
                case 1:
                    productPanel.refresh();
                    break;
                case 2:
                    orderPanel.refresh();
                    break;
                case 3:
                    financePanel.refresh();
                    break;
                case 4:
                    profilePanel.refresh();
                    break;
            }
        } catch (Exception e) {
            log.error("刷新面板失败", e);
            JOptionPane.showMessageDialog(this, 
                "刷新失败：" + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSettings() {
        // 显示设置对话框
        JOptionPane.showMessageDialog(this, "设置功能开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int result = JOptionPane.showConfirmDialog(this, 
            "确定要退出登录吗？", 
            "确认", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}