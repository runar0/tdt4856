package no.ntnu.eit.skeis.central.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Config;

/**
 * JSON API server
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class ApiServer extends Thread {

	private boolean running;	
	private int port;	
	private Logger log;
	
	public ApiServer(int port) {
		running = true;
		this.port = port;
		log = Logger.getLogger(getClass().getName());
	}
	
	public void stopServer() {
		running = false;
	}
	
	public int getPort() {
		return port;
	}
	
	@Override
	public void run() {
		ServerSocket server = null;
		try {
			server = new ServerSocket(getPort());
		} catch(IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
		
		while(running) {
			Socket socket = null;
			try {
				socket = server.accept();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String s;
				Map<String, String> params = null;
				while((s = in.readLine()) != null) {
					if(s.startsWith("GET /?")) {
						params = decodeUri(s.substring(6, s.lastIndexOf(' ')));
					}
					if(s.trim().equals("")) {
						break;
					}
				}
				
				// Write response header, we always succeed and handle any errors in the json
				socket.getOutputStream().write((
					"HTTP/1.1 OK\r\n"+
					"Content-Type: application/json\r\n"+
					"Connection: close\r\n"+
					"\r\n"
				).getBytes());
				
				if(params != null && params.containsKey("action")) {
					if(params.get("action").equals("mapDevice")) {
						socket.getOutputStream().write(
							handleMapDevice(params.get("mac"), socket.getInetAddress().getHostAddress(), params.get("alias")).getBytes()
						);
					} else if(params.get("action").equals("status")) {
						socket.getOutputStream().write("{ \"status\": 1}\r\n".getBytes());
					}
				}
				
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				if(socket != null) { 
					try { socket.close(); } catch(IOException e) {}
				}
			}
		}
	}

	/**
	 * Handle a map device request. 
	 * 
	 * This is called by the android app to create a mapping between a bluetooth mac address and a
	 * ip address
	 * 
	 * @param mac
	 * @param ip
	 * @return
	 */
	private String handleMapDevice(String mac, String ip, String alias) {
		log.info("Mapping device "+mac+"<->"+ip + "("+alias+")");
		Config.ipBMacMapping.put(ip, mac);
		Config.deviceAliases.put(mac, alias);
		
		return "OK";
	}
	
	/**
	 * Convert a query string to a map
	 * 
	 * @param params
	 * @return
	 */
	public Map<String, String> decodeUri(String params) {
		Map<String, String> map = new HashMap<String, String>();
		String[] parts = params.split("&");
		
		for(String part : parts) {
			map.put(part.substring(0, part.indexOf('=')), part.substring(part.indexOf('=')+1));			
		}
		return map;
	}
	
}
