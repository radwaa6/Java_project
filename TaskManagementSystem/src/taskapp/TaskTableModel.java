package taskapp;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;

public class TaskTableModel extends AbstractTableModel {
    private final TaskManager manager;
    // Column headers in English
    private final String[] columns = {"Title", "Description", "Due Date", "Priority", "Status"};
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TaskTableModel(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public int getRowCount() {
        return manager.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Task t = manager.get(rowIndex);
        if (t == null) return "";
        switch (columnIndex) {
            case 0: return t.getTitle();
            case 1: return t.getDescription();
            case 2: return t.getDueDateTime() == null ? "" : fmt.format(t.getDueDateTime());
            case 3: return t.getPriority(); // relies on Priority.toString() -> "High/Medium/Low"
            case 4: return t.getStatus();   // relies on Status.toString() -> "Pending/..."
            default: return "";
        }
    }

    public Task taskAt(int row) {
        return manager.get(row);
    }

    public void addTask(Task t) {
        manager.addTask(t);
        int idx = manager.size() - 1;
        fireTableRowsInserted(idx, idx);
    }

    public void updateTask(int row, Task t) {
        manager.updateTask(row, t);
        fireTableRowsUpdated(row, row);
    }

    public void removeTask(int row) {
        manager.removeTask(row);
        fireTableRowsDeleted(row, row);
    }

    public void refreshAll() {
        fireTableDataChanged();
    }
}