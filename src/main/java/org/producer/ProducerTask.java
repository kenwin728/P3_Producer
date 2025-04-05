package org.producer;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class ProducerTask implements Runnable {
    private final Path videoFolderPath;
    private final String consumerHost;
    private final int consumerPort;

    public ProducerTask(Path videoFolderPath, String consumerHost, int consumerPort) {
        this.videoFolderPath = videoFolderPath;
        this.consumerHost = consumerHost;
        this.consumerPort = consumerPort;
        System.out.println("Producer task created for folder: " + videoFolderPath);
    }

    @Override
    public void run() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(videoFolderPath)) {
            for (Path videoFile : stream) {
                if (Files.isRegularFile(videoFile)) {
                    System.out.println("[" + Thread.currentThread().getName() + "] Processing file: " + videoFile.getFileName());
                    sendFile(videoFile);
                }
            }
        } catch (IOException e) {
            System.err.println("[" + Thread.currentThread().getName() + "] Error listing files in " + videoFolderPath + ": " + e.getMessage());
        }
        System.out.println("[" + Thread.currentThread().getName() + "] Finished processing folder " + videoFolderPath);
    }

    private void sendFile(Path videoFile) {
        String fileName = videoFile.getFileName().toString();
        long fileSize = -1;
        try {
            fileSize = Files.size(videoFile);
        } catch (IOException e) {
            System.err.println("[" + Thread.currentThread().getName() + "] Error getting size for file " + fileName + ": " + e.getMessage());
            return; // Skip this file
        }

        // Try-with-resources for Socket, InputStream, and DataOutputStream
        try (Socket socket = new Socket(consumerHost, consumerPort);
             InputStream fileInputStream = Files.newInputStream(videoFile);
             OutputStream socketOutputStream = socket.getOutputStream();
             DataOutputStream dataOut = new DataOutputStream(socketOutputStream)) { // Wrap for easier primitive writing

            System.out.println("[" + Thread.currentThread().getName() + "] Connected to consumer for file: " + fileName);

            // 1. Send filename length (as int)
            byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
            dataOut.writeInt(fileNameBytes.length);

            // 2. Send filename (as UTF-8 bytes)
            dataOut.write(fileNameBytes);

            // 3. Send file size (as long)
            dataOut.writeLong(fileSize);

            // 4. Send file content
            byte[] buffer = new byte[8192]; // 8KB buffer
            int bytesRead;
            long totalSent = 0;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
                totalSent += bytesRead;
            }
            dataOut.flush(); // Ensure all data is sent

            System.out.println("[" + Thread.currentThread().getName() + "] Sent file: " + fileName + " (" + totalSent + " bytes)");

        } catch (IOException e) {
            System.err.println("[" + Thread.currentThread().getName() + "] Network or File I/O error sending " + fileName + ": " + e.getMessage());
            // Consumer might have disconnected or refused connection (queue full)
        }
    }
}
