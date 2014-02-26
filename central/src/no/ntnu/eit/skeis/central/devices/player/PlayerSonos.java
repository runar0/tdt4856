package no.ntnu.eit.skeis.central.devices.player;

import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.devices.PlayerManager;
import de.kalass.sonoscontrol.api.control.SonosDevice;
import de.kalass.sonoscontrol.api.model.avtransport.AVTransportURI;

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
		if(play) {
			device.getAVTransportService().setAVTransportURI(AVTransportURI.getInstance(this.url), null, null);
			device.getAVTransportService().play(null);
		} else {
			device.getAVTransportService().stop(null);
		}
	}

	@Override
	public void setVolume(int volume) {
		// TODO Auto-generated method stub

	}

}
