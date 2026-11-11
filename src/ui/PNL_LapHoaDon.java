//package ui;
//
//import dao.BanHangDAO;
//import entity.HoaDon;
//import service.HoaDonService;
//import java.awt.*;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//
//public class PNL_LapHoaDon extends JPanel {
//    private final JTextField customer=new JTextField(), phone=new JTextField(), voucher=new JTextField("0"), paid=new JTextField("0"), delivery=new JTextField(), promotion=new JTextField(), partner=new JTextField();
//    private final JComboBox<String> tier=new JComboBox<>(new String[]{"Khách lẻ","Member","Silver","Gold","Diamond"});
//    private final JComboBox<String> method=new JComboBox<>(new String[]{"Tiền mặt","Chuyển khoản","Trả góp"});
//    private final JComboBox<Integer> term=new JComboBox<>(new Integer[]{6,9,12,18,24});
//    private final JCheckBox confirmed=new JCheckBox("Đã nhận tiền / xác nhận chuyển khoản / hồ sơ trả góp đã duyệt và ký");
//    private final boolean sqlMode=HoaDonRepository.getInstance().isSql();
//    private final DefaultTableModel model=new DefaultTableModel(new String[]{"SKU","Tên sản phẩm","Danh mục","Số lượng","Đơn giá (đ)","Giảm tiền dòng (đ)"},0) {
//        @Override public boolean isCellEditable(int row,int col){return !sqlMode || col==3 || col==5;}
//    };
//    private final JTable table=new JTable(model);
//    private final JLabel total=new JLabel(" ");
//    private String employee, shift;
//    private LocalDate opened;
//    private final JTextField employeeInput = new JTextField(15);
//    private final JComboBox<String> shiftInput = new JComboBox<>(new String[]{"Ca sáng", "Ca chiều"});
//    private final JLabel shiftStatus = new JLabel("Chưa mở ca");
//    private final Runnable onSaved;
//    public PNL_LapHoaDon() { this(() -> {}); }
//    public PNL_LapHoaDon(Runnable saved) {
//        onSaved=saved;
//        setLayout(new BorderLayout()); setBackground(BusinessUI.BG);
//        JPanel root=new JPanel(new BorderLayout(10,10)); root.setBorder(BorderFactory.createEmptyBorder(15,15,15,15)); add(root);
//        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        heading.add(new JLabel("LẬP HÓA ĐƠN • Nhân viên")); heading.add(employeeInput); heading.add(shiftInput);
//        heading.add(BusinessUI.button(sqlMode?"Chọn / Bỏ chọn ca":"Mở / Đóng ca", this::toggleShift)); heading.add(shiftStatus);
//        shiftInput.setVisible(!sqlMode);
//        JPanel form=new JPanel(new GridLayout(0,4,10,8));
//        field(form,"Khách hàng",customer); field(form,"Hạng thành viên",tier); field(form,"SĐT (10 chữ số)",phone); field(form,"Phương thức",method);
//        field(form,"Giảm tiền hóa đơn (đ)",voucher); field(form,sqlMode?"Mã / tên khuyến mãi":"Tên chương trình / voucher",promotion); field(form,"Khách đưa / trả trước (đ)",paid); field(form,"Giao hàng / lắp đặt",delivery);
//        field(form,"Đối tác trả góp",partner); field(form,"Kỳ hạn (tháng)",term);
//        JPanel north = new JPanel(new BorderLayout(0, 10)); north.add(heading, BorderLayout.NORTH); north.add(form); root.add(north,BorderLayout.NORTH);
//        table.setRowHeight(30); table.putClientProperty("terminateEditOnFocusLost",Boolean.TRUE);
//        JPanel center=new JPanel(new BorderLayout(0,8)); center.add(new JScrollPane(table));
//        JPanel lineButtons=new JPanel(new FlowLayout(FlowLayout.LEFT));
//        lineButtons.add(BusinessUI.button("Thêm sản phẩm",this::addProduct));
//        if(sqlMode)lineButtons.add(BusinessUI.button("Tìm thành viên theo SĐT",this::findCustomer));
//        lineButtons.add(BusinessUI.button("Xóa dòng",()-> { if(table.getSelectedRow()>=0)model.removeRow(table.getSelectedRow()); }));
//        lineButtons.add(new JLabel("Nhập giá tại thời điểm bán; giảm dòng là số tiền cố định cho cả dòng.")); center.add(lineButtons,BorderLayout.SOUTH); root.add(center);
//        JPanel bottom=new JPanel(new GridLayout(0,1,0,8));
//        bottom.add(new JLabel("Hạng thành viên tự giảm 0% / 3% / 5% / 8% trên tiền hàng."));
//        total.setFont(new Font("Segoe UI",Font.BOLD,16)); total.setForeground(BusinessUI.BLUE); bottom.add(total); bottom.add(confirmed);
//        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT)); buttons.add(BusinessUI.button("Tính tiền",this::preview)); buttons.add(BusinessUI.button("Quay lại danh sách",onSaved)); buttons.add(BusinessUI.button("Xóa nội dung",this::clearForm)); buttons.add(BusinessUI.button("Xác nhận thanh toán & Lưu",this::save)); bottom.add(buttons); root.add(bottom,BorderLayout.SOUTH);
//        model.addTableModelListener(e->preview()); tier.addActionListener(e->preview()); voucher.addActionListener(e->preview());
//        method.addActionListener(e-> { boolean installment=method.getSelectedIndex()==2; partner.setEnabled(installment); term.setEnabled(installment); confirmed.setSelected(false); });
//        partner.setEnabled(false); term.setEnabled(false); preview();
//        if(sqlMode) { customer.setEditable(false);tier.setEnabled(false);phone.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
//            private void changed(){customer.setText("");tier.setSelectedIndex(0);}
//            public void insertUpdate(javax.swing.event.DocumentEvent e){changed();} public void removeUpdate(javax.swing.event.DocumentEvent e){changed();} public void changedUpdate(javax.swing.event.DocumentEvent e){changed();}
//        }); }
//    }
//    private void addProduct() {
//        if(!sqlMode){model.addRow(new Object[]{"","","",1,0,0});return;}
//        BusinessUI.background(this,()->new BanHangDAO().products(),products->{
//            if(products.isEmpty()){JOptionPane.showMessageDialog(this,"Chưa có sản phẩm còn kinh doanh và còn tồn kho.");return;}
//            BanHangDAO.SanPham selected=(BanHangDAO.SanPham)JOptionPane.showInputDialog(this,"Chọn sản phẩm từ SQL Server","Thêm sản phẩm",JOptionPane.PLAIN_MESSAGE,null,products.toArray(),products.get(0));
//            if(selected==null)return;
//            for(int i=0;i<model.getRowCount();i++)if(selected.sku().equals(text(i,0))){
//                try{model.setValueAt(Math.addExact(Integer.parseInt(text(i,3)),1),i,3);}catch(RuntimeException e){BusinessUI.error(this,new IllegalArgumentException("Số lượng hiện tại không hợp lệ."));}return;
//            }
//            model.addRow(new Object[]{selected.sku(),selected.name(),selected.category(),1,selected.price().toPlainString(),0});
//        });
//    }
//    private void findCustomer() {
//        String value=phone.getText().trim();
//        if(!value.matches("[0-9]{10}")){BusinessUI.error(this,new IllegalArgumentException("Nhập SĐT gồm 10 chữ số."));return;}
//        BusinessUI.background(this,()->new BanHangDAO().customer(value),found->{
//            if(!phone.getText().trim().equals(value))return;
//            customer.setText(found.name());tier.setSelectedItem(found.tier());
//        });
//    }
//    private void toggleShift() {
//        if(sqlMode){selectSqlShift();return;}
//        if (employee != null && BusinessUI.now().toLocalDate().equals(opened)) {
//            if (JOptionPane.showConfirmDialog(this,"Đóng ca hiện tại?","Ca làm việc",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
//            employee=null; shift=null; opened=null; employeeInput.setEnabled(true); shiftInput.setEnabled(true); shiftStatus.setText("Chưa mở ca"); return;
//        }
//        if (employeeInput.getText().isBlank()) { JOptionPane.showMessageDialog(this,"Nhập mã / tên nhân viên trước khi mở ca."); return; }
//        employee=employeeInput.getText().trim(); shift=shiftInput.getSelectedItem().toString(); opened=BusinessUI.now().toLocalDate();
//        employeeInput.setEnabled(false); shiftInput.setEnabled(false); shiftStatus.setText("Đang mở • " + opened.format(BusinessUI.DATE));
//    }
//    private void selectSqlShift() {
//        if(employee!=null){employee=null;shift=null;opened=null;employeeInput.setEnabled(true);shiftStatus.setText("Chưa chọn ca");return;}
//        String id=employeeInput.getText().trim();
//        if(id.isEmpty()){JOptionPane.showMessageDialog(this,"Nhập mã nhân viên trong SQL Server.");return;}
//        BusinessUI.background(this,()->new BanHangDAO().shifts(id),shifts->{
//            if(shifts.isEmpty()){JOptionPane.showMessageDialog(this,"Nhân viên chưa có ca đang mở được phân công trong ngày.");return;}
//            BanHangDAO.Ca ca=(BanHangDAO.Ca)JOptionPane.showInputDialog(this,"Chọn ca đang mở được phân công","Ca bán hàng",JOptionPane.PLAIN_MESSAGE,null,shifts.toArray(),shifts.get(0));
//            if(ca==null)return;
//            employee=id;shift=ca.id();opened=BusinessUI.now().toLocalDate();employeeInput.setText(id);employeeInput.setEnabled(false);shiftStatus.setText("Đang chọn • "+ca);
//        });
//    }
//    private void clearForm() {
//        if (table.isEditing()) table.getCellEditor().cancelCellEditing();
//        customer.setText(""); phone.setText(""); voucher.setText("0"); paid.setText("0"); delivery.setText(""); promotion.setText(""); partner.setText("");
//        tier.setSelectedIndex(0); method.setSelectedIndex(0); term.setSelectedIndex(0); model.setRowCount(0); confirmed.setSelected(false); preview();
//    }
//    private static void field(JPanel p,String name,JComponent input) { p.add(new JLabel(name)); p.add(input); }
//    private BigDecimal amount(Object value) {
//        String s=String.valueOf(value).trim();
//        if(!s.matches("[0-9]{1,15}"))throw new IllegalArgumentException("Số tiền phải là số nguyên không âm, tối đa 15 chữ số (không nhập dấu phân cách).");
//        return new BigDecimal(s);
//    }
//    private HoaDon draft() {
//        if(table.isEditing()&&!table.getCellEditor().stopCellEditing())throw new IllegalArgumentException("Hoàn tất dòng sản phẩm đang sửa.");
//        List<HoaDon.Dong> lines=new ArrayList<>(); BigDecimal base=BigDecimal.ZERO;
//        for(int i=0;i<model.getRowCount();i++) {
//            String sku=text(i,0),name=text(i,1),category=text(i,2); int qty;
//            try { qty=Integer.parseInt(text(i,3)); }catch(NumberFormatException e){throw new IllegalArgumentException("Số lượng phải là số nguyên dương.");}
//            HoaDon.Dong d=new HoaDon.Dong(sku,name,category,qty,amount(model.getValueAt(i,4)),amount(model.getValueAt(i,5))); lines.add(d); base=base.add(d.tienHang());
//        }
//        String rank=tier.getSelectedItem().toString();
//        BigDecimal discount=amount(voucher.getText()).add(HoaDonService.uuDai(rank,base));
//        return new HoaDon("",BusinessUI.now(),employee,shift,rank.equals("Khách lẻ")&&customer.getText().isBlank()?"Khách lẻ":customer.getText().trim(),phone.getText().trim(),rank,
//            method.getSelectedItem().toString(),method.getSelectedIndex()==2?HoaDon.TRA_GOP:HoaDon.DA_TT,discount,amount(paid.getText()),delivery.getText().trim(),promotion.getText().trim(),partner.getText().trim(),method.getSelectedIndex()==2?(Integer)term.getSelectedItem():0,lines);
//    }
//    private String text(int r,int c) { Object v=model.getValueAt(r,c); return v==null?"":v.toString().trim(); }
//    private boolean previewing;
//    private void preview() {
//        if(previewing)return; previewing=true;
//        try { HoaDon h=draft(); total.setText("Tiền hàng: "+BusinessUI.money(h.tienHang())+"  |  Giảm: "+BusinessUI.money(h.giamGia())+"  |  Thanh toán: "+BusinessUI.money(h.tongTien())); }
//        catch(Exception e){total.setText(e.getMessage());} finally{previewing=false;}
//    }
//    private void save() {
//        try {
//            if(employee == null || opened == null || !opened.equals(BusinessUI.now().toLocalDate()))throw new IllegalArgumentException("Mở ca trong ngày trước khi lập hóa đơn.");
//            if(!confirmed.isSelected())throw new IllegalArgumentException("Cần xác nhận thanh toán hoặc hồ sơ trả góp đã duyệt và ký.");
//            HoaDon h=draft(); HoaDonService.validate(h);
//            if(JOptionPane.showConfirmDialog(this,"Lưu hóa đơn "+BusinessUI.money(h.tongTien())+"? Hóa đơn đã lưu không chỉnh sửa.","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
//            if(sqlMode)BusinessUI.background(this,()->HoaDonRepository.getInstance().save(h),saved->{clearForm();JOptionPane.showMessageDialog(this,"Đã lưu hóa đơn "+saved.ma());onSaved.run();});
//            else {HoaDon saved=HoaDonRepository.getInstance().save(h);clearForm();JOptionPane.showMessageDialog(this,"Đã lưu hóa đơn "+saved.ma());onSaved.run();}
//        }catch(Exception e){BusinessUI.error(this,e);}
//    }
//}
