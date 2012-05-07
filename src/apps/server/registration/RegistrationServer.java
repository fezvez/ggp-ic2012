package apps.server.registration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import server.threads.PingRequestThread;
import util.networking.NetworkUtils;


import external.JSON.JSONException;
import external.JSON.JSONObject;


public class RegistrationServer {

	public static final String DEFAULT_REG_IP = "10.31.1.100";

	private static final String FAILURE_MSG = "failure";

	public static int DEFAULT_REG_PORT = 9117;
	
	public static String REGISTRATION_CMD = "register";
	public static String BOUNCE_CMD = "bounce";
	public static String SUCCESS_RESP = "success";
	public static String INACCESSIBLE_RESP = "inaccessible";
	public static String LIST_CMD = "list";

	class Forward {
		public final Socket playerSide;
		public final ServerSocket exposedSide;
		
		public Forward(Socket playerSide) throws IOException {
			this.exposedSide = generateServerSocket();
			this.playerSide = playerSide;
		}
		
		private ServerSocket generateServerSocket() throws IOException {
			InetAddress inetAddress = NetworkUtils.getALocalIPAddress();
			ServerSocket result = new ServerSocket(0, 0, inetAddress);
			String host = result.getInetAddress().getHostAddress();
			return result;
		}
		
		public void tearDown() throws IOException {
			if (playerSide != null) {
				playerSide.close();
			}
			
			if (exposedSide != null) {
				exposedSide.close();
			}
		}
	};
	
	HashMap<String, URL> registrations = new HashMap<String, URL>();
	HashMap<String, Forward> forwards = new HashMap<String, Forward>();
	int port;
	
	public RegistrationServer() {
		this(DEFAULT_REG_PORT);
	}
	
	public RegistrationServer(int port) {
		this.port = port;
	}
	
	static public void main(String argv[]) throws IOException {
		ServerSocket socket = new ServerSocket(DEFAULT_REG_PORT);
		
		RegistrationServer regServer = new RegistrationServer();
		
		while (true) {
			Socket newConnection = socket.accept();
			BufferedReader in;
			in = new BufferedReader(new InputStreamReader(newConnection.getInputStream()));
			
			String message = in.readLine();
			if (message == null) {
				in.close();
				newConnection.close();
				continue;
			}
			String[] tokens = message.split("\\s+");
			System.out.println("Recieved: " + message);
			
			boolean keepAlive = false;
			if (tokens.length > 0) {
				String command = tokens[0].toLowerCase();
				String response = FAILURE_MSG + "\n";
				
				if (command.equals(REGISTRATION_CMD) && tokens.length > 2) {
					String name = tokens[1];
					String host = null;
					int port = -1;
					if (tokens.length > 3) {
						host = tokens[2];
						port = Integer.parseInt(tokens[3]);
					}
					response = regServer.register(name, host, port);
				} else if (command.equals(BOUNCE_CMD) && tokens.length > 2) {
					String name = tokens[1];
					Forward newForward = regServer.new Forward(newConnection);
					ServerSocket newSocket = newForward.exposedSide;
					String host = newSocket.getInetAddress().getHostAddress();
					regServer.register(name, host, newSocket.getLocalPort());
					keepAlive = true;
				} else if (command.equals(LIST_CMD)) {
					response = regServer.list();
				}
				
				PrintWriter out = new PrintWriter(newConnection.getOutputStream(), true);
				out.write(response);
				out.close();
			}
			
			in.close();
			if (!keepAlive) newConnection.close();
		}
	}
	
	private String list() {
		JSONObject jobject = new JSONObject(registrations);
		return jobject.toString() + "\n";
	}

	private String register(String name, String host, int port2) throws MalformedURLException {
		String response = FAILURE_MSG + "\n";
		if (host != null) {
			URL url = new URL("http", host, port, "");
			boolean accessible = verifyPlayerAccessible(name, url);
			if (accessible) {
				registrations.put(name, url);
				response = SUCCESS_RESP + "\n";
			} else {
				response = INACCESSIBLE_RESP;
			}
		} else {
			clearRegistration(name);
			response = "clear\n";
		}
		
		displayRegistrations(registrations);
		return response;
	}
	
	public void clearRegistration(String name) {
		this.registrations.remove(name);
		this.forwards.remove(name);
	}

	private static boolean verifyPlayerAccessible(String name, URL url) {
		// Attempt to ping the player at the given URL
		PingRequestThread pingThread = 
				new PingRequestThread(url.getHost(), url.getPort(), name);
		pingThread.run();
		return pingThread.result;
	}

	static private void displayRegistrations(Map<String,URL> registrations) {
		System.out.println();
		System.out.println("Current Registrations: ");
		for (String name : registrations.keySet()) {
			System.out.println("  " + name + ": " + registrations.get(name).toString());
		}
	}

	static public String 
	sendRegistration (Socket serverSocket, String playerName, String address, int port) 
			throws IOException {
		
		String message = REGISTRATION_CMD + " " + playerName + " " + address + " " + port + "\n";
		PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
		out.write(message);
		out.flush();
		
		BufferedReader in;
		in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
		String response = in.readLine();
		
		in.close();
		out.close();
		
		return response;

	}
	
	static public Map<String, URL>
	queryList() throws UnknownHostException, IOException {
		Socket server = new Socket(DEFAULT_REG_IP, DEFAULT_REG_PORT);
		return queryList(server);
	}
	
	static public Map<String, URL> 
	queryList(Socket server) throws IOException {
		
		String message = LIST_CMD + "\n";
		PrintWriter out = new PrintWriter(server.getOutputStream(), true);
		out.write(message);
		out.flush();
		
		BufferedReader in;
		in = new BufferedReader(new InputStreamReader(server.getInputStream()));
		String response = in.readLine();
		
		// Convert JSON back into map of URLs
		HashMap<String, URL> result = new HashMap<String, URL>();
		if (response != null) {
			try {
				JSONObject jobject = new JSONObject(response);
				if (jobject != null && jobject.length() > 0) {
					for (String name : JSONObject.getNames(jobject)) {
						String addressString = jobject.getString(name);
						URL address = new URL(addressString);
						result.put(name, address);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		out.close();
		in.close();
		
		return result;
	}
}
