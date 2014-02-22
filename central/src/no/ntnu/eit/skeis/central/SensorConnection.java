package no.ntnu.eit.skeis.central;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.protocol.device.SensorProtos.SensorUpdate;

/**
 * SensorConnection
 * 
 * Central<->Sensor connection object
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class SensorConnection {

	private final Logger log;
	private final SensorManager manager;
	
	private InputStream in;
	private OutputStream out;
	
	private String alias;
	
	private Thread readThread;
	
	
	public SensorConnection(SensorManager manager, String alias, InputStream in, OutputStream out) {
		log = Logger.getLogger(getClass().getName());
		this.alias = alias;
		this.in = in;
		this.out = out;
		this.manager = manager;
		startReadLoop();
	}
	
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Start a read thread that continues to read sensor updates until the stream is
	 * closed. On every update read a message is sent to the central for further 
	 * processing.
	 * 
	 */
	private void startReadLoop() {
		final SensorConnection sensor = this;
		readThread = new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						SensorUpdate update = SensorUpdate.parseDelimitedFrom(in);
						
						if (update == null) {
							throw new IOException("Sensor update is null");
						}
						
						manager.onSensorUpdate(sensor, update);						
					} catch(IOException ioe) {
						log.info("Excpetion in read loop, dropping sensor");
						log.warning(ioe.toString());
						manager.removeSensor(alias);
						return;
					}
				}
			}
		};
		readThread.start();
	}
	
	public String toString() {
		return "Sensor["+alias+"]";
	}
	
}
