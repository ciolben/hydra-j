package epfl.project.controlinterface.gui;

import epfl.project.controlinterface.console.Console;
import epfl.project.nodes.Master;
import epfl.project.sense.Reporter;
import javax.swing.JTextArea;

/**
 *
 * @author Loic
 */
public class ConsoleBean extends JTextArea implements Reporter {
    private Console console = null;
    
    public ConsoleBean() {
        super();
        this.setEditable(false);
    }

    public void setMaster(Master master) {
        console = new Console(master, null,
                new OutConnect(this).createPrintStream());
    }

    @Override
    public void print(Object obj) {
        if (console == null) return;
        console.print(obj);
        this.setCaretPosition(WIDTH);
    }
}
