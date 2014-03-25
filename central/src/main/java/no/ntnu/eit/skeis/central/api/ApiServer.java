package no.ntnu.eit.skeis.central.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Central;
import no.ntnu.eit.skeis.central.Config;
import no.ntnu.eit.skeis.central.Device;
import no.ntnu.eit.skeis.central.DeviceTracker.DeviceSensorEntry;

import com.google.gson.Gson;

/**
 * JSON API server
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class ApiServer extends Thread {

	private boolean running;	
	private int port;	
	private Logger log;
	private final Central central;
	private final Gson gson;
	
	public ApiServer(int port, Central central) {
		this.gson = new Gson();
		running = true;
		this.central = central;
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
			log.info("API server serving requests at "+getPort()+"/TCP");
		} catch(IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
		
		while(running) {
			try {
				final Socket socket = server.accept();
				
				Thread handler = new Thread() {
					public void run() {
						try {
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
								} else if(params.get("action").equals("sensor-queues")) {
									socket.getOutputStream().write(
										handleSensorQueues().getBytes()
									);
								} else if(params.get("action").equals("device-readings")) {
									socket.getOutputStream().write(
										handleDeviceReadings(params.get("mac")).getBytes()
									);
								}
							}
							socket.close();
						} catch(Exception e) {}
					};
				};
				handler.setDaemon(true);
				handler.start();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String handleDeviceReadings(String mac) {
		Device device = central.getDeviceTracker().getDevice(mac);
		if(device == null) {
			return "{\"error\": \"Unknown device\"}";
		}
		Map<String, Double> readings = new HashMap<String, Double>();
		for(String sensor : central.getSensorManager().getSensorAliases()) {
			readings.put(sensor, device.getLastReading(sensor));
		}
		return gson.toJson(readings);
	}

	class SensorQueueEntry {
		public long timestamp = 0;
		public int priority = 0;
		public boolean active = false;
		public String device;
		public SensorQueueEntry(DeviceSensorEntry d) {
			timestamp = d.timestamp;
			priority = d.priority;
			active = d.device.isActive();
			device = d.device.toString();
		}
	}

	/**
	 * Return a list of all sensor queues
	 * 
	 * @return
	 */
	private String handleSensorQueues() {
		Map<String, SortedSet<DeviceSensorEntry>> queues = central.getDeviceTracker().getSensorRegistrations();
		
		Map<String, List<SensorQueueEntry>> simplified = new HashMap<String, List<SensorQueueEntry>>();
		for(String sensor : queues.keySet()) {
			simplified.put(sensor, new LinkedList<SensorQueueEntry>());
			for(DeviceSensorEntry entry : queues.get(sensor)) {
				simplified.get(sensor).add(new SensorQueueEntry(entry));
			}
		}		
		return gson.toJson(simplified);
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
