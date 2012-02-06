package org.osgi.play;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import play.Logger;

public class DeployMonitor implements Runnable {
    
    private Map<File, Bundle> deployed = new HashMap<File, Bundle>();
    
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    
    private List<Bundle> newlyInstalledBundles = new LinkedList<Bundle>();
    private List<File> toBeRemovedBundles = new LinkedList<File>();
    
    public void start() {
        if (executor != null) 
          stop();
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
        
    }
    
    public void stop() {
        executor.shutdown();
        executor.shutdownNow();
    }

    @Override
    public void run() {    	
        if (OSGiBoostrap.deployDir.exists()) {
            for (File f : OSGiBoostrap.deployDir.listFiles()) {
                if (f.getName().endsWith(".jar")) {
                    if (!deployed.containsKey(f)) {
                        try {
                            Logger.info("Installing bundle : " + f.getName());
                            Bundle b = OSGi.bundleContext().installBundle("file:" + f.getAbsolutePath());
                            deployed.put(f, b);
                            newlyInstalledBundles.add(b);
//                            if (b.getHeaders().get(Constants.BUNDLE_ACTIVATOR) != null) {
//                                if (b.getHeaders().get(Constants.FRAGMENT_HOST) == null) {
//                                    b.start();
//                                }
//                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.error(e, "An exception occurred when trying to install bundle from file " + f.getAbsolutePath());
                        }
                    }
                }
            }
            
            for (Bundle newlyInstalledBundle : newlyInstalledBundles) {
            	try {
            		if (newlyInstalledBundle.getHeaders().get(Constants.FRAGMENT_HOST) == null)
            			newlyInstalledBundle.start();
            	} catch (Exception e) {
            		e.printStackTrace();
            		Logger.error(e, "An exception has been thrown while trying to start bundle " + newlyInstalledBundle.getSymbolicName());
            	}
            }
            newlyInstalledBundles.clear();
            
            for (File f : deployed.keySet()) {
                if (!f.exists() || deployed.get(f).getState() == Bundle.UNINSTALLED) {
                    try {
                        Logger.info("Uninstalling bundle : " + f.getName());
                        Bundle b = deployed.get(f);
                        if (b.getState() == Bundle.ACTIVE) {
                          b.stop();
                        }
                        if (b.getState() != Bundle.UNINSTALLED) {
                          b.uninstall();                          
                        }
                        Logger.info("State of bundle: " + b.getState());                        
                        toBeRemovedBundles.add(f);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            for (File toBeRemovedFile : toBeRemovedBundles) {
            	Logger.info("Removing deployed file from map...: " + deployed.remove(toBeRemovedFile));
            }
            toBeRemovedBundles.clear();
        }
    }
}
