/** Console **/
//**a**/

package epfl.project.controlinterface.console;

import epfl.project.nodes.Master;
import epfl.project.sense.Reporter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;

public class Console implements Runnable, Reporter {

    protected Master master;
    private PrintStream out;
    private InputStream in;
    private Command[] cmds;
    private int verbose = 1;
    private boolean noInputMode = false;
    private boolean enable = true;
    protected boolean tryClose = false;
    protected boolean hasStarted = false;
    private String versionInfo = "MapReduce console | type help for "
            + "cmd list";

    public Console(Master master) {
        this.master = master;
        out = System.out;
        in = System.in;
        createCommandList();
    }

    public Console(Master master, InputStream in, PrintStream out) {
        this.master = master;
        if (out == null) {
            this.out = System.out;
        } else {
            this.out = out;
        }
        if (in == null) {
            this.in = System.in;
        } else {
            this.in = in;
        }
        createCommandList();
    }

    private void createCommandList() {
        cmds = Command.listCommands();
        if (cmds == null) {
            cmds = new Command[1];
            cmds[0] = new Command() {

                @Override
                public boolean executeCommand(String[] args, Console console) {
                    print("Command list is empty");
                    return true;
                }

                @Override
                public boolean isName(String name) {
                    return false;
                }

                @Override
                public String getName() {
                    return "";
                }

                @Override
                public String getDescriptionLine() {
                    return "";
                }
            };
        }
    }

    /**
     * Set the level of wordiness. The level can be between <i>0</i>&nbsp and
     * <i>5</i>&nbsp included.
     * @param num integer in [0, 5]
     */
    public void setVerbose(int num) {
        verbose = Math.abs(num);
    }

    public void addVersionInfo(String info) {
        versionInfo += "\n" + info;
    }

    /**
     * Print a message in output.
     * @param msg the message/object to print.
     */
    @Override
    public synchronized void print(Object msg) {
        if (verbose == 0) {
            return;
        }
        GregorianCalendar time = new GregorianCalendar();
        out.println("< " + time.get(Calendar.HOUR_OF_DAY)
                + ":" + time.get(Calendar.MINUTE)
                + ":" + time.get(Calendar.SECOND)
                + "" + (verbose > 4 ? ":" + time.get(Calendar.MILLISECOND) : "")
                + " " + msg);
    }

    /**
     * Print a message in output.
     * @param msg the message/object to print.
     * @param verbose indicate the level of wordiness of the message. If it is
     * <i>lower</i> than the one set, then the message will be displayed.
     */
    public void print(Object msg, int verbose) {
        if (this.verbose > verbose) {
            print(msg);
        }
    }
    
    /**
     * Print in output without adding a timestamp.
     * @param msg the object to print.
     */
    public void directPrint(Object msg) {
        out.println(msg);
    }

    @Override
    public void run() {
        //****************
        out.println(versionInfo);
        //****************
        String entry;
        LinkedList<Integer> buffer = new LinkedList<>();
        while (enable) {
            if (!noInputMode) {
                out.print(">> ");
            }
            try {
                buffer.clear();
                //read a new line
                do { //10 : newline \n
                    buffer.add(new Integer(in.read()));
                } while (buffer.getLast() != 10);
                buffer.removeLast();
                //check if the last but one is \r (char = 13)
                if (buffer.size() != 0) {
                    if (buffer.getLast() == 13) {
                        buffer.removeLast();
                    }
                }

                byte[] buffArray = new byte[buffer.size()];
                int i = 0;
                for (int integer : buffer) {
                    buffArray[i] = (byte) integer;
                    i++;
                }
                entry = new String(buffArray);
            } catch (IOException ex) {
                System.err.println("Error with input stream. Bye.");
                enable = false;
                return;
            }
            interpretCmd(entry);
        }
    }

    public void close() {
        enable = false;
        try {
            in.close();
        } catch (IOException _) {
        }
    }

    public void interpretCmd(String input) {
        if (input.isEmpty()) {
            return;
        }
        String[] cmdsArray = input.split(" ");
        String cmdName = cmdsArray[0];

        if (cmdName.isEmpty()) {
            return;
        }
        //*****check builtin cmd
        switch (cmdName) {
            case "version":
                directPrint(versionInfo);
                return;
            case "verbose":
                if (cmds.length == 2) {
                    try {
                        verbose = Math.abs(Integer.parseInt(cmdsArray[1]));
                    } catch (NumberFormatException ex) {
                        directPrint("wrong argument.");
                    }
                } else {
                    directPrint("wrong number of argument.");
                }
                return;
        }
        //******
        outfor : {
            for (Command cmd : cmds) {
                if (cmd.isName(cmdName)) {
                    if (!cmd.executeCommand(cmdsArray, this)) {
                        directPrint("invalid command");
                    }
                    break outfor;
                }
            }
            directPrint("unkown command : " + input);
        }
    }

    protected String compileHelp() {
        String help = "MapReduce help\n"
                + "version\t\tprint version info\n"
                + "verbose num\twordiness between 0 and 5 (5: max)\n";
        for (Command cmd : cmds) {
            help = help.concat(cmd.getName() + (cmd.getName().length() < 9 ? "\t\t"
                    : cmd.getName().length() < 17 ? "\t"
                    : " ")
                    + cmd.getDescriptionLine() + "\n");
        }
        return help;
    }

    public void setNoInputMode(boolean choice) {
        noInputMode = choice;
    }
}
