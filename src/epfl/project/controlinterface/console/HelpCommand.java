/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package epfl.project.controlinterface.console;

/**
 *
 * @author Loic
 */
public class HelpCommand extends Command {
    private final String name = "help";
    private final String description = "print this help message";
    
    @Override
    public boolean executeCommand(String[] args, Console console) {
        if (!isName(args[0])) return false;
        console.print(console.compileHelp());
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
