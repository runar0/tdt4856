package no.ntnu.eit.skeis.sensor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Bluez - A limited bluetooth control class
 * 
 * This class implements the functionality needed to initialize a continuous bluetooth
 * scan on a bluez based bluetooth stack (linux).
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class Bluez {

	public interface ResponseListener {
		public void onBluetoothResponse(String mac, int rssi);
	}
	
	private static boolean running = false;	
	private static Thread readThread;
	private static boolean stopping = false;
	
	private static String device;
	
	public static void startScan(final String device, final ResponseListener listener) throws IOException {
		if (running) {
			//throw new IOException("Scan already started!");
		}
		Bluez.device = device;
		stopping = false;

		Logger.getLogger("Bluez").finest("Attempting to enable periodic bluetooth scan");
		
		// Bring up interface
		Process p = Runtime.getRuntime().exec("hciconfig "+device+" up");
		while(true) {
			try {
				if (p.waitFor() != 0) {
					throw new IOException("Unable bring bluetooth interface up, do you have root?");
				}
				break;
			} catch(InterruptedException e) {
				// Ignore and loop around
			}
		}
		// Start periodic can mode
		p = Runtime.getRuntime().exec("hcitool cmd 01 0003 0A 00 09 00 33 8b 9e 08 00");		
		while(true) {
			try {
				if (p.waitFor() != 0) {
					throw new IOException("Unable to start periodic scan, do you have root?");
				}
				break;
			} catch(InterruptedException e) {
				// Ignore and loop around
			}
		}
		
		Logger.getLogger("Bluez").info("Periodic bluetooth scan started");
		
		// Periodic scan mode enabled, start reader thread
		readThread = new Thread() {
			@Override
			public void run() {
				super.run();
				
				try {
					Process scan = Runtime.getRuntime().exec("hcidump -X -i "+device);
					
					BufferedReader in = new BufferedReader(new InputStreamReader(scan.getInputStream()));
					String line;
					
					while((line = in.readLine()) != null) {
						if (line.toLowerCase().trim().startsWith("exit periodic inquiry mode")) {
							Logger.getLogger("Bluez - Read Thread").info("Clean shutdown");
							// Scan ended, kill thread
							break;
						}
						
						// bdaddr DC:A9:71:1C:C2:4D mode 1 clkoffset 0x16ee class 0x02010c rssi -79
						if (line.toLowerCase().trim().startsWith("bdaddr")) {
							String[] parts = line.toLowerCase().trim().split("\\s+");
							int s = Integer.parseInt(parts[9]);							
							listener.onBluetoothResponse(parts[1], s);
						}
					}
					
					if (!stopping) {
						Logger.getLogger("Bluez - Read Thread").warning("Thread stopping before stop was called!");
					}
					
				} catch(IOException e) {
					System.err.println("Exception in bluetooth read thread!");
					e.printStackTrace();
					System.exit(1);					
				}
			}
		};
		running = true;
		readThread.setDaemon(false);
		readThread.start();
	}
	
	
	public static void stopScan() throws IOException {
		//if(!running) {
		//	return;
		//}
		
		stopping = true;
		
		// Stop periodic can mode
		Process p = Runtime.getRuntime().exec("hcitool cmd 01 0004");		
		while(true) {
			try {
				if (p.waitFor() != 0) {
					throw new IOException("Unable to stop periodic scan, do you have root?");
				}
				break;
			} catch(InterruptedException e) {
				// Ignore and loop around
			}
		}
		// Bring interface down
		p = Runtime.getRuntime().exec("hciconfig "+device+" down");		
		while(true) {
			try {
				if (p.waitFor() != 0) {
					throw new IOException("Unable to bring interface down, do you have root?");
				}
				break;
			} catch(InterruptedException e) {
				// Ignore and loop around
			}
		}

		Logger.getLogger("Bluez").finest("Waiting for read thread to die");
		while(true) {
			try {
				readThread.join();
				Logger.getLogger("Bluez").finest("Joined with read thread");
				break;
			} catch(InterruptedException e) {
				// Ignore and loop around
			}
		}
		
		running = false;
		readThread = null;
		Logger.getLogger("Bluez").info("Scan stopped");
	}
	
}
