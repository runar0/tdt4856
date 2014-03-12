package no.ntnu.eit.skeis.android;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import no.ntnu.eit.skeis.protocol.BeaconProtos.CentralBeacon;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

/**
 * Background service that will listen for beacons from the central and 
 * response with a simple JSON API call containing the bluetooth mac as well as possibly
 * a nickname for the device
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class BackgroundService extends Service {

	private class Lookout extends Thread {
		private boolean running;
		private String mac;
		
		private long serverId = 0;
		private Vibrator v;
		
		private String alias = "";
		
		public Lookout(String mac, Vibrator v, String alias) {
			this.v = v;
			this.mac = mac;
			this.alias = alias;
		}
		
		@Override
		public void run() {
			running = true;
			DatagramSocket socket = null;			
			try {
				socket = new DatagramSocket(12354);
				socket.setBroadcast(true);
				byte[] buffer = new byte[1024];
				Log.w(getClass().getName(), "Lookout waiting for beacon on port 12354");
				
				while(running) {
					try {
						DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
						socket.receive(packet);			
						

						CentralBeacon beacon = CentralBeacon.parseDelimitedFrom(new ByteArrayInputStream(buffer));
						
						// Server ID changed, we should let it know who we are
						if(serverId != beacon.getServerId()) {
							serverId = beacon.getServerId();
							
							Socket apisocket = new Socket(packet.getAddress().getHostAddress(), beacon.getApiPort());
							apisocket.getOutputStream().write((
								"GET /?action=mapDevice&mac="+mac+"&alias="+alias+" HTTP/1.1\r\n"+
								"\r\n"
							).getBytes());
							apisocket.getOutputStream().flush();
							apisocket.close();
							
							Log.w(getClass().getName(), "Received beacon package from "+packet.getAddress().getHostAddress() + " i would have reported "+mac);
							v.vibrate(500);
						}
						
						// TODO Send a Toast and possibly vibrate the phone
						// TODO Actually send data
						
						
					} catch(IOException e) {}
				}				
			} catch(IOException e) {
			} finally {
				if(socket != null) {
					socket.close();
				}
			}
			Log.w(getClass().getName(), "Killing lookout");
		}
	}
	
	private Lookout lookout;

	@Override
	public void onCreate() {
		
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();		
		Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				 
		// Start background lookout thread
		lookout = new Lookout(bluetooth.getAddress().toLowerCase(), v, intent.getExtras().get("alias").toString());
		lookout.setPriority(Thread.MIN_PRIORITY);
		lookout.start();
		
		
		Toast.makeText(this, "SKEIS Central Lookout started", Toast.LENGTH_LONG).show();
		
		// Make sure we're restarted if killed
return START_REDELIVER_INTENT;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy() {
		if(lookout != null) {
			lookout.running = false;
		}

		Toast.makeText(this, "SKEIS Central Lookout stopped", Toast.LENGTH_LONG).show();
	}
}
