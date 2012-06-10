/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package epfl.project.controlinterface.console;

import epfl.project.sense.Prediction;

/**
 * KVCommand.java (UTF-8)
 *
 * 17 mai 2012
 * @author Loic
 */
public class KVCommand extends Command {

    private String name = "kv_test";
    
    @Override
    public boolean executeCommand(String[] args, Console console) {
        console.directPrint("will calculate the average number kv on a complete mr job.");
        float res = Prediction.getKeyValuePerWorker(false);
        console.directPrint("Prediction : avg key/value pair per worker : " + res);
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
        return "calculate the average key/value on a sample.";
    }

}
