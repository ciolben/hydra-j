package epfl.project.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import epfl.project.scheduler.TaskDescription;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class allows us to access to every collector keeping intermediate
 * keyvalue pair
 *
 * @author Nicolas
 *
 * @param <K>
 * @param <V>
 */
public class DataCollectorSet<K extends Comparable<K>, V> extends AbstractDataCollectorSet<K, V> {

    private CollectionSerializer serializer;
    private ConcurrentHashMap<Integer, Integer> partionOnDisk;
    private ConcurrentLinkedQueue<AbstractDataCollector> setDataCollector;
    private Kryo kryo;
    private Integer maxpartitions;

    public DataCollectorSet(final TaskDescription taskDescription) {
        super(taskDescription);
        maxpartitions = taskDescription.getPartitioner().getMaxIndexPartition();
        setDataCollector = new ConcurrentLinkedQueue<>();
        
        kryo = new Kryo();
        serializer = new CollectionSerializer(kryo);
        serializer.setElementClass(Tuple.class);
        serializer.setElementsCanBeNull(false);
    }

    @Override
    public void addCollector(AbstractDataCollector<K, V> newCollector) {
        // each worker has a collector
        

        //put the new collector for the worker (workerID) for the task (taskID)	
        setDataCollector.add(newCollector);
    }

    @Override
    protected void finalize() throws Throwable {
            deleteRecursiveTaskfilesAndDirectory(new File(DIRNAME+"/"+taskDescription.getId()+"/"));
    	super.finalize();
    }

    @Override
    public ArrayList<Tuple<K, V>> getPartitionForTask() {
        int id;
        synchronized (maxpartitions) {
            if (maxpartitions <= 0) {
                return null;
            }
            id = maxpartitions - 1;
            maxpartitions--;
        }

        ArrayList<Tuple<K, V>> tmp;
        if (partionOnDisk != null) {

            if (isOnDisk(id)) {
                try {
                    tmp = getFromDisk(id);
                    if (tmp == null) {
                        throw new FileNotFoundException("dataFromDisk = null something went wrong");
                    }
                } catch (FileNotFoundException | SecurityException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                tmp = new ArrayList<>();
            }

        } else {//is only in memory
            tmp = new ArrayList<>();
        }

        if (setDataCollector.isEmpty()) {
            return tmp;
        }


        for (AbstractDataCollector<K, V> mapWorkerCollector : setDataCollector) {
            ArrayList<Tuple<K, V>> extracted = mapWorkerCollector.getPartition(id);
            if (extracted != null && !extracted.isEmpty()) {
                tmp.isEmpty();
                tmp.addAll(extracted);
            }

            if (mapWorkerCollector instanceof DataCollectorCommonFixedSizeArray) {
                return tmp;
            }
        }

        return tmp;


    }

    private boolean isOnDisk(int partitionID) {//TODO if removing data from disk or more cleaver way to choose partition must synchronized
        return (partionOnDisk != null && partionOnDisk.containsKey(partitionID));

    }

    private ArrayList<Tuple<K, V>> getFromDisk(int partitionID) {
        if (!isOnDisk(partitionID)) {
            System.out.println("partition not found id" + partitionID);
            return null;
        }
        Kryo kryoR = new Kryo();
        Input input;
        try {
            input = new Input(new FileInputStream(DIRNAME + "/" + taskDescription.getId() + "/" + partitionID + ".bin"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        int times = partionOnDisk.get(partitionID);

        ArrayList<Tuple<K, V>> partitionList = new ArrayList<>();

        //long startTime = System.currentTimeMillis();////////////////////
        for (int i = 0; i < times; i++) {//read from disk
            partitionList.addAll(kryoR.readObject(input, ArrayList.class, serializer));

        }
        input.close();

        return partitionList;

    }

    @Override
    public boolean writeToDisk() {

        if (setDataCollector.isEmpty()) {
            return false;
        }

        //check if already written something if not create persistent file
        try {
            File directory = new File(DIRNAME);

            // if the directory does not exist, create it
            if (!directory.exists()) {
                boolean result = directory.mkdir();
                if (!result) {
                    System.err.println("can't create the directory ./data, can't write on the disk!");
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
        } catch (SecurityException e) {
		 		throw new Error(e.getMessage());

        }

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);


       // long startTime = System.currentTimeMillis();//////////




        ArrayList<Tuple> partition;

        Kryo save = new Kryo();
        Output output;

        boolean ret = false;
        try {
            int p = 0;
            AbstractDataCollector collector = setDataCollector.poll();
            while ((partition = collector.getPartition(p)) != null) {
                ret = false;
                output = new Output(new FileOutputStream(DIRNAME + "/" + taskDescription.getId() + "/" + p + ".bin", true));

                kryo.writeObject(output, partition, serializer);
                ret = true;
                output.close();

                partition = null;

                if (partionOnDisk == null) {
                    partionOnDisk = new ConcurrentHashMap<>();
                }

                if (partionOnDisk.putIfAbsent(p, 1) != null) {
                    partionOnDisk.put(p, partionOnDisk.get(p) + 1);
                }

                p++;


            }
            collector.clearCollector(); //TODO check if i can really do this before saving data. */
            collector = null;








        } catch (FileNotFoundException | SecurityException e) {
            
            e.printStackTrace();

        }





        //System.out.println("partition written from "+count+" worker in "+ (System.currentTimeMillis() - startTime));
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        return ret;

    }
}
