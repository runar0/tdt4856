package no.ntnu.eit.skeis.sensor;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Sensor emulation
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class Sensor implements Bluez.ResponseListener {

	final Logger log;
	
	/**
	 * Client version. 
	 * 
	 * Increment after a change that will break backwards compatibility to prevent
	 * hard to detect bugs in Sensor<->Central communication.
	 */
	public final static long VERSION = 1;
	
	public static void main(String[] args) throws Exception {
		if(args.length != 3) {
			System.out.println("Usage: sensor central-ip central-port client-alias");
			System.exit(1);
		}
		while(true) {
			try {
				new Sensor(args[0], Integer.parseInt(args[1]), args[2]);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}			
	}
	
	private CentralConnection central;
	
	/**
	 * 
	 * @throws Exception
	 */
	public Sensor(String ip, int port, String alias) throws Exception {
		log = Logger.getLogger("Sensor");
		central = new CentralConnection(ip, port, alias);
		Bluez.startScan(this);
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
			log.info("Central gone away.");
			try {
				Bluez.stopScan();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.exit(1);
		}
		
	}

}
