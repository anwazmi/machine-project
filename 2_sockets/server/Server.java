import java.io.*;
import java.net.*;
import java.util.*;

class Server {

  private ServerSocket server;
  private Map<String, ClientHandler> clients;
  private ArrayList<String> fileNames;
  
  public Server(int port) throws IOException {
    server = new ServerSocket(port);
    clients = new HashMap<>();
    fileNames = new ArrayList<>();
  }

  public void start() throws IOException {
    while(true) {
      Socket client = server.accept();
      BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
      String handle = in.readLine();
      if(handle == null) continue;
      handle = handle.substring(1);
      ClientHandler handler = new ClientHandler(client, handle);
      clients.put(handle, handler);
      handler.start();
    }
  }

  private void broadcast(String message) {
    for(ClientHandler client : clients.values()) {
      client.sendMessage(message);
    }
  }
  
  private class ClientHandler extends Thread {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private String handle;

    public ClientHandler(Socket client, String handle) throws IOException {
      this.client = client;
      this.handle = handle;
      in = new BufferedReader(new InputStreamReader(client.getInputStream()));
      out = new PrintWriter(client.getOutputStream(), true);
      sendMessage("Registration successful. Welcome " + handle);
    }

    public void run() {
      try {
        while(true) {
          String request = in.readLine();
          if(request.startsWith("/store")) {
            handleStore(request);
          }
          else if(request.equals("/dir")) {
            handleDir();  
          }
          else if(request.startsWith("/get")) {
            handleGet(request);
          }
          else if(request.equals("/leave")) {
            break;
          }
          else {
            sendError("Invalid command");
          }
        }
      } catch (IOException e) {
        // Client disconnected
      } finally {
        clients.remove(handle);
        broadcast(handle + " has left");
      }
    }

    private void handleStore(String request) {
      String[] parts = request.split(" ");
      if(parts.length != 2) {
        sendError("Invalid parameters");
        return;
      }

      String fileName = parts[1];
      if(!new File(fileName).exists()) {
        sendError("File not found");
        return;
      }

      fileNames.add(fileName);
      broadcast(handle + " stored " + fileName);
    }

    private void handleDir() {
      String fileList = String.join(",", fileNames);
      sendMessage("Files on server: " + fileList);
    }

    private void handleGet(String request) {
     String[] parts = request.split(" ");
      if(parts.length != 2) {
        sendError("Invalid parameters");
        return;  
      }
      String fileName = parts[1];
      if(!fileNames.contains(fileName)) {
        sendError("File not found");
        return;
      }
    }

    public void sendMessage(String msg) {
      out.println(msg); 
    }
    
    public void sendError(String err) {
      out.println("Error: " + err);
    }

  }

  public static void main(String[] args) throws IOException {
    Server server = new Server(12345);
    server.start(); 
  }

}