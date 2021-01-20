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
package com.percussion.extension;

import com.percussion.content.PSMimeContentAdapter;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.server.PSRequest;
import com.percussion.testing.IPSServerBasedJunitTest;
import com.percussion.util.PSIteratorUtils;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the PSExtensionManager class.
 */
@Category(IntegrationTest.class)
public class PSExtensionManagerTest
    implements IPSServerBasedJunitTest
{

   /**
    * Make sure the error handling relating to init() and shutdown()
    * works as specified.
    */
   @Test
   public void testInitAndShutdownErrors() throws Exception
   {
      PSExtensionManager mgr = new PSExtensionManager();
      
      boolean didThrow = false;
      try
      {
         mgr.init(null, null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue("caught init(null, null)", didThrow);

      didThrow = false;
      try
      {
         mgr.init(new File("/temp/"), null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue("caught init(File, null)", didThrow);

      didThrow = false;
      try
      {
         mgr.init(null, new Properties());
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue("caught init(File, null)", didThrow);

      didThrow = false;
      try
      {
         mgr.init(new File("/temp/testing/extmgr"), new Properties());
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue("caught init(File, Properties) without required props", didThrow);

      // make sure shutdown is idempotent
      mgr.shutdown();

      // make sure no methods can be called when it's shut down
      didThrow = false;
      try
      {
         mgr.getExtensionHandlerNames();
      }
      catch (IllegalStateException e)
      {
         didThrow = true;
      }
      assertTrue("caught uninitialized call to getExtensionHandlerNames()", didThrow);

      didThrow = false;
      try
      {
         Iterator i = mgr.getExtensionNames(null, null, null, null);
         assertTrue(!i.hasNext());
      }
      catch (IllegalStateException e)
      {
         didThrow = true;
      }
      assertTrue("caught uninitialized call to getExtensionNames()", didThrow);

      didThrow = false;
      try
      {
         mgr.startExtensionHandler("nonexistenthandler");
      }
      catch (IllegalStateException e)
      {
         didThrow = true;
      }
      assertTrue("caught uninitialized call to startExtensionHandler()", didThrow);

      didThrow = false;
      try
      {
         mgr.stopExtensionHandler("nonexistenthandler");
      }
      catch (IllegalStateException e)
      {
         didThrow = true;
      }
      assertTrue("caught uninitialized call to stopExtensionHandler()", didThrow);
   }

   /**
    * Init the manager and install, then prepare, a dummy extension.
    */
   @Test
   public void testInstall() throws Exception
   {
      // INIT EXTENSION MANAGER
      File configFile = new File("/temp/testing/extmgr" + 
         new Date().getTime() + "/config.xml");
      configFile.delete();
      createConfigFile(configFile);
      PSExtensionManager mgr = new PSExtensionManager();
      Properties initProps = new Properties();
      initProps.setProperty(IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
         configFile.getName());
      mgr.init(configFile.getParentFile(), initProps);

      try
      {
         assertTrue(configFile.exists());
         assertTrue(configFile.length() > 0L);

         // make sure that the built-in extension handlers are represented
         PSExtensionRef javaRef = PSExtensionRef.handlerRef("Java");
         if (!mgr.exists(javaRef))
         {
            Iterator javaInterfaces =
               PSIteratorUtils.iterator(IPSExtensionHandler.class.getName());

            Properties javaInitParams = new Properties();
            javaInitParams.setProperty("className", PSJavaExtensionHandler.class.getName());
            javaInitParams.setProperty(
               IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
               IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);
            javaInitParams.setProperty(IPSExtensionDef.INIT_PARAM_REENTRANT,
               "yes");
            javaInitParams.setProperty(IPSExtensionDef.INIT_PARAM_SCRIPTABLE,
               "no" );
            javaInitParams.setProperty( IPSExtensionDef.INIT_PARAM_DESCRIPTION,
               "Java handler" );

            IPSExtensionDef javaDef = new PSExtensionDef(javaRef, javaInterfaces,
               null, javaInitParams, null);

            mgr.installExtension(javaDef, PSIteratorUtils.emptyIterator());
         }

         assertTrue(!mgr.getExtensionNames(null, null, null, null).hasNext());

         // INSTALL DUMMY EXTENSION
         PSExtensionRef dummyRef = new PSExtensionRef("Java", "TopContext/SubContext", "dummy");
         assertTrue(!mgr.exists(dummyRef));
         IPSExtensionDef def = createTestingDef(dummyRef);

         // now create a MIME context for the PSTestingExtension's class file
         File classFile = new File("build/classes/com/percussion/extension/PSTestingExtension.class");
         assertTrue("Test must be run from E2 directory", classFile.exists());
         FileInputStream in = new FileInputStream(classFile);
         PSMimeContentAdapter classContent = new PSMimeContentAdapter(in, null, null, null, -1);
         classContent.setName("com/percussion/extension/PSTestingExtension.class");
         Iterator resources = PSIteratorUtils.iterator(classContent);
         mgr.installExtension(def, resources, null);

         // MAKE SURE EXTENSION SHOWS UP IN APPROPRIATE PLACES
         testCataloging(mgr, dummyRef);

         // PREPARE EXTENSION
         // force the class file from the extension dir to be used
         File backupClassFile = new File("PSTestingExtension.bak");
         assertTrue(classFile.renameTo(backupClassFile));
         assertTrue(backupClassFile.exists());
         assertTrue(!classFile.exists());

         try
         {
            IPSExtension ext = mgr.prepareExtension(def.getRef(), null);
            assertEquals(ext.getClass().getName(), "com.percussion.extension.PSTestingExtension");
            assertTrue(ext.getClass().getClassLoader() instanceof PSExtensionClassLoader);
            ext = null;
         }
         finally
         {
            // restore renamed classfile
            assertTrue(backupClassFile.renameTo(classFile));
         }

         // remove the extension
         mgr.removeExtension(def.getRef());

         // make sure it can't be loaded
         boolean didThrow = false;
         try
         {
            IPSExtension ext = mgr.prepareExtension(def.getRef(), null);
            ext = null;
         }
         catch (PSNotFoundException e)
         {
            didThrow = true;
         }
         assertTrue(didThrow);
      }
      finally
      {
         mgr.shutdown();
      }
   }

   private void testCataloging(IPSExtensionManager mgr, PSExtensionRef ref)
      throws Exception
   {
      assertTrue("mgr.exists(ref)?", mgr.exists(ref));
      for (Iterator i = generateCatalogingMatrix(ref); i.hasNext(); )
      {
         CatalogParams params = (CatalogParams)i.next();
         Iterator exts = mgr.getExtensionNames(
            params.hnd,
            params.cxt,
            params.ifc,
            params.ext);

         testCataloging(exts, ref);
      }
      
   }

   private void testCataloging(Iterator i, PSExtensionRef ref)
   {
      assertTrue(i.hasNext());
      assertTrue(i.next().equals(ref));
      assertTrue(!i.hasNext());
   }

   /**
    * Generates a testing matrix.
    */
   private Iterator generateCatalogingMatrix(PSExtensionRef ref)
   {
      Collection matrix = new ArrayList();
      String[] handlers = new String[] {
         null,
         "%",
         ref.getHandlerName()
      };

      String[] contexts = new String[] {
         null,
         ref.getContext()
      };

      String[] interfaces = new String[] {
         null,
         "%",
         "com.percussion.extension.IPSExtension"
      };

      String[] extensions = new String[] {
         null,
         "%",
         ref.getExtensionName()
      };

      for (int h = 0; h < handlers.length; h++)
      {
         for (int c = 0; c < contexts.length; c++)
         {
            for (int i = 0; i < interfaces.length; i++)
            {
               for (int e = 0; e < extensions.length; e++)
               {
                  matrix.add(new CatalogParams(
                     handlers[h],
                     contexts[c],
                     interfaces[i],
                     extensions[e]
                     ));
               }
            }
         }
      }

      return matrix.iterator();
   }

   class CatalogParams
   {
      public CatalogParams(String handler,
         String context, String intface, String extension)
      {
         hnd = handler;
         cxt = context;
         ifc = intface;
         ext = extension;
      }

      public String hnd;
      public String cxt;
      public String ifc;
      public String ext;
   }

   /**
    * Creates a def for PSTestingException, but does not load the class.
    */
   public static IPSExtensionDef createTestingDef(PSExtensionRef ref)
      throws MalformedURLException
   {
      Iterator interfaces = PSIteratorUtils.iterator("com.percussion.extension.IPSExtension");
      
      Iterator resourceURLs = PSIteratorUtils.iterator(
         new URL("file:com/percussion/extension/PSTestingExtension.class"));

      Properties initParams = new Properties();
      initParams.setProperty("className", "com.percussion.extension.PSTestingExtension");

      return new PSExtensionDef(ref, interfaces, resourceURLs, initParams, null);
   }

   static void createConfigFile(File configFile)
      throws IOException
   {
      // create the config file if it doesn't already exist
      configFile.getParentFile().mkdirs();
      configFile.delete();
      configFile.createNewFile();

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc,
         "PSXExtensionHandlerConfiguration");
      
      root.setAttribute("handlerName", "testing");

      OutputStream out = null;
      try
      {
         out = new FileOutputStream(configFile);
         PSXmlDocumentBuilder.write(doc, out);
      }
      finally
      {
         if (out != null)
         {
            try { out.close(); } catch (IOException e) { /* ignore */ }
         }
      }
   }


   /**
    * The loadable handler will call this method once before any test method.
    *
    * @param req The request that was passed to the loadable handler.
    *            Never <code>null</code>;
    */
   @Override
   public void oneTimeSetUp(Object req) {

   }

   /* (non-Javadoc)
    * @see IPSServerBasedJunitTest#oneTimeTearDown()
    */
   @AfterClass
   public void oneTimeTearDown()
   {
      // noop      
   }

}
