package no.ntnu.eit.skeis.sensor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.sensor.Lookout.ConnectionInfo;

/**
 * Sensor emulation
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class Sensor implements Bluez.ResponseListener {

	final Logger log;
	
	public volatile boolean running;
	
	/**
	 * Client version. 
	 * 
	 * Increment after a change that will break backwards compatibility to prevent
	 * hard to detect bugs in Sensor<->Central communication.
	 */
	public final static long VERSION = 1;
	
	public static void main(String[] args) throws Exception {
		if(args.length != 2 && args.length != 4) {
			System.out.println("Usage: sensor client-alias hci-device [central-ip central-port]");
			System.exit(1);
		}
		try {
			while(true) {
				Sensor s;
				InetAddress address;
				int port;
				String alias = args[0];
				String device = args[1];
				if (args.length == 2) {					
					Lookout lookout = new Lookout();
					ConnectionInfo info = lookout.detectCentral();
					if(info == null) continue;
					Logger.getGlobal().info("Central detected "+info.toString());
					
					address = info.address;
					port = info.sensor_port;
				} else {
					address = InetAddress.getByName(args[2]);
					port = Integer.parseInt(args[3]);
				}
				s = new Sensor(address, device, port, alias);
				
				while(s.running) {
					try {
						Thread.sleep(1000);
					} catch(Exception e) {
						
					}
				}
				Logger.getGlobal().info("Sensor exited cleanly, restarting!");				
			}
		} catch(Exception e) {
			Logger.getGlobal().warning("Exception escaped sensor, exitting.");
			e.printStackTrace();
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Stop scan before we exit
				try {
					Bluez.stopScan();
				} catch (IOException e) {
					// Really not important 
				}
			}
		});
	}
	
	private CentralConnection central;
	
	/**
	 * 
	 * @throws Exception
	 */
	public Sensor(InetAddress address, String device, int port, String alias) throws Exception {
		log = Logger.getLogger("Sensor");	
		
		central = new CentralConnection(address, port, alias);
		running = true;
		Bluez.startScan(device, this);
	}

	/**
	 * Notify server on bluetooth response
	 * 
	 * TODO Would it be an idea to accumulate responses and send updates to server at set intervals?
	 */
	@Override
	public void onBluetoothResponse(String mac, int rssi) {
		try {
			central.sendSensorUpdate(mac, rssi);
		} catch(IOException e) {
			this.running = false;
			log.info("Central gone away.");
			try {
				Bluez.stopScan();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
	}

}
