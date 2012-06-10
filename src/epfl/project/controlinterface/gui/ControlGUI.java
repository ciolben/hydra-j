package epfl.project.controlinterface.gui;

import epfl.project.controlinterface.console.Console;
import epfl.project.nodes.Master;
import epfl.project.sense.Reporter;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;

public final class ControlGUI implements Reporter {

    private Console console;
    private InConnect cmdOut;
    private JFrame frmMapreduce;

    /**
     * @wbp.parser.constructor
     */
    public ControlGUI() {
        initialize(null);
    }

    /**
     * Create the GUI. Note : gui console must be get back by a call to
     * getConsole.
     */
    public ControlGUI(Master master) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        initialize(master);
        frmMapreduce.setVisible(true);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize(Master master) {
        frmMapreduce = new JFrame();
        frmMapreduce.setResizable(false);
        frmMapreduce.setTitle("MapReduce");
        frmMapreduce.setBounds(100, 100, 565, 400);
        frmMapreduce.getContentPane().setLayout(null);
        
        frmMapreduce.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frmMapreduce.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                console.interpretCmd("quit");
            }
        });
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 212, 539, 149);
        final JTextArea textArea = new JTextArea();
        textArea.setEditable(true);
        textArea.setLineWrap(true);
        textArea.setAutoscrolls(true);
        textArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        textArea.setBounds(0, 0, 539, 149);
        DefaultCaret caret = new DefaultCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        textArea.setCaret(caret);
        scrollPane.setViewportView(textArea);
        
        frmMapreduce.getContentPane().add(scrollPane);

        JLabel lblOutput = new JLabel("Output");
        lblOutput.setBounds(10, 186, 46, 14);
        frmMapreduce.getContentPane().add(lblOutput);
        
        JLabel lblPerc = new JLabel("< percentage"); //66
        JLabel lblTime = new JLabel("< time (ms)"); //55
        final JTextField txtPerc = new JTextField("10");
        final JTextField txtTime = new JTextField("10");
        txtPerc.setBounds(186, 10 + 10, 25, 16);
        lblPerc.setBounds(221, 10 + 11, 66, 14);
        txtTime.setBounds(296, 10 + 10, 25, 16);
        lblTime.setBounds(331, 10 + 11, 55, 14);
        frmMapreduce.getContentPane().add(txtPerc);
        frmMapreduce.getContentPane().add(txtTime);
        frmMapreduce.getContentPane().add(lblPerc);
        frmMapreduce.getContentPane().add(lblTime);
        
        JLabel lblFile = new JLabel("filename : "); //50
        final JTextField txtFile = new JTextField("log.txt");
        lblFile.setBounds(186, 53 + 11, 50, 16);
        txtFile.setBounds(246, 53 + 9, 200, 21);
        frmMapreduce.getContentPane().add(lblFile);
        frmMapreduce.getContentPane().add(txtFile);
        
        buildButton("Start", 10, 11, 78, 33, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
//				cmdOut.injectDataString("start\r\n");
                console.interpretCmd("start");
            }
        });
        
        buildButton("Stop", 10, 54, 78, 33, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                console.interpretCmd("stop");
            }
        });

        final ControlGUI ref = this;
        buildButton("Quit", 10, 97, 78, 33, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                console.interpretCmd("quit");
                synchronized (this) {
                    try {
                        wait(2000);
                    } catch (InterruptedException _) {
                    } finally {
                        ref.frmMapreduce.dispose();
                    }
                }
            }
        });

        buildButton("WarmUp", 98, 11, 78, 33, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //check the fields
                int perc = 0;
                int time = 0;
                try {
                    perc = Integer.valueOf(txtPerc.getText());
                    time = Integer.valueOf(txtTime.getText());
                    if (perc < 1 || time < 1) {
                        textArea.append("WarmUp : Fields < 1\n");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    textArea.append("WarmUp : Wrong fields\n");
                    return;
                }
                //send the command
                console.interpretCmd("warmup " + perc + " " + time);
            }
        });
        
        buildButton("Log", 98, 54, 78, 33, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //check the field
                String name = txtFile.getText();
                if (name.isEmpty()) {
                    textArea.append("Log : Wrong field\n");
                    return;
                }
                //send the command
                console.interpretCmd("save " + name);
            }
        });
        
//	configure in and out stream of the console
        cmdOut = new InConnect(); //maybe useless if interpretCmd is public
        console = new Console(master, cmdOut,
                new OutConnect(textArea).createPrintStream());
        console.setNoInputMode(true);
        textArea.append("Console linked\n");
//		console.start();
    }

    public Console getGUIConsole() {
        return console;
    }

    @Override
    public void print(Object obj) {
        console.print(obj);
    }

    private JButton buildButton(String title, int x, int y, int width,
            int height, ActionListener listener) {
        JButton bt = new JButton(title);
        final ControlGUI ref = this;
        bt.addActionListener(listener);
        bt.setBounds(x, y, width, height);
        frmMapreduce.getContentPane().add(bt);
        return bt;
    }
    
    private int computeStringWidth(JLabel lbl) {
        int res = SwingUtilities.computeStringWidth(lbl.getFontMetrics(
                lbl.getFont()),lbl.getText());
        System.out.println("swidth : " + res);
        return res;
    }
}
