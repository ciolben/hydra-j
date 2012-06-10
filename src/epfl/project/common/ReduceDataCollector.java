package epfl.project.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ReduceDataCollector<KR extends Comparable<KR>, VR>
extends AbstractDataCollector<KR, VR>{

    private ArrayList<Tuple<KR, VR>> reduceData;
    private BufferedWriter bWriter = null;
    private String thisReduceResultFileName;

    public ReduceDataCollector(boolean redundantWorkersEnabled) {
        reduceData = new ArrayList<>();
        if (redundantWorkersEnabled){
        	super.enableHash();
			}
    }

    @Override
    public void collect(KR key, VR value) {
    	super.collect(key, value);
        reduceData.add(new Tuple<>(key, value));
    }

    public ArrayList<Tuple<KR, VR>> getReduceData() {//deprecated not used anymore we use only getPartition
        return reduceData;
    }

    private synchronized void createBufferedWriterReducer(int assignedTaskID) {
    
    	
    }
    
    
    /**
     * write to disk using a BufferedWriter, this reducer partition
     */
    public synchronized void writeReducerPartialResult(int assignedTaskID){


    	createResultDirectory();
    	
    	File file = new File("Result" + "/" + assignedTaskID);
        if (!file.exists()) {
            file.mkdir();
        }
        try {
        	thisReduceResultFileName = "Result" + "/"+ assignedTaskID + "/" + this + ".txt";
            bWriter = new BufferedWriter(
                    new FileWriter(thisReduceResultFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
        for (Tuple<KR, VR> reduceTuple : reduceData) {
            writeFinalKeyValue(reduceTuple);
            //maybe give possibility to delete from memory if this return true -> remove
            //reduceTuple = null;
        }
        //maybe give possibility to delete from memory if this return true -> remove
       // reduceData = null;
        
        
        if (bWriter != null) {
            try {
                bWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
    }

    /**
     * Write the final key value pair. Each reducer write his final key value
     * pair in his own file.
     *
     * @param id the identification of the reducer
     * @param tuple the key value pair to write on the hdd
     */
    private boolean writeFinalKeyValue( Tuple<?, ?> tuple) {
        boolean ret = false;
    	if (bWriter != null) {
            try {
                bWriter.write(tuple.toString() + "\n");
                
                bWriter.flush();///TODO maybe is not a good idea...
                ret = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("BufferedWriter for the Reducer " + this
                    + " has not been created ");
        }
    	return ret;
    }

    /**
     * close the current BufferedWriter of a reducer
     */
    private void closeBuffereWriterReducer() {
        
    }

	@Override
	public void clearCollector() {
		if (reduceData != null){
			reduceData.clear();
			reduceData = null;
		}
		if(bWriter != null) {
			closeBuffereWriterReducer();
			bWriter = null;
		}
		
		
	}

	@Override
	public ArrayList<Tuple<KR, VR>> getPartition(int index) {
		return reduceData;
	}
}
