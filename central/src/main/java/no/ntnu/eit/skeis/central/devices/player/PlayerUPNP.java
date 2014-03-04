package no.ntnu.eit.skeis.central.devices.player;

import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.audio.AudioSource;
import no.ntnu.eit.skeis.central.devices.PlayerManager;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

/**
 * Player implementation that uses a AVTransport UPNP backend
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class PlayerUPNP extends AbstractPlayer {

	class SetUrlActionInvocation extends ActionInvocation<RemoteService> {
		public SetUrlActionInvocation(RemoteService service, String url) {
			super(service.getAction("SetAVTransportURI"));
			try {
				setInput("InstanceID", new UnsignedIntegerFourBytes(0));
				setInput("CurrentURI", url.replace("x-rincon-mp3radio:", "http"));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	class PlayActionInvocation extends ActionInvocation<RemoteService> {
		public PlayActionInvocation(RemoteService service) {
			super(service.getAction("Play"));
			try {
				setInput("InstanceID", new UnsignedIntegerFourBytes(0));
				setInput("Speed", "1");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class StopActionInvocation extends ActionInvocation<RemoteService> {
		public StopActionInvocation(RemoteService service) {
			super(service.getAction("Stop"));
			try {
				setInput("InstanceID", new UnsignedIntegerFourBytes(0));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	class DefaultActionCallback extends ActionCallback {
		private Logger log;
		public DefaultActionCallback(ActionInvocation<RemoteService> invocation, Logger log) {
			super(invocation);
			this.log = log;
		}
		public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			log.warning("Failure during action invocation, message: '"+defaultMsg+"'");
			log.warning(invocation.toString());
		};
		public void success(ActionInvocation invocation) {};
	}
	
	private final RemoteService service;
	private final UpnpService upnp;
	
	private final Logger log;
	
	/**
	 * Construct a new Player instance based on a UPNP AVTransport service
	 * 
	 * @param manager
	 * @param alias
	 * @param upnp
	 * @param service
	 */
	public PlayerUPNP(PlayerManager manager, String alias, UpnpService upnp, RemoteService service) {
		super(manager, alias);		
		this.service = service;
		this.upnp = upnp;
		log = Logger.getLogger(getClass().getName());
	}
	
	/**
	 * Set playback url
	 */
	@Override
	public void setUrl(String url) {
		upnp.getControlPoint().execute(
			new DefaultActionCallback(new SetUrlActionInvocation(service, url), log)
		);
	}

	/**
	 * Start/Stop playback
	 */
	@Override
	public void setPlayState(boolean play) {
		ActionInvocation<RemoteService> action;
		if(play) {
			action = new PlayActionInvocation(service);
		} else {
			action = new StopActionInvocation(service);
		}
		upnp.getControlPoint().execute(new DefaultActionCallback(action, log));
	}

	@Override
	public void setVolume(int volume) {
		log.info("setVolume - NOT IMPLEMENTED FOR UPNP DEVICES");
		/*upnp.getControlPoint().execute(
			new DefaultActionCallback(new SetUrlActionInvocation(service, url), log)
		);*/
	}

	@Override
	protected void playAudioSource(AudioSource source) {
		setUrl(source.getHttpUrl());
		setPlayState(true);
		setVolume(50);
	}

}
