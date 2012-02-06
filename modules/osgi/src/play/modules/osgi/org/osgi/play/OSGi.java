package org.osgi.play;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

public class OSGi {
    
    static Framework osgiFramework;

    public static BundleContext bundleContext() {
        return osgiFramework.getBundleContext();
    }
    
    public static <T> T service(final Class<T> service) {
        return (T) Proxy.newProxyInstance(OSGi.class.getClassLoader(), 
                new Class[] { service }, new InvocationHandler() {

            public Object invoke(Object o, Method method, Object[] os) throws Throwable {
                ServiceReference ref = bundleContext().getServiceReference(service.getName());
                if (ref == null) {
                    throw new RuntimeException("Unable to find service for " + service.getName());
                }
                Object ret = null;
                Object instance = bundleContext().getService(ref);
                try {
                    ret = method.invoke(instance, os);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                } finally {
                    bundleContext().ungetService(ref);
                }
                return ret;
            }
        });
    }

    private static class SpecialInvocationHandler implements InvocationHandler {
      private ServiceReference reference;
      
      public SpecialInvocationHandler(ServiceReference reference) {
        this.reference = reference;
      }
      
      public Object invoke(Object o, Method method, Object[] os) throws Throwable {
        ServiceReference ref = reference;
        Object ret = null;
        Object instance = bundleContext().getService(ref);
       
        try {
          boolean acc = method.isAccessible();
          method.setAccessible(true);
          if (os == null) 
            ret = method.invoke(instance);
          else
            ret = method.invoke(instance, os);
          method.setAccessible(acc);
        } finally {
          bundleContext().ungetService(ref);
        }
        return ret;
      }
    }
    
    public static <T> Iterable<T> services(final Class<T> service, final String filter) {
        return new Iterable<T>() {

            public Iterator<T> iterator() {
                try {
                    ServiceReference[] refs = bundleContext().getServiceReferences(service.getName(), filter);
                   if (refs == null) {
                        return new Iterator<T>() { 
                          public boolean hasNext() {return false;} 
                          public T next() { return null; } 
                          public void remove() {} 
                        };
                    }
                    List<T> services = new ArrayList<T>();
                    for (ServiceReference s : refs) {
                    	Object serv = bundleContext().getService(s);
                        services.add((T) Proxy.newProxyInstance(OSGi.class.getClassLoader(),
                                new Class[]{service}, new SpecialInvocationHandler(s)));
                    }
                    return services.iterator();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } 
            }
        };
    }
}
