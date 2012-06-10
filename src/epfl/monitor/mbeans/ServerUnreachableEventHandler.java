package epfl.monitor.mbeans;

/**
 * ServerUnreachableNotificationHandler.java (UTF-8)
 *
 * 3 mai 2012
 * @author Loic
 */
public interface ServerUnreachableEventHandler {
    public void handleEvent(BeanClient client);
}
