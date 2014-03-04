package no.ntnu.eit.skeis.central.upnp.mediarenderer;

import no.ntnu.eit.skeis.central.audio.StreamingSource;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.renderingcontrol.RenderingControlException;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelMute;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

public class CentralAudioRenderingControl extends AbstractAudioRenderingControl {

	private final CentralMediaRenderer mediarenderer;
	private final int instance;
	
	private final Channel[] channels = new Channel[] { Channel.Master };
	
	public CentralAudioRenderingControl(CentralMediaRenderer mediarenderer, int instance) {
		this.mediarenderer = mediarenderer;
		this.instance = instance;
	}
	
	private StreamingSource getStream() {
		return mediarenderer.getStreamingServer(instance);
	}

	@Override
	public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
		return new UnsignedIntegerFourBytes[] { new UnsignedIntegerFourBytes(0), new UnsignedIntegerFourBytes(0) };
	}

	@Override
	protected Channel[] getCurrentChannels() {
		return channels;
	}

	@Override
	public boolean getMute(UnsignedIntegerFourBytes instanceId,
			String channelName) throws RenderingControlException {
		if(getStream() != null) {
			return getStream().getMute();
		}
		return false;
	}

	@Override
	public void setMute(UnsignedIntegerFourBytes instanceId,
			String channelName, boolean desiredMute)
			throws RenderingControlException {
		if(getStream() != null) {
			getStream().setMute(desiredMute);
			getLastChange().setEventedValue(
				instanceId,
				new RenderingControlVariable.Mute(new ChannelMute(Channel.Master, getStream().getMute()))
			);			
		}
		
	}

	@Override
	public UnsignedIntegerTwoBytes getVolume(
			UnsignedIntegerFourBytes instanceId, String channelName)
			throws RenderingControlException {
		if(getStream() != null) {
			return new UnsignedIntegerTwoBytes(getStream().getVolume());
		}
		return new UnsignedIntegerTwoBytes(0);
	}

	@Override
	public void setVolume(UnsignedIntegerFourBytes instanceId,
			String channelName, UnsignedIntegerTwoBytes desiredVolume)
			throws RenderingControlException {
		if(getStream() != null) {
			getStream().setVolume(desiredVolume.getValue().intValue());
			getLastChange().setEventedValue(
				instanceId,
				new RenderingControlVariable.Volume(new ChannelVolume(Channel.Master, getStream().getVolume()))
			);			
		}
	}

}
