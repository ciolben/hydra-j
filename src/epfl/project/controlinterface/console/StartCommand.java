/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package epfl.project.controlinterface.console;

/**
 *
 * @author Loic
 */
public class StartCommand extends Command {
    private final String name = "start [enableprobes]";
    private final String description = "start the mapreduce task";
    
    @Override
    public boolean executeCommand(String[] args, Console console) {
        if (!isName(args[0])) return false;
        console.master.getInstalledConfigurator().setEnableProbes(false);
        if (args.length == 2) {
            if (args[1].equals("enableprobes")) {
                console.master.getInstalledConfigurator().setEnableProbes(true);
                console.directPrint("Probes are enabled for this job.");
            } else {
                return false;
            }
        }
        if (console.master.getState() == Thread.State.TERMINATED) {
            console.master = console.master.clone();
            console.hasStarted = false;
        }
        if (!console.hasStarted) {
                console.hasStarted = true;
                console.tryClose = false;	//Authorization to type close.
                console.master.start();
            } else {
                console.directPrint("A task is already running.");
            }
        return true;
    }

    @Override
    public boolean isName(String name) {
        return name.equals("start");
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getDescriptionLine() {
        return description;
    }
}
