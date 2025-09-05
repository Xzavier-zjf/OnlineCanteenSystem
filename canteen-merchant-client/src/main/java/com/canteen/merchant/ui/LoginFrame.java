package com.canteen.merchant.ui;

import com.canteen.merchant.service.AuthService;
import com.canteen.merchant.service.impl.AuthServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 商户登录界面
 */
@Slf4j
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    
    private final AuthService authService;

    public LoginFrame() {
        this.authService = new AuthServiceImpl();
        initComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initComponents() {
        setTitle("食堂管理系统 - 商户登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        // 创建组件
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("登录");
        exitButton = new JButton("退出");

        // 设置字体
        Font font = new Font("微软雅黑", Font.PLAIN, 14);
        usernameField.setFont(font);
        passwordField.setFont(font);
        loginButton.setFont(font);
        exitButton.setFont(font);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(52, 152, 219));
        JLabel titleLabel = new JLabel("商户管理端");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // 登录表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("用户名:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("密码:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // 登录按钮事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        // 退出按钮事件
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // 回车键登录
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名", "提示", JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入密码", "提示", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        // 显示加载状态
        loginButton.setEnabled(false);
        loginButton.setText("登录中...");

        // 在后台线程执行登录
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        // 登录成功，打开主界面
                        SwingUtilities.invokeLater(() -> {
                            new MainFrame(authService.getCurrentUser()).setVisible(true);
                            dispose();
                        });
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this, 
                            "登录失败，请检查用户名和密码", 
                            "错误", 
                            JOptionPane.ERROR_MESSAGE);
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception e) {
                    log.error("登录异常", e);
                    JOptionPane.showMessageDialog(LoginFrame.this, 
                        "登录异常：" + e.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("登录");
                }
            }
        };

        worker.execute();
    }
}