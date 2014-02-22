package no.ntnu.eit.skeis.vlcplayer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class VLC {
	Process vlc = null;
	
	private String url;
	
	private Logger log;
	
	public VLC() {
		log = Logger.getLogger(getClass().getName());
	}
	
	public void setUrl(String url) {
		log.info("Setting url to "+url);
		this.url = url;
	}
	
	public void start() {
		log.info("Starting playback of "+url);
		if(vlc != null) {
			stop();
		}
		
		try {
			vlc = Runtime.getRuntime().exec("vlc -I http --http-password 1234 "+url);
		} catch(Exception e) {}
	}
	
	public void stop() {
		log.info("Stopping playback");
		vlc.destroy();
		vlc = null;
	}
	
	public void setVolume(int volume) {
		// VLC uses 0-255, central assumes 0-100
		volume = (int)Math.floor((double)volume*2.55);
		log.info("Setting volume to "+volume);
		try {
			URL u = new URL("http://0.0.0.0:8080/requests/status.xml?command=volume&val="+volume);
			
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.addRequestProperty("Authorization", "Basic OjEyMzQ=");
			
			conn.connect();
			//BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			//String l;
			//while((l = in.readLine()) != null) {
				//System.out.println(l);
			//}
		} catch(Exception e) {
			
		}
	}
}
