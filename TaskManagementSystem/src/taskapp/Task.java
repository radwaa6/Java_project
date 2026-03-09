package taskapp;

import java.time.LocalDateTime;

public class Task {
    private String title;
    private String description;
    private LocalDateTime dueDateTime;
    private Priority priority;
    private Status status;

    public Task(String title, String description, LocalDateTime dueDateTime, Priority priority, Status status) {
        this.title = title;
        this.description = description;
        this.dueDateTime = dueDateTime;
        this.priority = priority;
        this.status = status;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDueDateTime() { return dueDateTime; }
    public void setDueDateTime(LocalDateTime dueDateTime) { this.dueDateTime = dueDateTime; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public boolean isOverdue() {
        return dueDateTime != null && LocalDateTime.now().isAfter(dueDateTime);
    }
}