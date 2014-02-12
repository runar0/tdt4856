package no.ntnu.eit.skeis;

public class Main implements BluetoothScanner.Listener {

	public static void main(String[] args) throws Exception {
		new Main();
	}
	
	BluetoothScanner scanner;
	VLC vlc;
	
	public Main() throws Exception {
		vlc = new VLC();
		vlc.start("http://lyd.nrk.no/nrk_radio_p1_sogn_og_fjordane_mp3_h.m3u");
		vlc.setVolume(0);
		
		scanner = new BluetoothScanner();
		scanner.startScan(this);
	}
	
	private int lastVol = -1;
	
	@Override
	public void onBluetoothDiscover(String addr, double distance) {
		
		if (addr.startsWith("a8:")) {
			System.out.println(addr + " - d "+distance);
			int vol = 0;
			if (distance > 7) {
				vol = 0;
			} else if(distance > 3) {
				vol = (int) Math.round((200)*(1-(distance-3)/5));
			} else {
				vol = 200;
			}
			
			if (lastVol != vol) {
				System.out.println(vol);
				vlc.setVolume(vol);
				lastVol = vol;
			}
		}
		
	}
	
}
