/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package epfl.project.controlinterface.console;

import epfl.project.controlinterface.console.classloader.ClassFilter;
import epfl.project.controlinterface.console.classloader.PackageHelper;
import java.util.Collection;

/**
 *
 * @author Loic
 */
public abstract class Command {

    public Command() {
    }
    
    public static Command[] listCommands() {
        ClassFilter classFilter = new ClassFilter() {
            @Override
            public boolean accept(Class<?> p_Class) {
                return p_Class.getSimpleName().endsWith("Command")
                        && !p_Class.getSimpleName().equals("Command");
            }
        };
        Command [] commands = null;
        try {
            
            Collection<Class<?>> res = PackageHelper.getInstance().getClasses(
                    Command.class.getPackage().getName(), false, classFilter);
            commands = new Command[res.size()];
            int counter = -1;
            for (Class command : res) {
                try {
                    counter++;
                    commands[counter] = (Command) command.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                } finally {
                    continue;
                }
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Cannot create commands list");
        }
        return commands;
    }

    public abstract boolean executeCommand(String [] args, Console console);
    public abstract boolean isName(String name);
    public abstract String getName();
    public abstract String getDescriptionLine();
}
