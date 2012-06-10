package epfl.project.algorithm;

import epfl.project.common.DataWrapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * *
 *
 * class : SpliterAlgorithm
 *
 * The splitter algorithm can be redefined by overridden the split function.
 *
 */
public class DefaultSpliterAlgorithm extends SpliterAlgorithmFile {

    private byte[] parti;
    public int chunkSize = 250000;
    private RandomAccessFile fis;

    public DefaultSpliterAlgorithm(File file) {
        super(file);

        try {
            fis = new RandomAccessFile(file, "r");
            fis.seek(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.parti = new byte[chunkSize];
    }

    @Override
    public DataWrapper nextChunk() throws IOException {
        DataWrapper data = new DataWrapper();
        int i = fis.read(parti);
        if (i > 0) {
            data.wrapByteArray(Arrays.copyOfRange(parti, 0, i));
            return data;
        }

        return null;

    }

    @Override
    public void reset() {
        try {
            fis.seek(0);
        } catch (IOException _) {
        }
    }
}
