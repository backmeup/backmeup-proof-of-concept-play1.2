package org.osgi.play;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.felix.framework.FrameworkFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import play.Play;

public class OSGiBoostrap {
    
    public static File deployDir = new File(Play.configuration.getProperty("osgi.autodeployDirectory", "autodeploy"));
    
    public static File dataDir = new File(Play.configuration.getProperty("osgi.tempDirectory", "osgiTmp"));
   
    public static File uploadedDir = new File(Play.configuration.getProperty("osgi.tempDirectory", "osgiTmp"), "uploaded");
    
    public static String osgiExportedPackages = Play.configuration.getProperty("osgi.systemPackages", "models, play");

    public static void createDeployDir() {
        if (!deployDir.exists()) {
            deployDir.mkdirs();
        }
        cascadeDelete(dataDir);
    }
    
    private static void cascadeDelete(File in) {
        if (in != null && in.listFiles() != null) {
            for (File f : in.listFiles()) {
                if (f.isFile()) {
                    f.delete();
                } else {
                    cascadeDelete(f);
                    f.delete();
                }
            }
        }
    }

    public static boolean initOSGiFramework() {
        try {
            FrameworkFactory factory = new FrameworkFactory();
            Map<String, String> config = new HashMap<String, String>();
            config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, osgiExportedPackages);
            config.put(Constants.FRAMEWORK_STORAGE, dataDir.getAbsolutePath());
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");
            config.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_APP);
            config.put(Constants.FRAMEWORK_BOOTDELEGATION, osgiExportedPackages);
            //config.put(Constants.FRAMEWORK_EXECUTIONENVIRONMENT, "J2SE-1.6");
            //config.put("osgi.shell.telnet", "on");
            //config.put("osgi.shell.telnet.port", "6666");
            OSGi.osgiFramework = factory.newFramework(config);
            OSGi.osgiFramework.start();
            
            if (!uploadedDir.exists()) {
                uploadedDir.mkdirs();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    } 

    public static void stopOSGiFramework() {
        try {
            OSGi.osgiFramework.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
