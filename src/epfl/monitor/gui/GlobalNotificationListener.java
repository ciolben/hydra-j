package epfl.monitor.gui;

import java.util.HashMap;
import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * GlobalListener.java (UTF-8)
 *
 * Class : intercept the notification and handle it.
 * 
 * 5 mai 2012
 * @author Loic
 */
public class GlobalNotificationListener implements NotificationListener {

    private HashMap<String, GraphComponent> treeOccurences;
    private MainGui mainGui;
    
    public GlobalNotificationListener (HashMap<String,
            GraphComponent> treeOccurences,
            MainGui ref) {
        this.treeOccurences = treeOccurences;
        mainGui = ref;
    }
    
    @Override
    public void handleNotification(Notification notification, Object handback) {
        //filter the notification and do appropriate action
        /*
         * getMessage : 0:874
         * getType : MRStats.roundended
         * 
         * getMessage : 874
         * getType : MRStats.start
         */
        System.err.println("NOTIFICATION : " +  notification.getType());
       switch (notification.getType()) {
           case "MRStats.roundended":
//               System.out.println("ROUNDENDED");
               OverviewGraphComponent graph = (OverviewGraphComponent)
                       treeOccurences.get(TreeNames.Overview.t_name);
               //round that ended | time of the round
               String [] values = notification.getMessage().split(":");
               graph.setCurrentRound(Integer.valueOf(values[0]));
               graph.addNewEvent("round " + values[0] + " is terminated ("
                       + values[1] + " ms)");
               mainGui.currentRound = Integer.valueOf(values[0]);
               break;
           case "MRStats.allroundended":
//               System.out.println("ALLROUNDENDED");
               graph = (OverviewGraphComponent)
                       treeOccurences.get(TreeNames.Overview.t_name);
               graph.signalAllRoundEnded();
               graph.addNewEvent("job ended in " + notification.getMessage() + " ms");
               break;
           case "MRStats.start":
               //reset the overview
               mainGui.currentRound = 0;
               mainGui.mxp.MRStats_clearTimes();
               graph = (OverviewGraphComponent)
                       treeOccurences.get(TreeNames.Overview.t_name);
               graph.addNewEvent("----------new job-----------");
//               graph.clearProgressions();
//               if (graph.isLoaded()) {
//                   graph.scheduleRefresh(-1);
//               }
               break;
       }
    }
    
}
