package epfl.project.controlinterface.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;

public class OutConnect extends OutputStream {

	private JTextArea textArea;
	
	public OutConnect(JTextArea textArea) {
		this.textArea = textArea;
	}
	
	@Override
	public void write(int b) throws IOException {
		byte [] array = {(byte) (b)};
		textArea.append(new String(array));
	}

	@Override
	public void write(byte[] b) {
		textArea.append(new String(b));
	}
	
	/***
	 * Create a PrintStream object from this object.
	 * @return
	 */
	public PrintStream createPrintStream() {
		return new PrintStream(this);
	}
}
