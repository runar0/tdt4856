package no.ntnu.eit.skeis.central.audio.mp3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A single MP3 frame with header and associated data
 * 
 * NOTE: This is far away from a complete implementation, we only support MPEG1 Level 3, i.e.
 * normal mp3's
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class Frame {

	private static int[] bitrate = new int[] {
		0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, -1
	};
	
	private static int[] samplerate = new int[] {
		44100, 48000, 3200, 0
	};
	
	private byte[] header;
	private byte[] data;
	
	public Frame(byte[] header) {
		this.header = header;
	}
	
	/**
	 * Bit rate
	 * 
	 * @return
	 */
	public int getBitrate() {
		return bitrate[(header[2]&0xF0) >> 4];
	}
	
	/**
	 * Sample rate 
	 * 
	 * @return
	 */
	public int getSamplerate() {
		return samplerate[(header[2] >> 2)&0x03];
	}
	
	/**
	 * Is the frame padded?
	 * 
	 * @return
	 */
	public boolean isPadded() {
		return ((header[2] >> 1)&0x01) == 1;
	}
	
	/**
	 * Get total frame size
	 * @return
	 */
	public int getFrameSize() {
		return ((144 * getBitrate()*1000) / getSamplerate()) + (isPadded() ? 1 : 0);
	}
	
	/**
	 * Validate the frame, we should do this by checking that all combinations of data is 
	 * as expected, in case we synced on a bad header sync byte
	 * 
	 * @return
	 */
	private boolean isValid() {
		// TODO Expand
		return getSamplerate() != 0 && getBitrate() != -1 && getFrameSize() > 4;
	}
	
	private void setData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Dump the entire frame, header and data, to the given output stream
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void writeToOutputStream(OutputStream out) throws IOException {
		out.write(header);
		out.write(data);
	}
	
	/**
	 * Called by the fromInputStream if it detects a ID3 header
	 * 
	 * This should read the entire header off the stream, leaving it where the first mpeg frame is
	 * suppose to start
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static int skipID3Tag(InputStream in) throws IOException {
		// We assume that the 3 byte tag has been read
		byte buffer[] = new byte[7];
		int read = 0;
		while(read < buffer.length) {
			read += in.read(buffer);
		}
		
		int size = (buffer[6]&0x7F) + ((buffer[5]&0x7F) << 7) + ((buffer[4]&0x7F) << 14) + ((buffer[3]&0x7F) << 21);
		//System.out.println("ID3 tag size "+size);
		
		for(int i = 0; i < size; i++) { in.read(); }
		
		return size+10;
	}
	
	/**
	 * Read the next MPEG frame from the given input stream and return it
	 * 
	 * @param in
	 * @return the next frame or null if we're at the end of stream
	 * @throws IOException
	 */
	public static Frame fromInputStream(InputStream in) throws IOException {
		byte[] header = new byte[4];
		Frame frame;
		int read = 0;
		
		int current = 0, previous = in.read();
		@SuppressWarnings("unused")
		int dropped = 0;
		while((current = in.read()) != -1) {
			//System.out.println(previous + " - " + current);
			if (previous == 0xFF && (current&0xFF) == 0xFB) {
				// We found the frame header
				header[0] = (byte)(previous&0xFF);
				header[1] = (byte)(current&0xFF);
				
				// Place a mark and create frame a frame instance
				in.mark(10);
				
				read = 0;
				while(read < 2) {
					read += in.read(header, 2+read, 2-read);
					
				}
				if(read != 2) {
					System.out.print("Read is only "+read+"!");
					System.exit(1);
				}
				frame = new Frame(header);
				
				// TODO Validate that the frame is actually valid
				if(frame.isValid()) {
					//System.out.println("Found MP3 frame header after dropping "+dropped+" bytes");
					
					//System.out.println("Bitrate "+frame.getBitrate());
					//System.out.println("Samplerate "+frame.getSamplerate());
					//System.out.println("Is padded: "+frame.isPadded());
					//System.out.println("Frame size:"+frame.getFrameSize());
					
					byte[] data = new byte[frame.getFrameSize()-4];
					
					read = 0;
					while(read < data.length) {
						read += in.read(data, read, data.length-read);						
					}

					frame.setData(data);
					if(read != data.length) {
						System.out.println("Read is only "+read+" expected "+data.length);
						System.exit(1);
					}
					
					return frame;
				} else {
					in.reset();
				}	
			// HEAD ID3
			} else if(previous == 'I' && current == 'D') {
				in.mark(1);
				if ((previous = in.read()) == '3') {
					dropped += skipID3Tag(in) + 1;
				} else {
					in.reset();
				}
			// Tail TAG
			} else if(previous == 'T' && current == 'A') {
				in.mark(1);;
				if ((previous = in.read()) == 'G') {
					return null;
				} else {
					in.reset();
				}
			}
			dropped ++;
			previous = current;
		}
		return null;
		
	}

}
