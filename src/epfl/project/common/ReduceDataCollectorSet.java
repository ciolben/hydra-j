package epfl.project.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import epfl.project.scheduler.TaskDescription;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class allows us to access to Reduce collector keeping final
 * keyvalue pair
 */
public class ReduceDataCollectorSet<KR extends Comparable<KR>, VR> extends AbstractDataCollectorSet<KR, VR>{

	private CollectionSerializer serializer;
	private ConcurrentLinkedQueue<String> collectorDataOnDisk;
	private ConcurrentLinkedQueue<ReduceDataCollector> collectorOnMemory;
	private Kryo kryo;

	
    public ReduceDataCollectorSet(TaskDescription taskDescription) {
        super(taskDescription);
        collectorOnMemory = new ConcurrentLinkedQueue<>();
        
  			kryo = new Kryo();
     	   serializer = new CollectionSerializer(kryo);
  			serializer.setElementClass(Tuple.class);
  		 	serializer.setElementsCanBeNull(false);//TODO not sure maybe value = null??
  		 	
  		 	collectorDataOnDisk = new ConcurrentLinkedQueue<>();
    }
    
	@Override
   public void addCollector( AbstractDataCollector<KR, VR> newCollector) {
		if (newCollector instanceof ReduceDataCollector){
    	collectorOnMemory.add((ReduceDataCollector) newCollector);
		}
    }  
       
   
    public void mergeFinalResult() {
    	
    	
        File resultFile = new File("Result");
        File fileToMerge;

        if (resultFile.exists()) {
            try {
                try (BufferedWriter bWriterFinal = new BufferedWriter(new FileWriter(new File("Result/" + taskDescription.getOutput().getName()), true))) {
                    BufferedReader bReader;
                    String line = "";
                    
                    

                    for (AbstractDataCollector i : collectorOnMemory) {
                        fileToMerge = new File( "Result" + "/"+ taskDescription.getId() + "/"+i.toString()+".txt");

                        if (fileToMerge.exists() && fileToMerge.canRead()) {
                            bReader = new BufferedReader(new FileReader(fileToMerge));

                            while ((line = bReader.readLine()) != null) {
                                bWriterFinal.write(line + "\n");
                            }
                            bWriterFinal.flush();
                            bReader.close();
                            fileToMerge.delete();
//                            OutOfMemory.stopAddingNewJob();
                        }
                    }
                    
                    for (String i : collectorDataOnDisk) {
                        fileToMerge = new File( "Result" + "/"+ taskDescription.getId() + "/"+i+".txt");

                        if (fileToMerge.exists() && fileToMerge.canRead()) {
                            bReader = new BufferedReader(new FileReader(fileToMerge));

                            while ((line = bReader.readLine()) != null) {
                                bWriterFinal.write(line + "\n");
                            }
                            bWriterFinal.flush();
                            bReader.close();
                            fileToMerge.delete();
//                          OutOfMemory.stopAddingNewJob();

                        }
                    }
                    
                    deleteRecursiveTaskfilesAndDirectory(new File("Result/" + taskDescription.getId()));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	@Override
    protected void finalize() throws Throwable {
            deleteRecursiveTaskfilesAndDirectory(new File("Result/" + taskDescription.getId()));
            deleteRecursiveTaskfilesAndDirectory(new File(DIRNAME+"/"+taskDescription.getId()+"/"));
    	super.finalize();
    }

	@Override
	public synchronized ArrayList<Tuple<KR, VR>> getPartitionForTask() {
		if (collectorDataOnDisk!= null && !collectorDataOnDisk.isEmpty()){
			return getFromDisk(collectorDataOnDisk.poll());
		}
		if (!collectorOnMemory.isEmpty()){
			return collectorOnMemory.poll().getPartition(0);
		}
		return null;
	}

	private synchronized ArrayList<Tuple<KR, VR>> getFromDisk(String filename){
		Kryo kryoR = new Kryo();
		Input input;
		try {
			input = new Input(new FileInputStream(DIRNAME+"/"+taskDescription.getId()+"/"+filename+".bin"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		ArrayList<Tuple<KR, VR>> partitionList = new ArrayList<>();
		
	//	long startTime = System.currentTimeMillis();////////////////////
		
		partitionList.addAll(kryoR.readObject(input, ArrayList.class, serializer));

		input.close();
	//	System.out.println("partition read in "+ (System.currentTimeMillis() - startTime));//////////////

		return partitionList;
	}

	@Override
	public synchronized boolean writeToDisk() {

		if(collectorOnMemory.isEmpty()){
  			return false;
  		}  			
  		 	try{
  		 		File directory = new File(DIRNAME);

  		 		// if the directory does not exist, create it
  		 		if (!directory.exists()) {
  		 			boolean result = directory.mkdir();  
  		 			if(!result){
  		 				System.err.println("can't create the directory "+DIRNAME+", can't write on the disk!");
  		 				return false;
  		 			}
  		 		}
  	            File directory2 = new File(DIRNAME+"/"+taskDescription.getId());

  		 		// if the directory does not exist, create it
  		 		if (!directory2.exists()) {
  		 			boolean result = directory2.mkdir();  
  		 			if(!result){
  		 				System.err.println("can't create the taskdata directory, can't write on the disk!");
  		 				return false;
  		 			}
  		 		}
  		 	} catch (SecurityException e ) {
  		 		throw new Error(e.getMessage());

  		 	}
  		 
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

  	//	long startTime = System.currentTimeMillis();
  		  		
  		Kryo save =  new Kryo();
  		
  		ReduceDataCollector<KR, VR> collectorToWrite = collectorOnMemory.poll();
  		boolean ret = false;
		try {
	  		Output output = new Output(new FileOutputStream(DIRNAME+"/"+taskDescription.getId()+"/"+collectorToWrite.toString()+".bin", true));
			save.writeObject(output, collectorToWrite.getPartition(0), serializer);
	  		output.close();
	  		collectorToWrite.toString();
	  		collectorDataOnDisk.add(collectorToWrite.toString());
	  		ret = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

  		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
  		return ret;
  		
   	}
}