/*
 * MapReduce API @ 2012
 */

package epfl.monitor.mbeans;

import java.io.IOException;
import java.util.ArrayList;
import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * BeanClient.java (UTF-8)
 *
 * 16 avr. 2012
 * @author Loic
 */
public class BeanClient {
    protected JMXConnector jmxc;
    protected MBeanServerConnection mbsc;
    private int port = 9876;
    private String address = "localhost";
    
    private ArrayList<Object []> temporaryListeners = new ArrayList<>(1);
    
    public BeanClient() {
        /*nothing*/
    }
    
    public BeanClient(int port, String address) {
        if (port > 0) {
            this.port = port;
        }
        if (address != null && !address.isEmpty()) {
            this.address = address;
        }
    }
    
    /**
     * Try to connect to the remote JVM. If a connection already exists, this
     * method does nothing.
     * @return <italic>true</italic> if client started successfully.
     */
    public boolean startClient() {
        if (jmxc != null) return true;
        try {
            JMXServiceURL url = new JMXServiceURL(
                            "service:jmx:rmi:///jndi/rmi://"
                            + address + ":" + port
                            + "/server");
            jmxc = JMXConnectorFactory.connect(url, null);
            mbsc = jmxc.getMBeanServerConnection();
            
            //register all the awaiting listeners and empty the list.
            for (Object [] listener : temporaryListeners) {
                try {
                    addNotificationListener((String)listener[0],
                            (NotificationListener)listener[1]);
                } catch (ServerUnreachableException ex) {
                    /*never happens*/
                }
            }
            temporaryListeners.clear();
            temporaryListeners.trimToSize(); //use less memory
            
        } catch (IOException ex) {
            System.err.println("Cannot connect to server " + address
                    + ":" + port);
            jmxc = null;
            mbsc = null;
            return false;
        }
        return true;
    }
    
    /**
     * Must be called to properly close the connection. If the connection is
     * already closed or doesn't exist, this method does nothing. <br>
     * (This method takes some time)
     */
    public void stopClient() {
        if (jmxc == null) return;
        try {
            jmxc.close();
        } catch (IOException ex) {
            /*we don't care about exception here*/
        } finally {
            jmxc    = null;
            mbsc    = null;
        }
    }
    
    //------------------------------------------------
    long pre = 0L;
    int counter = 0;
    private synchronized void time() {
        counter++;
        long curr = System.currentTimeMillis();
        long delta = curr - pre;
        if (delta >= 1000) {
            System.out.println("-----------------------Req/s : " + (float) counter / (delta / 1000.0));
            counter = 0;
            pre = curr;
        }
    }
    //------------------------------------------------
    
    /**
     * Get an attribute on the distant running JVM.
     * @param <T> Can be any type.
     * @param oname The object key (ex.: "type=memory")
     * @param attrName The name of the attribute (ex.: "counter")
     * @return The attribute value or null if the call failed.
     * @throws ServerUnreachableException
     */
    public <T> T getAttribute(String oname, String attrName)
            throws ServerUnreachableException {
        if (mbsc == null) throw new ServerUnreachableException(); //second check
        ObjectName oattr;
        //------------
        time();
        //------------
        try {
             oattr = new ObjectName(mbsc.getDefaultDomain()
                        + ":" + oname);
        } catch (MalformedObjectNameException | IOException ex) {
            throw new ServerUnreachableException(ex.getMessage());
        }
        try {
            return (T) mbsc.getAttribute(oattr, attrName);
        } catch (MBeanException | AttributeNotFoundException
                | InstanceNotFoundException | ReflectionException
                | IOException ex) {
            System.err.println("EXCEPTION : " + ex.getMessage());
            throw new ServerUnreachableException(ex.getMessage());
        }
    }
    
    public <T> T invokeMethod(String oname, String methodName, Object ... params)
            throws ServerUnreachableException {
        if (mbsc == null) throw new ServerUnreachableException();
        ObjectName oparam;
        //------------
        time();
        //------------
        try {
             oparam = new ObjectName(mbsc.getDefaultDomain()
                        + ":" + oname);
        } catch (MalformedObjectNameException | IOException ex) {
            throw new ServerUnreachableException(ex.getMessage());
        }
        try {
            String [] signatures = params.length == 0
                    ? null : new String[params.length];
            for (int i = 0; i < params.length; i++) {
                signatures[i] = params[i].getClass().getName();
            }
            return (T) mbsc.invoke(oparam, methodName, params, signatures);
        } catch (InstanceNotFoundException | MBeanException
                | ReflectionException | IOException ex) {
            throw new ServerUnreachableException(ex.getMessage());
        }
    }
    
    /**
     * Set an attribute on the distant running JVM.
     * @param oname The object key (ex.: "type=memory")
     * @param attrName The name of the attribute (ex.: "counter")
     * @param attr The attribute value to set.
     * @return <italic>true</italic> if the operation was successful.
     * @throws ServerUnreachableException
     */
    public boolean setAttribute(String oname, String attrName, Object attr)
            throws ServerUnreachableException{
        if (mbsc == null) throw new ServerUnreachableException();
        ObjectName oattr;
        try {
             oattr = new ObjectName(mbsc.getDefaultDomain()
                        + ":" + oname);
        } catch (MalformedObjectNameException | IOException ex) {
            throw new ServerUnreachableException(ex.getMessage());
        }
        try {
            mbsc.setAttribute(oattr, new Attribute(attrName, attr));
        } catch (InstanceNotFoundException | AttributeNotFoundException
                | InvalidAttributeValueException | MBeanException
                | ReflectionException | IOException ex) {
            throw new ServerUnreachableException(ex.getMessage());
        }
        return true;
    }
    
    /**
     * Register the notification listener with no filter and no handback object.
     * @param oname the object name registered with the server.
     * @param listener the listener for the notification
     * @throws ServerUnreachableException 
     */
    public void addNotificationListener(String oname,
            NotificationListener listener) throws ServerUnreachableException {
        if (mbsc == null) {
            //save the listener and add it when the connection is up.
            temporaryListeners.add(new Object [] {oname, listener});
            return;
        }
        ObjectName onameObject;
        try {
             onameObject = new ObjectName(mbsc.getDefaultDomain()
                        + ":" + oname);
        } catch (MalformedObjectNameException | IOException ex) {
            throw new ServerUnreachableException(ex.getMessage());
        }
        try {
            mbsc.addNotificationListener(onameObject, listener, null, null);
        } catch (InstanceNotFoundException | IOException ex) {
            throw new ServerUnreachableException("Wrong oname OR " + ex.getMessage());
        }
    }
    
    /**
     * Unregister a notification listener.
     * @param oname the object name registered with this server. 
     * @param listener the listener to remove
     * @throws ServerUnreachableException
     */
    public void removeNotificationListener(String oname,
            NotificationListener listener) throws ServerUnreachableException {
        if (mbsc == null) {
            return;
        }
        ObjectName onameObject;
        try {
             onameObject = new ObjectName(mbsc.getDefaultDomain()
                        + ":" + oname);
        } catch (MalformedObjectNameException | IOException ex) {
            throw new ServerUnreachableException(ex.getMessage());
        }
        try {
            mbsc.removeNotificationListener(onameObject, listener);
        } catch (InstanceNotFoundException | ListenerNotFoundException | IOException ex) {
            throw new ServerUnreachableException("Wrong oname OR " + ex.getMessage());
        }
    }
}
