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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.math.BigDecimal;
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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import connect.ConnectDB;
import dao.KhuyenMai_DAO;
import entity.KhuyenMai;

public class PNL_KhuyenMai extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	// Chế độ của form
	private static final int MODE_THEM = 0;
	private static final int MODE_SUA = 1;
	private static final int MODE_XEM = 2;

	private static final int COT_THAO_TAC = 7;
	private static final int PAGE_SIZE = 10;

	private static final String[] LOAI_KM = { KhuyenMai.LOAI_PHAN_TRAM, KhuyenMai.LOAI_TIEN_MAT,
			KhuyenMai.LOAI_TANG_SP };
	private static final String[] TRANG_THAI_KM = { KhuyenMai.TT_CHO, KhuyenMai.TT_DANG, KhuyenMai.TT_KET_THUC,
			KhuyenMai.TT_HUY };

	private JTable table;
	private DefaultTableModel model;

	private JTextField txtSearch, txtFromDate, txtToDate;
	private JComboBox<String> cbStatus, cbType, cbValue;
	private JButton btnAdd, btnClearFilter;
	private JLabel lblTableTitle, lblInfo;
	private JPanel pnlPages;

	private final KhuyenMai_DAO km_dao = new KhuyenMai_DAO();

	private List<KhuyenMai> dsAll = new ArrayList<>();
	private List<KhuyenMai> dsHienThi = new ArrayList<>();

	private int trangHienTai = 1;
	private int viTriBatDau = 0;

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final DateTimeFormatter DATE_PARSE = DateTimeFormatter.ofPattern("dd/MM/uuuu")
			.withResolverStyle(ResolverStyle.STRICT);

	private Color bgMain = new Color(245, 247, 250);
	private Color bgWhite = Color.WHITE;
	private Color textDark = new Color(30, 30, 30);
	private Color textGray = new Color(130, 130, 130);
	private Color colorBlue = new Color(37, 99, 235);
	private Color bgHover = new Color(239, 246, 255);

	// ==========================================
	// PANEL BO TRÒN
	// ==========================================

	class RoundedPanel extends JPanel {

		private static final long serialVersionUID = 1L;
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
	// RENDERER ICON XEM / SỬA / TÙY CHỌN
	// ==========================================

	class ActionRenderer extends JPanel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;
		private JLabel lblView = new JLabel();
		private JLabel lblEdit = new JLabel();
		private JLabel lblMore = new JLabel();

		public ActionRenderer() {
			setLayout(new FlowLayout(FlowLayout.CENTER, 6, 8));
			setOpaque(true);

			napIcon(lblView, "/icon/ic_xemchitiet.png", "Xem");
			napIcon(lblEdit, "/icon/ic_chinhsua.png", "Sửa");
			napIcon(lblMore, "/icon/ic_tuychon.png", "...");

			add(lblView);
			add(lblEdit);
			add(lblMore);
		}

		private void napIcon(JLabel lbl, String path, String textDuPhong) {
			try {
				java.net.URL url = getClass().getResource(path);
				if (url != null) {
					Image img = new ImageIcon(url).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
					lbl.setIcon(new ImageIcon(img));
				} else {
					lbl.setText(textDuPhong);
				}
			} catch (Exception e) {
				lbl.setText(textDuPhong);
			}
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
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

			private static final long serialVersionUID = 1L;

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

		cbStatus = new JComboBox<>(
				new String[] { "Tất cả trạng thái", "Đang diễn ra", "Sắp diễn ra", "Đã kết thúc", "Đã hủy" });
		cbStatus.setBackground(Color.WHITE);
		gbcTop.gridx = 1;
		pnlFilterTop.add(createFilterItem("Trạng thái", cbStatus, 130), gbcTop);

		// Lọc loại khuyến mãi

		cbType = new JComboBox<>(new String[] {
			    "Tất cả",
			    KhuyenMai.LOAI_PHAN_TRAM,
			    KhuyenMai.LOAI_TIEN_MAT,
			    KhuyenMai.LOAI_TANG_SP
			});
		cbType.setBackground(Color.WHITE);
		gbcTop.gridx = 2;
		pnlFilterTop.add(createFilterItem("Loại khuyến mãi", cbType, 130), gbcTop);

		// Lọc giá trị

		cbValue = new JComboBox<>(new String[] { "Tất cả", "Dưới 50k", "50k - 200k", "Trên 200k" });
		cbValue.setBackground(Color.WHITE);
		gbcTop.gridx = 3;
		pnlFilterTop.add(createFilterItem("Giá trị giảm", cbValue, 130), gbcTop);

		// Ngày bắt đầu (để trống = không lọc, nhập xong nhấn Enter)

		txtFromDate = new JTextField();
		txtFromDate.setBackground(Color.WHITE);
		txtFromDate.setToolTipText("dd/MM/yyyy - nhấn Enter để lọc, để trống để bỏ lọc");
		gbcTop.gridx = 4;
		pnlFilterTop.add(createFilterItem("Từ ngày", txtFromDate, 110), gbcTop);

		// Ngày kết thúc

		txtToDate = new JTextField();
		txtToDate.setBackground(Color.WHITE);
		txtToDate.setToolTipText("dd/MM/yyyy - nhấn Enter để lọc, để trống để bỏ lọc");
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
		pnlFilterBottom.add(javax.swing.Box.createHorizontalStrut(10));

		// Nút bỏ lọc: đưa toàn bộ bộ lọc về mặc định
		btnClearFilter = new JButton("Bỏ lọc");
		btnClearFilter.setBackground(Color.WHITE);
		btnClearFilter.setForeground(textDark);
		btnClearFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
		btnClearFilter.setFocusPainted(false);
		btnClearFilter.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
		btnClearFilter.setPreferredSize(new Dimension(100, 36));
		btnClearFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnClearFilter.setToolTipText("Xóa tất cả điều kiện lọc");

		pnlFilterBottom.add(btnClearFilter);

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

			private static final long serialVersionUID = 1L;

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

		// Không cho kéo đổi vị trí cột, vì xử lý click dựa vào vị trí cột "Thao tác"
		table.getTableHeader().setReorderingAllowed(false);
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
		table.getColumnModel().getColumn(7).setPreferredWidth(110);

		table.getColumnModel().getColumn(COT_THAO_TAC).setCellRenderer(new ActionRenderer());

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

		pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		pnlPages.setBackground(bgWhite);
		pnlPagination.add(pnlPages, BorderLayout.EAST);

		pnlTableContainer.add(pnlPagination, BorderLayout.SOUTH);

		add(pnlTableContainer, BorderLayout.CENTER);

		// ==========================================
		// 4. ĐĂNG KÝ SỰ KIỆN
		// ==========================================

		btnAdd.addActionListener(this);
		btnClearFilter.addActionListener(this);
		cbStatus.addActionListener(this);
		cbType.addActionListener(this);
		cbValue.addActionListener(this);

		// Ngày: lọc khi nhấn Enter (có kiểm tra định dạng)
		txtFromDate.addActionListener(this);
		txtToDate.addActionListener(this);

		// Ô tìm kiếm: lọc ngay khi gõ
		txtSearch.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				locTheoTuKhoa();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				locTheoTuKhoa();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				locTheoTuKhoa();
			}
		});

		table.addMouseListener(this);

		// Đổi con trỏ thành bàn tay khi rê chuột vào cột "Thao tác"
		table.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());
				boolean tren = row >= 0 && col == COT_THAO_TAC && (viTriBatDau + row) < dsHienThi.size();
				table.setCursor(Cursor.getPredefinedCursor(tren ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			}
		});

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
			moForm(MODE_THEM, null);
			return;
		}

		if (o == btnClearFilter) {
			boLoc();
			return;
		}

		// Kiểm tra ngày lọc trước khi lọc
		if (o == txtFromDate || o == txtToDate) {
			if (!ngayLocHopLe()) {
				return;
			}
		}

		trangHienTai = 1;
		locDuLieu();
	}

	// Đưa toàn bộ bộ lọc về mặc định rồi hiển thị lại toàn bộ danh sách
	private void boLoc() {
		txtSearch.setText("");
		cbStatus.setSelectedIndex(0);
		cbType.setSelectedIndex(0);
		cbValue.setSelectedIndex(0);
		txtFromDate.setText("");
		txtToDate.setText("");
		trangHienTai = 1;
		locDuLieu();
	}

	private void locTheoTuKhoa() {
		trangHienTai = 1;
		locDuLieu();
	}

	// Kiểm tra 2 ô ngày dùng để lọc (cho phép để trống)
	private boolean ngayLocHopLe() {
		String tu = txtFromDate.getText().trim();
		String den = txtToDate.getText().trim();

		if (!tu.isEmpty() && parseNgay(tu) == null) {
			JOptionPane.showMessageDialog(this, "\"Từ ngày\" phải đúng định dạng dd/MM/yyyy!");
			txtFromDate.requestFocus();
			return false;
		}
		if (!den.isEmpty() && parseNgay(den) == null) {
			JOptionPane.showMessageDialog(this, "\"Đến ngày\" phải đúng định dạng dd/MM/yyyy!");
			txtToDate.requestFocus();
			return false;
		}
		if (!tu.isEmpty() && !den.isEmpty() && parseNgay(den).isBefore(parseNgay(tu))) {
			JOptionPane.showMessageDialog(this, "\"Đến ngày\" phải sau hoặc bằng \"Từ ngày\"!");
			return false;
		}
		return true;
	}

	// ==========================================
	// FORM THÊM / SỬA / XEM KHUYẾN MÃI
	// ==========================================

	private void moForm(int mode, KhuyenMai km) {

		JTextField txtMa = new JTextField();
		JTextField txtTen = new JTextField();
		JComboBox<String> cboLoai = new JComboBox<>(LOAI_KM);
		JTextField txtGiaTri = new JTextField();
		JTextField txtBatDau = new JTextField();
		JTextField txtKetThuc = new JTextField();
		JComboBox<String> cboTrangThai = new JComboBox<>(TRANG_THAI_KM);
		JTextField txtPhamVi = new JTextField();
		JTextField txtDoiTuong = new JTextField();

		// Đổ dữ liệu vào form
		if (mode == MODE_THEM) {
			String maMoi = km_dao.taoMaMoi();
			if (maMoi == null) {
				JOptionPane.showMessageDialog(this, "Không tạo được mã khuyến mãi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return;
			}
			txtMa.setText(maMoi);
		} else {
			txtMa.setText(km.getMaKM());
			txtTen.setText(km.getTenKM());
			cboLoai.setSelectedItem(km.getLoaiKM());
			txtGiaTri.setText(BigDecimal.valueOf(km.getGiaTriGiam()).stripTrailingZeros().toPlainString());
			if (km.getThoigianBatDau() != null) {
				txtBatDau.setText(toLocalDate(km.getThoigianBatDau()).format(DATE_FMT));
			}
			if (km.getThoigianKetThuc() != null) {
				txtKetThuc.setText(toLocalDate(km.getThoigianKetThuc()).format(DATE_FMT));
			}
			cboTrangThai.setSelectedItem(km.getTrangThai());
			txtPhamVi.setText(km.getPhamViApDung() == null ? "" : km.getPhamViApDung());
			txtDoiTuong.setText(km.getDoiTuongApDung() == null ? "" : km.getDoiTuongApDung());
		}

		txtMa.setEditable(false);

		// "Tặng sản phẩm" thì không có giá trị giảm
		Runnable capNhatGiaTri = () -> {
			boolean tang = KhuyenMai.LOAI_TANG_SP.equals(cboLoai.getSelectedItem());
			if (tang) {
				txtGiaTri.setText("0");
			}
			txtGiaTri.setEnabled(!tang && mode != MODE_XEM);
		};
		capNhatGiaTri.run();
		cboLoai.addActionListener(ev -> capNhatGiaTri.run());

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

		// Chế độ xem: khóa toàn bộ, chỉ hiển thị
		if (mode == MODE_XEM) {
			txtTen.setEditable(false);
			cboLoai.setEnabled(false);
			txtBatDau.setEditable(false);
			txtKetThuc.setEditable(false);
			cboTrangThai.setEnabled(false);
			txtPhamVi.setEditable(false);
			txtDoiTuong.setEditable(false);
			JOptionPane.showMessageDialog(this, pnl, "Chi tiết khuyến mãi", JOptionPane.PLAIN_MESSAGE);
			return;
		}

		String tieuDe = (mode == MODE_THEM) ? "Thêm khuyến mãi" : "Cập nhật khuyến mãi";

		// Lặp để khi nhập sai, form mở lại và không mất dữ liệu đã gõ
		while (true) {

			int result = JOptionPane.showConfirmDialog(this, pnl, tieuDe, JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);

			if (result != JOptionPane.OK_OPTION) {
				return;
			}

			try {
				String ten = txtTen.getText().trim();
				String loai = cboLoai.getSelectedItem().toString();
				String trangThai = cboTrangThai.getSelectedItem().toString();
				boolean tang = KhuyenMai.LOAI_TANG_SP.equals(loai);

				if (ten.isEmpty() || txtBatDau.getText().trim().isEmpty() || txtKetThuc.getText().trim().isEmpty()
						|| (!tang && txtGiaTri.getText().trim().isEmpty())) {
					JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
					continue;
				}

				double giaTri = tang ? 0 : Double.parseDouble(txtGiaTri.getText().trim());

				if (!tang && giaTri <= 0) {
					JOptionPane.showMessageDialog(this, "Giá trị giảm phải lớn hơn 0!");
					continue;
				}
				if (KhuyenMai.LOAI_PHAN_TRAM.equals(loai) && giaTri > 100) {
					JOptionPane.showMessageDialog(this, "Phần trăm giảm không được vượt quá 100%!");
					continue;
				}

				LocalDate bd = parseNgay(txtBatDau.getText());
				LocalDate kt = parseNgay(txtKetThuc.getText());

				if (bd == null || kt == null) {
					JOptionPane.showMessageDialog(this, "Ngày phải đúng định dạng dd/MM/yyyy!");
					continue;
				}
				if (kt.isBefore(bd)) {
					JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu!");
					continue;
				}

				// Bắt đầu: 00:00:00, kết thúc: 23:59:59 để khuyến mãi áp dụng trọn ngày cuối
				Date ngayBatDau = Date.from(bd.atStartOfDay(ZoneId.systemDefault()).toInstant());
				Date ngayKetThuc = Date.from(kt.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

				KhuyenMai kmMoi = new KhuyenMai(txtMa.getText(), ten, loai, giaTri, ngayBatDau, ngayKetThuc, trangThai,
						txtPhamVi.getText().trim(), txtDoiTuong.getText().trim());

				// Cảnh báo trùng thời gian (dùng kiemTraTrungLapKM của entity)
				KhuyenMai trung = timKhuyenMaiTrung(kmMoi);
				if (trung != null) {
					int chon = JOptionPane.showConfirmDialog(this,
							"Khuyến mãi \"" + trung.getTenKM() + "\" (" + trung.getMaKM()
									+ ") đang trùng thời gian và cùng loại giảm %.\nBạn vẫn muốn lưu?",
							"Cảnh báo trùng khuyến mãi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (chon != JOptionPane.YES_OPTION) {
						continue;
					}
				}

				boolean ok = (mode == MODE_THEM) ? km_dao.create(kmMoi) : km_dao.update(kmMoi);

				if (ok) {
					JOptionPane.showMessageDialog(this,
							(mode == MODE_THEM ? "Thêm" : "Cập nhật") + " khuyến mãi thành công!");
					loadDuLieu();
				} else {
					JOptionPane.showMessageDialog(this,
							(mode == MODE_THEM ? "Thêm" : "Cập nhật") + " khuyến mãi thất bại!");
				}
				return;

			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Giá trị giảm phải là số hợp lệ!");
			} catch (IllegalArgumentException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage());
			}
		}
	}

	// Tìm khuyến mãi đang hoạt động bị trùng với khuyến mãi mới (bỏ qua chính nó)
	private KhuyenMai timKhuyenMaiTrung(KhuyenMai kmMoi) {
		if (!conHoatDong(kmMoi)) {
			return null;
		}
		for (KhuyenMai k : dsAll) {
			if (k.equals(kmMoi) || !conHoatDong(k)) {
				continue;
			}
			if (kmMoi.kiemTraTrungLapKM(k)) {
				return k;
			}
		}
		return null;
	}

	private boolean conHoatDong(KhuyenMai km) {
		return KhuyenMai.TT_CHO.equals(km.getTrangThai()) || KhuyenMai.TT_DANG.equals(km.getTrangThai());
	}

	// ==========================================
	// LOAD DỮ LIỆU TỪ DATABASE
	// ==========================================

	private void loadDuLieu() {
		km_dao.capNhatKhuyenMaiHetHan();

		dsAll = km_dao.getalltblKhuyenMai();
		if (dsAll == null) {
			dsAll = new ArrayList<>();
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
				trangThaiDB = KhuyenMai.TT_DANG;
			} else if ("Sắp diễn ra".equals(trangThai)) {
				trangThaiDB = KhuyenMai.TT_CHO;
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
		if (!KhuyenMai.LOAI_TIEN_MAT.equals(km.getLoaiKM())) {
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
	// HIỂN THỊ DỮ LIỆU LÊN TABLE (CÓ PHÂN TRANG)
	// ==========================================

	private void hienThiBang() {

		int tong = dsHienThi.size();
		int tongTrang = Math.max(1, (int) Math.ceil((double) tong / PAGE_SIZE));

		if (trangHienTai > tongTrang) {
			trangHienTai = tongTrang;
		}
		if (trangHienTai < 1) {
			trangHienTai = 1;
		}

		viTriBatDau = (trangHienTai - 1) * PAGE_SIZE;
		int viTriKetThuc = Math.min(viTriBatDau + PAGE_SIZE, tong);

		model.setRowCount(0);

		for (int i = viTriBatDau; i < viTriKetThuc; i++) {

			KhuyenMai km = dsHienThi.get(i);
			String thoiGian = "";

			if (km.getThoigianBatDau() != null && km.getThoigianKetThuc() != null) {
				thoiGian = toLocalDate(km.getThoigianBatDau()).format(DATE_FMT) + " - "
						+ toLocalDate(km.getThoigianKetThuc()).format(DATE_FMT);
			}

			model.addRow(new Object[] { i + 1, km.getMaKM(), km.getTenKM(), tagLoai(km.getLoaiKM()), formatGiaTri(km),
					thoiGian, tagTrangThai(km.getTrangThai()), "" });
		}

		lblTableTitle.setText("<html>" + "<span style='color:#1e3a8a; font-size:14px;'>"
				+ "<b>Danh sách khuyến mãi</b></span> " + "<span style='color:#6b7280; font-size:11px;'>" + "(Tổng: "
				+ dsAll.size() + " chương trình)</span></html>");

		if (tong == 0) {
			lblInfo.setText("Hiển thị 0 trong tổng số 0 chương trình");
		} else {
			lblInfo.setText(
					"Hiển thị " + (viTriBatDau + 1) + "-" + viTriKetThuc + " trong tổng số " + tong + " chương trình");
		}

		capNhatPhanTrang(tongTrang);
	}

	// ==========================================
	// NÚT PHÂN TRANG
	// ==========================================

	private void capNhatPhanTrang(int tongTrang) {

		pnlPages.removeAll();

		pnlPages.add(taoNutTrang("<", false, trangHienTai > 1, () -> {
			trangHienTai--;
			hienThiBang();
		}));

		int dau = Math.max(1, trangHienTai - 2);
		int cuoi = Math.min(tongTrang, dau + 4);
		dau = Math.max(1, cuoi - 4);

		for (int p = dau; p <= cuoi; p++) {
			final int trang = p;
			pnlPages.add(taoNutTrang(String.valueOf(p), p == trangHienTai, true, () -> {
				trangHienTai = trang;
				hienThiBang();
			}));
		}

		pnlPages.add(taoNutTrang(">", false, trangHienTai < tongTrang, () -> {
			trangHienTai++;
			hienThiBang();
		}));

		pnlPages.revalidate();
		pnlPages.repaint();
	}

	private JButton taoNutTrang(String text, boolean active, boolean enabled, Runnable onClick) {

		JButton btn = new JButton(text);
		btn.setFocusPainted(false);
		btn.setPreferredSize(new Dimension(32, 32));
		btn.setMargin(new Insets(0, 0, 0, 0));
		btn.setEnabled(enabled);

		if (active) {
			btn.setBackground(colorBlue);
			btn.setForeground(Color.WHITE);
			btn.setBorderPainted(false);
		} else {
			btn.setBackground(bgWhite);
			btn.setForeground(textDark);
			btn.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
		}

		if (enabled && !active) {
			btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		btn.addActionListener(e -> onClick.run());
		return btn;
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
		if (KhuyenMai.LOAI_PHAN_TRAM.equals(km.getLoaiKM())) {
			return BigDecimal.valueOf(km.getGiaTriGiam()).stripTrailingZeros().toPlainString() + "%";
		}
		if (KhuyenMai.LOAI_TIEN_MAT.equals(km.getLoaiKM())) {
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
		case KhuyenMai.LOAI_PHAN_TRAM:
			return tag(loai, "#e0f2fe", "#0284c7");
		case KhuyenMai.LOAI_TIEN_MAT:
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
		case KhuyenMai.TT_DANG:
			return tag("Đang diễn ra", "#dcfce7", "#16a34a");
		case KhuyenMai.TT_CHO:
			return tag("Sắp diễn ra", "#e0f2fe", "#0284c7");
		case KhuyenMai.TT_KET_THUC:
			return tag("Đã kết thúc", "#f1f5f9", "#64748b");
		case KhuyenMai.TT_HUY:
			return tag("Đã hủy", "#fee2e2", "#dc2626");
		default:
			return tag(tt, "#f1f5f9", "#64748b");
		}
	}

	// ==========================================
	// SỰ KIỆN CLICK BẢNG: ô "Thao tác" chia 3 phần = Xem / Sửa / Tùy chọn
	// ==========================================

	@Override
	public void mouseClicked(MouseEvent e) {
		int row = table.rowAtPoint(e.getPoint());
		int col = table.columnAtPoint(e.getPoint());

		if (row < 0 || col != COT_THAO_TAC) {
			return;
		}

		int idx = viTriBatDau + row;
		if (idx >= dsHienThi.size()) {
			return;
		}

		Rectangle rect = table.getCellRect(row, col, false);
		int phan = Math.min(2, Math.max(0, (e.getX() - rect.x) * 3 / rect.width));

		KhuyenMai km = dsHienThi.get(idx);

		if (phan == 0) {
			moForm(MODE_XEM, km);
		} else if (phan == 1) {
			moForm(MODE_SUA, km);
		} else {
			hienThiTuyChon(km, e);
		}
	}

	// Menu tùy chọn: hủy khuyến mãi (xóa mềm)
	private void hienThiTuyChon(KhuyenMai km, MouseEvent e) {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem itemHuy = new JMenuItem("Hủy khuyến mãi");

		boolean daXong = KhuyenMai.TT_HUY.equals(km.getTrangThai()) || KhuyenMai.TT_KET_THUC.equals(km.getTrangThai());
		itemHuy.setEnabled(!daXong);

		itemHuy.addActionListener(ev -> {
			int confirm = JOptionPane.showConfirmDialog(this,
					"Bạn có chắc muốn hủy khuyến mãi \"" + km.getTenKM() + "\"?", "Xác nhận",
					JOptionPane.YES_NO_OPTION);

			if (confirm == JOptionPane.YES_OPTION) {
				if (km_dao.delete(km.getMaKM())) {
					JOptionPane.showMessageDialog(this, "Hủy khuyến mãi thành công!");
					loadDuLieu();
				} else {
					JOptionPane.showMessageDialog(this, "Hủy khuyến mãi thất bại!");
				}
			}
		});

		menu.add(itemHuy);
		menu.show(table, e.getX(), e.getY());
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