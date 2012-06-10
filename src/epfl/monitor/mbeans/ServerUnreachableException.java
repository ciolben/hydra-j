package epfl.monitor.mbeans;

/**
 * ServerUnreachableException.java (UTF-8)
 *
 * 3 mai 2012
 * @author Loic
 */
public class ServerUnreachableException extends Exception {
    public ServerUnreachableException() {
        super("Client cannot connect to the server.");
    }
    public ServerUnreachableException(String additionalMessage) {
        super("Client cannot connect to the server. " + additionalMessage);
    }
}
