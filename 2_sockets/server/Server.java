import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

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
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                DataInputStream disReader = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dosWriter = new DataOutputStream(clientSocket.getOutputStream());

                String inputLine = disReader.readUTF();                    
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
                                storeFile(filename, disReader);
                                //out.println(cName + ": File stored on the server: " + filename);
                            } else {
                                out.println("Error: /store command requires a filename.");
                            }
                            break;
                        case "/dir":
                            /* String directoryPath = "C:\\Users\\anwar\\OneDrive\\Desktop\\DLSU\\CSNETWRK\\Machine Project";
                            String directoryInfo = listFilesInDirectory(directoryPath);
                            out.println(directoryInfo);
                            break;
                            String directoryPath = "C:\\Users\\anwar\\OneDrive\\Desktop\\DLSU\\CSNETWRK\\Machine Project"; */
                            
                            String directoryPath = "C:\\Users\\user\\Downloads\\school\\CSNETWK\\mp\\Server";
                            File directory = new File(directoryPath);
                            File[] contentsOfDirectory = directory.listFiles();

                            if (contentsOfDirectory != null) {
                                for (File object : contentsOfDirectory) 
                                    if (object.isFile()) 
                                        dosWriter.writeUTF("File fName: %s%n" + object.getName());
                            } else {
                                out.println("Error: Directory not found or is not a directory.");
                            }
                            dosWriter.close();
                            break;
                        case "/get":
                            if (commType.length >= 2) {
                                String reqFileName = commType[1];
                                sendFile(reqFileName, dosWriter);
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
                

                /* Close all
                disReader.close();
                out.close();
                clientSocket.close(); */
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
        private void storeFile(String filename, DataInputStream disReader) {

            File file = new File(filename);
            
            try {
                file.createNewFile();
                OutputStream dosWriter = new FileOutputStream(filename); 

                byte[] bytes = new byte[4096];
                int count;
                while ((count = disReader.read(bytes)) > 0)
                    dosWriter.write(bytes, 0, count); 
                
                System.out.println("Stored file \"" + filename + "\" successfully.");
                dosWriter.close();

            } catch (SocketException e) {
                System.out.println("Error: Not connected to a server.");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error: File not found.");
            }
        }

        // Send a file to the client
        private void sendFile(String filename, DataOutputStream dosWriter) {
            try {
                    File file = new File(filename);
                    InputStream disReader = new FileInputStream(file);

                    System.out.println("Server: Sending file \"" + file.getName() + "\" (" + file.length() + " bytes)");

                    byte[] bytes = new byte[1024];
                    int count;
                    while ((count = disReader.read(bytes)) > 0) 
                        dosWriter.write(bytes, 0, count);

                    clientSocket.close();
                System.out.print(filename + " received successfully.");
            } catch (IOException e) {
                System.out.println("Error: File not found on the server.");
            }
        }
    }
}
