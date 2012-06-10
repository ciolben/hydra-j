/*
 * MapReduce API @ 2012
 */

package epfl.project.sense;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import javax.management.*;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * BeanServer.java (UTF-8)
 *
 * resources : http://www.jmdoudoux.fr/java/dej/chap-jmx.htm
 * 
 * 16 avr. 2012
 * @author Loic
 */
public final class BeanServer {
    private static BeanServer singleton = null;
    
    private MBeanServer mbs;
    private JMXConnectorServer cs;
    private int port = 9876;
    private String address = "localhost";
    
    private BeanServer() {
        //use of the platform server
        mbs = ManagementFactory.getPlatformMBeanServer();
    }
    
    /**
     * Get the instance of the MBean server.
     * @return the server instance.
     */
    public static synchronized BeanServer getInstance() {
        if (singleton == null) {
            singleton = new BeanServer();
        }
        return singleton;
    }
    
    @Override
    public BeanServer clone() throws CloneNotSupportedException{
        throw new CloneNotSupportedException(
                "BeanServer not supposed to be cloned");
    }
    
    public void registerObject(String completeName, Object mbean) {
        try {
            if (mbs == null) getInstance();
            ObjectName oname;
            oname = new ObjectName(mbs.getDefaultDomain() + ":" + completeName);
            mbs.registerMBean(mbean, oname);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException
                | NotCompliantMBeanException | MalformedObjectNameException ex){
            System.err.println(ex.getMessage());
        }
    }
    
    public ObjectInstance getMBean(String name) throws MalformedObjectNameException,
            InstanceNotFoundException {
        if (mbs == null) getInstance();
        return mbs.getObjectInstance(new ObjectName(mbs.getDefaultDomain()
                + ":" + name));
    }
    
    public void startServer() {
        if (mbs == null) getInstance();
        else stopServer();
        
        try {
            LocateRegistry.createRegistry(port);
            JMXServiceURL url = new JMXServiceURL(
                        "service:jmx:rmi:///jndi/rmi://" + address + ":"
                        + port + "/server");
            cs =
                JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
            cs.start();
        } catch (MalformedURLException | RemoteException ex) {
            
        } catch (IOException ex) {
        
        }
    }
    
    public void stopServer() {
        if (cs != null) {
            try {
                cs.stop();
                cs  = null;
                //mbs = null;
            } catch (IOException _) {
            }
        }
    }
}
