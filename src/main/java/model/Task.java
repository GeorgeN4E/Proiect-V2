package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Task implements Serializable {
    private String title;
    private String description;
    private boolean isCompleted;
    private LocalDate dueDate;

    // Constructor
    public Task(String title, String description, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.isCompleted = false;
        this.dueDate = dueDate;
    }

    // Marks the task as complete
    public void markComplete() {
        isCompleted = true;
    }

    // Marks the task as incomplete
    public void markIncomplete() {
        isCompleted = false;
    }

    // Edits the task's details
    public void editTask(String newTitle, String newDescription, LocalDate newDueDate) {
        this.title = newTitle;
        this.description = newDescription;
        this.dueDate = newDueDate;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    // String representation of the task
    @Override
    public String toString() {
        String status = isCompleted ? "Completed" : "Pending";
        String due = (dueDate != null) ? " Due: " + dueDate : "";
        return title + " - " + description + " [" + status + "]" + due;
    }
}
