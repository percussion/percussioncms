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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.integration;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This factory class holds the appropriate class to instantiate
 * instance of the interface <code>IPSWsHelper</code>. 
 * Examples of using this class can be found in the tag library source
 * included with Rhythmyx. See the file <code>taglibsrc.jar</code>
 */
public class PSWsHelperFactory
{
   /**
    * This static holds the class that will be instantiated. This value
    * defaults to the AXIS webservices helper class, which is expected
    * to be the only one in use.
    * 
    * The value is defaulted in {@link #getHelperClass()}.
    */
   private static Class s_helperClass = null;

   /**
    * Each arglist is that of one of the PSWsHelper constructors
    */
   private static Class[] s_arglist_1 =
      {
         ServletContext.class,
         HttpServletRequest.class,
         HttpServletResponse.class,
         URL.class };
   private static Class[] s_arglist_2 =
      {
         ServletContext.class,
         HttpServletRequest.class,
         HttpServletResponse.class };

   /**
    * Assign the helper class into a static
    * @param hc the class to be instantiated in the <code>create</code>
    * method. May not be <code>null</code> and must implement the 
    * interface <code>IPSWsHelper</code>.
    */
   public static synchronized void setHelperClass(Class hc)
   {
      if (hc == null)
      {
         throw new IllegalArgumentException("Helper class may not be null");
      }

      if (!IPSWsHelper.class.isAssignableFrom(hc))
      {
         throw new IllegalArgumentException("Helper class must implement IPSWsHelper");
      }

      s_helperClass = hc;
   }

   /**
    * Retrieve the helper class
    * @return returns the helper class set via <code>setHelperClass</code>
    * @throws ClassNotFoundException if the class was never set using
    * {@link #setHelperClass(Class)} and the default class, 
    * com.percussion.integration.PSWsHelper, cannot be found.
    */
   public static synchronized Class getHelperClass() 
   throws ClassNotFoundException
   {
      if (s_helperClass == null)
      {
         s_helperClass = Class.forName("com.percussion.integration.PSWsHelper");
      }
      return s_helperClass;
   }

   /**
    * Creates an instance of t class specified using the static method
    * <code>setHelperClass</code>, passing all  
    * the needed ports and endpoint.
    * 
    * @param context the current servlet context, may not be <code>null</code>
    * @param req the current servlet request, may not be <code>null</code>
    * @param resp the current servlet response, may not be <code>null</code>
    * @param targetEndpoint the url location to send the soap message, may be
    *    <code>null</code>, @see init for more information
    *    
    * @throws ServletException
    * @throws IOException
    * @throws ParserConfigurationException
    * @throws ClassNotFoundException
    * @throws NoSuchMethodException
    * @throws IllegalAccessException
    * @throws InstantiationException
    * @throws InvocationTargetException
    */
   public static IPSWsHelper create(
      ServletContext context,
      HttpServletRequest req,
      HttpServletResponse resp,
      URL targetEndpoint)
      throws
         ClassNotFoundException,
         NoSuchMethodException,
         IllegalAccessException,
         InstantiationException,
         InvocationTargetException,
         ServletException,
         ParserConfigurationException,
         IOException
   {
      Constructor c = null;
      Class helper = getHelperClass();
      
      if (helper == null)
      {
         throw new ClassNotFoundException("Helper class has not been defined");
      }

      c = helper.getConstructor(s_arglist_1);

      Object args[] = new Object[4];
      args[0] = context;
      args[1] = req;
      args[2] = resp;
      args[3] = targetEndpoint;

      return (IPSWsHelper) c.newInstance(args);
   }

   /**
     * Creates an instance of the class specified using the static method
     * <code>setHelperClass</code>, passing all  
     * the needed ports and endpoint.
    * 
    * @param context the current servlet context, may not be <code>null</code>
    * @param req the current servlet request, may not be <code>null</code>
    * @param resp the current servlet response, may not be <code>null</code>
    *    
    * @throws ServletException
    * @throws IOException
    * @throws ParserConfigurationException
    * @throws ClassNotFoundException
    * @throws NoSuchMethodException
    * @throws IllegalAccessException
    * @throws InstantiationException
    * @throws InvocationTargetException
    */
   public static IPSWsHelper create(
      ServletContext context,
      HttpServletRequest req,
      HttpServletResponse resp)
      throws
         ClassNotFoundException,
         NoSuchMethodException,
         IllegalAccessException,
         InstantiationException,
         InvocationTargetException,
         ServletException,
         ParserConfigurationException,
         IOException
   {
      Constructor c = null;
      Class helper = getHelperClass();

      if (helper == null)
      {
         throw new ClassNotFoundException("Helper class has not been defined");
      }

      c = helper.getConstructor(s_arglist_2);

      Object args[] = new Object[3];
      args[0] = context;
      args[1] = req;
      args[2] = resp;

      return (IPSWsHelper) c.newInstance(args);
   }
}
