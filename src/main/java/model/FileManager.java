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
            System.out.println("Checklist saved successfully to " + filename);
        } catch (IOException e) {
            System.err.println("Error: Failed to save checklist to " + filename);
            e.printStackTrace(); // debug
        }
    }

    // Loads the checklist from a file
    public Checklist load() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (Checklist) in.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No existing checklist found. Creating a new checklist.");
        } catch (IOException e) {
            System.err.println("Error: Failed to read from file " + filename);
            e.printStackTrace(); // debug
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Checklist class not found while loading.");
            e.printStackTrace(); // debug
        }
        return new Checklist();
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
