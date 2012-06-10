/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package epfl.project.controlinterface.console;

import epfl.project.sense.BeanServer;

/**
 *
 * @author Loic
 */
public class QuitCommand extends Command {
    private final String name = "quit";
    private final String description = "stop and exit the application";
    
    @Override
    public boolean executeCommand(String[] args, Console console) {
        if (!isName(args[0])) return false;
        console.interpretCmd("stop notclone");
        console.close();
        console.directPrint("rmi : shutting down services");
        BeanServer.getInstance().stopServer();
        console.print("Bye.");
        return true;
    }

    @Override
    public boolean isName(String name) {
        return this.name.equals(name);
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
