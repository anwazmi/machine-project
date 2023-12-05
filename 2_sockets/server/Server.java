import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Server: Client connected: " + clientSocket.getRemoteSocketAddress());

                Thread clientHandler = new Thread(new ClientHandler(clientSocket));
                clientHandler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private Socket serverConnectionSocket;
        private String cName;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String[] commType = inputLine.split(" ");
                    String command = commType[0].toLowerCase();

                    switch (command) {
                        case "/join":
                            if (commType.length == 3) {
                                String serverIP = commType[1];
                                int port = Integer.parseInt(commType[2]);

                                try {
                                    // Establish connection to server
                                    serverConnectionSocket = new Socket(serverIP, port);
                                    System.out.println("Server: Connected to server at " + serverIP + ":" + port);

                                    out.println("Connection to the File Exchange Server is successful!");
                                } catch (IOException e) {
                                    // Connection failure
                                    out.println("Error: Unable to connect to the specified server.");
                                }
                            } else {
                                out.println("Error: /join <server-address> <port>");
                            }
                            break;
                        case "/leave":
                            try {
                                // Close the connection to the server
                                if (serverConnectionSocket != null && !serverConnectionSocket.isClosed()) {
                                    serverConnectionSocket.close();
                                    System.out.println("Server: Disconnected from the connected server.");
                                }
                            } catch (IOException e) {
                                // Disconnection failure
                                System.out.println("Error: Unable to disconnect from the server.");
                            }
                            out.println("Connection closed. Thank you!");
                            break;
                        case "/register":
                            if (commType.length >= 2) {
                                cName = commType[1];
                                out.println("Welcome " + cName + "!");
                            } else {
                                // Registration failure
                                out.println("Error: Registration failed. Name or alias already exists.");
                            }
                            break;
                        case "/store":
                            if (commType.length >= 2) {
                                String filename = commType[1];
                                storeFile(filename, in);
                                out.println(cName + ": File stored on the server: " + filename);
                            } else {
                                out.println("Error: /store command requires a filename.");
                            }
                            break;
                        case "/dir":
                            String directoryPath = "C:\\Users\\anwar\\OneDrive\\Desktop\\DLSU\\CSNETWRK\\Machine Project";
                            String directoryInfo = listFilesInDirectory(directoryPath);
                            out.println(directoryInfo);
                            break;
                        case "/get":
                            if (commType.length >= 2) {
                                String reqFileName = commType[1];
                                sendFile(reqFileName, out);
                            } else {
                                out.println("Error: /get command requires a filename.");
                            }
                            break;
                        case "/?":
                            out.println("Available commands:\n" +
                                    "/join <server-address> <port>\n" +
                                    "/leave\n" +
                                    "/register <cName>\n" +
                                    "/store <filename>\n" +
                                    "/dir\n" +
                                    "/get <filename>\n" +
                                    "/?");
                            break;
                        default:
                            out.println("Error: Command not found.");
                            break;
                    }
                }

                // Close all
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // List files in a directory
        private String listFilesInDirectory(String path) {
            File directory = new File(path);
            StringBuilder directoryInfo = new StringBuilder("Directory Listing for " + path + "\n");

            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            directoryInfo.append(file.getName()).append("\n");
                        }
                    }
                }
            } else {
                directoryInfo.append("Error: Directory not found or is not a directory.");
            }

            return directoryInfo.toString();
        }

        // Store a file
        private void storeFile(String filename, BufferedReader in) {
            try {
                // Assuming files will be stored in the same directory
                String filePath = "C:\\Users\\anwar\\OneDrive\\Desktop\\DLSU\\CSNETWRK\\Machine Project\\" + filename;
                PrintWriter fileWriter = new PrintWriter(new FileWriter(filePath), true);

                String line;
                while ((line = in.readLine()) != null && !line.equals("/endstore")) {
                    fileWriter.println(line);
                }

                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Send a file to the client
        private void sendFile(String filename, PrintWriter out) {
            try {
                // Assuming files are stored in the same directory
                String filePath = "C:\\Users\\anwar\\OneDrive\\Desktop\\DLSU\\CSNETWRK\\Machine Project\\" + filename;
                File file = new File(filePath);

                if (file.exists() && file.isFile()) {
                    out.println("Sending file: " + filename);

                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        out.println(new String(buffer, 0, bytesRead));
                    }

                    out.println("/endget"); // Signal end of the file
                    fileInputStream.close();
                } else {
                    out.println("Error: File not found on the server.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
