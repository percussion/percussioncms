/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.server;

import java.lang.reflect.Method;
import java.util.Properties;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class helps us to get around issues caused by certain java features
 * being defined as server properties. These values are not thread safe and
 * therefore you cannot change a value for a specific thread without affecting
 * the entire system. We replace the Server properties object with this class
 * then we can set property values that are only valid for the current thread,
 * the existing underlying property object will retain the original value for
 * all other threads. We will set this in PSSecurityFilter before we set saxon as the parser.
 * 
 * @author stephenbolton
 *
 */
public class ThreadLocalProperties
{
   private static volatile boolean initialized = false;
   /**
    * Logger for this class.
    */
   public static Log log = LogFactory.getLog(ThreadLocalProperties.class);

   public static synchronized void setupProperties()
   {
      if (!initialized)
      {
         final Enhancer e = new Enhancer();
         e.setClassLoader(Thread.currentThread().getContextClassLoader());
    
         Properties props = System.getProperties();
         e.setSuperclass(props.getClass());
         e.setCallback(new ServerLocalPropertyInterceptor(props));
         log.debug("Injecting ThreadLocalProperties proxy into system properties: Existing class is "+props.getClass().getName());
         System.setProperties((Properties) e.create());
         initialized = true;
      }

   }

   static class ServerLocalPropertyInterceptor implements MethodInterceptor
   {

      private static final String THREADLOCAL_PREFIX = "threadlocal.";

      private final ThreadLocal<Properties> localProperties = new ThreadLocal<Properties>()
      {
         @Override
         protected Properties initialValue()
         {
            return new Properties();
         }
      };

      private Properties orig = null;

      public ServerLocalPropertyInterceptor(Properties props)
      {
         this.orig = props;
      }

      public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
      {

         if (args!= null && args.length > 0 && args[0] instanceof String
               && ((String) args[0]).startsWith(THREADLOCAL_PREFIX))
         {
            args[0] = ((String) args[0]).substring(THREADLOCAL_PREFIX.length());
            // Find method with same name in local properties object if it
            // exists.
            Properties lprops = localProperties.get();
            Method localPropMethod = lprops.getClass()
                  .getMethod(method.getName(), method.getParameterTypes());
            if (localPropMethod != null)
            {
               log.debug("Setting threadlocal property method=" + method.getName() + " key=" + args[0]);
               return localPropMethod.invoke(lprops, args);
            }

         }
         else if (args != null && args.length > 0 && method.getName().equalsIgnoreCase("getProperty"))
         {
            Object value = localProperties.get().get((String) args[0]);
            if (value != null)
            {
               log.debug("Using thread local property key=" + args[0] + " value=" + value);
               return value;
            }
         }

         return methodProxy.invoke(orig, args);
      }

   }

}