package mp;

import java.io.*;
import java.net.*;
import java.util.*;

public class MPClient {

    static Socket clientEndpoint = new Socket();

    public static void joinServer(String serverAddress, int port) {
        try {

            clientEndpoint = new Socket(serverAddress, port);
            System.out.println("Client: Connected to server at" + clientEndpoint.getRemoteSocketAddress());

            // TODO: Detect & error message for unsuccessful connection
        
        } catch (UnknownHostException | ConnectException e) {
			System.out.println("Error: Connection failed.");
		} catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void storeFile(String filename) {

        File file = new File(filename);

        System.out.println("Storing file \"" + file.getName() + "\" (" + file.length() + " bytes)");

        try {

            InputStream disReader = new FileInputStream(file);
            DataOutputStream dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());

            byte[] bytes = new byte[1024];
            int count;
            while ((count = disReader.read(bytes)) > 0) 
                dosWriter.write(bytes, 0, count);

            System.out.println(file.getName() + " stored successfully.");
            disReader.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: Failed to store file " + file.getName() + ".");
        }

        // TODO: Test

    }

    public static void getFile(String filename) {

        // TODO: send name of file requested to server

        try {

            InputStream disReader = new DataInputStream(clientEndpoint.getInputStream());
            OutputStream dosWriter = new FileOutputStream(filename + "_received.txt");
            
            byte[] bytes = new byte[4096];
            int count;
            while ((count = disReader.read(bytes)) > 0)
                dosWriter.write(bytes, 0, count);
            
            System.out.println("Downloaded file \"" + filename + "_received.txt\"");
            dosWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: File not found.");
        }

    }

  
    public static void main(String[] args)
	{
        Scanner scn = new Scanner(System.in);
        StringTokenizer st;
        ArrayList<String> input = new ArrayList<String>();
        String command = "default";
        
        do {
            // get user input
            input.clear();
            System.out.print("\nCommand > ");
            command = scn.nextLine();
            st = new StringTokenizer(command);

            //tokenize
            while(st.hasMoreTokens())
                input.add(st.nextToken());

            /*input validation
            if(inputValidation(input)) break;
            else */
            // find user command to execute
            try {
                switch(input.get(0)) {
                    
                    case "/join":
                        joinServer(input.get(1), Integer.parseInt(input.get(2)));
                        break;
                    case "/register":
                    case "/store":
                        storeFile(input.get(1));
                        break;
                    case "/dir":
                    case "/get":
                        getFile(input.get(1));
                        break;
                    case "/?":
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

	}
}
