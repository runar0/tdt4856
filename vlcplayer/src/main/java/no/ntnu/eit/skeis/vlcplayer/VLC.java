package no.ntnu.eit.skeis.vlcplayer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * VLC controller
 * 
 * This starts a VLC instance in the background with the HTTP interface enabled. Control of 
 * playback is done using HTTP get requests.
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class VLC {
	
	/**
	 * VLC process handle
	 */
	private final Process vlc;
	
	private final String VLC_URL = "http://0.0.0.0:12358/requests/status.xml";
	
	/**
	 * Last playback URL set.
	 * 
	 * This is stored because the interface expects a call to setURL() to change the url but not
	 * do anything with the current play state. But VLC will begin playback when we load a new URL.
	 */
	private String url = null;
	
	private Logger log;
	
	/**
	 * Construct a new VLC instance
	 * 
	 */
	public VLC() {
		log = Logger.getLogger(getClass().getName());
		
		// Make sure we shutdown gracefully taking VLC with us
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if(vlc != null) {
					vlc.destroy();	
				}
			}
		});
		
		Process vlc = null;
		try {
			vlc = Runtime.getRuntime().exec("vlc -I http --http-port 12358 --http-password 1234");
		} catch(IOException e) {			
			// At this point there is no need to run, so we can just kill it here
			System.err.println("UNABLE TO START VLC!");
			System.exit(1);
		}
		this.vlc = vlc;
	}
	
	/**
	 * Set playback URI but do not start playback untill start is called
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
		log.info("Setting url to "+url);
		this.url = url;
	}
	
	/**
	 * Helper method for sending a get request
	 */
	private void sendRequest(String params) throws IOException {
		URL u = new URL(VLC_URL+"?"+params);
		
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		// We're lazy, this would match :1234
		conn.addRequestProperty("Authorization", "Basic OjEyMzQ=");		
		conn.connect();
		
//		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//		String l;
//		while((l = in.readLine()) != null) {
//			System.out.println(l);
//		}
	}
	
	public void start() {
		log.info("Starting playback of "+url);
		try {
			setVolume(50);
			sendRequest("command=in_play&input="+url);
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public void stop() {
		log.info("Stopping playback");
		try {
			sendRequest("command=pl_stop");
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public void setVolume(int volume) {
		// VLC uses 0-255, central assumes 0-100
		volume = (int)Math.floor((double)volume*2.55);
		log.info("Setting volume to "+volume);
		try {
			sendRequest("command=volume&val="+volume);
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
