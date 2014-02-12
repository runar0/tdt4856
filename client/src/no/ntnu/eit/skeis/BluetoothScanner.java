package no.ntnu.eit.skeis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BluetoothScanner {

	interface Listener {
		public void onBluetoothDiscover(String addr, double distance);
	}
	
	private static final float N = 3;
	
	private Process scan;
	
	public void startScan(final Listener listener) throws Exception {
		if (scan != null) {
			throw new Exception("Scan already started");
		}
		
		Process p = Runtime.getRuntime().exec("hcitool cmd 01 0003 0A 00 09 00 33 8b 9e 08 00");
		if (p.waitFor() != 0) {
			System.err.println("Unable to start continous scan");
		}
		p = Runtime.getRuntime().exec("hcitool cmd 03 0045 01");
		if (p.waitFor() != 0) {
			System.err.println("Unable to enable rssi reporting");
		}
		
		final Process scan = Runtime.getRuntime().exec("hcidump -X");
		this.scan = scan;
		new Thread() {
			
			public void run() {
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(scan.getInputStream()));
					String line;
					
					while((line = in.readLine()) != null) {
						// bdaddr DC:A9:71:1C:C2:4D mode 1 clkoffset 0x16ee class 0x02010c rssi -79
						if (line.toLowerCase().trim().startsWith("bdaddr")) {
							String[] parts = line.toLowerCase().trim().split("\\s+");
							int s = Integer.parseInt(parts[9]);
							
							// @TODO 55 is rssi at distance 1 m, specific per device and computer 
							double d = Math.pow(10, -(s + 55.0)/(10.0*N));
							
							listener.onBluetoothDiscover(parts[1], d);
						}
					}
					
				} catch(IOException e) {
					// Stream is closed, ignore
				}
			};
		}.start();
	}
	
	public void stopScan() {
		if (scan == null) {
			return;
		}
		scan.destroy();
		while (true) {
			try {
				scan.waitFor();
			} catch(InterruptedException e) {
				continue;
			}
			break;
		}
		scan = null;
	}
	
}
