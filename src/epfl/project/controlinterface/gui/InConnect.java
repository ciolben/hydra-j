package epfl.project.controlinterface.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class InConnect extends InputStream {

	private boolean endOfStream = false;
	private LinkedBlockingQueue<String> buffer;
	private byte [] currentReading; //must never be null
	private int currentPos;
	private Object lock;
	
	public InConnect() {
		buffer = new LinkedBlockingQueue<String>();
		currentReading = new byte[0];
		currentPos = 0;
		lock = new Object();
	}

	@Override
	public int read() throws IOException {
		if (endOfStream) return -1;
		synchronized(lock) {
			if (currentPos == currentReading.length) {
				String next = buffer.poll();
				if (next != null) {
					currentReading = next.getBytes();
					currentPos = 0;
				} else {
					try {
						lock.wait();
						assert(currentPos < currentReading.length);
					} catch (InterruptedException e) {
						throw new java.io.IOException("Monitor exception.");
					}
				}
			}
			if (endOfStream) return -1;
			byte b = currentReading[currentPos];
			currentPos++;
			return b;
		}
	}
	
	/***
	 * Fill the stream with a string.
	 * @param data The string to offer to the stream.
	 * @throws InterruptedException
	 */
	public void injectDataString(String data) {
		if (data == null) return;
		synchronized(lock) {
			if (currentPos < currentReading.length) {
				try {
					buffer.put(data);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			} else {
				currentReading = data.getBytes();
				currentPos = 0;
			}
			lock.notify();
		}
	}

	@Override
	public void close() throws IOException {
		endOfStream = true;
		synchronized(lock) {
			lock.notify();
		}
		super.close();
	}
}
