package no.ntnu.eit.skeis.vlcplayer;



/**
 * Sensor emulation
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class VLCPlayer {
	
	public final static VLC vlc = new VLC();
	
	/**
	 * Client version. 
	 * 
	 * Increment after a change that will break backwards compatibility to prevent
	 * hard to detect bugs in VLCPLayer<->Central communication.
	 */
	public final static long VERSION = 1;
	
	public static void main(String[] args) throws Exception {
		UPNPServer upnp = new UPNPServer();
		upnp.start();		
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				vlc.destroy();
			}
		});
	}
}
