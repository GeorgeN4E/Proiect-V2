package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Task implements Serializable {
    private String title;
    private String description;
    private boolean isCompleted;
    private LocalDate dueDate;

    public Task(String title, String description, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.isCompleted = false;
        this.dueDate = dueDate;
    }

    public void markComplete() {
        isCompleted = true;
    }

    public void markIncomplete() {
        isCompleted = false;
    }

    public void editTask(String newTitle, String newDescription, LocalDate newDueDate) {
        this.title = newTitle;
        this.description = newDescription;
        this.dueDate = newDueDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    @Override
    public String toString() {
        String status = isCompleted ? "Completed" : "Pending";
        String due = (dueDate != null) ? " Due: " + dueDate : "";
        return title + " - " + description + " [" + status + "]" + due;
    }
}
