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

package com.percussion.testing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class TestErrorCoverage 
{
   /**
    * Entry point for the error coverage test program.
    * <p>
    * First arg: filename of a properties file containing settings for
    * this test run.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/21
    * 
    * @param   args Command line arguments.
    * 
    */
   public static void main(String[] args)
   {
      if (args.length < 1)
         printUsage(System.out);

      try
      {
         ms_props = new Properties();
         loadProperties(ms_props, args[0]);
         runTests();
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }

   /**
    * Prints a help message to the given stream.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/21
    * 
    * @param   out
    * 
    */
   public static void printUsage(PrintStream out)
   {
      out.println("Usage: TestErrorCoverage <propsfile>");
   }

   /**
    * Initializes and loads the properties from the file with the
    * given name.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/21
    * 
    * @param   filename
    * 
    * @throws   IOException
    * 
    */
   private static void loadProperties(Properties props, String filename)
      throws IOException
   {
      InputStream in = new FileInputStream(new File(filename));
      try
      {
         props.load(new BufferedInputStream(in));
      }
      finally
      {
         in.close();
      }
   }

   /**
    * Main testing loop.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/21
    * 
    */
   private static void runTests()
      throws IOException, ClassNotFoundException, IllegalAccessException
   {
      if (ms_props == null)
         throw new IllegalStateException("properties have not been initialized");

      String classes = ms_props.getProperty(PROP_CLASSES);
      String bundles = ms_props.getProperty(PROP_BUNDLES);

      if (classes == null)
         throw new IllegalArgumentException("no " + PROP_CLASSES + " specified in properties");
      if (bundles == null)
         throw new IllegalArgumentException("no " + PROP_BUNDLES + " specified in properties");

      StringTokenizer classesTok = new StringTokenizer(classes, ";");
      StringTokenizer bundTok = new StringTokenizer(bundles, ";");
      
      // load ALL of the bundles, making each default to the previous
      Properties allProps = new Properties();
      while (bundTok.hasMoreTokens())
      {
         allProps = new Properties(allProps);
         loadProperties(allProps, bundTok.nextToken());
      }

      while (classesTok.hasMoreTokens())
      {
         testCoverage(classesTok.nextToken(), allProps);
      }
   }

   @SuppressWarnings("unchecked")
   private static void testCoverage(String errorCodesClassName, Properties props)
      throws ClassNotFoundException, IllegalAccessException
   {
      Class errorsClass = Class.forName(errorCodesClassName);
      Field[] fields = errorsClass.getFields();
      for (int i = 0; i < fields.length; i++)
      {
         Field field = fields[i];
         int code = field.getInt(null);

         // check for duplicate IDs
         Integer codeInt = new Integer(code);
         Object prevEntry = m_codes.get(codeInt);
         if (null != prevEntry)
         {
            System.out.println("Duplicate msg ID: \t" + errorCodesClassName + "." + field.getName()
               + "\t(" + code + ") ------" + prevEntry.toString());
         }

         // mark this one as taken
         m_codes.put(codeInt, errorCodesClassName + "." + field.getName());

         String message = props.getProperty("" + code);
         if (message == null)
         {
            System.out.println("No msg for\t" + errorCodesClassName + "." + field.getName() + "\t(" + code + ")");
         }
      }
   }

   private static final Map m_codes = new HashMap();
   private static final String PROP_BUNDLES = "errorBundles";
   private static final String PROP_CLASSES = "errorClasses";
   private static Properties ms_props;

}
