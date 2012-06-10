package epfl.project.controlinterface.console;

import epfl.project.sense.Prediction;

/**
 * WarmCommand.java (UTF-8)
 *
 * 18 mai 2012
 * @author Loic
 */
public class WarmCommand extends Command {

    private final String name = "warmup [perc] [sec]";
    private final String description = "run a test on the input file (< perc or sec)";
    private final int DEFAULT_PERC = 10;
    private final int DEFAULT_SEC = 10;
    
    @Override
    public boolean executeCommand(String[] args, Console console) {
        if (!isName(args[0])) return false;
        if (args.length != 3) {
            Prediction.warmupMapReduce(DEFAULT_PERC, DEFAULT_SEC);
        } else {
            try {
                Prediction.warmupMapReduce(Integer.valueOf(args[1]),
                    Integer.valueOf(args[2]));
            } catch (NumberFormatException ex) {
                console.directPrint("Error : args must be integers.");
            }
        }
        
        return true;
    }

    @Override
    public boolean isName(String name) {
        return name.equals("warmup") || name.equals("warm");
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
