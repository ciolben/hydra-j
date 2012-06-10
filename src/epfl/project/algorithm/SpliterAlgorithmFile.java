package epfl.project.algorithm;

import java.io.File;


/**
 * class : SpliterAlgorithmStandard
 *
 * The spliter algorithm can be redefined by overridden the split function.
 * This spliter must be used when we split a file
 *
 */
public abstract class SpliterAlgorithmFile implements SpliterAlgorithm{
    public File file;

    public SpliterAlgorithmFile(File file) {
        this.file = file;
    }

}
