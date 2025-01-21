import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import model.Checklist;
import model.Task;
import model.FileManager;
import model.MailSender;
import java.time.LocalDate;
import java.io.*;

public class ChecklistAppGUI {
    private Checklist checklist;
    private JFrame frame;
    private DefaultListModel<String> taskListModel;
    private JList<String> taskList;
    private MailSender mailSender;
    private FileManager fileManager;
    private List<String> savedEmails;
    private static final String EMAIL_FILE = "emails.dat";

    public ChecklistAppGUI() {
        this.checklist = new Checklist();
        this.mailSender = new MailSender("en", "checklistultaupreferat");
        this.fileManager = new FileManager("checklist.dat");
        this.savedEmails = loadEmails();
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Checklist Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Task list panel
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        frame.add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1));

        // Task buttons
        JPanel taskButtonPanel = new JPanel();
        JButton addButton = new JButton("Add Task");
        JButton removeButton = new JButton("Remove Task");
        JButton toggleButton = new JButton("Toggle Completion");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");

        taskButtonPanel.add(addButton);
        taskButtonPanel.add(removeButton);
        taskButtonPanel.add(toggleButton);
        taskButtonPanel.add(saveButton);
        taskButtonPanel.add(loadButton);

        JPanel emailButtonPanel = new JPanel();
        JButton emailButton = new JButton("Manage Emails");

        emailButtonPanel.add(emailButton);

        buttonPanel.add(taskButtonPanel);
        buttonPanel.add(emailButtonPanel);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addTask());
        removeButton.addActionListener(e -> removeTask());
        toggleButton.addActionListener(e -> toggleTaskCompletion());
        saveButton.addActionListener(e -> saveChecklist());
        loadButton.addActionListener(e -> loadChecklist());
        emailButton.addActionListener(e -> openEmailManager());

        frame.setVisible(true);
        loadChecklist();
    }

    private void openEmailManager() {
        JDialog emailDialog = new JDialog(frame, "Email Manager", true);
        emailDialog.setSize(400, 300);
        emailDialog.setLayout(new BorderLayout());

        DefaultListModel<String> emailListModel = new DefaultListModel<>();
        savedEmails.forEach(emailListModel::addElement);
        JList<String> emailList = new JList<>(emailListModel);
        emailDialog.add(new JScrollPane(emailList), BorderLayout.CENTER);

        JPanel emailControlPanel = new JPanel();
        JButton addEmailButton = new JButton("Add Email");
        JButton deleteEmailButton = new JButton("Delete Email");
        JButton sendSummaryButton = new JButton("Send Summary");

        emailControlPanel.add(addEmailButton);
        emailControlPanel.add(deleteEmailButton);
        emailControlPanel.add(sendSummaryButton);
        emailDialog.add(emailControlPanel, BorderLayout.SOUTH);

        addEmailButton.addActionListener(e -> {
            String email = JOptionPane.showInputDialog(emailDialog, "Enter email address:");
            if (email != null && !email.trim().isEmpty() && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                savedEmails.add(email);
                emailListModel.addElement(email);
                saveEmails();
            } else {
                JOptionPane.showMessageDialog(emailDialog, "Invalid or empty email.");
            }
        });

        deleteEmailButton.addActionListener(e -> {
            int selectedIndex = emailList.getSelectedIndex();
            if (selectedIndex != -1) {
                savedEmails.remove(selectedIndex);
                emailListModel.remove(selectedIndex);
                saveEmails();
            } else {
                JOptionPane.showMessageDialog(emailDialog, "No email selected.");
            }
        });

        sendSummaryButton.addActionListener(e -> {
            int selectedIndex = emailList.getSelectedIndex();
            if (selectedIndex != -1) {
                String selectedEmail = savedEmails.get(selectedIndex);
                sendSummary(selectedEmail);
            } else {
                JOptionPane.showMessageDialog(emailDialog, "No email selected.");
            }
        });

        emailDialog.setVisible(true);
    }

    private void sendSummary(String email) {
        StringBuilder emailBody = new StringBuilder("Checklist Summary:\n\n");
        for (int i = 0; i < checklist.getTaskCount(); i++) {
            Task task = checklist.getTask(i);
            emailBody.append("- ").append(task.getTitle())
                    .append(" [").append(task.isCompleted() ? "Completed" : "Incomplete").append("]\n")
                    .append("  Description: ").append(task.getDescription()).append("\n")
                    .append("  Due Date: ").append(task.getDueDate()).append("\n\n");
        }

        boolean success = mailSender.sendMail(email, "Checklist Summary", emailBody.toString(), 1, "");
        if (success) {
            JOptionPane.showMessageDialog(frame, "Summary sent to " + email);
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to send summary.");
        }
    }

    private void saveEmails() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(EMAIL_FILE))) {
            oos.writeObject(savedEmails);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Failed to save emails.");
        }
    }

    private List<String> loadEmails() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(EMAIL_FILE))) {
            return (List<String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
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

    private void updateTaskList() {
        taskListModel.clear();
        for (int i = 0; i < checklist.getTaskCount(); i++) {
            Task task = checklist.getTask(i);
            taskListModel.addElement(task.getTitle() + " (" + (task.isCompleted() ? "Completed" : "Incomplete") + ")");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChecklistAppGUI::new);
    }
}
