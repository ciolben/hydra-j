package epfl.project.algorithm;

import epfl.project.common.DataWrapper;
import java.io.IOException;

/**
 * *
 *
 * class : SpliterAlgorithmStandard
 *
 * General implementation of the splitter
 *
 */
public interface SpliterAlgorithm {
    
    /**
     * The method is called each time a worker needs to be feed with new data.
     * 
     * @return the data wrapper containing the data.
     * @throws IOException
     */
    public DataWrapper nextChunk() throws IOException;
    
    /**
     * *
     * Reset the algorithm. All fields must be set to initial values.
     */
    public void reset();
}
