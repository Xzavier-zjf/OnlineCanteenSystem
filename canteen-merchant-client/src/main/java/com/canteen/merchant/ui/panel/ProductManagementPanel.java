package com.canteen.merchant.ui.panel;

import com.canteen.merchant.dto.MerchantUser;
import com.canteen.merchant.model.Product;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 菜品管理面板
 */
@Slf4j
public class ProductManagementPanel extends JPanel {
    
    private final MerchantUser currentUser;
    
    // 表格组件
    private JTable productTable;
    private DefaultTableModel tableModel;
    
    // 按钮组件
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    
    // 搜索组件
    private JTextField searchField;
    private JButton searchButton;
    
    public ProductManagementPanel(MerchantUser user) {
        this.currentUser = user;
        initComponents();
        setupLayout();
        setupEventHandlers();
        refresh();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建表格
        String[] columnNames = {"ID", "菜品名称", "价格", "分类", "库存", "状态", "创建时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        productTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        
        // 创建按钮
        addButton = new JButton("添加菜品");
        editButton = new JButton("编辑菜品");
        deleteButton = new JButton("删除菜品");
        refreshButton = new JButton("刷新");
        
        // 创建搜索组件
        searchField = new JTextField(20);
        searchButton = new JButton("搜索");
        
        // 设置按钮字体
        Font buttonFont = new Font("微软雅黑", Font.PLAIN, 12);
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        refreshButton.setFont(buttonFont);
        searchButton.setFont(buttonFont);
    }
    
    private void setupLayout() {
        // 顶部搜索栏
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("搜索:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);
        
        // 中间表格
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // 底部按钮栏
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // 添加菜品
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddProductDialog();
            }
        });
        
        // 编辑菜品
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = productTable.getSelectedRow();
                if (selectedRow >= 0) {
                    showEditProductDialog(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(ProductManagementPanel.this, 
                        "请选择要编辑的菜品", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        // 删除菜品
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = productTable.getSelectedRow();
                if (selectedRow >= 0) {
                    deleteProduct(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(ProductManagementPanel.this, 
                        "请选择要删除的菜品", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        // 刷新
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        
        // 搜索
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // 回车搜索
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
    }
    
    private void showAddProductDialog() {
        ProductEditDialog dialog = new ProductEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                currentUser);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            refresh(); // 刷新表格
        }
    }
    
    private void showEditProductDialog(int selectedRow) {
        // 获取选中行的数据
        Object[] rowData = new Object[tableModel.getColumnCount()];
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            rowData[i] = tableModel.getValueAt(selectedRow, i);
        }
        
        Product product = new Product(); // Placeholder
        ProductEditDialog dialog = new ProductEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                product, currentUser);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            refresh(); // 刷新表格
        }
    }
    
    private void deleteProduct(int selectedRow) {
        int result = JOptionPane.showConfirmDialog(this, 
            "确定要删除选中的菜品吗？", 
            "确认删除", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                // 获取菜品ID
                Long productId = (Long) tableModel.getValueAt(selectedRow, 0);
                
                // 调用删除服务
                // productService.deleteProduct(productId, currentUser.getId());
                
                JOptionPane.showMessageDialog(this, "删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } catch (Exception e) {
                log.error("删除菜品失败", e);
                JOptionPane.showMessageDialog(this, 
                    "删除失败：" + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void performSearch() {
        String keyword = searchField.getText().trim();
        // 实现搜索逻辑
        JOptionPane.showMessageDialog(this, "搜索功能开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void refresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 清空表格
                    SwingUtilities.invokeLater(() -> tableModel.setRowCount(0));
                    
                    // 模拟数据（实际应该调用服务获取数据）
                    Object[][] sampleData = {
                        {1L, "宫保鸡丁", "18.00", "川菜", 50, "上架", "2024-01-01 10:00:00"},
                        {2L, "麻婆豆腐", "12.00", "川菜", 30, "上架", "2024-01-01 10:30:00"},
                        {3L, "红烧肉", "25.00", "家常菜", 20, "下架", "2024-01-01 11:00:00"}
                    };
                    
                    SwingUtilities.invokeLater(() -> {
                        for (Object[] row : sampleData) {
                            tableModel.addRow(row);
                        }
                    });
                    
                } catch (Exception e) {
                    log.error("刷新菜品列表失败", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(ProductManagementPanel.this, 
                            "刷新失败：" + e.getMessage(), 
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