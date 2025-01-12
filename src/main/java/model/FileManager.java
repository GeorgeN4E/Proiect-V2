package model;

import java.io.*;

public class FileManager {
    private String filename;

    public FileManager(String filename) {
        this.filename = filename;
    }

    public void save(Checklist checklist) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(checklist);
        } catch (IOException e) {
            System.out.println("Failed to save checklist.");
        }
    }

    public Checklist load() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (Checklist) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Failed to load checklist. Starting a new checklist.");
            return new Checklist();
        }
    }
}
