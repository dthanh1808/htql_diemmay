package ui;

import java.awt.Component;
import java.awt.Container;
import java.util.IdentityHashMap;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

/** Chạy JDBC của hai mục Sản Phẩm/Khách Hàng ngoài EDT, cập nhật UI trên EDT. */
final class SanPhamKhachHangWorker {
    private final JComponent panel;
    private final Consumer<Throwable> failure;
    private final Map<Component, Boolean> enabled = new IdentityHashMap<>();
    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private boolean active;

    SanPhamKhachHangWorker(JComponent panel, Consumer<Throwable> failure) {
        this.panel = panel;
        this.failure = failure;
    }

    <T> void run(Callable<T> query, Consumer<T> success) {
        tasks.add(() -> start(query, success));
        next();
    }

    private void next() {
        if (active) return;
        Runnable task = tasks.poll();
        if (task == null) { panel.putClientProperty("productCustomer.busy", false); return; }
        active = true;
        task.run();
    }

    private <T> void start(Callable<T> query, Consumer<T> success) {
        panel.putClientProperty("productCustomer.busy", true);
        disable(panel);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return query.call(); }
            @Override protected void done() {
                enabled.forEach(Component::setEnabled);
                enabled.clear();
                try { success.accept(get()); }
                catch (ExecutionException e) { failure.accept(e.getCause()); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); failure.accept(e); }
                catch (RuntimeException e) { failure.accept(e); }
                finally { active = false; next(); }
            }
        }.execute();
    }

    private void disable(Container parent) {
        for (Component child : parent.getComponents()) {
            enabled.putIfAbsent(child, child.isEnabled());
            child.setEnabled(false);
            if (child instanceof Container container) disable(container);
        }
    }
}
