
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import connect.ConnectDB;
import dao.KhuyenMai_DAO;
import entity.KhuyenMai;

public class PNL_KhuyenMai extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JTable table;
	private DefaultTableModel model;

	private JTextField txtSearch, txtFromDate, txtToDate;
	private JComboBox<String> cbStatus, cbType, cbValue;
	private JButton btnAdd;
	private JLabel lblTableTitle, lblInfo;

	private final KhuyenMai_DAO km_dao = new KhuyenMai_DAO();

	private List<KhuyenMai> dsAll = new ArrayList<>();
	private List<KhuyenMai> dsHienThi = new ArrayList<>();

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final DateTimeFormatter DATE_PARSE = DateTimeFormatter.ofPattern("dd/MM/uuuu")
			.withResolverStyle(ResolverStyle.STRICT);

	private Color bgMain = new Color(245, 247, 250);
	private Color bgWhite = Color.WHITE;
	private Color textDark = new Color(30, 30, 30);
	private Color textGray = new Color(130, 130, 130);
	private Color colorBlue = new Color(37, 99, 235);
	private Color bgHover = new Color(239, 246, 255);

	private JTextField txtBatDau;

	private JTextField txtKetThuc;

	// ==========================================
	// PANEL BO TRÒN
	// ==========================================

	class RoundedPanel extends JPanel {

		private int cornerRadius;

		public RoundedPanel(int radius, Color bgColor) {
			this.cornerRadius = radius;
			setBackground(bgColor);
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setColor(getBackground());
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

			g2.dispose();
			super.paintComponent(g);
		}
	}

	// ==========================================
	// RENDERER ICON XEM VÀ SỬA
	// ==========================================

	class ActionRenderer extends JPanel implements TableCellRenderer {

		private JLabel lblView;
		private JLabel lblEdit;

		public ActionRenderer() {

			setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
			setOpaque(true);

			lblView = new JLabel();
			lblEdit = new JLabel();

			try {
				java.net.URL urlView = getClass().getResource("/icon/ic_xemchitiet.png");

				if (urlView != null) {
					Image img = new ImageIcon(urlView).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);

					lblView.setIcon(new ImageIcon(img));
				}

				java.net.URL urlEdit = getClass().getResource("/icon/ic_chinhsua.png");

				if (urlEdit != null) {
					Image img = new ImageIcon(urlEdit).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);

					lblEdit.setIcon(new ImageIcon(img));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			add(lblView);
			add(lblEdit);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			if (isSelected) {
				setBackground(table.getSelectionBackground());
			} else {
				setBackground(table.getBackground());
			}

			return this;
		}
	}

	// ==========================================
	// CONSTRUCTOR
	// ==========================================

	public PNL_KhuyenMai() {

		ConnectDB.getInstance();

		if (ConnectDB.getConnection() != null) {
			System.out.println("Connected!!!!");
		} else {
			System.out.println("Kết nối database thất bại!");
		}

		setLayout(new BorderLayout(0, 15));
		setBackground(bgMain);
		setBorder(new EmptyBorder(15, 20, 15, 20));

		// ==========================================
		// 1. THANH TÌM KIẾM VÀ LỌC
		// ==========================================

		RoundedPanel pnlFilter = new RoundedPanel(12, bgWhite);

		pnlFilter.setLayout(new BorderLayout(0, 10));
		pnlFilter.setBorder(new EmptyBorder(15, 15, 15, 15));

		JPanel pnlFilterTop = new JPanel(new GridBagLayout());

		pnlFilterTop.setOpaque(false);
		pnlFilterTop.setBorder(new EmptyBorder(0, 0, 5, 0));
		pnlFilterTop.setMinimumSize(new Dimension(0, 60));
		pnlFilterTop.setPreferredSize(new Dimension(0, 60));

		GridBagConstraints gbcTop = new GridBagConstraints();

		gbcTop.fill = GridBagConstraints.BOTH;
		gbcTop.anchor = GridBagConstraints.NORTH;
		gbcTop.insets = new Insets(0, 0, 0, 12);
		gbcTop.weighty = 1.0;

		// Ô tìm kiếm

		JPanel pnlSearchBox = new JPanel(new BorderLayout(5, 0));

		pnlSearchBox.setBackground(Color.WHITE);

		pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(210, 210, 210)), new EmptyBorder(0, 10, 0, 10)));

		JLabel lblSearchIcon = new JLabel();

		try {
			java.net.URL searchURL = getClass().getResource("/icon/ic_timkiem.png");

			if (searchURL != null) {
				Image img = new ImageIcon(searchURL).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);

				lblSearchIcon.setIcon(new ImageIcon(img));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		txtSearch = new JTextField() {

			@Override
			protected void paintComponent(Graphics g) {

				super.paintComponent(g);

				if (getText().isEmpty()) {

					Graphics2D g2 = (Graphics2D) g.create();

					g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

					g2.setColor(Color.GRAY);
					g2.setFont(getFont());

					FontMetrics fm = g2.getFontMetrics();

					g2.drawString("Tìm theo mã hoặc tên khuyến mãi...", getInsets().left + 2,
							(getHeight() - fm.getHeight()) / 2 + fm.getAscent());

					g2.dispose();
				}
			}
		};

		txtSearch.setForeground(Color.GRAY);
		txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		txtSearch.setBorder(null);
		txtSearch.setBackground(Color.WHITE);

		pnlSearchBox.add(lblSearchIcon, BorderLayout.WEST);
		pnlSearchBox.add(txtSearch, BorderLayout.CENTER);

		gbcTop.gridx = 0;
		gbcTop.weightx = 2.5;

		pnlFilterTop.add(createFilterItem(" ", pnlSearchBox, 260), gbcTop);

		gbcTop.weightx = 1.0;

		// Lọc trạng thái

		cbStatus = new JComboBox<>(new String[] { "Tất cả trạng thái", "Đang diễn ra", "Sắp diễn ra", "Đã kết thúc" });

		cbStatus.setBackground(Color.WHITE);

		gbcTop.gridx = 1;

		pnlFilterTop.add(createFilterItem("Trạng thái", cbStatus, 130), gbcTop);

		// Lọc loại khuyến mãi

		cbType = new JComboBox<>(new String[] { "Tất cả", "Giảm theo %", "Giảm tiền mặt", "Tặng sản phẩm" });

		cbType.setBackground(Color.WHITE);

		gbcTop.gridx = 2;

		pnlFilterTop.add(createFilterItem("Loại khuyến mãi", cbType, 130), gbcTop);

		// Lọc giá trị

		cbValue = new JComboBox<>(new String[] { "Tất cả", "Dưới 50k", "50k - 200k", "Trên 200k" });

		cbValue.setBackground(Color.WHITE);

		gbcTop.gridx = 3;

		pnlFilterTop.add(createFilterItem("Giá trị giảm", cbValue, 130), gbcTop);

		// Ngày bắt đầu

		txtFromDate = new JTextField("01/11/2026");
		txtFromDate.setBackground(Color.WHITE);

		gbcTop.gridx = 4;

		pnlFilterTop.add(createFilterItem("Từ ngày", txtFromDate, 110), gbcTop);

		// Ngày kết thúc

		txtToDate = new JTextField("30/11/2026");
		txtToDate.setBackground(Color.WHITE);

		gbcTop.gridx = 5;
		gbcTop.insets = new Insets(0, 0, 0, 0);

		pnlFilterTop.add(createFilterItem("Đến ngày", txtToDate, 110), gbcTop);

		// Nút thêm

		JPanel pnlFilterBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

		pnlFilterBottom.setOpaque(false);

		pnlFilterBottom.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		btnAdd = new JButton("Thêm khuyến mãi");

		try {
			java.net.URL addURL = getClass().getResource("/icon/ic_them.png");

			if (addURL != null) {
				Image img = new ImageIcon(addURL).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);

				btnAdd.setIcon(new ImageIcon(img));
				btnAdd.setIconTextGap(8);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		btnAdd.setBackground(colorBlue);
		btnAdd.setForeground(Color.WHITE);
		btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
		btnAdd.setFocusPainted(false);
		btnAdd.setBorderPainted(false);
		btnAdd.setPreferredSize(new Dimension(175, 36));
		btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

		pnlFilterBottom.add(btnAdd);

		pnlFilter.add(pnlFilterTop, BorderLayout.CENTER);
		pnlFilter.add(pnlFilterBottom, BorderLayout.SOUTH);

		add(pnlFilter, BorderLayout.NORTH);

		// ==========================================
		// 2. BẢNG DANH SÁCH KHUYẾN MÃI
		// ==========================================

		RoundedPanel pnlTableContainer = new RoundedPanel(12, bgWhite);

		pnlTableContainer.setLayout(new BorderLayout());

		pnlTableContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

		lblTableTitle = new JLabel(
				"<html><span style='color:#1e3a8a; font-size:14px;'>" + "<b>Danh sách khuyến mãi</b></span> "
						+ "<span style='color:#6b7280; font-size:11px;'>" + "(Tổng: 0 chương trình)</span></html>");

		lblTableTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

		pnlTableContainer.add(lblTableTitle, BorderLayout.NORTH);

		String[] columns = { "STT", "Mã KM", "Tên chương trình", "Loại KM", "Giá trị giảm", "Thời gian áp dụng",
				"Trạng thái", "Thao tác" };

		model = new DefaultTableModel(columns, 0) {

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table = new JTable(model);

		table.setRowHeight(45);
		table.setShowVerticalLines(false);
		table.setGridColor(new Color(240, 240, 240));
		table.setSelectionBackground(bgHover);
		table.setSelectionForeground(textDark);
		table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

		table.getTableHeader().setBackground(new Color(248, 250, 252));

		table.getTableHeader().setForeground(textDark);

		table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

		table.getTableHeader().setPreferredSize(new Dimension(0, 40));

		table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();

		leftRenderer.setHorizontalAlignment(JLabel.LEFT);

		table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(75);
		table.getColumnModel().getColumn(2).setPreferredWidth(180);
		table.getColumnModel().getColumn(3).setPreferredWidth(120);
		table.getColumnModel().getColumn(4).setPreferredWidth(100);
		table.getColumnModel().getColumn(5).setPreferredWidth(170);
		table.getColumnModel().getColumn(6).setPreferredWidth(120);
		table.getColumnModel().getColumn(7).setPreferredWidth(85);

		table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());

		JScrollPane scrollPane = new JScrollPane(table);

		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		scrollPane.getViewport().setBackground(bgWhite);

		pnlTableContainer.add(scrollPane, BorderLayout.CENTER);

		// ==========================================
		// 3. PHÂN TRANG
		// ==========================================

		JPanel pnlPagination = new JPanel(new BorderLayout());

		pnlPagination.setBackground(bgWhite);

		pnlPagination.setBorder(new EmptyBorder(35, 0, 0, 0));

		lblInfo = new JLabel("Hiển thị 0 trong tổng số 0 chương trình");

		lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

		lblInfo.setForeground(textGray);

		pnlPagination.add(lblInfo, BorderLayout.WEST);

		JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

		pnlPages.setBackground(bgWhite);

		String[] pages = { "<", "1", "2", "3", "4", "5", ">" };

		for (String p : pages) {

			JButton btnPage = new JButton(p);

			btnPage.setFocusPainted(false);

			btnPage.setPreferredSize(new Dimension(32, 32));

			btnPage.setMargin(new Insets(0, 0, 0, 0));

			if (p.equals("1")) {

				btnPage.setBackground(colorBlue);
				btnPage.setForeground(Color.WHITE);
				btnPage.setBorderPainted(false);

			} else {

				btnPage.setBackground(bgWhite);
				btnPage.setForeground(textDark);

				btnPage.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
			}

			pnlPages.add(btnPage);
		}

		pnlPagination.add(pnlPages, BorderLayout.EAST);

		pnlTableContainer.add(pnlPagination, BorderLayout.SOUTH);

		add(pnlTableContainer, BorderLayout.CENTER);

		// ==========================================
		// 4. ĐĂNG KÝ SỰ KIỆN
		// ==========================================

		btnAdd.addActionListener(this);
		cbStatus.addActionListener(this);
		cbType.addActionListener(this);
		cbValue.addActionListener(this);

		txtSearch.addActionListener(this);
		txtFromDate.addActionListener(this);
		txtToDate.addActionListener(this);

		table.addMouseListener(this);

		loadDuLieu();
	}

	// ==========================================
	// TẠO Ô LỌC
	// ==========================================

	private JPanel createFilterItem(String title, JComponent comp, int width) {

		JPanel pnl = new JPanel(new BorderLayout(0, 5));

		pnl.setOpaque(false);

		JLabel lbl = new JLabel(title.trim().isEmpty() ? " " : title);

		lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));

		lbl.setForeground(textDark);

		comp.setPreferredSize(new Dimension(width, 36));

		pnl.add(lbl, BorderLayout.NORTH);
		pnl.add(comp, BorderLayout.CENTER);

		pnl.setMinimumSize(new Dimension(width, 56));

		pnl.setPreferredSize(new Dimension(width, 56));

		return pnl;
	}

	// ==========================================
	// XỬ LÝ SỰ KIỆN
	// ==========================================

	@Override
	public void actionPerformed(ActionEvent e) {

		Object o = e.getSource();

		if (o == btnAdd) {

			themKhuyenMai();

		} else {

			locDuLieu();
		}
	}

	// ==========================================
	// THÊM KHUYẾN MÃI
	// ==========================================

	private void themKhuyenMai() {
		JTextField txtMa = new JTextField();
		JTextField txtTen = new JTextField();
		JComboBox<String> cboLoai = new JComboBox<>(new String[] { "Giảm theo %", "Giảm tiền mặt", "Tặng sản phẩm" });
		JTextField txtGiaTri = new JTextField();
		txtBatDau = new JTextField("");
		txtKetThuc = new JTextField("");
		JComboBox<String> cboTrangThai = new JComboBox<>(
				new String[] { "Chờ kích hoạt", "Đang kích hoạt", "Đã kết thúc", "Đã hủy" });
		JTextField txtPhamVi = new JTextField();
		JTextField txtDoiTuong = new JTextField();

		// Tạo mã tự động

		try {

			txtMa.setText(km_dao.taoMaMoi());

		} catch (SQLException ex) {

			JOptionPane.showMessageDialog(this, "Không tạo được mã khuyến mãi!\n" + ex.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);

			return;
		}

		txtMa.setEditable(false);

		// Tạo form nhập

		JPanel pnl = new JPanel(new GridLayout(0, 2, 8, 8));

		pnl.add(new JLabel("Mã khuyến mãi:"));
		pnl.add(txtMa);

		pnl.add(new JLabel("Tên chương trình:"));
		pnl.add(txtTen);

		pnl.add(new JLabel("Loại khuyến mãi:"));
		pnl.add(cboLoai);

		pnl.add(new JLabel("Giá trị giảm:"));
		pnl.add(txtGiaTri);

		pnl.add(new JLabel("Ngày bắt đầu (dd/MM/yyyy):"));
		pnl.add(txtBatDau);

		pnl.add(new JLabel("Ngày kết thúc (dd/MM/yyyy):"));
		pnl.add(txtKetThuc);

		pnl.add(new JLabel("Trạng thái:"));
		pnl.add(cboTrangThai);

		pnl.add(new JLabel("Phạm vi áp dụng:"));
		pnl.add(txtPhamVi);

		pnl.add(new JLabel("Đối tượng áp dụng:"));
		pnl.add(txtDoiTuong);

		int result = JOptionPane.showConfirmDialog(this, pnl, "Thêm khuyến mãi", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		// Kiểm tra và lưu dữ liệu

		try {
			String ten = txtTen.getText().trim();

			String loai = cboLoai.getSelectedItem().toString();

			String trangThai = cboTrangThai.getSelectedItem().toString();

			if (ten.isEmpty() || txtGiaTri.getText().trim().isEmpty() || txtBatDau.getText().trim().isEmpty()
					|| txtKetThuc.getText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
				return;
			}
			double giaTri = Double.parseDouble(txtGiaTri.getText().trim());
			if (giaTri < 0) {
				JOptionPane.showMessageDialog(this, "Giá trị giảm không được âm!");
				return;
			}
			if ("Giảm theo %".equals(loai) && giaTri > 100) {

				JOptionPane.showMessageDialog(this, "Phần trăm giảm không được vượt quá 100%!");

				return;
			}

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

			sdf.setLenient(false);

			Date ngayBatDau = sdf.parse(txtBatDau.getText().trim());

			Date ngayKetThuc = sdf.parse(txtKetThuc.getText().trim());

			if (ngayKetThuc.before(ngayBatDau)) {

				JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu!");

				return;
			}

			KhuyenMai kmMoi = new KhuyenMai(txtMa.getText(), ten, loai, giaTri, ngayBatDau, ngayKetThuc, trangThai,
					txtPhamVi.getText().trim(), txtDoiTuong.getText().trim());

			if (km_dao.create(kmMoi)) {

				JOptionPane.showMessageDialog(this, "Thêm khuyến mãi thành công!");

				loadDuLieu();

			} else {

				JOptionPane.showMessageDialog(this, "Thêm khuyến mãi thất bại!");
			}

		} catch (NumberFormatException ex) {

			JOptionPane.showMessageDialog(this, "Giá trị giảm phải là số hợp lệ!");

		} catch (ParseException ex) {

			JOptionPane.showMessageDialog(this, "Ngày phải đúng định dạng dd/MM/yyyy!");

		} catch (SQLException ex) {

			JOptionPane.showMessageDialog(this, "Lỗi khi lưu khuyến mãi!\n" + ex.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);

		} catch (IllegalArgumentException ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage());
		}
	}

	// ==========================================
	// LOAD DỮ LIỆU TỪ DATABASE
	// ==========================================

	private void loadDuLieu() {
		try {
			dsAll = km_dao.getalltblKhuyenMai();
			if (dsAll == null) {
				dsAll = new ArrayList<>();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			dsAll = new ArrayList<>();
			JOptionPane.showMessageDialog(this, "Không thể tải danh sách khuyến mãi!\n" + ex.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
		}

		locDuLieu();
	}

	// ==========================================
	// LỌC DỮ LIỆU
	// ==========================================

	private void locDuLieu() {

		String kw = txtSearch.getText().trim().toLowerCase();

		String trangThai = cbStatus.getSelectedItem().toString();

		String loai = cbType.getSelectedItem().toString();

		LocalDate tuNgay = parseNgay(txtFromDate.getText());

		LocalDate denNgay = parseNgay(txtToDate.getText());

		dsHienThi = new ArrayList<>();

		for (KhuyenMai km : dsAll) {
			String maKM = km.getMaKM() == null ? "" : km.getMaKM().toLowerCase();
			String tenKM = km.getTenKM() == null ? "" : km.getTenKM().toLowerCase();
			if (!kw.isEmpty() && !maKM.contains(kw) && !tenKM.contains(kw)) {
				continue;
			}
			String trangThaiDB = trangThai;
			if ("Đang diễn ra".equals(trangThai)) {
				trangThaiDB = "Đang kích hoạt";
			} else if ("Sắp diễn ra".equals(trangThai)) {
				trangThaiDB = "Chờ kích hoạt";
			}
			if (!"Tất cả trạng thái".equals(trangThai) && !trangThaiDB.equals(km.getTrangThai())) {
				continue;
			}

			if (!"Tất cả".equals(loai) && !loai.equals(km.getLoaiKM())) {
				continue;
			}
			if (!khopGiaTri(km)) {
				continue;
			}
			LocalDate ngayBatDau = km.getThoigianBatDau() == null ? null : toLocalDate(km.getThoigianBatDau());
			LocalDate ngayKetThuc = km.getThoigianKetThuc() == null ? null : toLocalDate(km.getThoigianKetThuc());
			if (tuNgay != null && ngayKetThuc != null && ngayKetThuc.isBefore(tuNgay)) {
				continue;
			}
			if (denNgay != null && ngayBatDau != null && ngayBatDau.isAfter(denNgay)) {
				continue;
			}
			dsHienThi.add(km);
		}
		hienThiBang();
	}

	// ==========================================
	// LỌC GIÁ TRỊ GIẢM
	// ==========================================
	private boolean khopGiaTri(KhuyenMai km) {
		String giaTri = cbValue.getSelectedItem().toString();

		if ("Tất cả".equals(giaTri)) {
			return true;
		}

		if (!"Giảm tiền mặt".equals(km.getLoaiKM())) {
			return false;
		}
		double gia = km.getGiaTriGiam();
		switch (giaTri) {
		case "Dưới 50k":
			return gia < 50000;
		case "50k - 200k":
			return gia >= 50000 && gia <= 200000;
		case "Trên 200k":
			return gia > 200000;
		default:
			return true;
		}
	}

	// ==========================================
	// CHUYỂN ĐỔI NGÀY
	// ==========================================
	private LocalDate parseNgay(String ngay) {
		try {
			if (ngay == null || ngay.trim().isEmpty()) {
				return null;
			}
			return LocalDate.parse(ngay.trim(), DATE_PARSE);

		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private LocalDate toLocalDate(Date date) {
		if (date instanceof java.sql.Date) {
			return ((java.sql.Date) date).toLocalDate();
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	// ==========================================
	// HIỂN THỊ DỮ LIỆU LÊN TABLE
	// ==========================================
	private void hienThiBang() {

		model.setRowCount(0);

		int stt = 1;

		for (KhuyenMai km : dsHienThi) {

			String thoiGian = "";

			if (km.getThoigianBatDau() != null && km.getThoigianKetThuc() != null) {

				thoiGian = toLocalDate(km.getThoigianBatDau()).format(DATE_FMT) + " - "
						+ toLocalDate(km.getThoigianKetThuc()).format(DATE_FMT);
			}

			model.addRow(new Object[] { stt++, km.getMaKM(), km.getTenKM(), tagLoai(km.getLoaiKM()), formatGiaTri(km),
					thoiGian, tagTrangThai(km.getTrangThai()), "" });
		}
		lblTableTitle.setText("<html>" + "<span style='color:#1e3a8a; font-size:14px;'>"
				+ "<b>Danh sách khuyến mãi</b></span> " + "<span style='color:#6b7280; font-size:11px;'>" + "(Tổng: "
				+ dsAll.size() + " chương trình)</span></html>");
		lblInfo.setText("Hiển thị " + dsHienThi.size() + " trong tổng số " + dsAll.size() + " chương trình");
	}

	// ==========================================
	// TẠO TAG
	// ==========================================

	private String tag(String text, String bg, String fg) {
		return "<html><span style='background-color:" + bg + "; color:" + fg
				+ "; padding:4px 10px; border-radius:12px;'>" + text + "</span></html>";
	}

	// ==========================================
	// ĐỊNH DẠNG GIÁ TRỊ GIẢM
	// ==========================================

	private String formatGiaTri(KhuyenMai km) {
		if ("Giảm theo %".equals(km.getLoaiKM())) {
			return km.getGiaTriGiam() + "%";
		}
		if ("Giảm tiền mặt".equals(km.getLoaiKM())) {
			return String.format("%,.0fđ", km.getGiaTriGiam());
		}
		return "Tặng sản phẩm";
	}

	// ==========================================
	// TAG LOẠI KHUYẾN MÃI
	// ==========================================

	private String tagLoai(String loai) {
		if (loai == null) {
			return "";
		}
		switch (loai) {
		case "Giảm theo %":
			return tag(loai, "#e0f2fe", "#0284c7");
		case "Giảm tiền mặt":
			return tag(loai, "#f3e8ff", "#9333ea");
		default:
			return tag(loai, "#fee2e2", "#dc2626");
		}
	}

	// ==========================================
	// TAG TRẠNG THÁI
	// ==========================================

	private String tagTrangThai(String tt) {
		if (tt == null) {
			return "";
		}
		switch (tt) {
		case "Đang kích hoạt":
			return tag("Đang diễn ra", "#dcfce7", "#16a34a");
		case "Chờ kích hoạt":
			return tag("Sắp diễn ra", "#e0f2fe", "#0284c7");
		case "Đã kết thúc":
			return tag("Đã kết thúc", "#f1f5f9", "#64748b");
		case "Đã hủy":
			return tag("Đã hủy", "#fee2e2", "#dc2626");
		default:
			return tag(tt, "#f1f5f9", "#64748b");
		}
	}

	// ==========================================
	// SỰ KIỆN CLICK BẢNG
	// ==========================================

	@Override
	public void mouseClicked(MouseEvent e) {
		int row = table.rowAtPoint(e.getPoint());
		int col = table.columnAtPoint(e.getPoint());
		if (row < 0 || col != 7 || row >= dsHienThi.size()) {
			return;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
