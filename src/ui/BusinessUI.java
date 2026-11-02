package ui;
import java.awt.*;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

final class BusinessUI {
    static final Color BLUE = new Color(37,99,235), BG = new Color(245,247,250);
    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    static LocalDateTime now() { return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")); }
    static String money(Object n) { return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(n) + " đ"; }
    static DefaultTableModel model(String... c) { return new DefaultTableModel(c,0) {
        public boolean isCellEditable(int r,int col) { return false; }
        public Class<?> getColumnClass(int col) { return getRowCount()==0 || getValueAt(0,col)==null ? Object.class : getValueAt(0,col).getClass(); }
    }; }
    static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m); t.setRowHeight(32); t.setAutoCreateRowSorter(true); t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFont(new Font("Segoe UI",Font.PLAIN,12)); t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        t.setDefaultRenderer(java.math.BigDecimal.class,new javax.swing.table.DefaultTableCellRenderer(){
            protected void setValue(Object value){setHorizontalAlignment(SwingConstants.RIGHT);setText(value==null?"":NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(value));}
        });
        t.setDefaultRenderer(LocalDateTime.class,new javax.swing.table.DefaultTableCellRenderer(){protected void setValue(Object value){setText(value==null?"":((LocalDateTime)value).format(TIME));}});
        return t;
    }
    static JButton button(String text, Runnable action) { JButton b = new JButton(text); b.setFocusPainted(false); b.addActionListener(e -> action.run()); return b; }
    /** Chạy JDBC ngoài EDT; khóa panel trong lúc giao dịch để tránh lưu hai lần. */
    static <T> void background(JComponent parent,java.util.concurrent.Callable<T> work,java.util.function.Consumer<T> done) {
        if(Boolean.TRUE.equals(parent.getClientProperty("database.busy")))return;
        parent.putClientProperty("database.busy",true);
        java.util.Map<Component,Boolean> states=new java.util.IdentityHashMap<>();disable(parent,states);
        new SwingWorker<T,Void>() {
            protected T doInBackground() throws Exception{return work.call();}
            protected void done(){
                states.forEach(Component::setEnabled);parent.putClientProperty("database.busy",false);
                try{done.accept(get());}catch(Exception e){Throwable cause=e instanceof java.util.concurrent.ExecutionException?e.getCause():e;error(parent,cause instanceof Exception ex?ex:new Exception(cause));}
            }
        }.execute();
    }
    private static void disable(Container root,java.util.Map<Component,Boolean> states){for(Component c:root.getComponents()){states.put(c,c.isEnabled());c.setEnabled(false);if(c instanceof Container container)disable(container,states);}}
    static void error(Component p, Exception e) { JOptionPane.showMessageDialog(p,e.getMessage(),"Không thể thực hiện",JOptionPane.ERROR_MESSAGE); }
    static Path choose(Component parent,String name,String ext) {
        JFileChooser c = new JFileChooser(); c.setSelectedFile(new java.io.File(name + "." + ext));
        c.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(ext.toUpperCase(),ext));
        if (c.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return null;
        Path p = c.getSelectedFile().toPath();
        if (!p.toString().toLowerCase(Locale.ROOT).endsWith("." + ext)) p = Path.of(p + "." + ext);
        if (java.nio.file.Files.exists(p) && JOptionPane.showConfirmDialog(parent,"Ghi đè tệp đã tồn tại?","Xuất tệp",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return null;
        return p;
    }
}
