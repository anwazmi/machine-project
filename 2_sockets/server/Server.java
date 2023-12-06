import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.file.*;

public class Server {
    private static Map<String, DataOutputStream> clientOutputStreams = new HashMap<>();
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Server: Client connected: " + clientSocket.getRemoteSocketAddress());

                String clientKey = generateUniqueKeyForClient();

                DataOutputStream clientDos = new DataOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.put(clientKey, clientDos);

                Thread clientHandler = new Thread(new ClientHandler(clientSocket, clientKey));
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
        private DataInputStream disReader;
        private DataOutputStream dosWriter;
        private String clientKey;

        public ClientHandler(Socket clientSocket, String clientKey) {
            this.clientSocket = clientSocket;
            this.clientKey = clientKey;
            try {
                this.disReader = new DataInputStream(clientSocket.getInputStream());
                this.dosWriter = new DataOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.put(clientKey, dosWriter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                DataInputStream disReader = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dosWriter = new DataOutputStream(clientSocket.getOutputStream());
                String command;
                ArrayList<String> aliases = new ArrayList<String>();
                
                do {
                    String inputLine = disReader.readUTF();                    
                    String[] commType = inputLine.split(" ");
                    command = commType[0].toLowerCase();

                    System.out.println("Awaiting request from " + clientSocket.getRemoteSocketAddress());
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
                            /* try {
                                // Close the connection to the server
                                if (serverConnectionSocket != null && !serverConnectionSocket.isClosed()) {
                                    serverConnectionSocket.close();
                                    System.out.println("Server: Disconnected from the connected server.");
                                }
                            } catch (IOException e) {
                                // Disconnection failure
                                System.out.println("Error: Unable to disconnect from the server.");
                            } */
                            clientSocket.close();
                            out.println("Connection closed. Thank you!");
                            break;
                        case "/register":
                            boolean flag = true;
                            for(int i=0; i < aliases.size(); i++) 
                                if(aliases.get(i).equals(commType[1]))
                                    flag = false;

                            if (commType.length >= 2 && flag) {
                                cName = commType[1];
                                aliases.add(commType[1]);
                                String welcomeMessage = "Welcome " + cName + "!";
                                clientOutputStreams.get(clientKey).writeUTF(welcomeMessage);
                                System.out.println("Server: Sent registration message to client " + clientKey + ": " + welcomeMessage);
                            } else {
                                // Registration failure
                                String errorMessage = "Error: Registration failed. Name or alias already exists.";
                                clientOutputStreams.get(clientKey).writeUTF(errorMessage);
                                System.out.println("Server: Sent registration failure message to client " + clientKey + ": " + errorMessage);
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
                        default:
                            out.println("Error: Command not found.");
                            break;
                    }
                
                    command = "clear";

                } while (!command.equals("/leave"));
                //Close all
                disReader.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("main error");
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

            /*File file = new File(filename);
            
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
            */
            try {
            OutputStream dosWriter = null;

            if(disReader.readByte() != 0) {

                dosWriter = new FileOutputStream(filename); 

                byte[] bytes = new byte[disReader.readInt()];
                disReader.readFully(bytes);
                dosWriter.write(bytes);

                System.out.println("Stored file \"" + filename + "\" successfully.");
            } else 
                System.out.println("\"" + filename + "\" does not exist.");

            if(dosWriter != null)
                dosWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        }

        // Send a file to the client
        private void sendFile(String filename, DataOutputStream dosWriter) {
           InputStream disReader = null;
            try {
                    File file = new File(filename);
                    disReader = new FileInputStream(file);

                    dosWriter.writeByte(1);
                    byte[] fileData = Files.readAllBytes(file.toPath());
                    dosWriter.writeInt(fileData.length);
                    System.out.println("Server: Sending file \"" + file.getName() + "\" (" + file.length() + " bytes)");

                    byte[] bytes = new byte[1024];
                    int count;
                    while ((count = disReader.read(bytes)) > 0) 
                        dosWriter.write(bytes, 0, count);
                System.out.print(filename + " received successfully.");
            } catch (IOException e) {
                System.out.println("Error: File not found on the server.");
                try { dosWriter.writeByte(0); } catch (Exception ex) { int x=1; }
            }
        }
    }

    private static String generateUniqueKeyForClient() {
        return UUID.randomUUID().toString();
    }
}
