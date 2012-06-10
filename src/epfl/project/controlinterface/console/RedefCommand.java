/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package epfl.project.controlinterface.console;

import epfl.project.sense.MRMemory;
import epfl.project.sense.Prediction;

/**
 * RedefCommand.java (UTF-8)
 *
 * 23 mai 2012
 * @author Loic
 */
public class RedefCommand extends Command {
    
    private String name = "redef name value";
    
    @Override
    public boolean executeCommand(String[] args, Console console) {
        if (args.length != 3) return false;
        try {
            switch (args[1]) {
                case "partition":
                    int valuei = Integer.valueOf(args[2]);
                    Prediction.setEstimatedMaxIndex(valuei);
                    console.directPrint("partition index set to " + valuei);
                    break;
                case "memory":
                    long valuel = Long.valueOf(args[2]);
                    MRMemory.getInstance().setMemoryThreshold(valuel);
                    console.directPrint("memory threshold set to " + valuel);
                    break;
            }
            
        } catch (NumberFormatException ex) {
            console.directPrint("wrong arguments");
            return false;
        }
        return true;
    }

    @Override
    public boolean isName(String name) {
        return name.equals("redef");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescriptionLine() {
        return "redefine a specified value";
    }

}
