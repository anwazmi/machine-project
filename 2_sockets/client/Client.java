import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class MPClient {

    static Socket clientEndpoint = new Socket();
    
    public static void joinServer(String serverAddress, int port) {
        try {

            clientEndpoint = new Socket(serverAddress, port);
            System.out.println("Successfully connected to File Server at " + clientEndpoint.getRemoteSocketAddress());
        
        } catch (UnknownHostException | ConnectException e) {
			System.out.println("Error: Connection failed.");
		} catch (Exception e) {
            System.out.println("joinServer catch");
            e.printStackTrace();
        }
    }

    public static void storeFile(String filename) {

        /* File file = new File(filename);

        try {

            InputStream disReader = new FileInputStream(file);
            DataOutputStream dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());

            byte[] bytes = new byte[1024];
            int count;
            while ((count = disReader.read(bytes)) > 0) 
                dosWriter.write(bytes, 0, count);

            System.out.println(file.getName() + " stored successfully.");
            disReader.close();
            dosWriter.close();

        } catch (SocketException e) {
            System.out.println("Error: Cannot store file, not connected to a server.");
        } catch (FileNotFoundException e) {
            System.out.println("Error: File \"" + file.getName() + "\" not found.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("storefile catch");
            System.out.println("Error: Failed to store file " + file.getName() + ".");
        } */

        // TODO: Test
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
                try { dosWriter.writeByte(0); } catch (Exception ex) { int x=1; }
            }

    }

    public static void requestDirectory() {

        System.out.println("Directory file list:");
        // TODO: implement

    }

    public static void getFile(String filename) {
        try {
            DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());
            OutputStream dosWriter = null;

            if(disReader.readByte() != 0) {

                dosWriter = new FileOutputStream(filename); 

                byte[] bytes = new byte[disReader.readInt()];
                disReader.readFully(bytes);
                dosWriter.write(bytes);

                System.out.println("Downloaded file \"" + filename + "\" successfully.");
            } else 
                System.out.println("\"" + filename + "\" does not exist.");

            if(dosWriter != null)
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

  
    public static void main(String[] args)
	{
        ArrayList<String> input = new ArrayList<String>();
        try { DataOutputStream dosWriter = null;
        
        do {
            try {
                //DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());
                Scanner scn = new Scanner(System.in);
                StringTokenizer st;
                String command = "default";

                // get user input
                input.clear();
                System.out.print("\n> ");
                command = scn.nextLine();
                st = new StringTokenizer(command);

                //tokenize
                while(st.hasMoreTokens())
                    input.add(st.nextToken());

                if(dosWriter != null)
                    try { dosWriter.writeUTF(command); } catch (Exception e) { int x=1; System.out.println("doswriter catch"); }

                // find user command to execute
                try {
                    switch(input.get(0)) {
                        
                        case "/join":
                            joinServer(input.get(1), Integer.parseInt(input.get(2)));
                            dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                            break;
                        case "/leave": 
                            clientEndpoint.close();
                            System.out.println("Disconnected from server.");
                            break;
                        case "/register":
                            DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());
                            String messageServer = disReader.readUTF();
                            System.out.println("Server: " + messageServer);
                            break;
                        case "/store":
                            storeFile(input.get(1));
                            break;
                        case "/dir":
                            //requestDirectory();
                            //System.out.println(disReader.readUTF());
                            //disReader.close();
                            break;
                        case "/get":
                            getFile(input.get(1));
                            break;
                        case "/?":
                            printCommands();
                            break;
                        case "/stop": break;
                        default:
                            System.out.println("Error: Unknown command. Type /? for help.");

                    }
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    System.out.println("Error: Invalid command syntax. Type /? for help.");
                }

            } catch (Exception e) { int x=1; }

        } while(!input.get(0).equals("/stop"));

        } catch (Exception e) { int x=1; }

        try {
            // terminate connection
            clientEndpoint.close();
            System.out.println("Connection is terminated.");
        } catch (Exception e) { int x=1; }

	}
}
