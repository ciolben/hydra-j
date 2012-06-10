package epfl.project.scheduler;

import epfl.project.algorithm.DefaultCombinerAlgorithm;
import epfl.project.algorithm.DefaultPartitionerAlgorithm;
import epfl.project.algorithm.DefaultSpliterAlgorithm;
import java.io.File;
import java.util.*;

/**
 *
 * @author Loic
 */
public class TaskScheduler {
    private SortedMap<Integer, ArrayList<TaskDescription>> taskList;
    private HashMap<Integer, String> names;
    private int maxOrder    = 0;
    private int taskCounter = 0;
    private int currentPos  = -1;
    private Iterator<Integer> taskIterator = null;
    
    public TaskScheduler() {
        taskList = new TreeMap<>();
        names = new HashMap<>();
    }
    
    public void nameRound(int roundID, String name) {
        names.put(roundID, name);
    }
    
    public String getNameForRound(int roundID) {
        return names.get(roundID);
    }
    
    public boolean addTask(int order, TaskDescription taskDescription) {
        if (order < 0 || taskDescription == null) return false;
        ArrayList list = taskList.get(order);
        if (list == null) {
            list = new ArrayList<>();
        }
        
        switch (taskDescription.getCategory()) {
            case MAP:
                //if (taskDescription.getInput() == null) return false;
                if (taskDescription.getCombiner() == null) {
                    taskDescription.setCombiner(new DefaultCombinerAlgorithm());
                }
                if (taskDescription.getSpliter() == null) {
                    taskDescription.setSpliter(new DefaultSpliterAlgorithm(
                            (File)taskDescription.getInput()));
                }
            case REDUCE:
                if (taskDescription.getPartitioner() == null) {
                    taskDescription.setPartitioner(
                            new DefaultPartitionerAlgorithm());
                }
        }
        
        list.add(taskDescription);
        taskList.put(order, list);
        if (order > maxOrder) {
            maxOrder = order;
        }
        taskCounter++;
        return true;
    }
    
    public List<TaskDescription> getTasksForRound(int roundNumber) {
        List emptyList = new ArrayList<>();
        if (taskList.isEmpty()) return emptyList;
        List list = taskList.get(roundNumber);
        if (list == null) return emptyList;
        return list;
    }
    
    public int getMaxOrder() {
        return maxOrder;
    }
    
    public int getTotalNumberOfTask() {
        return taskCounter;
    }
    
    public int getRoundNumberOfTask(int round) {
        List list = taskList.get(round);
        if (list == null || list.isEmpty()) {
            return 0;
        } else {
            return list.size();
        }
    }
    
    public int getCurrentRound() {
        return currentPos;
    }
    
    public void resetIterator() {
        currentPos = -1;
        taskIterator = null;
    }
    
    /**
     * Only used by the master. If not, concurrency problems can occur.
     * @return 
     */
    public synchronized List<TaskDescription> nextList() {
        if (taskIterator != null && !taskIterator.hasNext()) {
            taskIterator = null;
            currentPos = -1;
            return null;
        }
        
        if (taskIterator == null) {
            taskIterator = taskList.keySet().iterator();
        }
        
        currentPos = taskIterator.next();
        return taskList.get(currentPos);
    }
    
    /**
     * Do not modify the map returned.
     * @return 
     */
    public Map<Integer, ArrayList<TaskDescription>> getSortedList() {
        return taskList;
    }
}
