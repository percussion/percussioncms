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
