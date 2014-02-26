package no.ntnu.eit.skeis.central.devices.player;

import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.devices.PlayerManager;
import de.kalass.sonoscontrol.api.control.SonosDevice;
import de.kalass.sonoscontrol.api.core.Callback0;
import de.kalass.sonoscontrol.api.model.avtransport.AVTransportURI;
import de.kalass.sonoscontrol.api.services.AVTransportService;

public class PlayerSonos extends AbstractPlayer {

	private SonosDevice device;
	private Logger log;
	
	public PlayerSonos(PlayerManager manager, String alias, SonosDevice device) {
		super(manager, alias);
		this.device = device;
		log = Logger.getLogger(getClass().getName());
	}
	
	private String url;
	
	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void setPlayState(boolean play) {
		final AVTransportService transport = device.getAVTransportService();
		if(play) {
			transport.setAVTransportURI(AVTransportURI.getInstance(this.url), null, new Callback0() {
                @Override
                public void success() {
                	System.out.println("Set url");
                	transport.play(new Callback0() {
                		@Override
                		public void success() {
                			System.out.println("Started playing");
                			
                		}
                		
                	});
                }
            });
			
		} else {
			transport.stop(new Callback0() {
				public void success() {
                	System.out.println("Stopped playback");
                	transport.removeAllTracksFromQueue(new Callback0() {
                		@Override
                		public void success() {
                			System.out.println("Cleared queue?");
                		}
                	});
                }
			});
		}
	}

	@Override
	public void setVolume(int volume) {
		// TODO Auto-generated method stub

	}

}
