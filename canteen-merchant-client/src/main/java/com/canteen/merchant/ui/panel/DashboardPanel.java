package com.canteen.merchant.ui.panel;

import com.canteen.merchant.dto.MerchantUser;
import com.canteen.merchant.service.MerchantOrderService;
import com.canteen.merchant.service.impl.MerchantOrderServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商户首页概览面板
 */
@Slf4j
public class DashboardPanel extends JPanel {
    
    private final MerchantUser currentUser;
    private final MerchantOrderService orderService;
    
    // 统计卡片
    private JLabel pendingOrdersLabel;
    private JLabel todayRevenueLabel;
    private JLabel totalOrdersLabel;
    private JLabel monthRevenueLabel;
    
    // 图表面板
    private JPanel chartPanel;
    
    public DashboardPanel(MerchantUser user) {
        this.currentUser = user;
        this.orderService = new MerchantOrderServiceImpl();
        initComponents();
        setupLayout();
        refresh();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 创建统计卡片
        pendingOrdersLabel = new JLabel("0");
        todayRevenueLabel = new JLabel("¥0.00");
        totalOrdersLabel = new JLabel("0");
        monthRevenueLabel = new JLabel("¥0.00");
        
        // 设置字体
        Font valueFont = new Font("微软雅黑", Font.BOLD, 24);
        pendingOrdersLabel.setFont(valueFont);
        todayRevenueLabel.setFont(valueFont);
        totalOrdersLabel.setFont(valueFont);
        monthRevenueLabel.setFont(valueFont);
    }
    
    private void setupLayout() {
        // 顶部统计卡片区域
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.NORTH);
        
        // 中间图表区域
        chartPanel = createChartPanel();
        add(chartPanel, BorderLayout.CENTER);
        
        // 底部快捷操作区域
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // 待处理订单
        JPanel pendingCard = createStatCard("待处理订单", pendingOrdersLabel, Color.ORANGE);
        panel.add(pendingCard);
        
        // 今日营业额
        JPanel todayCard = createStatCard("今日营业额", todayRevenueLabel, Color.GREEN);
        panel.add(todayCard);
        
        // 订单总数
        JPanel totalCard = createStatCard("订单总数", totalOrdersLabel, Color.BLUE);
        panel.add(totalCard);
        
        // 本月营业额
        JPanel monthCard = createStatCard("本月营业额", monthRevenueLabel, Color.MAGENTA);
        panel.add(monthCard);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("营业趋势"));
        
        // 这里可以添加图表组件，暂时用文本显示
        JTextArea chartArea = new JTextArea();
        chartArea.setEditable(false);
        chartArea.setText("营业趋势图表\n（图表功能开发中...）");
        chartArea.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        chartArea.setBackground(getBackground());
        
        JScrollPane scrollPane = new JScrollPane(chartArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("快捷操作"));
        
        JButton addProductButton = new JButton("添加菜品");
        JButton viewOrdersButton = new JButton("查看订单");
        JButton viewFinanceButton = new JButton("财务统计");
        
        // 设置按钮样式
        Font buttonFont = new Font("微软雅黑", Font.PLAIN, 14);
        addProductButton.setFont(buttonFont);
        viewOrdersButton.setFont(buttonFont);
        viewFinanceButton.setFont(buttonFont);
        
        // 添加事件处理
        addProductButton.addActionListener(e -> {
            // 切换到菜品管理页面
            JTabbedPane parent = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
            if (parent != null) {
                parent.setSelectedIndex(1); // 菜品管理页面
            }
        });
        
        viewOrdersButton.addActionListener(e -> {
            // 切换到订单管理页面
            JTabbedPane parent = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
            if (parent != null) {
                parent.setSelectedIndex(2); // 订单管理页面
            }
        });
        
        viewFinanceButton.addActionListener(e -> {
            // 切换到财务统计页面
            JTabbedPane parent = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
            if (parent != null) {
                parent.setSelectedIndex(3); // 财务统计页面
            }
        });
        
        panel.add(addProductButton);
        panel.add(viewOrdersButton);
        panel.add(viewFinanceButton);
        
        return panel;
    }
    
    public void refresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 获取待处理订单数
                    Long pendingCount = orderService.getPendingOrderCount(currentUser.getId());
                    SwingUtilities.invokeLater(() -> 
                        pendingOrdersLabel.setText(String.valueOf(pendingCount)));
                    
                    // 获取今日营业额
                    BigDecimal todayRevenue = orderService.getTodayRevenue(currentUser.getId());
                    SwingUtilities.invokeLater(() -> 
                        todayRevenueLabel.setText("¥" + todayRevenue.toString()));
                    
                    // 获取订单总数
                    Long totalOrders = orderService.getTotalOrderCount(currentUser.getId());
                    SwingUtilities.invokeLater(() -> 
                        totalOrdersLabel.setText(String.valueOf(totalOrders)));
                    
                    // 获取本月营业额（暂时用今日营业额 * 30 模拟）
                    BigDecimal monthRevenue = todayRevenue.multiply(new BigDecimal("30"));
                    SwingUtilities.invokeLater(() -> 
                        monthRevenueLabel.setText("¥" + monthRevenue.toString()));
                    
                } catch (Exception e) {
                    log.error("刷新首页数据失败", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(DashboardPanel.this, 
                            "刷新数据失败：" + e.getMessage(), 
                            "错误", 
                            JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }
        };
        
        worker.execute();
    }
}