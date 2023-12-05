import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    static Socket clientEndpoint = new Socket();
    
    public static void joinServer(String serverAddress, int port) {
        try {

            clientEndpoint = new Socket(serverAddress, port);
            System.out.println("Successfully connected to Fie Server at" + clientEndpoint.getRemoteSocketAddress());
        
        } catch (UnknownHostException | ConnectException e) {
			System.out.println("Error: Connection failed.");
		} catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerAlias(String alias) {

        // TODO: implement

    }

    public static void storeFile(String filename) {

        File file = new File(filename);

        try {

            InputStream disReader = new FileInputStream(file);
            DataOutputStream dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());

            byte[] bytes = new byte[1024];
            int count;
            while ((count = disReader.read(bytes)) > 0) 
                dosWriter.write(bytes, 0, count);

            System.out.println(file.getName() + " stored successfully.");
            disReader.close();

        } catch (SocketException e) {
            System.out.println("Error: Cannot store file, not connected to a server.");
        } catch (FileNotFoundException e) {
            System.out.println("Error: File \"" + file.getName() + "\" not found.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: Failed to store file " + file.getName() + ".");
        }

        // TODO: Test

    }

    public static void requestDirectory() {

        System.out.println("Directory file list:");
        // TODO: implement

    }

    public static void getFile(String filename) {

        try {

            InputStream disReader = new DataInputStream(clientEndpoint.getInputStream());
            OutputStream dosWriter = new FileOutputStream(filename); 

            byte[] bytes = new byte[4096];
            int count;
            while ((count = disReader.read(bytes)) > 0)
                dosWriter.write(bytes, 0, count); 
            
            dosWriter.close();

            System.out.println("Downloaded file \"" + filename + "\" successfully.");

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
        try {
            DataOutputStream dosWriter = null;
            Scanner scn = new Scanner(System.in);
            StringTokenizer st;
            ArrayList<String> input = new ArrayList<String>();
            String command = "default";

            do {
                // get user input
                input.clear();
                System.out.print("\n> ");
                command = scn.nextLine();
                st = new StringTokenizer(command);

                //tokenize
                while(st.hasMoreTokens())
                    input.add(st.nextToken());

                if(dosWriter != null)
                    try { dosWriter.writeUTF(command);  } catch (Exception e) { e.printStackTrace(); }

                // find user command to execute
                try {
                    switch(input.get(0)) {
                        
                        case "/join":
                            joinServer(input.get(1), Integer.parseInt(input.get(2)));
                            dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                            break;
                        case "/register":
                            registerAlias(input.get(1));
                            break;
                        case "/store":
                            storeFile(input.get(1));
                            break;
                        case "/dir":
                            requestDirectory();
                            break;
                        case "/get":
                            getFile(input.get(1));
                            break;
                        case "/?":
                            printCommands();
                            break;
                        case "/leave": break;
                        default:
                            System.out.println("Error: Unknown command. Type /? for help.");

                    }
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    System.out.println("Error: Invalid command syntax. Type /? for help.");
                }

            } while(!input.get(0).equals("/leave"));
                
            // terminate connection
            try {
                clientEndpoint.close();
                System.out.println("Connection is terminated.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) { return; }
	}
}
