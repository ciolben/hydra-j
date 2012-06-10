/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package epfl.project.scheduler;

/**
 *
 * @author Loic
 */
public class TaskException extends Exception {
    public TaskException(String msg) {
        super("Task error : " + msg);
    }
}
