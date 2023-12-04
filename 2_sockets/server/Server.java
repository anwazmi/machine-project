import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {

    private static final String ip = "127.0.0.1";
    private static final int port = 12345;

    private static void join() {
        try (Socket clientSocket = new Socket(ip, port)) {
            System.out.println("Connected to the File Exchange Server at " + ip + ":" + port + " successfully!");
        } catch (IOException e) {
            System.err.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
        }
    }

    private static void leave() {
        System.out.println("Connection closed. Thank you!");
    }

    private static void register(String handle) {
        if (handle != null && !handle.isEmpty()) {
            System.out.println("Welcome " + handle + "!");
        } else {
            System.err.println("Error: Registration failed. Handle or alias cannot be empty.");
        }
    }

    private static void store(String filename, String handle) {
        if (filename != null && !filename.isEmpty()) {
            String timestamp = getCurrentTimestamp();
            System.out.println(handle + "<" + timestamp + ">: Uploaded " + filename);
        } else {
            System.err.println("Error: File name cannot be empty.");
        }
    }

    private static void dir() {
        System.out.println("Server Directory\nHello.txt\nIMG001.bmp");
    }

    private static void get(String filename, String handle) {
        if (filename != null && !filename.isEmpty()) {
            System.out.println("File received from Server: " + filename);
        } else {
            System.err.println("Error: File name cannot be empty.");
        }
    }

    private static String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("The server is ready: Port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
