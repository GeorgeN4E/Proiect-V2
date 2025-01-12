import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
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

    public ChecklistAppGUI() {
        this.checklist = new Checklist();
        this.mailSender = new MailSender("en", "your-email@example.com");
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Checklist Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
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

        addButton.addActionListener(e -> addTask());
        removeButton.addActionListener(e -> removeTask());
        toggleButton.addActionListener(e -> toggleTaskCompletion());
        saveButton.addActionListener(e -> saveChecklist());
        loadButton.addActionListener(e -> loadChecklist());
        sendEmailButton.addActionListener(e -> sendEmail());

        frame.setVisible(true);
    }

    private void addTask() {
        String title = JOptionPane.showInputDialog("Enter task title:");
        String description = JOptionPane.showInputDialog("Enter task description:");
        String dueDateInput = JOptionPane.showInputDialog("Enter due date (YYYY-MM-DD):");
        LocalDate dueDate = LocalDate.parse(dueDateInput);

        checklist.addTask(new Task(title, description, dueDate));
        updateTaskList();
    }

    private void removeTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            checklist.removeTask(index);
            updateTaskList();
        }
    }

    private void toggleTaskCompletion() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            checklist.toggleTaskCompletion(index);
            updateTaskList();
        }
    }

    private void saveChecklist() {
        FileManager.saveChecklist(checklist, "checklist.dat");
        JOptionPane.showMessageDialog(frame, "Checklist saved successfully.");
    }

    private void loadChecklist() {
        checklist = FileManager.loadChecklist("checklist.dat");
        updateTaskList();
    }

    private void sendEmail() {
        StringBuilder emailBody = new StringBuilder("Checklist Summary:\n");
        for (int i = 0; i < checklist.getTaskCount(); i++) {
            Task task = checklist.getTask(i);
            emailBody.append("- ").append(task.getTitle())
                    .append(" [").append(task.isCompleted() ? "Completed" : "Incomplete").append("]\n");
        }

        String recipient = JOptionPane.showInputDialog("Enter recipient email:");
        mailSender.sendMail(recipient, "Checklist Summary", emailBody.toString(), 1, "");
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
