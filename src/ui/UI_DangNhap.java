package ui;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class UI_DangNhap extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;
//    private TaiKhoanDAO taiKhoanDAO;

    // Lớp Panel bo góc cho Form Đăng Nhập
    class RoundedPanel extends JPanel {
        private int cornerRadius = 30;

        public RoundedPanel(int radius) {
            super();
            cornerRadius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Vẽ nền trắng
            graphics.setColor(Color.WHITE);
            graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
            
            // Viền nhẹ
            graphics.setColor(new Color(230, 230, 230));
            graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        }
    }

    // Nút đăng nhập bo góc
    class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setForeground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Màu xanh tím nhạt như trong ảnh
            if (getModel().isRollover()) {
                g2.setColor(new Color(60, 50, 120));
            } else {
                g2.setColor(new Color(45, 38, 100)); // Màu nút đăng nhập
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public UI_DangNhap() {
//        taiKhoanDAO = new TaiKhoanDAO();
        setTitle("Đăng nhập - Điện Máy Gia Thành");
        setSize(950, 600); // Kích thước rộng hơn để chứa 2 phần
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Panel chính chứa toàn bộ giao diện, màu nền xanh dương rất nhạt
        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(new Color(235, 245, 255));
        setContentPane(mainPanel);

        // --- PHẦN BÊN TRÁI: ẢNH GIỚI THIỆU ---
        // Giả lập bằng một JLabel chứa ảnh (Bạn cần thay "images/left_banner.jpg" bằng ảnh thực tế)
        JLabel lblLeftBanner = new JLabel();
        lblLeftBanner.setBounds(20, 20, 420, 520);
         lblLeftBanner.setIcon(new ImageIcon(new ImageIcon("image/banner.jpg").getImage().getScaledInstance(420, 520, Image.SCALE_SMOOTH)));
        lblLeftBanner.setOpaque(true);
//        lblLeftBanner.setBackground(Color.WHITE); // Thay tạm bằng nền trắng nếu chưa có ảnh
        mainPanel.add(lblLeftBanner);

        // --- PHẦN BÊN PHẢI: THỜI GIAN & FORM ĐĂNG NHẬP ---
        
        // 1. Hiển thị thời gian góc trên bên phải
        JLabel lblTime = new JLabel();
        lblTime.setBounds(650, 20, 250, 30);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTime.setForeground(new Color(50, 50, 100));
        
        // Cập nhật giờ (Giả lập, bạn có thể dùng Timer để chạy thật)
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy | HH:mm");
        lblTime.setText(sdf.format(new Date()));
        mainPanel.add(lblTime);

        // 2. Form đăng nhập chính
        RoundedPanel pnlForm = new RoundedPanel(40); // Bo góc 40
        pnlForm.setBounds(500, 70, 400, 470);
        pnlForm.setLayout(null);
        mainPanel.add(pnlForm);

        // Tiêu đề form
        JLabel lblFormTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG", JLabel.CENTER);
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblFormTitle.setForeground(new Color(30, 40, 120)); // Xanh đậm
        lblFormTitle.setBounds(0, 40, 400, 35);
        pnlForm.add(lblFormTitle);

        JLabel lblSubTitle = new JLabel("Chào mừng bạn đến với Điện máy Gia Thành", JLabel.CENTER);
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubTitle.setForeground(new Color(30, 40, 120));
        lblSubTitle.setBounds(0, 75, 400, 20);
        pnlForm.add(lblSubTitle);

        // Label Tài khoản
        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(new Color(30, 80, 150));
        lblUser.setBounds(40, 140, 320, 20);
        // Có thể thêm icon nhỏ phía trước label nếu cần
        pnlForm.add(lblUser);

        // Textfield Tài khoản
        txtUser = new JTextField();
        txtUser.setBounds(40, 170, 320, 40);
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUser.setBackground(new Color(245, 245, 245)); // Nền xám nhạt
        txtUser.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true), 
            new EmptyBorder(5, 10, 5, 10)
        ));
        pnlForm.add(txtUser);

        // Label Mật khẩu
        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(new Color(30, 80, 150));
        lblPass.setBounds(40, 240, 320, 20);
        pnlForm.add(lblPass);

        // Textfield Mật khẩu
        txtPass = new JPasswordField();
        txtPass.setBounds(40, 270, 320, 40);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setBackground(new Color(245, 245, 245));
        txtPass.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true), 
            new EmptyBorder(5, 10, 5, 10)
        ));
        pnlForm.add(txtPass);

        // Quên mật khẩu
        JLabel lblForgot = new JLabel("Quên mật khẩu?");
        lblForgot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblForgot.setForeground(new Color(50, 80, 180));
        lblForgot.setBounds(250, 320, 110, 20);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlForm.add(lblForgot);

        // Nút Đăng Nhập
        btnLogin = new RoundedButton("Đăng nhập");
        btnLogin.setBounds(40, 360, 320, 45);
        pnlForm.add(btnLogin);

//        // Xử lý sự kiện
//        btnLogin.addActionListener(this);
//        txtPass.addActionListener(e -> btnLogin.doClick());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> {
            new UI_DangNhap().setVisible(true);
        });
    }
}