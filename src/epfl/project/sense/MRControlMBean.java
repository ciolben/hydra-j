/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package epfl.project.sense;

/**
 *
 * @author Loic
 */
public interface MRControlMBean {

    /**
     * Execute a command through the console.
     * @param command the command to pass to the console
     */
    public void sendCommand(String command);
    
    /**
     * Used to probe the connection.
     */
    public void connectionTest();
}
