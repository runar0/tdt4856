package no.ntnu.eit.skeis.central.upnp.mediarenderer;

import no.ntnu.eit.skeis.central.Device;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.renderingcontrol.RenderingControlException;

public class CentralAudioRenderingControl extends AbstractAudioRenderingControl {

	private final CentralMediaRenderer mediarenderer;
	private final int instance;
	
	private UnsignedIntegerTwoBytes volume;
	private boolean mute;
	
	private final Channel[] channels = new Channel[] { Channel.LF, Channel.RF };
	
	public CentralAudioRenderingControl(CentralMediaRenderer mediarenderer, int instance, LastChange lastChange) {
		super(lastChange);
		this.mediarenderer = mediarenderer;
		this.instance = instance;
		
		volume = new UnsignedIntegerTwoBytes(100);
		mute = false;
	}

	@Override
	public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
		return new UnsignedIntegerFourBytes[] { new UnsignedIntegerFourBytes(0) };
	}

	@Override
	protected Channel[] getCurrentChannels() {
		return channels;
	}

	@Override
	public boolean getMute(UnsignedIntegerFourBytes instanceId,
			String channelName) throws RenderingControlException {
		return mute;
	}

	@Override
	public void setMute(UnsignedIntegerFourBytes instanceId,
			String channelName, boolean desiredMute)
			throws RenderingControlException {
		mute = desiredMute; 
		if(mediarenderer.getStreamingServer(instance) != null) {
			Device d = mediarenderer.getStreamingServer(instance).getDevice();
			if(d.getPlayerConnection() != null) {
				d.getPlayerConnection().setVolume((mute ? 0 : 50));
			}
		}
		
	}

	@Override
	public UnsignedIntegerTwoBytes getVolume(
			UnsignedIntegerFourBytes instanceId, String channelName)
			throws RenderingControlException {
		return volume;
	}

	@Override
	public void setVolume(UnsignedIntegerFourBytes instanceId,
			String channelName, UnsignedIntegerTwoBytes desiredVolume)
			throws RenderingControlException {
		volume = desiredVolume; 
		if(mediarenderer.getStreamingServer(instance) != null) {
			Device d = mediarenderer.getStreamingServer(instance).getDevice();
			if(d.getPlayerConnection() != null) {
				d.getPlayerConnection().setVolume((int)Math.min(volume.getValue(), 80));
			}
		}
	}

}
