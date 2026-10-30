package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class UI_TrangChu extends JFrame implements ActionListener {
    private Color bgMain = new Color(245, 247, 250);      
    private Color bgWhite = Color.WHITE;                  
    private Color textDark = new Color(30, 30, 30);       
    private Color textGray = new Color(130, 130, 130);    
    private Color colorBlue = new Color(37, 99, 235);     
    private Color bgHover = new Color(239, 246, 255);     

    // Đã đổi btnCaLamViec thành btnHoaDon
    private MenuButton btnTongQuan, btnSanPham, btnKhachHang, btnBanHang, btnKhuyenMai, btnNhanVien, btnHoaDon, btnBaoHanh, btnBaoCao;
    
    private JPanel pnlCards; 
    private CardLayout cardLayout;
    private JPanel pnlBreadcrumb;
    private JLabel lblPageTitle; 

    class HiDPIIcon implements Icon {
        private Image image;
        private int width, height;

        public HiDPIIcon(Image image, int width, int height) {
            this.image = image;
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            java.awt.image.BufferedImage tinted = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D tg = tinted.createGraphics();
            tg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            tg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            tg.drawImage(image, 0, 0, width, height, null);
            tg.setComposite(AlphaComposite.SrcAtop);
            tg.setColor(c.getForeground());
            tg.fillRect(0, 0, width, height);
            tg.dispose();

            g2.drawImage(tinted, x, y, null);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return width; }
        @Override
        public int getIconHeight() { return height; }
    }

    class MenuButton extends JButton {
        public boolean isActive = false; 

        public MenuButton(String text, String iconPath) {
            super(text); 
            try {
                java.net.URL imgURL = getClass().getResource(iconPath);
                if (imgURL != null) {
                    Image originalImage = new ImageIcon(imgURL).getImage();
                    setIcon(new HiDPIIcon(originalImage, 20, 20));
                }
            } catch (Exception e) {}
            
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(textGray);
            setBackground(bgWhite);
            setFocusPainted(false);
            setContentAreaFilled(false); 
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(10, 20, 10, 10));
            setIconTextGap(15); 
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { 
                    if (!isActive) { setBackground(bgHover); setForeground(colorBlue); repaint(); }
                }
                public void mouseExited(MouseEvent e) { 
                    if (!isActive) { setBackground(bgWhite); setForeground(textGray); repaint(); }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(10, 0, getWidth() - 20, getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public UI_TrangChu() {
        setTitle("Hệ Thống Quản Lý - Gia Thành");
        setSize(1300, 680); 
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. SIDEBAR
        JPanel pnlSidebar = new JPanel(new BorderLayout());
        pnlSidebar.setBackground(bgWhite);
        pnlSidebar.setPreferredSize(new Dimension(200, 0)); 
        pnlSidebar.setBorder(new MatteBorder(0, 0, 0, 1, new Color(230, 230, 230))); 

        JPanel pnlLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 20));
        pnlLogo.setBackground(bgWhite);
        pnlLogo.setBorder(new EmptyBorder(0, 20, 0, 0));
        
        JLabel lblLogoIcon = new JLabel(); 
        try {
            java.net.URL logoURL = getClass().getResource("/icon/ic_logo.png"); 
            if (logoURL != null) {
                Image imgLogo = new ImageIcon(logoURL).getImage();
                Image scaledLogo = imgLogo.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
                lblLogoIcon.setIcon(new ImageIcon(scaledLogo));
            }
        } catch (Exception e) {}

        JLabel lblLogoTitle = new JLabel("GIA THÀNH");
        lblLogoTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogoTitle.setForeground(colorBlue);
        
        pnlLogo.add(lblLogoIcon);
        pnlLogo.add(lblLogoTitle);
        pnlSidebar.add(pnlLogo, BorderLayout.NORTH);

        JPanel pnlMenuWrap = new JPanel(new BorderLayout());
        pnlMenuWrap.setBackground(bgWhite);
        pnlMenuWrap.setBorder(new EmptyBorder(15, 0, 0, 0)); 

        JPanel pnlMenuTop = new JPanel(new BorderLayout(0, 5)); 
        pnlMenuTop.setBackground(bgWhite);
        
        JLabel lblCategory = new JLabel("   QUẢN LÝ KINH DOANH");
        lblCategory.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblCategory.setForeground(new Color(180, 180, 180));
        pnlMenuTop.add(lblCategory, BorderLayout.NORTH);

        JPanel pnlMenu = new JPanel(new GridLayout(9, 1, 0, 2)); 
        pnlMenu.setBackground(bgWhite);

        btnTongQuan = new MenuButton("Tổng quan", "/icon/ic_tongquan.png");
        btnSanPham = new MenuButton("Sản phẩm", "/icon/ic_sanpham.png");
        btnKhachHang = new MenuButton("Khách hàng", "/icon/ic_khachhang.png");
        btnBanHang = new MenuButton("Bán hàng", "/icon/ic_banhang.png");
        btnKhuyenMai = new MenuButton("Khuyến mãi", "/icon/ic_khuyenmai.png");
        btnNhanVien = new MenuButton("Nhân viên", "/icon/ic_nhanvien.png");
        btnHoaDon = new MenuButton("Hóa đơn", "/icon/ic_hoadon.png"); // Đã đổi từ Ca làm việc sang Hóa đơn
        btnBaoHanh = new MenuButton("Bảo hành", "/icon/ic_baohanh.png");
        btnBaoCao = new MenuButton("Báo cáo", "/icon/ic_baocao.png");
        
        btnTongQuan.isActive = true;
        btnTongQuan.setForeground(colorBlue);
        btnTongQuan.setBackground(bgHover);

        pnlMenu.add(btnTongQuan);
        pnlMenu.add(btnSanPham);
        pnlMenu.add(btnKhachHang);
        pnlMenu.add(btnBanHang);
        pnlMenu.add(btnKhuyenMai);
        pnlMenu.add(btnNhanVien);
        pnlMenu.add(btnHoaDon);
        pnlMenu.add(btnBaoHanh);
        pnlMenu.add(btnBaoCao);
        
        pnlMenuTop.add(pnlMenu, BorderLayout.CENTER);
        pnlMenuWrap.add(pnlMenuTop, BorderLayout.NORTH); 
        pnlSidebar.add(pnlMenuWrap, BorderLayout.CENTER);

        add(pnlSidebar, BorderLayout.WEST);

        // 2. MAIN AREA
        JPanel pnlMainArea = new JPanel(new BorderLayout());
        pnlMainArea.setBackground(bgMain);

        JPanel pnlTopBar = new JPanel(new BorderLayout());
        pnlTopBar.setBackground(bgWhite);
        pnlTopBar.setPreferredSize(new Dimension(0, 85)); 
        pnlTopBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        
        JPanel pnlTopLeft = new JPanel(new BorderLayout(0, 2));
        pnlTopLeft.setBackground(bgWhite);
        pnlTopLeft.setBorder(new EmptyBorder(15, 25, 10, 10)); 
        
        pnlBreadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBreadcrumb.setBackground(bgWhite);
        
        lblPageTitle = new JLabel("Tổng quan hệ thống");
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblPageTitle.setForeground(textDark);
        
        pnlTopLeft.add(pnlBreadcrumb, BorderLayout.NORTH);
        pnlTopLeft.add(lblPageTitle, BorderLayout.CENTER);
        pnlTopBar.add(pnlTopLeft, BorderLayout.WEST);
        
        // Khu vực thông tin User bên phải
        JPanel pnlUserInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        pnlUserInfo.setBackground(bgWhite);
        pnlUserInfo.setBorder(new EmptyBorder(0, 0, 0, 30));
        
        JLabel lblBell = new JLabel();
        lblBell.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        try {
            java.net.URL bellURL = getClass().getResource("/icon/ic_thongbao.png"); 
            if (bellURL != null) {
                Image imgBell = new ImageIcon(bellURL).getImage();
                Image scaledBell = imgBell.getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                lblBell.setIcon(new ImageIcon(scaledBell));
            } 
        } catch (Exception e) {}
        
        JLabel lblAvatar = new JLabel("NA", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g); 
            }
        };
        lblAvatar.setPreferredSize(new Dimension(35, 35));
        lblAvatar.setOpaque(false); 
        lblAvatar.setBackground(bgHover);
        lblAvatar.setForeground(colorBlue);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JPanel pnlNameRole = new JPanel(new GridLayout(2, 1));
        pnlNameRole.setBackground(bgWhite);
        JLabel lblName = new JLabel("Nguyễn Văn A");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblRole = new JLabel("Quản trị viên");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(textGray);
        pnlNameRole.add(lblName);
        pnlNameRole.add(lblRole);
        
        pnlUserInfo.add(lblBell);
        pnlUserInfo.add(Box.createHorizontalStrut(10));
        pnlUserInfo.add(lblAvatar);
        pnlUserInfo.add(pnlNameRole);
        pnlTopBar.add(pnlUserInfo, BorderLayout.EAST);
        pnlMainArea.add(pnlTopBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        pnlCards = new JPanel(cardLayout);
        pnlCards.setBackground(bgMain);
        
        pnlCards.add(createTongQuanPanel(), "TongQuan"); 
        pnlCards.add(new UI_SanPham(), "SanPham"); 
        pnlCards.add(new UI_KhachHang(), "KhachHang");
//        pnlCards.add(new PNL_BanHang(), "BanHang");
//        pnlCards.add(new PNL_KhuyenMai(), "KhuyenMai");
//        pnlCards.add(new PNL_NhanVien(), "NhanVien");
//        pnlCards.add(new PNL_HoaDon(), "HoaDon");
//        pnlCards.add(new PNL_BaoHanh(), "BaoHanh");
        
        
        btnTongQuan.addActionListener(this);
        btnSanPham.addActionListener(this);
        btnKhachHang.addActionListener(this);
        btnBanHang.addActionListener(this);     
        btnKhuyenMai.addActionListener(this);
        btnNhanVien.addActionListener(this);
        btnHoaDon.addActionListener(this);
        btnBaoHanh.addActionListener(this);
        btnBaoCao.addActionListener(this);
        
        pnlMainArea.add(pnlCards, BorderLayout.CENTER);
        add(pnlMainArea, BorderLayout.CENTER);
        
        chuyenTrang(new String[][]{{"Tổng quan", "TongQuan", "Tổng quan hệ thống"}});
    }

    private JPanel createTongQuanPanel() {
        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setBackground(bgMain);
        
        JLabel lblHint = new JLabel("Màn hình Tổng quan đang trống. Hãy bấm menu Sản phẩm bên trái!");
        lblHint.setHorizontalAlignment(SwingConstants.CENTER);
        lblHint.setForeground(Color.GRAY);
        pnlContent.add(lblHint, BorderLayout.CENTER);
        
        return pnlContent;
    }

    private JPanel createEmptyPanel(String msg) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(bgMain);
        JLabel lbl = new JLabel(msg, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lbl.setForeground(textGray);
        pnl.add(lbl, BorderLayout.CENTER);
        return pnl;
    }
    
    public void chuyenTrang(String[][] path) {
        pnlBreadcrumb.removeAll(); 
        
        for (int i = 0; i < path.length; i++) {
            String tenTrang = path[i][0];
            String tenCard = path[i][1];
            String tieuDeChinh = path[i][2]; 
            
            JLabel lbl = new JLabel(tenTrang);
            if (i == path.length - 1) { 
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lbl.setForeground(textGray);
                pnlBreadcrumb.add(lbl);
                
                lblPageTitle.setText(tieuDeChinh);
            } else { 
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lbl.setForeground(colorBlue);
                lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                final int index = i;
                lbl.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        String[][] newPath = new String[index + 1][3];
                        for (int j = 0; j <= index; j++) {
                            newPath[j] = path[j];
                        }
                        chuyenTrang(newPath);
                    }
                });
                pnlBreadcrumb.add(lbl);
                
                JLabel lblSeparator = new JLabel(" > ");
                lblSeparator.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lblSeparator.setForeground(textGray);
                pnlBreadcrumb.add(lblSeparator);
            }
        }
        
        cardLayout.show(pnlCards, path[path.length - 1][1]);
        pnlBreadcrumb.revalidate();
        pnlBreadcrumb.repaint();
        pnlCards.revalidate();
        pnlCards.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        MenuButton[] btns = {btnTongQuan, btnSanPham, btnKhachHang, btnBanHang, btnKhuyenMai, btnNhanVien, btnHoaDon, btnBaoHanh, btnBaoCao};
        for(MenuButton btn : btns) {
            if(btn != null) {
                btn.isActive = false;
                btn.setBackground(bgWhite);
                btn.setForeground(textGray);
            }
        }
        
        MenuButton clickedBtn = (MenuButton) source;
        clickedBtn.isActive = true;
        clickedBtn.setBackground(bgHover);
        clickedBtn.setForeground(colorBlue);

        if (source == btnTongQuan) {
            chuyenTrang(new String[][]{{"Tổng quan", "TongQuan", "Tổng quan hệ thống"}});
        } else if (source == btnSanPham) {
            chuyenTrang(new String[][]{{"Sản phẩm", "SanPham", "Tra cứu sản phẩm"}});
        } else if (source == btnKhachHang) {
            chuyenTrang(new String[][]{{"Khách hàng", "KhachHang", "Quản lý khách hàng"}});
        } else if (source == btnBanHang) {
            chuyenTrang(new String[][]{{"Bán hàng", "BanHang", "Quản lý bán hàng tại quầy"}});
        } else if (source == btnKhuyenMai) {
            chuyenTrang(new String[][]{{"Khuyến mãi", "KhuyenMai", "Chương trình khuyến mãi"}});
        } else if (source == btnNhanVien) {
            chuyenTrang(new String[][]{{"Nhân viên", "NhanVien", "Quản lý nhân sự"}});
        } else if (source == btnHoaDon) {
            chuyenTrang(new String[][]{{"Hóa đơn", "HoaDon", "Quản lý hóa đơn"}});
        } else if (source == btnBaoHanh) {
            chuyenTrang(new String[][]{{"Bảo hành", "BaoHanh", "Tiếp nhận bảo hành & Sửa chữa"}});
        } else if (source == btnBaoCao) {
            chuyenTrang(new String[][]{{"Báo cáo", "BaoCao", "Thống kê doanh thu & Báo cáo"}});
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new UI_TrangChu().setVisible(true));
    }
}