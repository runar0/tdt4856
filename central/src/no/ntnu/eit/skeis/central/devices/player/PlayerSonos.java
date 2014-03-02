package no.ntnu.eit.skeis.central.devices.player;

import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.audio.AudioSource;
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
		log.info("PlayerSONOS set url: "+url);
		this.url = url;
	}

	@Override
	public void setPlayState(boolean play) {
		final AVTransportService transport = device.getAVTransportService();
		if(play) {
			System.out.println(this.url);
			transport.setAVTransportURI(AVTransportURI.getInstance(this.url), null, new Callback0() {
                @Override
                public void success() {
                	transport.play(null);
                }
            });
			
		} else {
			transport.stop(null);
		}
	}

	@Override
	public void setVolume(int volume) {
		log.info("SET VOLUME NOT IMPLEMETED FOR SONOS PLAYERS");

	}

	@Override
	protected void playAudioSource(AudioSource source) {
		setUrl(source.getSonosUrl());
		setPlayState(true);
	}

}
