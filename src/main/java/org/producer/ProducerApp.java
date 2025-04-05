package org.producer;

import java.nio.file.Files; // Import Files
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InputMismatchException; // Import for error handling
import java.util.List;
import java.util.Scanner; // Import Scanner
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProducerApp {

    public static void main(String[] args) {

        // Use Scanner for interactive input
        Scanner scanner = new Scanner(System.in);

        // --- Configuration Variables ---
        int p = 0;
        String consumerHost = "";
        int consumerPort = 0;
        List<Path> videoFolders = new ArrayList<>();

        System.out.println("--- Producer Configuration ---");

        // --- Get Number of Producer Threads (p) ---
        while (p <= 0) {
            System.out.print("Enter number of producer threads (positive integer, e.g., 2): ");
            try {
                p = scanner.nextInt();
                if (p <= 0) {
                    System.err.println("Error: Number of threads must be positive.");
                }
            } catch (InputMismatchException e) {
                System.err.println("Error: Invalid input. Please enter a whole number.");
            } finally {
                scanner.nextLine(); // Consume the leftover newline
            }
        }

        // --- Get Consumer Host ---
        while (consumerHost.trim().isEmpty()) {
            System.out.print("Enter consumer hostname or IP address (e.g., localhost or 192.168.1.101): ");
            consumerHost = scanner.nextLine();
            if (consumerHost.trim().isEmpty()) {
                System.err.println("Error: Consumer host cannot be empty.");
            }
        }


        // --- Get Consumer Port ---
        while (consumerPort <= 0 || consumerPort > 65535) {
            System.out.print("Enter consumer upload port number (1-65535, e.g., 8080): ");
            try {
                consumerPort = scanner.nextInt();
                if (consumerPort <= 0 || consumerPort > 65535) {
                    System.err.println("Error: Port number must be between 1 and 65535.");
                }
            } catch (InputMismatchException e) {
                System.err.println("Error: Invalid input. Please enter a whole number.");
            } finally {
                scanner.nextLine(); // Consume the leftover newline
            }
        }

        // --- Get Video Folders ---
        System.out.println("Enter paths to video folders, one per line.");
        System.out.println("Leave blank and press Enter when finished.");
        while (true) {
            System.out.print("Enter video folder path: ");
            String folderPathStr = scanner.nextLine().trim();

            if (folderPathStr.isEmpty()) {
                if (videoFolders.isEmpty()) {
                    System.err.println("Error: At least one video folder must be specified.");
                    // Continue loop to force user entry
                    continue;
                } else {
                    break; // Finished adding folders
                }
            }

            try {
                Path folderPath = Paths.get(folderPathStr);
                if (Files.isDirectory(folderPath)) {
                    videoFolders.add(folderPath);
                    System.out.println(" -> Added folder: " + folderPath.toAbsolutePath());
                } else {
                    System.err.println("Error: Path is not a valid directory or does not exist: " + folderPathStr);
                }
            } catch (InvalidPathException e) {
                System.err.println("Error: Invalid path format: " + folderPathStr);
            }
        }

        // --- Adjust producer count if needed ---
        if (videoFolders.size() < p) {
            System.err.println("\nWarning: Fewer folders specified (" + videoFolders.size() + ") than producer threads (" + p + ").");
            p = videoFolders.size();
            System.err.println("Adjusting producer thread count to: " + p);
        }

        // Close the scanner as we don't need it anymore
        scanner.close();

        // --- Display Final Configuration ---
        System.out.println("\n--- Starting Producer with Configuration ---");
        System.out.println("Producer Threads (p): " + p);
        System.out.println("Consumer Host:      " + consumerHost);
        System.out.println("Consumer Port:      " + consumerPort);
        System.out.println("Input Folders:      " + videoFolders);
        System.out.println("------------------------------------------");


        // --- Thread Pool Execution (Same as before) ---
        ExecutorService executor = Executors.newFixedThreadPool(p);

        for (int i = 0; i < p; i++) {
            executor.submit(new ProducerTask(videoFolders.get(i), consumerHost, consumerPort));
        }

        // --- Shutdown (Same as before) ---
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("Producer finished.");
    }
}