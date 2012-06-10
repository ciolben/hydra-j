package epfl.project.sense;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class : give a representation of the state of a mapreduce job.
 * Chain list with shortcuts to directly jump to a round.<br>
 * Futur work : as the framework provides a simple multi mapreduce, lots
 * of synchronization needs are avoided. If it is extended, this part has to be
 * revised in that sense.
 * 
 * @author Loïc
 */
public class OverviewList implements Serializable {
    
    private RootNode root;
    private Node last;
//    private Node currentR;
    private Node currentA;
    
    private HashMap<Object, Node> synchronizerR;
    
    public OverviewList() {
        root = new RootNode();
        synchronizerR = new HashMap<>();
    }
    
    public void addRound(String name, int id) {
        Node newNode = new Node(name, id, false);
        root.addRoundRef(newNode);
        if (last == null) {
            last = newNode;
        } else {
            last.next = newNode;
            last = newNode;
        }
    }
    
    public void addTask(String name, int id) {
        if (last == null) return;
        Node newNode = new Node(name, id, true);
        last.next = newNode;
        last = newNode;
    }
    
    /**
     * 0 : isTask (<code>true</code> : 1, otherwise 0) (always 1)<br/>
     * 1 : name<br/>
     * 2 : id<br/>
     * 3 : progression<br/>
     * <br/>
     * Will return <code>null</code> if there exists no more task for this round.
     * @param roundID the id of the round.
     * @param self the object on which to create an iterator, typically <code>this</code>.
     * @return the array containing the node info.
     */
    public synchronized String [] getNextTaskFor(Object self, int roundID) {
        Node currentR = synchronizerR.get(self);
        if (currentR == null) {
            currentR = root.getRound(roundID);
        }
        currentR = currentR.next;
        synchronizerR.put(self, currentR);
        if (currentR == null || !currentR.isTask) {
            synchronizerR.put(self, null);
            return null;
        }
        String[] res = new String [] {
            currentR.isTask ? "1" : "0",
            currentR.name,
            Integer.toString(currentR.id),
            Integer.toString(currentR.progression)};
        return res;
    }
    
    /**
     * 0 : isTask (<code>true</code> : 1, otherwise 0)<br/>
     * 1 : name<br/>
     * 2 : id<br/>
     * 3 : progression (0 for rounds)<br/>
     * <br/>
     * Will return <code>null</code> if there exists no more node.
     * @return the array containing the node info.
     */
    public String [] getNextNode() {
        if (currentA == null) {
            currentA = root.startRound;
        } else {
            currentA = currentA.next;
        }
        if (currentA == null) {
            return null;
        }
        String[] res = new String [] {
            currentA.isTask ? "1" : "0",
            currentA.name,
            Integer.toString(currentA.id),
            Integer.toString(currentA.progression)};
        return res;
    }
    
    public void updateTaskProgress(int roundID, int taskID, int progression) {
        Node node = root.getRound(roundID);
        do {
            node = node.next;
            if (node == null || !node.isTask) return;
        } while (node.id != taskID);
        node.progression = progression;
        //printProgressions();
    }
    
    public void resetProgressions() {
        Node node = root.startRound;
        while (node != null) {
            node.progression = 0;
            node = node.next;
        }
    }
    
    /**
     * Return the number of round this mapreduce has.
     * @return the size (not the total number of entries).
     */
    public int size() {
        return root.roundRefs.keySet().size();
    }
    
    public void printProgressions() {
        Node node = root.startRound;
//        System.out.println("------Progressions------");
        while (node != null) {
//            System.out.println(node.name + " (" + node.id + ")" + node.progression);
            node = node.next;
        }
//        System.out.println("------------------------");
    }
    
    private class RootNode implements Serializable {
        
        private HashMap<Integer, Node> roundRefs = new HashMap<>();
        private Node startRound;
      
        public void addRoundRef(Node node) {
            if (startRound == null) {
                startRound = node;
            }
            roundRefs.put(node.id, node);
        }
        
        public Node getRound(int roundID) {
            return roundRefs.get(roundID);
        }
    }
    
    private class Node implements Serializable {
        
        public final String name;
        public final int id;
        public final boolean isTask;
        public int progression;
        
        public Node next;
        
        public Node (String name, int id, boolean isTask) {
            this.name = name;
            this.id = id;
            this.isTask = isTask;
            progression = 0;
            
            next = null;
        }
    }
}
