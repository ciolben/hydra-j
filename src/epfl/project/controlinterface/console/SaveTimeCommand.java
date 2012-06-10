/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package epfl.project.controlinterface.console;

import epfl.project.common.OutOfMemory;
import epfl.project.sense.Prediction;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * SaveTimeCommand.java (UTF-8)
 *
 * 23 mai 2012
 * @author Loic
 */
public class SaveTimeCommand extends Command {

    private String name = "savetime [filename]";
    private String description = "save the last map and redcue statistics";
    
    @Override
    public boolean executeCommand(String[] args, Console console) {
        if (!isName(args[0])) return false;
        String filename;
        if (args.length == 1) {
            filename = "log.txt";
        } else {
            filename = args[1];
        }
        File file = new File(filename);
        FileWriter fw;
        if (file.exists()) {
            if (!file.canWrite()) return false;
            try {
                fw = new FileWriter(file, true);
            } catch (IOException ex) {
                console.print(ex);
                return false;
            }
        } else {
            try {
                fw = new FileWriter(file);
                fw.write("kv\tkvest\ttimeest\tmemest\tmemth\tmembase\tmemused\tmap\tred\ttot\tnbmap\tnbred\r\n");
            } catch (IOException ex) {
                console.print(ex);
                return false;
            }
        }
        Prediction.estimateMaxIndex(true);
        String res = "";
        res += Prediction.getKeyValuePerWorker(true) + "\t";
        res += Prediction.getEstimatedMaxIndex() + "\t";
        res += Prediction.getEstimatedTotalTime() + "\t";
        res += Prediction.getEstimatedMemoryThreshold() + "\t";
        res += OutOfMemory.getThreshold() + "\t";
        res += Prediction.justStartedMemory + "\t";
        res += Prediction.getMaxMemoryUsage() + "\t";
        res += Prediction.getLastMapTime() + "\t";
        res += Prediction.getLastReduceTime() + "\t";
        res += Prediction.getLastMapReduceTime() + "\t";
        res += Prediction.getLastMapperNumber() + "\t";
        res += Prediction.getLastReducerNumber() + "\t";
        res += "\r\n";
        
        //debug stage : print derivates
//        try {
//            FileWriter fw2 = new FileWriter("deriv");
//            for (Float f : Prediction.buffer) {
//                fw2.write(Float.toString(f) + "\n");
//            }
//            fw2.write("\r\n");
//            fw2.close();
//        } catch (IOException ex) {
//            
//        }
        
        try {
            fw.write(res);
            fw.close();
            console.print("logged (" + filename + ")");
        } catch (IOException ex) {
            console.print(ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean isName(String name) {
        return name.equals("savetime") || name.equals("save");
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
