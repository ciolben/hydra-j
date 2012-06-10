package epfl.project.common;

import epfl.project.nodes.Master;
import epfl.project.scheduler.TaskDescription;
import epfl.project.scheduler.TaskScheduler;
import epfl.project.sense.*;
import java.util.List;

/**
 *
 * class : MapReduce
 *
 * Mother abstract class that loads everything and start a mapreduce task.
 *
 */
public class MapReduce<K extends Comparable<K>, V, KR extends Comparable<KR>, VR> {

    private Configurator<K, V, KR, VR> configurator = null;
    private Master<K, V, KR, VR> master;
    /**
     * This boolean prevents that more than one instance of the mapreduce job is
     * started. This is an important point because the rmi server and the
     * statistics cannot be used/changed concurrently.<br> To use two or more
     * mapreduce jobs, it is better to make a script that launch two instances
     * separately, e.g :<br><code>
     * cd workspace1; <br>
     * java -jar mapreduce.jar; <br>
     * cd workspace2; <br>
     * java -jar mapreduce.jar <br><code>
     */
    private static boolean blockOtherInstances = false;

    /**
     * The initialize method must be called before any other method.
     *
     * @param config The properly configured Configurator object.
     * @return
     * <code>false</code> if the initialization process failed, true otherwise.
     */
    public boolean initialize(Configurator<K, V, KR, VR> config) {

        //Check important fields of the configurator object

        configurator = config;
        return true;
    }

    public Configurator getConfigurator() {
        return configurator;
    }
    
    public synchronized void start(String preCmd, String postCmd) {
        if (configurator == null) {
            return;
        }

        if (blockOtherInstances) {
            System.err.println("Cannot start two or more mapreduce instances on"
                    + " the same JVM.");
            return;
        }

        blockOtherInstances = true;

        /*
         * Organize the ids of the tasks. This part is implemented to support a
         * simple multi mapreduce. In the case of future work in extending the
         * multi mapreduce, the ids will be partition into round intead of
         * linearly starting from 0 to n, where n is the number of tasks. The
         * underlying structure already support this extension.
         */
        int taskCounter = 0;
        TaskScheduler scheduler = configurator.getTaskScheduler();
        List<TaskDescription> list;
        while ((list = scheduler.nextList()) != null) {
            /*
             * Only keep one task per list, this is the simple multi mapreduce.
             * Other tasks are deleted from the list : the master node will
             * start all the tasks if there is more than one in a round. For
             * now, this is not completely supported.
             */
            if (!list.isEmpty()) {
                TaskDescription task = list.get(0);
                task.setId(taskCounter);
                list.clear();
                list.add(task);
            }
            taskCounter++;
        }
        scheduler.resetIterator();

        //----jmx config part----
        BeanServer server = BeanServer.getInstance();
        server.registerObject("type=mrmemory", MRMemory.getInstance());
        server.registerObject("type=mrstats", MRStats.getInstance());
        server.registerObject("type=mrcontrol", MRControl.getInstance());
        server.startServer();
        //-----------------------

        //----stats----
        MRStats.getInstance().constructOverview(configurator);
        //-------------

        //Turn on the master
        master = new Master<>(configurator);

        Prediction.justStartedMemory = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();

        if (!configurator.isConsoleActive() && !configurator.isGUIActive()) {

            epfl.project.controlinterface.console.Console c;
            c = (epfl.project.controlinterface.console.Console) Master.reporter;
            c.interpretCmd(preCmd);
            
            master.start();
            try {
                master.join();
                c.interpretCmd(postCmd);
                blockOtherInstances = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Start a mapreduce task according to the configurator configuration.
     * Calling this method prior to <italic>initialize</italic> has no effect.
     */
    public synchronized void start() {
        if (configurator == null) {
            return;
        }

        if (blockOtherInstances) {
            System.err.println("Cannot start two or more mapreduce instances on"
                    + " the same JVM.");
            return;
        }

        blockOtherInstances = true;

        /*
         * Organize the ids of the tasks. This part is implemented to support a
         * simple multi mapreduce. In the case of future work in extending the
         * multi mapreduce, the ids will be partition into round intead of
         * linearly starting from 0 to n, where n is the number of tasks. The
         * underlying structure already support this extension.
         */
        int taskCounter = 0;
        TaskScheduler scheduler = configurator.getTaskScheduler();
        List<TaskDescription> list;
        while ((list = scheduler.nextList()) != null) {
            /*
             * Only keep one task per list, this is the simple multi mapreduce.
             * Other tasks are deleted from the list : the master node will
             * start all the tasks if there is more than one in a round. For
             * now, this is not completely supported.
             */
            if (!list.isEmpty()) {
                TaskDescription task = list.get(0);
                task.setId(taskCounter);
                list.clear();
                list.add(task);
            }
            taskCounter++;
        }
        scheduler.resetIterator();

        //----jmx config part----
        BeanServer server = BeanServer.getInstance();
        server.registerObject("type=mrmemory", MRMemory.getInstance());
        server.registerObject("type=mrstats", MRStats.getInstance());
        server.registerObject("type=mrcontrol", MRControl.getInstance());
        server.startServer();
        //-----------------------

        //----stats----
        MRStats.getInstance().constructOverview(configurator);
        //-------------

        //Turn on the master
        master = new Master<>(configurator);

        Prediction.justStartedMemory = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();

        if (!configurator.isConsoleActive() && !configurator.isGUIActive()) {            
            master.start();
            try {
                master.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stop the mapreduce task. Mind that there is some time lag before all
     * resources are released.
     */
    public void stop() {
        System.out.println("Stopping mapreduce");
        if (master != null) {
            master.close();
        }
    }

    public AbstractDataCollectorSet getResultDataCollectorSet() {
        if (master != null) {
            return master.getResultDataCollectorSet();
        }
        return null;
    }

    public void join() throws InterruptedException {
        master.join();
    }
}
