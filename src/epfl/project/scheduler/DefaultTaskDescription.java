package epfl.project.scheduler;

import epfl.project.mapreduce.Mapper;
import epfl.project.mapreduce.Reducer;
import java.io.File;

/**
 *
 * @author Loic
 */
public class DefaultTaskDescription extends TaskDescription {
    public DefaultTaskDescription(File input, Mapper mapper) {
        super(null, TaskCategory.MAP);
        setInput(input);
    }
    
    public DefaultTaskDescription(File output, Reducer reducer)
                                                        throws TaskException {
        super(null, TaskCategory.REDUCE);
        setOutput(output);
    }
}
