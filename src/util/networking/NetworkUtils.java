package util.networking;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class NetworkUtils {
	
	public static InetAddress getALocalIPAddress() throws SocketException {
		InetAddress address = null;
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
	    for (NetworkInterface net : Collections.list(nets)) {
	    	if (!net.isLoopback() && !net.isVirtual() && !net.isPointToPoint() && net.isUp()) {
	    		for (InetAddress curAddress : Collections.list(net.getInetAddresses())) {
	    			if (curAddress != null) {
	    				address = curAddress;
	    			}
	    		}
	    	}
	    	if (address != null) break;
	    }
	    
	    return address;
	}
}
