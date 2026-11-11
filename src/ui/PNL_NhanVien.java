package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import connect.ConnectDB;
import dao.NhanVien_DAO;
import entity.NhanVien;

public class PNL_NhanVien extends JPanel implements ActionListener, MouseListener {
	private JTable table;

	private JTextField txtHoTen, txtSDT, txtMaNV;
	private JTextField txtEmail, txtNgaySinh;
	private JTextField txtSearch;
	private JTextField txtFilterNamSinh;

	private JRadioButton rbNam, rbNu;

	private JComboBox<String> cbChucVu, cbTrangThai;
	private JComboBox<String> cbFilterChucVu;
	private JComboBox<String> cbFilterTrangThai;
	private JComboBox<String> cbFilterGioiTinh;

	private Color bgMain = new Color(245, 247, 250);
	private Color bgWhite = Color.WHITE;
	private Color textDark = new Color(30, 30, 30);
	private Color textGray = new Color(130, 130, 130);
	private Color colorBlue = new Color(37, 99, 235);
	private Color bgHover = new Color(239, 246, 255);

	private JTextField txtDiaChi, txtGhiChu;
	private String duongDanAnh;

	private TableRowSorter<TableModel> sorter;

	private JButton btnAddNhanVien, btnUpdate, btnChooseImg;

	private JLabel lblAvatarIcon;

	private NhanVien_DAO nv_dao;

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

	class ActionRenderer extends JPanel implements TableCellRenderer {
		private JLabel lblView = new JLabel();
		private JLabel lblEdit = new JLabel();
		private JLabel lblMore = new JLabel();

		public ActionRenderer() {
			setLayout(new FlowLayout(FlowLayout.CENTER, 6, 8));
			setOpaque(true);

			setCustomIcon(lblView, "/icon/ic_xemchitiet.png", 14, 14);
			setCustomIcon(lblEdit, "/icon/ic_chinhsua.png", 14, 14);
			setCustomIcon(lblMore, "/icon/ic_tuychon.png", 14, 14);

			add(lblView);
			add(lblEdit);
			add(lblMore);
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

	public PNL_NhanVien() {

		ConnectDB.getInstance();
		System.out.println("Connected!!!!");

		ConnectDB.getInstance();
		if (ConnectDB.getConnection() != null) {
			System.out.println("Connected!!!!");
		} else {
			System.out.println("Kết nối database thất bại!");
		}

		nv_dao = new NhanVien_DAO();

		setLayout(new BorderLayout());
		setBackground(bgMain);
		setBorder(new EmptyBorder(10, 12, 10, 12));

		JPanel pnlMainContent = new JPanel();
		pnlMainContent.setLayout(new BoxLayout(pnlMainContent, BoxLayout.Y_AXIS));
		pnlMainContent.setOpaque(false);

		// =========================================================================
		// PHẦN 1: KHUNG THÔNG TIN NHÂN VIÊN (FORM NHẬP LIỆU - Đã bỏ các dấu sao *)
		// =========================================================================
		RoundedPanel pnlFormContainer = new RoundedPanel(12, bgWhite);
		pnlFormContainer.setLayout(new BorderLayout());
		pnlFormContainer.setBorder(new EmptyBorder(10, 12, 10, 12));

		JPanel pnlFormTop = new JPanel(new BorderLayout());
		pnlFormTop.setOpaque(false);
		pnlFormTop.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 232, 235)),
				new EmptyBorder(0, 0, 6, 0)));

		JLabel lblFormTitle = new JLabel("Thông tin nhân viên");
		lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblFormTitle.setForeground(textDark);
		setCustomIcon(lblFormTitle, "/icon/ic_nhanvien.png", 15, 15);
		lblFormTitle.setIconTextGap(6);

		// Đổi thành 2 nút: Thêm nhân viên và Cập nhật ở đầu trang
		JPanel pnlFormActionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		pnlFormActionBtns.setOpaque(false);

		btnAddNhanVien = new JButton("Thêm nhân viên");
		setCustomIcon(btnAddNhanVien, "/icon/ic_them.png", 12, 12);
		btnAddNhanVien.setBackground(colorBlue);
		btnAddNhanVien.setForeground(Color.WHITE);
		btnAddNhanVien.setFont(new Font("Segoe UI", Font.BOLD, 10));
		btnAddNhanVien.setBorderPainted(false);
		btnAddNhanVien.setFocusPainted(false);
		btnAddNhanVien.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnAddNhanVien.setPreferredSize(new Dimension(125, 28));

		btnUpdate = new JButton("Cập nhật");
		setCustomIcon(btnUpdate, "/icon/ic_chinhsua.png", 12, 12);
		btnUpdate.setBackground(Color.WHITE);
		btnUpdate.setForeground(textDark);
		btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 10));
		btnUpdate.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 220)));
		btnUpdate.setFocusPainted(false);
		btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnUpdate.setPreferredSize(new Dimension(95, 28));

		pnlFormActionBtns.add(btnAddNhanVien);
		pnlFormActionBtns.add(btnUpdate);

		pnlFormTop.add(lblFormTitle, BorderLayout.WEST);
		pnlFormTop.add(pnlFormActionBtns, BorderLayout.EAST);
		pnlFormContainer.add(pnlFormTop, BorderLayout.NORTH);

		JPanel pnlFieldsCenter = new JPanel(new GridBagLayout());
		pnlFieldsCenter.setOpaque(false);
		pnlFieldsCenter.setBorder(new EmptyBorder(8, 0, 0, 0));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 5, 3, 5);

		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.weightx = 0.38;
		pnlFieldsCenter.add(createInputGroup("Họ và tên", ""), gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.38;
		pnlFieldsCenter.add(createInputGroup("Số điện thoại", ""), gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridheight = 4;
		gbc.weightx = 0.24;
		gbc.fill = GridBagConstraints.BOTH;
		pnlFieldsCenter.add(createAvatarPanel(), gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = 1;

		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.weightx = 0.38;
		pnlFieldsCenter.add(createInputGroup("Mã nhân viên", ""), gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.38;
		pnlFieldsCenter.add(createInputGroup("Email", ""), gbc);

		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.weightx = 0.38;
		pnlFieldsCenter.add(createGenderGroup(), gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.38;
		pnlFieldsCenter.add(createDropdownGroup("Chức vụ",
				new String[] { "Nhân viên bán hàng", "Kế toán", "Nhân viên kho", "Quản lý cửa hàng" }), gbc);

		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.weightx = 0.38;
		pnlFieldsCenter.add(createInputGroup("Ngày sinh", ""), gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.38;
		pnlFieldsCenter.add(createDropdownGroup("Trạng thái", new String[] { "Đang làm việc", "Tạm nghỉ" }), gbc);

		gbc.gridy = 4;
		gbc.gridx = 0;
		gbc.weightx = 0.38;
		pnlFieldsCenter.add(createInputGroup("Địa chỉ thường trú", ""), gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.38;
		pnlFieldsCenter.add(createInputGroup("Ghi chú", "Nhập ghi chú (nếu có)..."), gbc);

		pnlFormContainer.add(pnlFieldsCenter, BorderLayout.CENTER);
		pnlMainContent.add(pnlFormContainer);
		pnlMainContent.add(Box.createVerticalStrut(10));

		// =========================================================================
		// PHẦN 2: THANH BỘ LỌC
		// =========================================================================
		RoundedPanel pnlFilter = new RoundedPanel(12, bgWhite);
		pnlFilter.setLayout(new BorderLayout(8, 0));
		pnlFilter.setBorder(new EmptyBorder(8, 10, 8, 10));

		JPanel pnlSearchBox = new JPanel(new BorderLayout(6, 0));
		pnlSearchBox.setBackground(Color.WHITE);
		pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220, 224, 230)), new EmptyBorder(0, 8, 0, 8)));
		pnlSearchBox.setPreferredSize(new Dimension(230, 30));

		JLabel lblSearchIcon = new JLabel();
		setCustomIcon(lblSearchIcon, "/icon/ic_timkiem.png", 13, 13);

		txtSearch = new JTextField(" Tìm mã, tên, SĐT, email...");
		txtSearch.setForeground(textGray);
		txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		txtSearch.setBorder(null);
		pnlSearchBox.add(lblSearchIcon, BorderLayout.WEST);
		pnlSearchBox.add(txtSearch, BorderLayout.CENTER);

		JPanel pnlFilterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		pnlFilterControls.setOpaque(false);

		cbFilterChucVu = new JComboBox<>(
				new String[] { "Tất cả chức vụ", "Nhân viên bán hàng", "Kế toán", "Nhân viên kho" });
		cbFilterChucVu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		cbFilterChucVu.setBackground(Color.WHITE);
		cbFilterChucVu.setPreferredSize(new Dimension(130, 30));

		cbFilterTrangThai = new JComboBox<>(new String[] { "Tất cả trạng thái", "Đang làm việc", "Tạm nghỉ" });
		cbFilterTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		cbFilterTrangThai.setBackground(Color.WHITE);
		cbFilterTrangThai.setPreferredSize(new Dimension(125, 30));

		cbFilterGioiTinh = new JComboBox<>(new String[] { "Giới tính: Tất cả", "Nam", "Nữ" });
		cbFilterGioiTinh.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		cbFilterGioiTinh.setBackground(Color.WHITE);
		cbFilterGioiTinh.setPreferredSize(new Dimension(115, 30));

		txtFilterNamSinh = new JTextField(" Năm sinh");
		txtFilterNamSinh.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		txtFilterNamSinh.setForeground(textGray);
		txtFilterNamSinh.setPreferredSize(new Dimension(95, 30));
		txtFilterNamSinh.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220, 224, 230)), BorderFactory.createEmptyBorder(0, 4, 0, 4)));

		pnlFilterControls.add(cbFilterChucVu);
		pnlFilterControls.add(cbFilterTrangThai);
		pnlFilterControls.add(cbFilterGioiTinh);
		pnlFilterControls.add(txtFilterNamSinh);

		pnlFilter.add(pnlSearchBox, BorderLayout.CENTER);
		pnlFilter.add(pnlFilterControls, BorderLayout.EAST);

		pnlMainContent.add(pnlFilter);
		pnlMainContent.add(Box.createVerticalStrut(10));

		// =========================================================================
		// PHẦN 3: BẢNG DANH SÁCH NHÂN VIÊN
		// =========================================================================
		RoundedPanel pnlTableContainer = new RoundedPanel(12, bgWhite);
		pnlTableContainer.setLayout(new BorderLayout(0, 8));
		pnlTableContainer.setBorder(new EmptyBorder(10, 12, 10, 12));

		JLabel lblTableTitle = new JLabel("Danh sách nhân viên");
		lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblTableTitle.setForeground(textDark);
		pnlTableContainer.add(lblTableTitle, BorderLayout.NORTH);

		String[] columns = { "STT", "Mã NV", "Họ và tên", "Giới tính", "Ngày sinh", "Số điện thoại", "Email", "Chức vụ",
				"Trạng thái", "Thao tác" };

		String tagDangLamViec = "<html><span style='background-color:#dcfce7; color:#16a34a; padding:3px 8px; border-radius:10px; font-size:10px;'>● Đang làm việc</span></html>";
		String tagTamNghi = "<html><span style='background-color:#fef3c7; color:#d97706; padding:3px 8px; border-radius:10px; font-size:10px;'>● Tạm nghỉ</span></html>";

//		Object[][] data = {
//				{ "1", "NV0015", "Nguyễn Văn A", "Nam", "15/05/1995", "0912345678", "nv.a@example.com",
//						"Nhân viên bán hàng", tagDangLamViec, "" },
//				{ "2", "NV0014", "Trần Thị B", "Nữ", "22/08/1998", "0987654321", "tb@example.com", "Kế toán",
//						tagDangLamViec, "" },
//				{ "3", "NV0013", "Lê Văn C", "Nam", "10/04/1992", "0909123456", "c@example.com", "Nhân viên kho",
//						tagDangLamViec, "" },
//				{ "4", "NV0012", "Phạm Thị D", "Nữ", "03/11/1996", "0976543210", "d@example.com", "Nhân viên bán hàng",
//						tagTamNghi, "" },
//				{ "5", "NV0011", "Hoàng Văn E", "Nam", "17/07/1994", "0934567890", "e@example.com", "Quản lý cửa hàng",
//						tagDangLamViec, "" } };

//		table = new JTable(data, columns) {
//			@Override
//			public boolean isCellEditable(int row, int column) {
//				return false;
//			}
//		};

		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table = new JTable(model);

		table.setRowHeight(34);
		table.setShowVerticalLines(false);
		table.setGridColor(new Color(240, 240, 240));
		table.setSelectionBackground(bgHover);
		table.setSelectionForeground(textDark);
		table.setFont(new Font("Segoe UI", Font.PLAIN, 11));

		table.getTableHeader().setBackground(new Color(248, 250, 252));
		table.getTableHeader().setForeground(textDark);
		table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
		table.getTableHeader().setPreferredSize(new Dimension(0, 30));
		table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

		table.getColumnModel().getColumn(0).setPreferredWidth(35);
		table.getColumnModel().getColumn(1).setPreferredWidth(65);
		table.getColumnModel().getColumn(2).setPreferredWidth(110);
		table.getColumnModel().getColumn(3).setPreferredWidth(55);
		table.getColumnModel().getColumn(4).setPreferredWidth(80);
		table.getColumnModel().getColumn(5).setPreferredWidth(95);
		table.getColumnModel().getColumn(6).setPreferredWidth(125);
		table.getColumnModel().getColumn(7).setPreferredWidth(125);
		table.getColumnModel().getColumn(8).setPreferredWidth(105);
		table.getColumnModel().getColumn(9).setPreferredWidth(70);

		table.getColumnModel().getColumn(9).setCellRenderer(new ActionRenderer());

		JScrollPane scrollTable = new JScrollPane(table);
		scrollTable.setPreferredSize(new Dimension(0, 210));
		scrollTable.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 235)));
		scrollTable.getViewport().setBackground(bgWhite);
		pnlTableContainer.add(scrollTable, BorderLayout.CENTER);

		JPanel pnlPagination = new JPanel(new BorderLayout());
		pnlPagination.setBackground(bgWhite);
		pnlPagination.setBorder(new EmptyBorder(6, 0, 0, 0));

		JLabel lblInfo = new JLabel("Hiển thị 1–5 trên 15 nhân viên");
		lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		lblInfo.setForeground(textGray);
		pnlPagination.add(lblInfo, BorderLayout.WEST);

		JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		pnlPages.setBackground(bgWhite);

		String[] pages = { "<", "1", "2", "3", ">" };
		for (String p : pages) {
			JButton btnPage = new JButton(p);
			btnPage.setFocusPainted(false);
			btnPage.setPreferredSize(new Dimension(24, 24));
			btnPage.setFont(new Font("Segoe UI", Font.PLAIN, 11));
			btnPage.setMargin(new Insets(0, 0, 0, 0));
			btnPage.setCursor(new Cursor(Cursor.HAND_CURSOR));

			if (p.equals("1")) {
				btnPage.setBackground(colorBlue);
				btnPage.setForeground(Color.WHITE);
				btnPage.setBorderPainted(false);
			} else {
				btnPage.setBackground(bgWhite);
				btnPage.setForeground(textDark);
				btnPage.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
			}
			pnlPages.add(btnPage);
		}

		pnlPagination.add(pnlPages, BorderLayout.EAST);
		pnlTableContainer.add(pnlPagination, BorderLayout.SOUTH);

		pnlMainContent.add(pnlTableContainer);

		JScrollPane scrollMain = new JScrollPane(pnlMainContent);
		scrollMain.setBorder(BorderFactory.createEmptyBorder());
		scrollMain.getViewport().setBackground(bgMain);
		scrollMain.getVerticalScrollBar().setUnitIncrement(16);

		add(scrollMain, BorderLayout.CENTER);

		/**
		 * ĐĂNG KÝ SỰ KIỆN
		 */

		table.addMouseListener(this);

		btnAddNhanVien.addActionListener(this);
		btnUpdate.addActionListener(this);
		btnChooseImg.addActionListener(this);

		cbFilterChucVu.addActionListener(this);
		cbFilterTrangThai.addActionListener(this);
		cbFilterGioiTinh.addActionListener(this);

		// Tìm kiếm khi nhấn Enter
		txtSearch.addActionListener(e -> filterTable());

		// Lọc khi nhập năm sinh rồi nhấn Enter
		txtFilterNamSinh.addActionListener(e -> filterTable());

		sorter = new TableRowSorter<>(table.getModel());
		table.setRowSorter(sorter);

		// Tải dữ liệu SQL lên bảng khi mở giao diện
		loadDataNhanVien();

	}

	private JPanel createInputGroup(String labelText, String placeholder) {
		JPanel pnl = new JPanel(new BorderLayout(0, 1));
		pnl.setOpaque(false);

		JLabel lbl = new JLabel(labelText);
		lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
		lbl.setForeground(textDark);

		JTextField txt = new JTextField(" " + placeholder);

		switch (labelText) {
		case "Họ và tên":
			txtHoTen = txt;
			break;
		case "Số điện thoại":
			txtSDT = txt;
			break;
		case "Mã nhân viên":
			txtMaNV = txt;
			break;
		case "Email":
			txtEmail = txt;
			break;
		case "Ngày sinh":
			txtNgaySinh = txt;
			break;
		case "Địa chỉ thường trú":
			txtDiaChi = txt;
			break;
		case "Ghi chú":
			txtGhiChu = txt;
			break;
		}

		txt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		txt.setForeground(placeholder.contains("...") || placeholder.contains("Số") || placeholder.contains("123")
				|| placeholder.contains("NV") || placeholder.contains("Nguyễn") || placeholder.contains("nv.a")
				|| placeholder.contains("15/05") ? textGray : textDark);
		txt.setPreferredSize(new Dimension(0, 28));
		txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)),
				BorderFactory.createEmptyBorder(0, 4, 0, 4)));

		pnl.add(lbl, BorderLayout.NORTH);
		pnl.add(txt, BorderLayout.CENTER);
		return pnl;
	}

	private JPanel createDropdownGroup(String labelText, String[] items) {
		JPanel pnl = new JPanel(new BorderLayout(0, 1));
		pnl.setOpaque(false);

		JLabel lbl = new JLabel(labelText);
		lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
		lbl.setForeground(textDark);

		JComboBox<String> cb = new JComboBox<>(items);

		if (labelText.equals("Chức vụ")) {
			cbChucVu = cb;
		} else if (labelText.equals("Trạng thái")) {
			cbTrangThai = cb;
		}

		cb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		cb.setBackground(Color.WHITE);
		cb.setPreferredSize(new Dimension(0, 28));

		pnl.add(lbl, BorderLayout.NORTH);
		pnl.add(cb, BorderLayout.CENTER);
		return pnl;
	}

	private JPanel createGenderGroup() {
		JPanel pnl = new JPanel(new BorderLayout(0, 1));
		pnl.setOpaque(false);

		JLabel lbl = new JLabel("Giới tính");
		lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
		lbl.setForeground(textDark);

		JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
		pnlRadio.setOpaque(false);
		rbNam = new JRadioButton("Nam", true);
		rbNam.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		rbNam.setOpaque(false);
		rbNu = new JRadioButton("Nữ");
		rbNu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		rbNu.setOpaque(false);

		ButtonGroup bg = new ButtonGroup();
		bg.add(rbNam);
		bg.add(rbNu);

		pnlRadio.add(rbNam);
		pnlRadio.add(rbNu);

		pnl.add(lbl, BorderLayout.NORTH);
		pnl.add(pnlRadio, BorderLayout.CENTER);
		return pnl;
	}

	private JPanel createAvatarPanel() {
		JPanel pnl = new JPanel(new GridBagLayout());
		pnl.setOpaque(false);
		pnl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(230, 232, 235)),
				new EmptyBorder(4, 4, 4, 4)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(1, 1, 1, 1);

		lblAvatarIcon = new JLabel("👤", SwingConstants.CENTER) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(235, 240, 248));
				g2.fillOval(0, 0, getWidth(), getHeight());
				g2.dispose();
				super.paintComponent(g);
			}
		};
		lblAvatarIcon.setPreferredSize(new Dimension(45, 45));
		lblAvatarIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

		JLabel lblNameLabel = new JLabel("Ảnh đại diện");
		lblNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
		lblNameLabel.setForeground(textGray);

		btnChooseImg = new JButton("Chọn ảnh");
		btnChooseImg.setFont(new Font("Segoe UI", Font.BOLD, 9));
		btnChooseImg.setForeground(colorBlue);
		btnChooseImg.setBackground(Color.WHITE);
		btnChooseImg.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 220)));
		btnChooseImg.setFocusPainted(false);
		btnChooseImg.setPreferredSize(new Dimension(75, 22));
		btnChooseImg.setCursor(new Cursor(Cursor.HAND_CURSOR));

		gbc.gridy = 0;
		pnl.add(lblAvatarIcon, gbc);
		gbc.gridy = 1;
		pnl.add(lblNameLabel, gbc);
		gbc.gridy = 2;
		pnl.add(btnChooseImg, gbc);

		return pnl;
	}

	private void setCustomIcon(JLabel label, String iconPath, int width, int height) {
		try {
			java.net.URL url = getClass().getResource(iconPath);
			if (url != null) {
				Image img = new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
				label.setIcon(new ImageIcon(img));
			}
		} catch (Exception e) {
		}
	}

	private void setCustomIcon(AbstractButton button, String iconPath, int width, int height) {
		try {
			java.net.URL url = getClass().getResource(iconPath);
			if (url != null) {
				Image img = new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
				button.setIcon(new ImageIcon(img));
			}
		} catch (Exception e) {
		}
	}

	private void filterTable() {
		if (sorter == null)
			return;

		String chucVu = cbFilterChucVu.getSelectedItem().toString();
		String trangThai = cbFilterTrangThai.getSelectedItem().toString();
		String gioiTinh = cbFilterGioiTinh.getSelectedItem().toString();

		String tuKhoa = txtSearch.getText().trim();
		String namSinh = txtFilterNamSinh.getText().trim();

		sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
			@Override
			public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {

				String maNV = entry.getStringValue(1);
				String hoTen = entry.getStringValue(2);
				String gt = entry.getStringValue(3);
				String ngaySinh = entry.getStringValue(4);
				String sdt = entry.getStringValue(5);
				String email = entry.getStringValue(6);
				String cv = entry.getStringValue(7);
				String tt = entry.getStringValue(8);

				boolean matchChucVu = chucVu.equals("Tất cả chức vụ") || cv.equals(chucVu);

				boolean matchTrangThai = trangThai.equals("Tất cả trạng thái") || tt.equals(trangThai);

				boolean matchGioiTinh = gioiTinh.equals("Giới tính: Tất cả") || gt.equals(gioiTinh);

				boolean matchTuKhoa = tuKhoa.isEmpty() || maNV.toLowerCase().contains(tuKhoa.toLowerCase())
						|| hoTen.toLowerCase().contains(tuKhoa.toLowerCase()) || sdt.contains(tuKhoa)
						|| email.toLowerCase().contains(tuKhoa.toLowerCase());

				boolean matchNamSinh = namSinh.isEmpty() || ngaySinh.endsWith(namSinh);

				return matchChucVu && matchTrangThai && matchGioiTinh && matchTuKhoa && matchNamSinh;
			}
		});
	}

	/**
	 * PHẦN 4: XỬ LÝ SỰ KIỆN
	 */

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == btnAddNhanVien) {
			themNhanVien();

		} else if (o == btnUpdate) {
			capNhatNhanVien();

		} else if (o == btnChooseImg) {
			chonAnh();

		} else {
			filterTable();
		}
	}

	// THÊM NHÂN VIÊN
	private void themNhanVien() {
		String maNV = txtMaNV.getText().trim();
		String hoTen = txtHoTen.getText().trim();
		String sdt = txtSDT.getText().trim();
		String email = txtEmail.getText().trim();
		String ngaySinh = txtNgaySinh.getText().trim();

		if (maNV.isEmpty() || hoTen.isEmpty() || sdt.isEmpty() || email.isEmpty() || ngaySinh.isEmpty()) {

			JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
			return;
		}

		if (nv_dao.kiemTraMaNV(maNV)) {
			JOptionPane.showMessageDialog(this, "Mã nhân viên đã tồn tại!");
			return;
		}

		try {
			NhanVien nv = new NhanVien();

			nv.setMaNV(maNV);
			nv.setHoTen(hoTen);
			nv.setGioiTinh(rbNam.isSelected());
			nv.setSdt(sdt);
			nv.setEmail(email);

			nv.setNgaySinh(LocalDate.parse(ngaySinh, DateTimeFormatter.ofPattern("dd/MM/yyyy")));

			nv.setDiachi(txtDiaChi.getText().trim());
			nv.setChucVu(cbChucVu.getSelectedItem().toString());

			String trangThai = cbTrangThai.getSelectedItem().toString();
			nv.setTrangThai(trangThai.equals("Đang làm việc"));

			nv.setNgayVaoLam(new java.util.Date());
			nv.setAnhDaiDien(duongDanAnh);

			if (nv_dao.create(nv)) {
				JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");

				loadDataNhanVien();
				xoaTrangForm();
			} else {
				JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại!");
			}

		} catch (DateTimeParseException e) {
			JOptionPane.showMessageDialog(this, "Ngày sinh không hợp lệ! Vui lòng nhập dd/MM/yyyy.");

		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}

	private void capNhatNhanVien() {
		int row = table.getSelectedRow();

		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần cập nhật!");
			return;
		}

		int modelRow = table.convertRowIndexToModel(row);

		String maNVCu = table.getModel().getValueAt(modelRow, 1).toString();

		String maNV = txtMaNV.getText().trim();
		String hoTen = txtHoTen.getText().trim();
		String sdt = txtSDT.getText().trim();
		String email = txtEmail.getText().trim();
		String ngaySinh = txtNgaySinh.getText().trim();

		if (maNV.isEmpty() || hoTen.isEmpty() || sdt.isEmpty() || email.isEmpty() || ngaySinh.isEmpty()) {

			JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
			return;
		}

		// Nếu đổi mã, kiểm tra mã mới có bị trùng không
		if (!maNV.equalsIgnoreCase(maNVCu) && nv_dao.kiemTraMaNV(maNV)) {

			JOptionPane.showMessageDialog(this, "Mã nhân viên đã tồn tại!");
			return;
		}

		try {
			NhanVien nv = new NhanVien();

			nv.setMaNV(maNV);
			nv.setHoTen(hoTen);
			nv.setGioiTinh(rbNam.isSelected());
			nv.setSdt(sdt);
			nv.setEmail(email);

			nv.setNgaySinh(LocalDate.parse(ngaySinh, DateTimeFormatter.ofPattern("dd/MM/yyyy")));

			nv.setDiachi(txtDiaChi.getText().trim());
			nv.setChucVu(cbChucVu.getSelectedItem().toString());

			String trangThai = cbTrangThai.getSelectedItem().toString();
			nv.setTrangThai(trangThai.equals("Đang làm việc"));

			nv.setAnhDaiDien(duongDanAnh);

			// Giữ nguyên ngày vào làm ban đầu
			ArrayList<NhanVien> dsNV = nv_dao.getNhanVienTheoMa(maNVCu);

			if (!dsNV.isEmpty()) {
				nv.setNgayVaoLam(dsNV.get(0).getNgayVaoLam());
			}

			boolean ketQua = nv_dao.update(nv, maNVCu);

			if (ketQua) {
				JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");

				loadDataNhanVien();
				xoaTrangForm();
			} else {
				JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thất bại!");
			}

		} catch (DateTimeParseException e) {
			JOptionPane.showMessageDialog(this, "Ngày sinh không hợp lệ! Vui lòng nhập dd/MM/yyyy.");

		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(this, e.getMessage());

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Lỗi cập nhật nhân viên: " + e.getMessage());
		}
	}

	// CHỌN ẢNH

	private void chonAnh() {
		JFileChooser chooser = new JFileChooser();

		chooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh", "jpg", "jpeg", "png"));

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			duongDanAnh = chooser.getSelectedFile().getAbsolutePath();

			ImageIcon icon = new ImageIcon(duongDanAnh);

			Image img = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);

			lblAvatarIcon.setText("");
			lblAvatarIcon.setIcon(new ImageIcon(img));
		}
	}

	// XÓA TRẮNG

	private void xoaTrangForm() {
		txtMaNV.setText("");
		txtHoTen.setText("");
		txtSDT.setText("");
		txtEmail.setText("");
		txtNgaySinh.setText("");
		txtDiaChi.setText("");

		// Giữ lại nếu quyết định sử dụng trường ghi chú
		txtGhiChu.setText("");

		rbNam.setSelected(true);

		cbChucVu.setSelectedIndex(0);
		cbTrangThai.setSelectedIndex(0);

		duongDanAnh = null;

		lblAvatarIcon.setIcon(null);
		lblAvatarIcon.setText("👤");

		table.clearSelection();
	}

// Đã sửa
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private void loadDataNhanVien() {
		DefaultTableModel model = (DefaultTableModel) table.getModel();

		model.setRowCount(0);

		ArrayList<NhanVien> dsNV = nv_dao.getalltblNhanVien();

		int stt = 1;

		for (NhanVien nv : dsNV) {
			model.addRow(toRow(stt++, nv));
		}
	}

	private Object[] toRow(int stt, NhanVien nv) {
		String trangThai = nv.isTrangThai() ? "Đang làm việc" : "Nghỉ việc/Tạm nghỉ";

		return new Object[] { stt, nv.getMaNV(), nv.getHoTen(), nv.isGioiTinh() ? "Nam" : "Nữ",
				nv.getNgaySinh() != null ? nv.getNgaySinh().format(DATE_FMT) : "", nv.getSdt(), nv.getEmail(),
				nv.getChucVu(), trangThai, "" };
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int row = table.getSelectedRow();

		if (row == -1)
			return;

		int modelRow = table.convertRowIndexToModel(row);

		txtMaNV.setText(table.getModel().getValueAt(modelRow, 1).toString());

		txtHoTen.setText(table.getModel().getValueAt(modelRow, 2).toString());

		String gioiTinh = table.getModel().getValueAt(modelRow, 3).toString();

		rbNam.setSelected(gioiTinh.equals("Nam"));
		rbNu.setSelected(gioiTinh.equals("Nữ"));

		txtNgaySinh.setText(table.getModel().getValueAt(modelRow, 4).toString());

		txtSDT.setText(table.getModel().getValueAt(modelRow, 5).toString());

		txtEmail.setText(table.getModel().getValueAt(modelRow, 6).toString());

		cbChucVu.setSelectedItem(table.getModel().getValueAt(modelRow, 7).toString());

		String trangThai = table.getModel().getValueAt(modelRow, 8).toString();

		cbTrangThai.setSelectedItem(trangThai);
	}

	private void xemChiTiet(int row) {
		JOptionPane.showMessageDialog(this,
				"Mã nhân viên: " + table.getValueAt(row, 1) + "\nHọ tên: " + table.getValueAt(row, 2) + "\nGiới tính: "
						+ table.getValueAt(row, 3) + "\nNgày sinh: " + table.getValueAt(row, 4) + "\nSố điện thoại: "
						+ table.getValueAt(row, 5) + "\nEmail: " + table.getValueAt(row, 6) + "\nChức vụ: "
						+ table.getValueAt(row, 7));
	}

	private void hienThiThongTin(int row) {

		txtMaNV.setText(table.getValueAt(row, 1).toString());
		txtHoTen.setText(table.getValueAt(row, 2).toString());

		rbNam.setSelected(table.getValueAt(row, 3).equals("Nam"));
		rbNu.setSelected(table.getValueAt(row, 3).equals("Nữ"));

		txtNgaySinh.setText(table.getValueAt(row, 4).toString());
		txtSDT.setText(table.getValueAt(row, 5).toString());
		txtEmail.setText(table.getValueAt(row, 6).toString());

		cbChucVu.setSelectedItem(table.getValueAt(row, 7));

		String trangThai = table.getValueAt(row, 8).toString();

		if (trangThai.equals("Đang làm việc")) {
			cbTrangThai.setSelectedIndex(0);
		} else {
			cbTrangThai.setSelectedIndex(1);
		}
	}

	private void hienThiTuyChon(int row, MouseEvent e) {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem itemXoa = new JMenuItem("Cho nghỉ việc");

		itemXoa.addActionListener(ev -> {
			int modelRow = table.convertRowIndexToModel(row);

			String maNV = table.getModel().getValueAt(modelRow, 1).toString();

			int confirm = JOptionPane.showConfirmDialog(this,
					"Bạn có chắc muốn chuyển nhân viên này sang trạng thái nghỉ việc?", "Xác nhận",
					JOptionPane.YES_NO_OPTION);

			if (confirm == JOptionPane.YES_OPTION) {
				boolean ketQua = nv_dao.capNhatTrangThaiNghiViec(maNV);

				if (ketQua) {
					JOptionPane.showMessageDialog(this, "Cập nhật trạng thái nhân viên thành công!");

					loadDataNhanVien();
					xoaTrangForm();
				} else {
					JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thất bại!");
				}
			}
		});

		menu.add(itemXoa);
		menu.show(table, e.getX(), e.getY());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}