import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;

public class Client {

    static Socket clientEndpoint = new Socket();

    public static void joinServer(String serverAddress, int port) {
        try {
            clientEndpoint = new Socket(serverAddress, port);
            System.out.println("Successfully connected to File Server at " + clientEndpoint.getRemoteSocketAddress());
        } catch (UnknownHostException | ConnectException e) {
            System.out.println("Error: Connection failed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void storeFile(String filename) {
        DataOutputStream dosWriter = null;
        InputStream disReader = null;

        try {
            dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
            File file = new File(filename);
            disReader = new FileInputStream(file);

            dosWriter.writeByte(1);
            byte[] fileData = Files.readAllBytes(file.toPath());
            dosWriter.writeInt(fileData.length);
            System.out.println("Storing file \"" + file.getName() + "\" (" + file.length() + " bytes)");

            byte[] bytes = new byte[1024];
            int count;
            while ((count = disReader.read(bytes)) > 0)
                dosWriter.write(bytes, 0, count);

            System.out.print(filename + " stored successfully.");
        } catch (IOException e) {
            System.out.println("Error: File not found on the server.");
            try {
                if (dosWriter != null) dosWriter.writeByte(0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (disReader != null) disReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void requestDirectory(BufferedReader in) {
        try {
            String serverResponse;
            while ((serverResponse = in.readLine()) != null && !serverResponse.isEmpty()) {
                System.out.println(serverResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getFile(String filename) {
        try {
            DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());
            OutputStream dosWriter = null;

            if (disReader.readByte() != 0) {

                dosWriter = new FileOutputStream(filename);

                byte[] bytes = new byte[disReader.readInt()];
                disReader.readFully(bytes);
                dosWriter.write(bytes);

                System.out.println("Downloaded file \"" + filename + "\" successfully.");
            } else
                System.out.println("\"" + filename + "\" does not exist.");

            if (dosWriter != null)
                dosWriter.close();
        } catch (SocketException e) {
            System.out.println("Error: Not connected to a server.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: File not found.");
        }
    }

    public static void printCommands() {
        System.out.println("Application commands: \n" +
                "  /join <server_ip_add> <port> \t Connect to the server application \n" +
                "  /leave \t\t\t Disconnect from the server application \n" +
                "  /register <handle> \t\t Register a unique handle or alias \n" +
                "  /store <filename> \t\t Send file to server \n" +
                "  /dir \t\t\t\t Request directory file list from a server \n" +
                "  /get <filename> \t\t Fetch a file from a server \n" +
                "  /? \t\t\t\t Request command help");
    }

    public static void main(String[] args) {
        ArrayList<String> input = new ArrayList<String>();

        try {
            DataOutputStream dosWriter = null;
            boolean joined = false;
            boolean registered = false;

            do {
                try {
                    Scanner scn = new Scanner(System.in);
                    StringTokenizer st;
                    String command = "default";

                    // get user input
                    input.clear();
                    System.out.print("\n> ");
                    command = scn.nextLine();
                    st = new StringTokenizer(command);

                    // tokenize
                    while (st.hasMoreTokens())
                        input.add(st.nextToken());

                    if (dosWriter != null)
                        try {
                            dosWriter.writeUTF(command);
                        } catch (Exception e) {
                            if (!input.get(0).equals("/join") && !input.get(0).equals("/leave"))
                                System.out.println("Error: Not connected to a server.  (1)");
                        }

                    // find user command to execute
                    try {
                        if (!joined && !input.get(0).equals("/join") && !input.get(0).equals("/?"))
                            System.out.println("Error: Not connected to a server.");
                        else if (!registered && input.get(0).equals("/store") && input.get(0).equals("/dir") && input.get(0).equals("/get"))
                            System.out.println("Error: Not registered.");
                        else
                            switch (input.get(0)) {

                                case "/join":
                                    joinServer(input.get(1), Integer.parseInt(input.get(2)));
                                    dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                                    joined = true;
                                    break;
                                case "/leave":
                                    clientEndpoint.close();
                                    System.out.println("Disconnected from server.");
                                    joined = false;
                                    break;
                                case "/register":
                                    DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());
                                    String messageServer = disReader.readUTF();
                                    System.out.println("Server: " + messageServer);
                                    registered = true;
                                    break;
                                case "/store":
                                    storeFile(input.get(1));
                                    break;
                                case "/dir":
                                    //requestDirectory();
                                    break;
                                case "/get":
                                    getFile(input.get(1));
                                    break;
                                case "/?":
                                    printCommands();
                                    break;
                                case "/stop":
                                    break;
                                default:
                                    System.out.println("Error: Unknown command. Type /? for help.");

                            }
                    } catch (IndexOutOfBoundsException | NumberFormatException e) {
                        System.out.println("Error: Invalid command syntax. Type /? for help.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (!input.get(0).equals("/stop"));

            joined = false;
            registered = false;

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // terminate connection
            clientEndpoint.close();
            System.out.println("Connection is terminated.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
