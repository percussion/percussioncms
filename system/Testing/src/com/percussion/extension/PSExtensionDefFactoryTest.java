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

