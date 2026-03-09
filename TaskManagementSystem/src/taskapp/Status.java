package taskapp;

public enum Status {
    PENDING("Pending"),
    IN_PROGRESS("In-progress"),
    DONE("Done"),
    OVERDUE("Overdue");

    private final String label;

    Status(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}