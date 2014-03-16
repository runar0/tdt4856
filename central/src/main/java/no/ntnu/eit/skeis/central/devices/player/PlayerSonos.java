package no.ntnu.eit.skeis.central.devices.player;

import no.ntnu.eit.skeis.central.audio.AudioSource;
import no.ntnu.eit.skeis.central.devices.PlayerManager;
import de.kalass.sonoscontrol.api.control.SonosDevice;
import de.kalass.sonoscontrol.api.core.Callback0;
import de.kalass.sonoscontrol.api.model.avtransport.AVTransportURI;
import de.kalass.sonoscontrol.api.model.renderingcontrol.Channel;
import de.kalass.sonoscontrol.api.model.renderingcontrol.Mute;
import de.kalass.sonoscontrol.api.model.renderingcontrol.MuteChannel;
import de.kalass.sonoscontrol.api.model.renderingcontrol.Volume;
import de.kalass.sonoscontrol.api.services.AVTransportService;

public class PlayerSonos extends AbstractPlayer {

	private SonosDevice device;
	
	public PlayerSonos(PlayerManager manager, String alias, SonosDevice device) {
		super(manager, alias);
		this.device = device;
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

	private Volume volume = null;
	
	@Override
	public void setVolume(int volume) {
		
		Volume v = Volume.getInstance((long) Math.floor(((double)Volume.MAX-Volume.MIN)/100.0 * volume) + Volume.MIN);
		
		device.getRenderingControlService().setVolume(Channel.MASTER, v, new Callback0() {
            @Override
            public void success() {
            	log.info(getAlias() + " volume set");
            }
        });
	}
	
	public int getVolume() {
		if(volume == null) {
			volume = Volume.getInstance((long) Math.floor(((double)Volume.MAX-Volume.MIN)/100.0 * 40) + Volume.MIN); // TODO 
		}
		return volume.getValue().intValue();
	}
	
	private boolean muted = false;
	
	public void setMute(boolean flag) {
		if(flag) {
			device.getRenderingControlService().setMute(MuteChannel.MASTER, Mute.ON, new Callback0() {
	            @Override
	            public void success() {
	            	log.info(getAlias() + " mute set");
	            }
	        });
		} else {
			device.getRenderingControlService().setMute(MuteChannel.MASTER, Mute.ON, new Callback0() {
	            @Override
	            public void success() {
	            	log.info(getAlias() + " mute cleared");
	            }
	        });			
		}
		muted = flag;
	}
	
	public boolean getMute() {
		return muted;
	}

	@Override
	protected void playAudioSource(AudioSource source) {
		setUrl(source.getSonosUrl());
		setPlayState(true);
	}

}
