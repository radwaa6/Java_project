package taskapp;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private final List<Task> tasks = new ArrayList<>();

    public synchronized void addTask(Task t) { tasks.add(t); }

    public synchronized void updateTask(int index, Task updated) {
        if (index >= 0 && index < tasks.size()) tasks.set(index, updated);
    }

    public synchronized void removeTask(int index) {
        if (index >= 0 && index < tasks.size()) tasks.remove(index);
    }

    public synchronized Task get(int index) {
        if (index >= 0 && index < tasks.size()) return tasks.get(index);
        return null;
    }

    public synchronized List<Task> getAll() {
        return new ArrayList<>(tasks);
    }

    public synchronized int size() { return tasks.size(); }
}