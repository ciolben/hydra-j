/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package epfl.project.controlinterface.console;

/**
 *
 * @author Loic
 */
public class CommandTemplate extends Command {
    private final String name = "name";
    private final String description = "";
    
    @Override
    public boolean executeCommand(String[] args, Console console) {
        if (!isName(args[0])) return false;
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
