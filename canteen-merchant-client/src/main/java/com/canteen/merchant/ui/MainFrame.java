package com.canteen.merchant.ui;

import com.canteen.merchant.service.ApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

/**
 * 商户端主窗口
 */
public class MainFrame extends JFrame {
    
    private final ApiService apiService;
    private final ObjectMapper objectMapper;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, priceField, stockField, descField;
    private JComboBox<String> categoryComboBox;
    
    public MainFrame() {
        this.apiService = new ApiService();
        this.objectMapper = new ObjectMapper();
        
        initComponents();
        loadProducts();
        loadCategories();
    }
    
    private void initComponents() {
        setTitle("高校食堂订餐系统 - 商户端管理");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 顶部面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("系统信息"));
        topPanel.add(new JLabel("欢迎使用商户端管理系统"));
        
        JButton refreshButton = new JButton("刷新数据");
        refreshButton.addActionListener(e -> {
            loadProducts();
            loadCategories();
        });
        topPanel.add(refreshButton);
        
        // 中间面板 - 餐品列表
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("餐品管理"));
        
        // 表格
        String[] columnNames = {"ID", "名称", "价格", "分类", "库存", "销量", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 设置表格不可编辑
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 底部面板 - 餐品操作
        JPanel bottomPanel = createProductFormPanel();
        
        // 组装主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createProductFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("餐品信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 餐品名称
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("餐品名称:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        panel.add(nameField, gbc);
        
        // 价格
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("价格:"), gbc);
        gbc.gridx = 3;
        priceField = new JTextField(10);
        panel.add(priceField, gbc);
        
        // 分类
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("分类:"), gbc);
        gbc.gridx = 1;
        categoryComboBox = new JComboBox<>();
        panel.add(categoryComboBox, gbc);
        
        // 库存
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("库存:"), gbc);
        gbc.gridx = 3;
        stockField = new JTextField(10);
        panel.add(stockField, gbc);
        
        // 描述
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("描述:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        descField = new JTextField(30);
        panel.add(descField, gbc);
        
        // 按钮面板
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton addButton = new JButton("添加餐品");
        addButton.addActionListener(new AddProductAction());
        buttonPanel.add(addButton);
        
        JButton updateButton = new JButton("更新餐品");
        updateButton.addActionListener(new UpdateProductAction());
        buttonPanel.add(updateButton);
        
        JButton deleteButton = new JButton("删除餐品");
        deleteButton.addActionListener(new DeleteProductAction());
        buttonPanel.add(deleteButton);
        
        JButton clearButton = new JButton("清空表单");
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);
        
        panel.add(buttonPanel, gbc);
        
        // 表格选择监听器
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });
        
        return panel;
    }
    
    private void loadProducts() {
        try {
            String response = apiService.get("/api/product/list?current=1&size=100");
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.get("code").asInt() == 200) {
                JsonNode data = responseNode.get("data");
                JsonNode records = data.get("records");
                
                // 清空现有数据
                tableModel.setRowCount(0);
                
                // 添加新数据
                for (JsonNode product : records) {
                    Object[] row = {
                        product.get("id").asLong(),
                        product.get("name").asText(),
                        product.get("price").asDouble(),
                        product.get("categoryId").asLong(),
                        product.get("stock").asInt(),
                        product.get("sales").asInt(),
                        product.get("status").asInt() == 1 ? "上架" : "下架"
                    };
                    tableModel.addRow(row);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "获取餐品列表失败: " + responseNode.get("message").asText(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "获取餐品列表失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadCategories() {
        try {
            String response = apiService.get("/api/product/categories");
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.get("code").asInt() == 200) {
                categoryComboBox.removeAllItems();
                categoryComboBox.addItem("请选择分类");
                
                JsonNode categories = responseNode.get("data");
                for (JsonNode category : categories) {
                    String item = category.get("id").asLong() + " - " + category.get("name").asText();
                    categoryComboBox.addItem(item);
                }
            }
        } catch (Exception e) {
            System.err.println("加载分类失败: " + e.getMessage());
        }
    }
    
    private void fillFormFromSelection() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            priceField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            stockField.setText(tableModel.getValueAt(selectedRow, 4).toString());
            // 这里可以添加更多字段的填充
        }
    }
    
    private void clearForm() {
        nameField.setText("");
        priceField.setText("");
        stockField.setText("");
        descField.setText("");
        categoryComboBox.setSelectedIndex(0);
        productTable.clearSelection();
    }
    
    // 添加餐品动作
    private class AddProductAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 简化实现：显示提示信息
            JOptionPane.showMessageDialog(MainFrame.this, 
                "添加餐品功能需要连接后端API实现",
                "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // 更新餐品动作
    private class UpdateProductAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(MainFrame.this, 
                "更新餐品功能需要连接后端API实现",
                "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // 删除餐品动作
    private class DeleteProductAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow >= 0) {
                int result = JOptionPane.showConfirmDialog(MainFrame.this,
                    "确定要删除选中的餐品吗？",
                    "确认删除", JOptionPane.YES_NO_OPTION);
                    
                if (result == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(MainFrame.this, 
                        "删除餐品功能需要连接后端API实现",
                        "提示", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(MainFrame.this, 
                    "请先选择要删除的餐品",
                    "提示", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}