package taskapp;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TaskApp extends JFrame {
    private final TaskManager manager = new TaskManager();
    private final TaskTableModel model = new TaskTableModel(manager);
    private final JTable table = new JTable(model);

    // Input fields
    private final JTextField titleField = new JTextField();
    private final JTextArea descArea = new JTextArea(3, 20);
    private final JSpinner dueSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE));
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    private final JComboBox<Status> statusBox = new JComboBox<>(Status.values());

    private final ReminderService reminders = new ReminderService(manager, model);

    public TaskApp() {
        super("Task Management with Reminders");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        initTable();
        JPanel form = buildFormPanel();
        JScrollPane tableScroll = new JScrollPane(table);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, form);
        split.setResizeWeight(0.65);
        add(split, BorderLayout.CENTER);

        reminders.start();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                reminders.stop();
            }
        });
    }

    private void initTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.setDefaultRenderer(Object.class, new OverdueRowRenderer(model));

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && row >= 0) {
                Task t = model.taskAt(row);
                titleField.setText(t.getTitle());
                descArea.setText(t.getDescription());

                if (t.getDueDateTime() != null) {
                    Date d = Date.from(t.getDueDateTime().atZone(ZoneId.systemDefault()).toInstant());
                    dueSpinner.setValue(d);
                }

                priorityBox.setSelectedItem(t.getPriority());
                statusBox.setSelectedItem(t.getStatus());
            }
        });
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSpinner.DateEditor editor = new JSpinner.DateEditor(dueSpinner, "yyyy-MM-dd HH:mm");
        dueSpinner.setEditor(editor);

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; 
        c.gridy = 0;
        fields.add(new JLabel("Title:"), c);
        c.gridx = 1; 
        c.weightx = 1.0;
        fields.add(titleField, c);

        c.gridx = 0; 
        c.gridy++;
        c.weightx = 0;
        fields.add(new JLabel("Description:"), c);
        c.gridx = 1; 
        c.weightx = 1.0;
        JScrollPane descScroll = new JScrollPane(descArea);
        fields.add(descScroll, c);

        c.gridx = 0; 
        c.gridy++;
        fields.add(new JLabel("Due Date (yyyy-MM-dd HH:mm):"), c);
        c.gridx = 1;
        fields.add(dueSpinner, c);

        c.gridx = 0; 
        c.gridy++;
        fields.add(new JLabel("Priority:"), c);
        c.gridx = 1;
        fields.add(priorityBox, c);

        c.gridx = 0; 
        c.gridy++;
        fields.add(new JLabel("Status:"), c);
        c.gridx = 1;
        fields.add(statusBox, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton doneBtn = new JButton("Mark as Done");

        addBtn.addActionListener(e -> onAdd());
        updateBtn.addActionListener(e -> onUpdate());
        deleteBtn.addActionListener(e -> onDelete());
        doneBtn.addActionListener(e -> onMarkDone());

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(doneBtn);

        panel.add(fields, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private LocalDateTime spinnerToLdt() {
        Date d = (Date) dueSpinner.getValue();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(d.getTime()), ZoneId.systemDefault());
    }

    private void onAdd() {
        try {
            String title = titleField.getText().trim();
            String desc = descArea.getText().trim();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a task title.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDateTime due = spinnerToLdt();
            Priority pr = (Priority) priorityBox.getSelectedItem();
            Status st = (Status) statusBox.getSelectedItem();

            model.addTask(new Task(title, desc, due, pr, st));
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date and time.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdate() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a task first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            String title = titleField.getText().trim();
            String desc = descArea.getText().trim();
            LocalDateTime due = spinnerToLdt();
            Priority pr = (Priority) priorityBox.getSelectedItem();
            Status st = (Status) statusBox.getSelectedItem();

            model.updateTask(row, new Task(title, desc, due, pr, st));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int ok = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected task?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                model.removeTask(row);
                clearForm();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a task first.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onMarkDone() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            Task t = model.taskAt(row);
            t.setStatus(Status.DONE);
            model.refreshAll();
        } else {
            JOptionPane.showMessageDialog(this, "Select a task first.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearForm() {
        titleField.setText("");
        descArea.setText("");
        dueSpinner.setValue(new Date());
        priorityBox.setSelectedItem(Priority.MEDIUM);
        statusBox.setSelectedItem(Status.PENDING);
    }

    /** Highlights overdue rows in red and bold */
    static class OverdueRowRenderer extends DefaultTableCellRenderer {
        private final TaskTableModel model;
        private final Color overdueBg = new Color(255, 230, 230);
        private final Color overdueFg = new Color(180, 0, 0);

        public OverdueRowRenderer(TaskTableModel model) { this.model = model; }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                      boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Task t = model.taskAt(row);
            boolean isOverdue = t != null && t.getStatus() == Status.OVERDUE;

            if (isSelected) {
                if (isOverdue) {
                    c.setBackground(new Color(255, 200, 200));
                    c.setForeground(overdueFg);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
            } else {
                if (isOverdue) {
                    c.setBackground(overdueBg);
                    c.setForeground(overdueFg);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
            }
            return c;
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            TaskApp app = new TaskApp();
            app.setVisible(true);
        });
    }
}