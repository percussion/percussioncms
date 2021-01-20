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
