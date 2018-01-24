package batalhanavaljscliente;


import java.io.IOException;

import java.rmi.RemoteException;

import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;

import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.DiscoveryEvent;

class Lookup implements DiscoveryListener {
    private final ServiceTemplate theTemplate;
    private LookupDiscovery theDiscoverer;

    private Object theProxy;

    Lookup(Class aServiceInterface) {
        Class[] myServiceTypes = new Class[] {aServiceInterface};
        theTemplate = new ServiceTemplate(null, myServiceTypes, null);
    }

    Object getService() {
        synchronized(this) {
            if (theDiscoverer == null) {

                try {
                    theDiscoverer =
                        new LookupDiscovery(LookupDiscovery.ALL_GROUPS);
                    theDiscoverer.addDiscoveryListener(this);
                } catch (IOException anIOE) {
                    System.err.println("Failed to init lookup");
                    anIOE.printStackTrace(System.err);
                }
            }
        }

        return waitForProxy();
    }

    void terminate() {
        synchronized(this) {
            if (theDiscoverer != null)
                theDiscoverer.terminate();
        }
    }

    private Object waitForProxy() {
        synchronized(this) {
            while (theProxy == null) {

                try {
                    wait();
                } catch (InterruptedException anIE) {
                }
            }

            return theProxy;
        }
    }

    private void signalGotProxy(Object aProxy) {
        synchronized(this) {
            if (theProxy == null) {
                theProxy = aProxy;
                notify();
            }
        }
    }
    
    @Override
    public void discovered(DiscoveryEvent anEvent) {
        synchronized(this) {
            if (theProxy != null)
                return;
        }

        ServiceRegistrar[] myRegs = anEvent.getRegistrars();

        for (ServiceRegistrar myReg : myRegs) {
            Object myProxy;

            try {
                myProxy = myReg.lookup(theTemplate);

                if (myProxy != null) {
                    signalGotProxy(myProxy);
                    break;
                }
            } catch (RemoteException anRE) {
                System.err.println("ServiceRegistrar barfed");
                anRE.printStackTrace(System.err);
            }
        }
    }

    @Override
    public void discarded(DiscoveryEvent anEvent) {
    }
}
