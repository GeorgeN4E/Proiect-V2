import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import model.Checklist;
import model.Task;
import model.FileManager;
import model.MailSender;

public class ChecklistAppGUI {
    private Checklist checklist;
    private JFrame frame;
    private DefaultListModel<String> taskListModel;
    private JList<String> taskList;
    private MailSender mailSender;
    private FileManager fileManager;

    public ChecklistAppGUI() {
        this.checklist = new Checklist();
        this.mailSender = new MailSender("en", "georgeradu190@yahoo.com");
        this.fileManager = new FileManager("checklist.dat"); // Use FileManager with a predefined filename
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Checklist Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Task list panel
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        frame.add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Task");
        JButton removeButton = new JButton("Remove Task");
        JButton toggleButton = new JButton("Toggle Completion");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");
        JButton sendEmailButton = new JButton("Send Email");

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(sendEmailButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        addButton.addActionListener(e -> addTask());
        removeButton.addActionListener(e -> removeTask());
        toggleButton.addActionListener(e -> toggleTaskCompletion());
        saveButton.addActionListener(e -> saveChecklist());
        loadButton.addActionListener(e -> loadChecklist());
        sendEmailButton.addActionListener(e -> sendEmail());

        frame.setVisible(true);
    }

    private void addTask() {
        try {
            String title = JOptionPane.showInputDialog(frame, "Enter task title:");
            if (title == null || title.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Task title cannot be empty.");
                return;
            }

            String description = JOptionPane.showInputDialog(frame, "Enter task description:");
            if (description == null || description.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Task description cannot be empty.");
                return;
            }

            String dueDateInput = JOptionPane.showInputDialog(frame, "Enter due date (YYYY-MM-DD):");
            if (dueDateInput == null || dueDateInput.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Due date cannot be empty.");
                return;
            }

            LocalDate dueDate = LocalDate.parse(dueDateInput);
            checklist.addTask(new Task(title, description, dueDate));
            updateTaskList();
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(frame, "Invalid date format. Please use YYYY-MM-DD.");
        }
    }

    private void removeTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            checklist.removeTask(index);
            updateTaskList();
        } else {
            JOptionPane.showMessageDialog(frame, "No task selected to remove.");
        }
    }

    private void toggleTaskCompletion() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            checklist.toggleTaskCompletion(index);
            updateTaskList();
        } else {
            JOptionPane.showMessageDialog(frame, "No task selected to toggle completion.");
        }
    }

    private void saveChecklist() {
        fileManager.save(checklist);
        JOptionPane.showMessageDialog(frame, "Checklist saved successfully.");
    }

    private void loadChecklist() {
        checklist = fileManager.load();
        updateTaskList();
        JOptionPane.showMessageDialog(frame, "Checklist loaded successfully.");
    }

    private void sendEmail() {
        if (checklist.getTaskCount() == 0) {
            JOptionPane.showMessageDialog(frame, "Checklist is empty. Cannot send an email.");
            return;
        }

        StringBuilder emailBody = new StringBuilder("Checklist Summary:\n\n");
        for (int i = 0; i < checklist.getTaskCount(); i++) {
            Task task = checklist.getTask(i);
            emailBody.append("- ").append(task.getTitle())
                    .append(" [").append(task.isCompleted() ? "Completed" : "Incomplete").append("]\n")
                    .append("  Description: ").append(task.getDescription()).append("\n")
                    .append("  Due Date: ").append(task.getDueDate()).append("\n\n");
        }

        String recipient = JOptionPane.showInputDialog(frame, "Enter recipient email:");
        if (recipient == null || recipient.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Recipient email cannot be empty.");
            return;
        }

        boolean success = mailSender.sendMail(recipient, "Checklist Summary", emailBody.toString(), 1, "");
        if (success) {
            JOptionPane.showMessageDialog(frame, "Email sent successfully!");
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to send email.");
        }
    }

    private void updateTaskList() {
        taskListModel.clear();
        for (int i = 0; i < checklist.getTaskCount(); i++) {
            Task task = checklist.getTask(i);
            taskListModel.addElement(task.getTitle() + " (" + (task.isCompleted() ? "Completed" : "Incomplete") + ")");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChecklistAppGUI::new);
    }
}
