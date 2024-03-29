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
package com.percussion.testing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;


import junit.framework.TestCase;

/**
 * The utility class to provide various properties for all the unit tests.
 * It is essentially a config props factory that uses a classloader to fetch
 * a appropriate properties file based on the given CONN_TYPE_XX.
 * Prop. file names correspond to the CONN_TYPE_XX, ie: conn_rxserver.properties.
 */
public class PSConfigHelperTestCase implements
      IPSUnitTestConfigHelper
{     
   
   /**
    * Default constructor.
    */
   public PSConfigHelperTestCase()
   {
   }
   
   /**
    * Simply call super(String).
    *
    * @param arg0 the name of the TestCase.
    */
   public PSConfigHelperTestCase(String arg0)
   {
   }

   // Implements IPSClientBasedJunitTest.getRxConnectionProps()
   public static Properties getConnectionProps(int type) throws IOException
   {
      switch (type)
      {
         case CONN_TYPE_RXSERVER:
            return getPropertiesWithSystemOverrides("conn_rxserver.properties");
         case CONN_TYPE_TOMCAT:
            return getPropertiesWithSystemOverrides("conn_tomcat.properties");
         case CONN_TYPE_SQL:
            return getPropertiesWithSystemOverrides("conn_sql.properties");
         case CONN_TYPE_ORA:
            return getPropertiesWithSystemOverrides("conn_ora.properties");

         default:
         {
            //undefined connection type
            throw new IllegalArgumentException("invalid conn type: " + type);
         }
      }

   }

   
   /**
    * Loads properties from the classpath and overrides values from system properties.
    * This is useful when running tests from CI server's like Jenkins or from
    * Eclipse launch configurations. Assumes that properties are overridden using a
    * <filename>.<propertyname> notation.
    * 
    * @param fileName
    * 
    * @return
    * @throws IOException 
    */
   private static Properties getPropertiesWithSystemOverrides(String fileName) throws IOException{
         Properties props = loadFromFile(fileName);
         Properties system = System.getProperties();
      
         //Loop through the properties and search for overrides.
         Enumeration<?> e = props.propertyNames();
         while(e.hasMoreElements()){
            String key = (String)e.nextElement();
            String val = system.getProperty(fileName.concat(".").concat(key));
            if(val!=null){
               //Override the value with the System provided one.  
               props.setProperty(key, val);
            }
         }
         
         return props;         
   }
   
   /**
    * Loads a props file using this class's class loader.
    * @param fileName name of the props file to load, never <code>null</code>.
    * @return props never <code>null</code>.
    * @throws IOException
    */
   private static Properties loadFromFile(String fileName) throws IOException
   {
      InputStream in = PSConfigHelperTestCase.class.getResourceAsStream(fileName);

      try
      {
         Properties props = new Properties();
         props.load( in );

         return props;
      }
      finally
      {
         if (in != null)
            try
            {
               in.close();
            } catch (IOException e)
            {
            }
      }
   }
}
