/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package epfl.project.sense;

import epfl.project.controlinterface.console.Console;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

/**
 * MRControl.java (UTF-8)
 *
 * 26 mai 2012
 * @author Loic
 */
public class MRControl extends NotificationBroadcasterSupport implements MRControlMBean {

    private static MRControl mrControl = null;
    
    private Console console = null;
    private int sequenceNumber = 0;
    
    public MRControl() {
        /*nothing*/
    }
    
    public synchronized static MRControl getInstance() {
        if (mrControl == null) mrControl = new MRControl();
        return mrControl;
    }
    
    public void setConsole(Console console) {
        this.console = console;
    }
    
    @Override
    public void sendCommand(String command) {
        if (console == null) return;
        console.interpretCmd(command);
    }
    
    public void notifyPredictionResultsAvailable() {
        Notification notification = new Notification(
                "Prediction.results",
                this,
                sequenceNumber,
                System.currentTimeMillis(),
                /*
                 * result as a chain
                 */
                Prediction.getEstimatedKVAvg() + ":"
                + Prediction.getEstimatedMaxIndex() + ":"
                + Prediction.getEstimatedMemoryThreshold() + ":"
                + Prediction.getEstimatedTotalTime());
        sequenceNumber++;
        sendNotification(notification);
    }

    @Override
    public void connectionTest() {
        return;
    }
}
