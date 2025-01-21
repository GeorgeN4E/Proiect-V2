package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Checklist implements Serializable {
    private List<Task> tasks;

    public Checklist() {
        tasks = new ArrayList<>();
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void removeTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.remove(index);
        } else {
            System.out.println("Invalid task index.");
        }
    }

    public void toggleTaskCompletion(int index) {
        if (index >= 0 && index < tasks.size()) {
            Task task = tasks.get(index);
            if (task.isCompleted()) {
                task.markIncomplete();
            } else {
                task.markComplete();
            }
        } else {
            System.out.println("Invalid task index.");
        }
    }

    public void editTask(int index, String newTitle, String newDescription, LocalDate newDueDate) {
        if (index >= 0 && index < tasks.size()) {
            tasks.get(index).editTask(newTitle, newDescription, newDueDate);
        } else {
            System.out.println("Invalid task index.");
        }
    }

    public void displayTaskSummary() {
        long completedCount = tasks.stream().filter(Task::isCompleted).count();
        System.out.println("Total Tasks: " + tasks.size());
        System.out.println("Completed Tasks: " + completedCount);
        System.out.println("Pending Tasks: " + (tasks.size() - completedCount));
    }

    public void sortTasksByTitle() {
        tasks.sort(Comparator.comparing(task -> task.getTitle().toLowerCase()));
    }

    public void sortTasksByDueDate() {
        tasks.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    public int getTaskCount() {
        return tasks.size();
    }

    public Task getTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            return tasks.get(index);
        } else {
            throw new IndexOutOfBoundsException("Invalid task index.");
        }
    }

    public List<Task> getTasks() {
        return tasks;
    }
}
