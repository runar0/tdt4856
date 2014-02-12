package no.ntnu.eit.skeis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VLC {

	Process vlc;
	
	public void start(String source) throws Exception {
		vlc = Runtime.getRuntime().exec("vlc -I http --http-password 1234 "+source);
	}
	
	public void stop() {
		vlc.destroy();
	}
	
	public void setVolume(int volume) {
		if (volume > 150) {
			volume = 150;
		}
		try {
			URL u = new URL("http://0.0.0.0:8080/requests/status.xml?command=volume&val="+volume);
			
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.addRequestProperty("Authorization", "Basic OjEyMzQ=");
			
			conn.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String l;
			while((l = in.readLine()) != null) {
				//System.out.println(l);
			}
		} catch(Exception e) {
			
		}
	}
	
}
