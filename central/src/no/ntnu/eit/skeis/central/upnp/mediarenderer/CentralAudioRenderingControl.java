package no.ntnu.eit.skeis.central.upnp.mediarenderer;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.renderingcontrol.RenderingControlException;

public class CentralAudioRenderingControl extends AbstractAudioRenderingControl {

	@Override
	public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
		System.out.println("AC.getCurrentInstanceIds");
		return new UnsignedIntegerFourBytes[] { new UnsignedIntegerFourBytes(0) };
	}

	@Override
	protected Channel[] getCurrentChannels() {
		System.out.println("AC.getCurrentChannels");
		return new Channel[] { Channel.LF, Channel.RF };
	}

	@Override
	public boolean getMute(UnsignedIntegerFourBytes instanceId,
			String channelName) throws RenderingControlException {
		System.out.println("AC.getMute");
		return false;
	}

	@Override
	public void setMute(UnsignedIntegerFourBytes instanceId,
			String channelName, boolean desiredMute)
			throws RenderingControlException {
		System.out.println("AC.setMute");
		
	}

	@Override
	public UnsignedIntegerTwoBytes getVolume(
			UnsignedIntegerFourBytes instanceId, String channelName)
			throws RenderingControlException {
		System.out.println("AC.getVolume");
		return null;
	}

	@Override
	public void setVolume(UnsignedIntegerFourBytes instanceId,
			String channelName, UnsignedIntegerTwoBytes desiredVolume)
			throws RenderingControlException {
		System.out.println("AC.setVolume");
		
	}

}
