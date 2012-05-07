package player;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import apps.server.registration.RegistrationServer;

import player.gamer.Gamer;
import player.gamer.statemachine.reflex.random.RandomGamer;
import player.event.PlayerDroppedPacketEvent;
import player.event.PlayerReceivedMessageEvent;
import player.event.PlayerSentMessageEvent;
import player.request.factory.RequestFactory;
import player.request.grammar.Request;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import util.http.HttpReader;
import util.http.HttpWriter;
import util.logging.GamerLogger;
import util.networking.NetworkUtils;
import util.observer.Event;
import util.observer.Observer;
import util.observer.Subject;

public final class GamePlayer extends Thread implements Subject
{
    private final int port;
    private final Gamer gamer;
    private ServerSocket listener;
    private InetAddress address;
    private final List<Observer> observers;
    private Socket connection;
    private String instanceName;
    private String regServerIP = RegistrationServer.DEFAULT_REG_IP;
    private int regServerPort = RegistrationServer.DEFAULT_REG_PORT;
    private Random rand = new Random();
    
    public static int DEFAULT_PLAYER_PORT = 9147;

    public GamePlayer(int port, Gamer gamer) throws IOException {
    	this(port, gamer, null);
    }
    
    public GamePlayer(int port, Gamer gamer, String namePrefix) throws IOException
    {
    	if (namePrefix == null) {
    		namePrefix = "RAND_" + rand.nextInt() + "_";
    	}
    	
        observers = new ArrayList<Observer>();
        listener = null;
        connection = null;
        
        this.address = NetworkUtils.getALocalIPAddress();
        
        instanceName = namePrefix + gamer.getName();
        while(listener == null) {
            try {
                listener = new ServerSocket(port, 0, address);
                System.out.println("Gamer " + gamer.getName() + " started on address " + address + " " + " port " + port);
            } catch (IOException ex) {
                listener = null;
                port++;
                System.err.println("Failed to start gamer on port: " + (port-1) + " trying port " + port);
            }				
        }
        
        this.port = port;
        this.gamer = gamer;
    }

	public void addObserver(Observer observer)
	{
		observers.add(observer);
	}

	public void notifyObservers(Event event)
	{
		for (Observer observer : observers)
		{
			observer.observe(event);
		}
	}
	
	public final int getGamerPort() {
	    return port;
	}
	
	public final Gamer getGamer() {
	    return gamer;
	}
	

	@Override
	public void run()
	{
		Thread registrationThread = new Thread() {
			@Override
			public void run() {
				// Attempt to register with the registration server
				String response = "";
				try {
					Socket socket = new Socket(regServerIP, regServerPort);
					response = RegistrationServer.sendRegistration(socket, instanceName, 
							address.getHostAddress().toString(), listener.getLocalPort());
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				if (RegistrationServer.SUCCESS_RESP.equals(response)) {
					System.out.println("Registration with " + regServerIP + " succeeded.");
				} else if (RegistrationServer.INACCESSIBLE_RESP.equals(response)) {
					System.out.println("Registration with " + regServerIP + " unsuccessful: player inaccessible");
				} else {
					System.out.println("Registration with " + regServerIP + " on port " + regServerPort + "");
				}
			}
		};
		
		registrationThread.start();
		
		// Wait for GGP messages
		System.out.println(gamer.getName() + " listening ...");
		while (!isInterrupted())
		{
			try
			{
				connection = listener.accept();
				String in = HttpReader.readAsServer(connection);
				if (in.length() == 0) {
				    throw new IOException("Empty message received.");
				}
				
				notifyObservers(new PlayerReceivedMessageEvent(in));
				GamerLogger.log("GamePlayer", "[Received at " + System.currentTimeMillis() + "] " + in, GamerLogger.LOG_LEVEL_DATA_DUMP);

				RequestFactory factory = new RequestFactory();
				Request request = factory.create(gamer, in);
				String out = request.process(System.currentTimeMillis());
				
				HttpWriter.writeAsServer(connection, out);
				connection.close();
				connection = null;
				notifyObservers(new PlayerSentMessageEvent(out));
				GamerLogger.log("GamePlayer", "[Sent at " + System.currentTimeMillis() + "] " + out, GamerLogger.LOG_LEVEL_DATA_DUMP);
			}
			catch (Exception e)
			{
				notifyObservers(new PlayerDroppedPacketEvent());
			}
		}
	}

	public void clearRegistration() {
		try {
			Socket socket = new Socket(regServerIP, regServerPort);
			RegistrationServer.sendRegistration(socket, instanceName, "", 0);
			socket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	// Simple main function that starts a RandomGamer on a specified port.
	// It might make sense to factor this out into a separate app sometime,
	// so that the GamePlayer class doesn't have to import RandomGamer.
	public static void main(String[] args)
	{
		
		if (args.length != 1) {
			System.err.println("Usage: GamePlayer <port>");
			System.exit(1);
		}
		
		try {
			GamePlayer player = new GamePlayer(Integer.valueOf(args[0]), new RandomGamer());
			player.run();
		} catch (NumberFormatException e) {
			System.err.println("Illegal port number: " + args[0]);			
			e.printStackTrace();
			System.exit(2);
		} catch (IOException e) {
			System.err.println("IO Exception: " + e);			
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	/**
	 * This method cleans up the network resources when the GamePlayer is interrupted.
	 */
	@Override
	public void interrupt () {
		clearRegistration();
		
		abortAll();
		
		if (connection != null) {
			try {
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			connection = null;
		}
		
		if (listener != null) {
			try {
				listener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void abortAll() {
		if (gamer != null) {
			gamer.abortAll();
		}
	}
}