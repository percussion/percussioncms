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
package com.percussion.extension;

import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.xml.PSXmlDocumentBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Unit tests for the PSExtensionDefFactory class. All tests follow the same
 * basic pattern: create a def w certain properties set, serialize using the
 * factory, then pass the serialized node back into the factory to be de-
 * serialized. The original and the de-serialized versions are compared for
 * equality.
 */
@SuppressFBWarnings("INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE")
public class PSExtensionDefFactoryTest extends TestCase
{
   /**
    * Does a round trip test, with all possible properties containing values.
    */
   @SuppressWarnings("unchecked")
   public void testFull() throws Exception
   {
      // create an extension def, then round trip it thru the factory, then
      // compare
      PSExtensionRef ref = new PSExtensionRef("Handler1", "context1", "ext1");
      ArrayList ifaces = new ArrayList();
      ifaces.add("com.percussion.extension.IPSUdfProcessor");

      ArrayList urls = new ArrayList();
      urls.add(new URL("file", "", "archive1.jar"));
      urls.add(new URL("file", "", "archive2.zip"));

      Properties javaInitParams = new Properties();
      javaInitParams.setProperty("className", PSJavaExtensionHandler.class
         .getName());
      javaInitParams.setProperty(
         IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
         IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);
      javaInitParams.setProperty(IPSExtensionDef.INIT_PARAM_REENTRANT, "yes");

      ArrayList runtimeParams = new ArrayList();
      PSExtensionParamDef pdef = new PSExtensionParamDef("param1",
         "java.lang.String");
      runtimeParams.add(pdef);
      pdef = new PSExtensionParamDef("param2", "java.util.HashMap");
      pdef.setDescription("desc for param2");
      runtimeParams.add(pdef);

      PSExtensionDef def = new PSExtensionDef(ref, ifaces.iterator(), urls
         .iterator(), javaInitParams, runtimeParams.iterator());
      
      int methodCount = 2;
      int paramCount = 3;
      for (int i=0; i<methodCount; i++)
      {
         PSExtensionMethod method = new PSExtensionMethod("name_" + i,
            String.class.getName(), "description_" + i);
         for (int j=0; j<paramCount; j++)
         {
            PSExtensionMethodParam param = new PSExtensionMethodParam(
               "name_" + i + "." + j, String.class.getName(), 
               "description_" + i + "." + j);
            method.addParameter(param);
         }
         def.addExtensionMethod(method);
      }

      PSExtensionDefFactory factory = new PSExtensionDefFactory();

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "root");

      Element defElement = null;
      try
      {
         defElement = factory.toXml(root, def);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         assertTrue("Failed serialization: " + e.toString(), false);
         return;
      }

      IPSExtensionDef def2 = null;
      try
      {
         def2 = factory.fromXml(defElement);
      }
      catch (Exception e)
      {
         PSXmlDocumentBuilder.write(doc, System.out);
         e.printStackTrace();
         assertTrue("Failed de-serialization: " + e.toString(), false);
         return;
      }

      assertTrue("Full def failed comparison", def.equals(def2));
   }


   /**
    * Does a round trip test, using a def that has no URLs and no runtime
    * parameters defined..
    */
   @SuppressWarnings("unchecked")
   public void testNoUrlsNoRuntimeParams() throws Exception
   {
      // create an extension def, then round trip it thru the factory, then
      // compare
      PSExtensionRef ref = new PSExtensionRef("Handler1", "context1", "ext1");
      ArrayList ifaces = new ArrayList();
      ifaces.add("com.percussion.extension.IPSUdfProcessor");

      ArrayList urls = new ArrayList();

      Properties javaInitParams = new Properties();
      javaInitParams.setProperty("className", PSJavaExtensionHandler.class
         .getName());
      javaInitParams.setProperty(
         IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
         IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);
      javaInitParams.setProperty(IPSExtensionDef.INIT_PARAM_REENTRANT, "yes");

      PSExtensionDef def = new PSExtensionDef(ref, ifaces.iterator(), urls
         .iterator(), javaInitParams, null);

      PSExtensionDefFactory factory = new PSExtensionDefFactory();

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "root");

      Element defElement = null;
      try
      {
         defElement = factory.toXml(root, def);
      }
      catch (Exception e)
      {
         // e.printStackTrace();
         assertTrue("Failed serialization: " + e.toString(), false);
         return;
      }

      IPSExtensionDef def2 = null;
      try
      {
         def2 = factory.fromXml(defElement);
      }
      catch (Exception e)
      {
         PSXmlDocumentBuilder.write(doc, System.out);
         e.printStackTrace();
         assertTrue("Failed de-serialization: " + e.toString(), false);
         return;
      }

      assertTrue("Full def failed comparison", def.equals(def2));
   }

   /**
    * Does a round trip test, using a def that has no URLs defined.
    */
   @SuppressWarnings("unchecked")
   public void testNoUrls() throws Exception
   {
      // create an extension def, then round trip it thru the factory, then
      // compare
      PSExtensionRef ref = new PSExtensionRef("Handler1", "context1", "ext1");
      ArrayList ifaces = new ArrayList();
      ifaces.add("com.percussion.extension.IPSUdfProcessor");
      ifaces.add("com.percussion.extension.IPSRequestPreProcessor");
      ifaces.add("com.percussion.extension.IPSResultDocumentProcessor");

      ArrayList urls = new ArrayList();

      Properties javaInitParams = new Properties();
      javaInitParams.setProperty("className", PSJavaExtensionHandler.class
         .getName());
      javaInitParams.setProperty(
         IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
         IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);
      javaInitParams.setProperty(IPSExtensionDef.INIT_PARAM_REENTRANT, "yes");

      ArrayList runtimeParams = new ArrayList();
      PSExtensionParamDef pdef = new PSExtensionParamDef("param1",
         "java.lang.String");
      runtimeParams.add(pdef);
      pdef = new PSExtensionParamDef("param2", "java.util.HashMap");
      runtimeParams.add(pdef);

      PSExtensionDef def = new PSExtensionDef(ref, ifaces.iterator(), urls
         .iterator(), javaInitParams, runtimeParams.iterator());

      PSExtensionDefFactory factory = new PSExtensionDefFactory();

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "root");

      Element defElement = null;
      try
      {
         defElement = factory.toXml(root, def);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         assertTrue("Failed serialization: " + e.toString(), false);
         return;
      }

      IPSExtensionDef def2 = null;
      try
      {
         def2 = factory.fromXml(defElement);
      }
      catch (Exception e)
      {
         PSXmlDocumentBuilder.write(doc, System.out);
         e.printStackTrace();
         assertTrue("Failed de-serialization: " + e.toString(), false);
         return;
      }

      assertTrue("Full def failed comparison", def.equals(def2));
   }


   /**
    * Does a round trip test, using a def that has no runtime params defined.
    */
   @SuppressWarnings("unchecked")
   public void testNoRuntimeParamst() throws Exception
   {
      // create an extension def, then round trip it thru the factory, then
      // compare
      PSExtensionRef ref = new PSExtensionRef("Handler1", "context1", "ext1");
      ArrayList ifaces = new ArrayList();
      ifaces.add("com.percussion.extension.IPSUdfProcessor");

      ArrayList urls = new ArrayList();
      urls.add(new URL("file", "", "archive1.jar"));
      urls.add(new URL("file", "", "archive2.zip"));
      urls.add(new URL("file", "", "classes/"));

      Properties javaInitParams = new Properties();
      javaInitParams.setProperty("className", PSJavaExtensionHandler.class
         .getName());
      javaInitParams.setProperty(
         IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
         IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);
      javaInitParams.setProperty(IPSExtensionDef.INIT_PARAM_REENTRANT, "yes");

      PSExtensionDef def = new PSExtensionDef(ref, ifaces.iterator(), urls
         .iterator(), javaInitParams, null);

      PSExtensionDefFactory factory = new PSExtensionDefFactory();

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "root");

      Element defElement = null;
      try
      {
         defElement = factory.toXml(root, def);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         assertTrue("Failed serialization: " + e.toString(), false);
         return;
      }

      IPSExtensionDef def2 = null;
      try
      {
         def2 = factory.fromXml(defElement);
      }
      catch (Exception e)
      {
         PSXmlDocumentBuilder.write(doc, System.out);
         e.printStackTrace();
         assertTrue("Failed de-serialization: " + e.toString(), false);
         return;
      }

      assertTrue("Full def failed comparison", def.equals(def2));
   }
}

