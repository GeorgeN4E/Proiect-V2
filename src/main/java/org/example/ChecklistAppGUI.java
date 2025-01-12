import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import model.Checklist;
import model.Task;
import model.FileManager;
import model.MailSender;
import java.time.LocalDate;


public class ChecklistAppGUI {
    private Checklist checklist;
    private JFrame frame;
    private DefaultListModel<String> taskListModel;
    private JList<String> taskList;
    private MailSender mailSender;
    private FileManager fileManager;
    private List<String> savedEmails; // List of saved email addresses

    public ChecklistAppGUI() {
        this.checklist = new Checklist();
        this.mailSender = new MailSender("en", "checklistultaupreferat");
        this.fileManager = new FileManager("checklist.dat");
        this.savedEmails = new ArrayList<>(); // Initialize the list of saved emails
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
        JButton addEmailButton = new JButton("Add Email");
        JButton sendSummaryButton = new JButton("Send Summary");

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(addEmailButton);
        buttonPanel.add(sendSummaryButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        addButton.addActionListener(e -> addTask());
        removeButton.addActionListener(e -> removeTask());
        toggleButton.addActionListener(e -> toggleTaskCompletion());
        saveButton.addActionListener(e -> saveChecklist());
        loadButton.addActionListener(e -> loadChecklist());
        addEmailButton.addActionListener(e -> addEmail());
        sendSummaryButton.addActionListener(e -> sendSummary());

        frame.setVisible(true);
        loadChecklist();
    }

    private void addTask() {
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
        try {
            checklist.addTask(new Task(title, description, LocalDate.parse(dueDateInput)));
            updateTaskList();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Invalid date format. Use YYYY-MM-DD.");
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

    private void addEmail() {
        String email = JOptionPane.showInputDialog(frame, "Enter email address to add:");
        if (email == null || email.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Email address cannot be empty.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            JOptionPane.showMessageDialog(frame, "Invalid email address format.");
            return;
        }

        savedEmails.add(email);
        JOptionPane.showMessageDialog(frame, "Email added successfully.");
    }

    private void sendSummary() {
        if (savedEmails.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No saved emails. Please add an email first.");
            return;
        }

        // Display a dropdown for email selection
        String[] emailArray = savedEmails.toArray(new String[0]);
        String selectedEmail = (String) JOptionPane.showInputDialog(
                frame,
                "Select an email to send the summary:",
                "Send Summary",
                JOptionPane.QUESTION_MESSAGE,
                null,
                emailArray,
                emailArray[0]
        );

        if (selectedEmail == null || selectedEmail.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No email selected.");
            return;
        }

        // Build the email summary
        StringBuilder emailBody = new StringBuilder("Checklist Summary:\n\n");
        for (int i = 0; i < checklist.getTaskCount(); i++) {
            Task task = checklist.getTask(i);
            emailBody.append("- ").append(task.getTitle())
                    .append(" [").append(task.isCompleted() ? "Completed" : "Incomplete").append("]\n")
                    .append("  Description: ").append(task.getDescription()).append("\n")
                    .append("  Due Date: ").append(task.getDueDate()).append("\n\n");
        }

        boolean success = mailSender.sendMail(selectedEmail, "Checklist Summary", emailBody.toString(), 1, "");
        if (success) {
            JOptionPane.showMessageDialog(frame, "Email sent successfully to " + selectedEmail);
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
