package taskapp;

import javax.swing.SwingUtilities;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.List; // for Java 8 compatibility
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Checks every minute for overdue tasks, marks them OVERDUE, and shows a System Tray notification. */
public class ReminderService {
    private final TaskManager manager;
    private final TaskTableModel model;
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private TrayIcon trayIcon;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReminderService(TaskManager manager, TaskTableModel model) {
        this.manager = manager;
        this.model = model;
        initTray();
    }

    /** Initialize a simple tray icon if the OS supports System Tray. */
    private void initTray() {
        try {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();
                // Simple 16x16 red dot icon
                Image img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                Graphics g = img.getGraphics();
                g.setColor(new Color(220, 0, 0));
                g.fillOval(0, 0, 16, 16);
                g.dispose();

                trayIcon = new TrayIcon(img, "Task Reminders");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
            }
        } catch (Exception ignored) {
            trayIcon = null; // if tray not available or failed, just disable notifications
        }
    }

    /** Start checking every minute. */
    public void start() {
        exec.scheduleAtFixedRate(this::checkOverdues, 0, 1, TimeUnit.MINUTES);
    }

    /** Stop the scheduler and remove the tray icon. */
    public void stop() {
        exec.shutdownNow();
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }

    /** Scan all tasks and mark overdue ones, then refresh the table. */
    private void checkOverdues() {
        boolean anyChange = false;

        // Use List<Task> for Java 8 compatibility (avoid 'var')
        List<Task> tasks = manager.getAll();

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (t.getStatus() != Status.DONE && t.getStatus() != Status.OVERDUE && t.isOverdue()) {
                t.setStatus(Status.OVERDUE);
                anyChange = true;
                notifyOverdue(t);
            }
        }

        if (anyChange) {
            SwingUtilities.invokeLater(model::refreshAll);
        }
    }

    /** Notify the user when a task becomes overdue. */
    private void notifyOverdue(Task t) {
        String title = "Task Overdue";
        String msg = "Task: " + t.getTitle() + " is overdue since " + fmt.format(t.getDueDateTime());

        if (trayIcon != null) {
            trayIcon.displayMessage(title, msg, TrayIcon.MessageType.ERROR);
        } else {
            // Fallback if System Tray is not supported (some Linux setups)
            System.out.println("[Reminder] " + msg);
        }
        Toolkit.getDefaultToolkit().beep();
    }
}
