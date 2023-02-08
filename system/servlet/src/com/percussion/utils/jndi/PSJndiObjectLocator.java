/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.utils.jndi;

import org.springframework.jndi.JndiObjectLocator;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Create an object locator that prepends an appropriate prefix on jndi lookups
 * for the given application server. This is set by a servlet init property in
 * the initialization for the application or can be performed by any other
 * reasonable mechanism if this code is used in a client context.
 * 
 * <P>
 * The System property set is ps.jndi.prefix
 * 
 * <P>
 * This class also makes a number of typed lookups available to simplify the
 * process of using JNDI.
 * 
 * @author dougrand
 */
public class PSJndiObjectLocator extends JndiObjectLocator
{
   private static String ms_prefix = "java:comp/env/";
   
   /**
    * Create an object locator with a given name. The appropriate prefix will be
    * prepended to the lookup for the current environment.
    * 
    * @param resourceName the jndi resource to lookup, never <code>null</code>
    *           or empty
    */
   public PSJndiObjectLocator(String resourceName) {
      super();

      if (resourceName == null || resourceName.trim().length() == 0)
      {
         throw new IllegalArgumentException(
               "resourceName may not be null or empty");
      }
      
      setJndiName(resourceName);
      setResourceRef(true);
   }
   
   /**
    * Lookup a data source
    * @return data source, never <code>null</code>
    * @throws NamingException if the data source is not known
    */
   public DataSource lookupDataSource() throws NamingException
   {
      return (DataSource) lookup();
   }
   
   /**
    * Copied from base class, turn off logging
    */
   protected Object lookup(String jndiName) throws NamingException
   {
      String jndiNameToUse = convertJndiName(jndiName);
      Object jndiObject = getJndiTemplate().lookup(jndiNameToUse);

      return jndiObject;
   }   
   
   /**
    * Set the prefix for the jndi lookup. Note that this method is not
    * synchronized, so it should only be called on initialization
    * @param prefix prefix for the lookup
    */
   public static void setPrefix(String prefix)
   {
      // Modify the container prefix for all lookups
      ms_prefix = prefix;
   }

   /**
    * Get the prefix for the jndi lookup. 
    * 
    * @return the prefix for the lookup, may be <code>null</code> or empty.
    */
   public static String getPrefix()
   {
      return ms_prefix;
   }
   
    /**
     * Convert the given JNDI name to the actual JNDI name to use.
     * Default implementation applies the "java:comp/env/" prefix if
     * resourceRef is true and no other scheme like "java:" is given.
     * @param jndiName the original JNDI name
     * @return the JNDI name to use
     * @see #ms_prefix
     * @see #setResourceRef
     */
    protected String convertJndiName(String jndiName) {
        // prepend container prefix if not already specified and no other scheme given
        if (isResourceRef() && !jndiName.startsWith(ms_prefix) && jndiName.indexOf(':') == -1) {
            jndiName = ms_prefix + jndiName;
        }
        return jndiName;
    }
}
