package player;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
import player.request.modes.BounceServerMode;
import player.request.modes.DisjointHttpServerMode;
import player.request.modes.PlayerServerMode;
import server.threads.PingRequestThread;
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
    private String instanceName;
    private String regServerIP = RegistrationServer.DEFAULT_REG_IP;
    private int regServerPort = RegistrationServer.DEFAULT_REG_PORT;
    private Random rand = new Random();

	private PlayerServerMode serverMode;
	private boolean switchServerMode = false;
	private PlayerServerMode newServerMode = null;
	private boolean doAbort;
    
    public static int DEFAULT_PLAYER_PORT = 9147;

    public GamePlayer(int port, Gamer gamer) throws IOException {
    	this(port, gamer, null);
    }
    
    public GamePlayer(String host, int port, Gamer gamer) throws IOException {
    	this(host, port, gamer, null);
    }
    
    public GamePlayer(int port, Gamer gamer, String namePrefix) throws IOException {
    	this(null, port, gamer, namePrefix);
    }
    
    public GamePlayer(String host, int port, Gamer gamer, String namePrefix) throws IOException
    {
    	if (namePrefix == null) {
    		namePrefix = "RAND_" + rand.nextInt() + "_";
    	}
    	
        observers = new ArrayList<Observer>();
        listener = null;
        
        if (host == null) {
        	this.address = NetworkUtils.getALocalIPAddress();
        } else {
        	this.address = InetAddress.getByName(host);
        }
        
        instanceName = namePrefix + gamer.getName();
        while(listener == null) {
            try {
                listener = new ServerSocket(port, 0, address);
                System.out.println("Gamer " + gamer.getName() + " started on address " + address + " " + " port " + port);
        		this.serverMode = new DisjointHttpServerMode(listener);
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
		final GamePlayer playerReference = this;
		Thread registrationThread = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// Attempt to register with the registration server
				System.out.println("Attempting to register...");
				String response = "";
				try {
					Socket socket = new Socket(regServerIP, regServerPort);
					response = RegistrationServer.sendRegistration(socket, instanceName, 
							address.getHostAddress().toString(), listener.getLocalPort());
					socket.close();
				} catch (IOException e1) {
					System.out.println("Registration server connection failed.");
				}
				
				if (RegistrationServer.SUCCESS_RESP.equals(response)) {
					System.out.println("Registration with " + regServerIP + " succeeded.");
				} else if (RegistrationServer.INACCESSIBLE_RESP.equals(response)) {
					System.out.println("Registration with " + regServerIP + " unsuccessful: player inaccessible");
					System.out.println("Attempting to setup bounce registration...");

					setupBounce();
				} 
			}
			
			public void setupBounce() {
				try {
					Socket socket = new Socket(regServerIP, regServerPort);
					boolean success = RegistrationServer.sendBounceRegistration(socket, instanceName);
					if (success) {
						BounceServerMode bounceMode = new BounceServerMode(socket);
						playerReference.changeServerMode(bounceMode);
						System.out.println("Bounce registration successful");
					} else {
						System.out.println("Bounce registration failed...");
						socket.close();
					}
					
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Registration server connection failed.");
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
				String in = serverMode.getRequest();
				
				notifyObservers(new PlayerReceivedMessageEvent(in));
				GamerLogger.log("GamePlayer", "[Received at " + System.currentTimeMillis() + "] " + in, GamerLogger.LOG_LEVEL_DATA_DUMP);

				RequestFactory factory = new RequestFactory();
				Request request = factory.create(gamer, in);
				String out = request.process(System.currentTimeMillis());
				serverMode.sendResponse(out);
				
				notifyObservers(new PlayerSentMessageEvent(out));
				GamerLogger.log("GamePlayer", "[Sent at " + System.currentTimeMillis() + "] " + out, GamerLogger.LOG_LEVEL_DATA_DUMP);
				
				if (switchServerMode) {
					this.serverMode.tearDown();
					this.serverMode = newServerMode;
					this.switchServerMode = false;
				}
				
				if (doAbort) abortAll();
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
		} catch (IOException e1) { }
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
		doAbort = true;
		
		if (this.serverMode != null) {
			try {
				this.serverMode.tearDown();
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
	
	/**
	 * Prepares to switch the server mode at the next available opportunity
	 * @param newMode
	 */
	public synchronized void changeServerMode(PlayerServerMode newMode) {
		this.newServerMode = newMode;
		this.switchServerMode = true;
		
		// Ping ourselves to make sure we don't stick in the ServerSocket accept.
		PingRequestThread pingThread = 
				new PingRequestThread(this.listener.getInetAddress().getHostAddress(), 
				this.listener.getLocalPort(), this.instanceName);
		pingThread.start();
	}
}