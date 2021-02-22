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
package com.percussion.util;

import com.percussion.content.IPSMimeContent;
import com.percussion.content.PSMimeContentAdapter;
import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.design.objectstore.PSNonUniqueException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSExtensionHandler;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionDefFactory;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionManager;
import com.percussion.extension.PSExtensionRef;
import com.percussion.extension.PSJavaExtensionHandler;
import com.percussion.extension.PSJavaScriptExtensionHandler;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is used to bootstrap the extension manager and manipulate it
 * without having to load the entire server.
 */
public class PSExtensionInstallTool
{
   /**
    * Installs the extensions from the provided resource directory to the
    * supplied server directory.
    *
    * @param args takes two arguments, argument 0 is the server directory
    *    while argument 1 is the resource directory.
    */
   public static void main(String[] args)
   {
      if (args.length != 2)
      {
         System.err.println("Usage: java PSExtensionInstallTool"+
                              " <server dir> <resource dir>");
         System.exit(1);
      }

      String serverDir = args[0];
      String resourceDir = args[1]; // the "kit" directory
      File extFile = new File(resourceDir, IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);

      if (!extFile.canRead())
      {
         System.err.println("Unable to read extension file: " + extFile);
         System.exit(1);
      }

      // create the resource document so we can install extensions
      FileInputStream fIn = null;
      Document doc = null;

      try
      {
         fIn = new FileInputStream(extFile);
         doc = PSXmlDocumentBuilder.createXmlDocument(fIn, false);
      } catch (Exception e)
      {
         System.err.println("Exception creating extension doc: " + e.toString());
         e.printStackTrace();
         System.exit(1);
      } finally
      {
         try
         {
            if (fIn != null)
               fIn.close();
         } catch (Exception ignore)
         {
         }
      }

      try
      {
         InstallExtensions(doc, serverDir, resourceDir);
      } 
      catch (Exception e)
      {
         System.err.println("Exception installing extensions: "+e.toString());
         e.printStackTrace();
         System.exit(1);
      }

      System.out.println("Completed successfully.");
      System.exit(0);
   }

   /**
    * Install (or register) extensions from the supplied document
    * 
    * @param doc the document that contains the to be installed extensions. 
    *    If the supplied extensions already exist in the current system, then
    *    the existing extensions will be replaced with the supplied ones.
    *    Never <code>null</code>. 
    * @param serverDir the Rhythmyx root directory, never <code>null</code> 
    *    or empty.
    * @param resourceDir the resource directory, never <code>null</code> or 
    *    empty. It is only used when the supplied document contains resources;
    *    otherwise it is not used.
    * 
    * @throws Exception if an error occurs during the installation 
    *    (or registration) process.
    */
   static public void InstallExtensions(Document doc, String serverDir,
         String resourceDir) throws Exception
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      if (serverDir == null || serverDir.trim().length() == 0)
         throw new IllegalArgumentException(
               "serverDir may not be null or empty");
      if (resourceDir == null || resourceDir.trim().length() == 0)
         throw new IllegalArgumentException(
               "resourceDir may not be null or empty");

      PSExtensionInstallTool extensionInstTool =
         new PSExtensionInstallTool(new File(serverDir));

      extensionInstTool.installJavaHandler();
      extensionInstTool.installJavaScriptHandler();
      extensionInstTool.installExtensions(doc, new File(resourceDir));
      extensionInstTool.convertOldExits();
   }
   
   /**
    * Initialize the install tool in the given directory, which represents
    * the "Extensions/" directory under the server root. If an existing
    * extension manager is already installed in this location, all
    * configuration information will be migrated to the new model.
    *
    * @param serverDir The server directory. Must not be
    * <CODE>null</CODE>.
    * @throws IOException if any IO operation fails.
    * @throws PSExtensionException if any extension installation fails.
    */
   public PSExtensionInstallTool(File serverDir)
      throws IOException, PSExtensionException
   {
      if (serverDir == null)
         throw new IllegalArgumentException("serverDir cannot be null");

      m_serverDir = serverDir;
      if (!m_serverDir.exists())
      {
         throw new IOException("Server directory does not exist: " +
            m_serverDir);
      }

      PSServer.setRxDir(m_serverDir);
      
      File extensionsDir = new File(m_serverDir, IPSExtensionHandler.EXTENSIONS_SUBDIR);
      if (!extensionsDir.exists())
      {
         if (!extensionsDir.mkdirs())
         {
            throw new IOException("Could not create directory " +
               extensionsDir);
         }
      }
      else
      {
         if (!extensionsDir.isDirectory())
         {
            throw new IOException("Could not create directory " +
               extensionsDir + " because a file by that name exists.");
         }
      }

      File configFile = new File(extensionsDir, IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);
      prepareConfig(configFile);
      initManager(configFile);
   }

   /**
    * Installs the Java extension handler if it is not already installed.
    *
    * @throws PSExtensionException If the extension definition fails
    *    the handler's validation rules or if the extension could not be
    *    loaded (some implementations may defer loading until prepareExtension
    *    is called), or if the extension handler could not be started. In any
    *    case, the defined extension will not be installed.
    * @throws PSNotFoundException If the appropriate extension handler
    *    does not exist. The defined extension will not be installed.
    * @throws PSNonUniqueException If the extension already exists. Use
    *    updateExtension instead. The defined extension will not be installed.
    */
   public void installJavaHandler()
      throws PSExtensionException, PSNonUniqueException, PSNotFoundException
   {
      PSExtensionRef javaRef = PSExtensionRef.handlerRef("Java");
      if (!m_extMgr.exists(javaRef))
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
         javaInitParams.setProperty(IPSExtensionDef.INIT_PARAM_DESCRIPTION,
            "Processes extensions written in the Java language." );

         IPSExtensionDef javaDef = new PSExtensionDef(javaRef, javaInterfaces,
            null, javaInitParams, null);

         m_extMgr.installExtension(javaDef, PSIteratorUtils.emptyIterator());
      }
   }

   /**
    * Installs the JavaScript extension handler if it is not already installed.
    *
    * @throws PSExtensionException If the extension definition fails
    *    the handler's validation rules or if the extension could not be
    *    loaded (some implementations may defer loading until prepareExtension
    *    is called), or if the extension handler could not be started. In any
    *    case, the defined extension will not be installed.
    * @throws PSNotFoundException If the appropriate extension handler
    *    does not exist. The defined extension will not be installed.
    * @throws PSNonUniqueException If the extension already exists. Use
    *    updateExtension instead. The defined extension will not be installed.
    */
   public void installJavaScriptHandler()
      throws PSExtensionException, PSNonUniqueException, PSNotFoundException
   {
      String handler = "JavaScript";
      PSExtensionRef ref = PSExtensionRef.handlerRef(handler);
      if (!m_extMgr.exists(ref))
      {
         Properties initParams = new Properties();
         initParams.setProperty("className", PSJavaScriptExtensionHandler.class.getName());
         initParams.setProperty(
            IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
            IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);
         initParams.setProperty(IPSExtensionDef.INIT_PARAM_REENTRANT, "yes");
         initParams.setProperty(IPSExtensionDef.INIT_PARAM_SCRIPTABLE, "yes" );
         initParams.setProperty(IPSExtensionDef.INIT_PARAM_DESCRIPTION,
            "Processes scriptable functions written in the JavaScript language." );

         IPSExtensionDef def = new PSExtensionDef( ref,
            PSIteratorUtils.iterator( IPSExtensionHandler.class.getName()),
            null,
            initParams, null );
         Iterator res = PSIteratorUtils.emptyIterator();
         m_extMgr.installExtension( def, res );
      }
   }

   /**
    * Returns the initialized extension manager instance.
    *
    * @return the extension manager instance, never <code>null</code>.
    */
   public IPSExtensionManager getExtensionManager()
   {
      return m_extMgr;
   }

   /**
    * Installs all of the given extensions. If the extensions already
    * exist, they will be updated rather than installed.
    *
    * @param extensions The extensions document, containing zero or more
    *    &lt;Extension&gt; elements, not <code>null</code>.
    * @param resourceRoot The root directory where referenced
    *    resources will be found, not <code>null</code>.
    * @throws PSExtensionException If the extension definition fails
    *    the handler's validation rules or if the extension could not be
    *    loaded (some implementations may defer loading until prepareExtension
    *    is called), or if the extension handler could not be started. In any
    *    case, the defined extension will not be installed.
    * @throws IOException if any IO operation fails.
    * @throws PSNotFoundException If the appropriate extension handler
    *    does not exist. The defined extension will not be installed.
    * @throws PSNonUniqueException If the extension already exists. Use
    *    updateExtension instead. The defined extension will not be installed.
    */
   public void installExtensions(Document extensions, File resourceRoot)
      throws PSExtensionException, IOException, PSNonUniqueException,
         PSNotFoundException
   {
      if (extensions == null || resourceRoot == null)
         throw new IllegalArgumentException("parameters cannot be null");

      NodeList extNodes = extensions.getElementsByTagName("Extension");
      for (int i = 0; i < extNodes.getLength(); i++)
      {
         installExtension((Element) extNodes.item(i), resourceRoot);
      }
   }

   /**
    * Installs all of the given extensions. If the extensions already
    * exist, they will be updated rather than installed.
    *
    * @param extElement The extension element, not <code>null</code>.
    * @param resourceRoot The root directory where referenced
    *    resources will be found, not <code>null</code>.
    * @throws PSExtensionException If the extension definition fails
    *    the handler's validation rules or if the extension could not be
    *    loaded (some implementations may defer loading until prepareExtension
    *    is called), or if the extension handler could not be started. In any
    *    case, the defined extension will not be installed.
    * @throws IOException if any IO operation fails.
    * @throws PSNotFoundException If the appropriate extension handler
    *    does not exist. The defined extension will not be installed.
    * @throws PSNonUniqueException If the extension already exists. Use
    *    updateExtension instead. The defined extension will not be installed.
    */
   public void installExtension(Element extElement, File resourceRoot)
      throws PSExtensionException, IOException, PSNonUniqueException,
         PSNotFoundException
   {
      if (extElement == null || resourceRoot == null)
         throw new IllegalArgumentException("parameters cannot be null");

      if (m_defFactory == null)
      {
         m_defFactory = new PSExtensionDefFactory();
      }

      IPSExtensionDef def = m_defFactory.fromXml(extElement);
      Iterator suppliedResources = getSuppliedResources(def, extElement,
         resourceRoot);
      try
      {
         installExtension(def, suppliedResources);
      }
      finally
      {
         while (suppliedResources.hasNext())
         {
            // close all remaining resources
            try
            {
               IPSMimeContent content = (IPSMimeContent)suppliedResources.next();
               content.getContent().close();
            }
            catch (Throwable t)
            {
               // ignore
            }
         }
      }
   }

   /**
    * Installs the given extension if it does not already exists. If it
    * already exists, it will be updated instead.
    *
    * @param def The extension definition. Must not be <CODE>null</CODE>.
    * @param resources An Iterator over 0 or more non-<CODE>null</CODE>
    *    named IPSMimeContent objects specifying any resources that should be
    *    saved along with the extension. The resources may or may not
    *    correspond to the URLs returned from the def's
    *    <CODE>getResourceLocations()</CODE> method. Must not be
    *    <CODE>null</CODE>.
    * @throws PSExtensionException If the extension definition fails
    *    the handler's validation rules or if the extension could not be
    *    loaded (some implementations may defer loading until prepareExtension
    *    is called), or if the extension handler could not be started. In any
    *    case, the defined extension will not be installed.
    * @throws PSNotFoundException If the appropriate extension handler
    *    does not exist. The defined extension will not be installed.
    * @throws PSNonUniqueException If the extension already exists. Use
    *    updateExtension instead. The defined extension will not be installed.
    */
   public void installExtension(IPSExtensionDef def, Iterator resources)
      throws PSExtensionException, PSNonUniqueException, PSNotFoundException
   {
      if (!m_extMgr.exists(def.getRef()))
      {
         System.out.println( "Installing extension: " + def.getRef().toString());
         m_extMgr.installExtension(def, resources);
      }
      else
      {
         System.out.println( "Updating extension: " + def.getRef().toString());
         m_extMgr.updateExtension(def, resources);
      }
   }

   /**
    * Closes all resources used in this class.
    */
   public void close()
   {
      try
      {
         if (m_extMgr != null)
         {
            m_extMgr.shutdown();
            m_extMgr = null;
         }
      }
      catch (PSExtensionException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Converts the exit directory structure from the version 1.0 format to
    * the version 2.0 format.
    *
    * @deprecated as of version 2.1.
    * @throws IOException for any IO operation that fails.
    * @throws PSExtensionException If the extension definition fails
    *    the handler's validation rules or if the extension could not be
    *    loaded (some implementations may defer loading until prepareExtension
    *    is called), or if the extension handler could not be started. In any
    *    case, the defined extension will not be installed.
    * @throws SAXException for any SAX parser error.
    * @throws PSNotFoundException If the appropriate extension handler
    *    does not exist. The defined extension will not be installed.
    * @throws PSNonUniqueException If the extension already exists. Use
    *    updateExtension instead. The defined extension will not be installed.
    */
   public void convertOldExits()
      throws IOException, PSExtensionException, SAXException,
         PSNotFoundException, PSNonUniqueException
   {
      File oldExitsDir = new File(m_serverDir, "Exits");
      if (oldExitsDir.isDirectory())
      {
         convertOldExits(oldExitsDir);
      }
   }

   private void convertOldExits(File oldExitsDir)
      throws IOException, PSExtensionException, SAXException,
         PSNotFoundException, PSNonUniqueException
   {
      // DBG>
      System.out.println("Converting old exits directory: " + oldExitsDir);
      // <DBG
      File oldConfig = new File(oldExitsDir, "ExtensionHandlers.xml");
      if (!oldConfig.isFile())
      {
         return;
      }

      Document configDoc;
      InputStream in = new BufferedInputStream(new FileInputStream(
         oldConfig));

      try
      {
         configDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      finally
      {
         in.close();
      }

      // if JavaScript handler is installed, then we will convert
      // the JavaScript UDFs
      boolean javaScriptInstalled = false;
      PSXmlTreeWalker tree = new PSXmlTreeWalker(
         configDoc.getDocumentElement());

      for (Element e = tree.getNextElement("className", false); e != null; )
      {
         String className = PSXmlTreeWalker.getElementData(e);
         // DBG>
         System.out.println("className=" + className);
         // <DBG
         if (className != null
            && className.equals("com.percussion.exit.PSJavaScriptExtensionHandler"))
         {
            javaScriptInstalled = true;
         }
         e = tree.getNextElement("className", false);
      }

      if (javaScriptInstalled)
      {
         // DBG>
         System.out.println("JavaScript is installed");
         // <DBG

         File jscriptDir = new File(oldExitsDir, "JavaScript");
         if (!jscriptDir.isDirectory())
         {
            throw new IOException(jscriptDir + " does not exist.");
         }

         // The 1.1 filename was and will always have been "Extensions.xml"
         File jscriptConfig = new File(jscriptDir, "Extensions.xml");
         if (!jscriptConfig.isFile())
         {
            throw new IOException(jscriptConfig + " does not exist.");
         }

         InputStream configIn = new BufferedInputStream(
            new FileInputStream(jscriptConfig));

         Document jscriptDoc = PSXmlDocumentBuilder.createXmlDocument(
            configIn, false);

         convertJscriptUDF(jscriptDoc);
      }
   }

   private void convertJscriptUDF(Document doc)
      throws PSExtensionException,
         PSNotFoundException,
         PSNonUniqueException
   {
      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      for (Element UDF = tree.getNextElement("PSXScriptExtensionDef", firstFlag); UDF != null;)
      {
         IPSExtensionDef def = defineUDFExtension(UDF);
         installExtension(def, PSIteratorUtils.emptyIterator());
         UDF = tree.getNextElement("PSXScriptExtensionDef", nextFlag);
      }
   }


   /**
    * Converts the given PSXScriptExtensionDef element into an
    * extension def. The element is <STRONG>not</STRONG> modified or removed.
    */
   private IPSExtensionDef defineUDFExtension(Element scriptDefEl)
   {
      PSXmlTreeWalker tree = new PSXmlTreeWalker(scriptDefEl);

      // get handler name element
      Element handlerElement = tree.getNextElement(
         "PSXExtensionHandlerDef", true, true);

      if (handlerElement == null)
      {
         throw new IllegalArgumentException("PSXExtensionHandlerDef is required");
      }

      Element handlerNameElement = tree.getNextElement("name", true, true);
      if (handlerNameElement == null)
      {
         throw new IllegalArgumentException("Extension handler name is required");
      }

      String handlerName = PSXmlTreeWalker.getElementData(handlerNameElement);
      if (handlerName == null || handlerName.trim().length() == 0)
      {
         throw new IllegalArgumentException("Extension handler name is required");
      }

      // get extension name
      Element nameElement = tree.getNextElement("name", false, true);
      if (nameElement == null)
      {
         throw new IllegalArgumentException("extension name is required.");
      }

      tree.setCurrent(scriptDefEl);
      String name = PSXmlTreeWalker.getElementData(nameElement);
      if (name == null || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("extension name is required.");
      }

      // get extension type(s)
      tree.setCurrent(scriptDefEl);
      Element typeElement = tree.getNextElement(
         "type", true, true);

      if (typeElement == null)
      {
         throw new IllegalArgumentException("extension type is required");
      }

      String typeStr = PSXmlTreeWalker.getElementData(typeElement);
      int type = Integer.parseInt(typeStr);
      final Collection<String> interfaces = new ArrayList<String>();
      if (0 != (type & EXT_TYPE_UDF_PROC))
      {
         interfaces.add("com.percussion.extension.IPSUDFProcessor");
      }

      if (0 != (type & EXT_TYPE_REQUEST_PRE_PROC))
      {
         interfaces.add("com.percussion.extension.IPSRequestPreProcessor");
      }

      if (0 != (type & EXT_TYPE_RESULT_DOC_PROC))
      {
         interfaces.add("com.percussion.extension.IPSResultDocumentProcessor");
      }

      // get extension body
      tree.setCurrent(scriptDefEl);
      Element bodyElement = tree.getNextElement("body", true, true);
      if (bodyElement == null)
      {
         throw new IllegalArgumentException("extension body is required.");
      }

      String body = PSXmlTreeWalker.getElementData(bodyElement);
      if (body == null || body.trim().length() == 0)
      {
         throw new IllegalArgumentException("extension body is required.");
      }

      // get extension description
      tree.setCurrent(scriptDefEl);
      Element descriptionElement = tree.getNextElement("description", true, true);
      String description = null; // optional
      if (descriptionElement != null)
      {
         description = PSXmlTreeWalker.getElementData(descriptionElement);
      }

      PSExtensionRef ref = new PSExtensionRef(
         handlerName,
         "global/",
         name);

      // build init params
      Properties initParams = new Properties();
      initParams.setProperty(IPSExtensionDef.INIT_PARAM_REENTRANT, "yes");
      initParams.setProperty(IPSExtensionDef.INIT_PARAM_FACTORY,
         "com.percussion.extension.PSExtensionFactory");
      initParams.setProperty("com.percussion.rhythmyx.oldName",
         name);

      if (description != null)
      {
         initParams.setProperty(IPSExtensionDef.INIT_PARAM_DESCRIPTION,
            description);
      }

      initParams.setProperty("scriptBody", body);

      // build runtime parameters
      tree.setCurrent(scriptDefEl);
      final Collection<PSExtensionParamDef> runtimeParams =
            new ArrayList<PSExtensionParamDef>();
      for (Element paramDefEl = tree.getNextElement("PSXExtensionParamDef", firstFlag);
         paramDefEl != null;)
      {
         String paramName = tree.getElementData("name", false);
         String dataType = tree.getElementData("dataType", false);
         String desc = tree.getElementData("description", false);
         PSExtensionParamDef paramDef = new PSExtensionParamDef(paramName,
            dataType);

         paramDef.setDescription(desc);
         runtimeParams.add(paramDef);

         paramDefEl = tree.getNextElement("PSXExtensionParamDef", nextFlag);
      }

      PSExtensionDef def = new PSExtensionDef(
         ref, // name
         interfaces.iterator(), // interfaces
         null, // resource URLs
         initParams,
         runtimeParams.iterator() // runtime params
         );

      return def;
   }

   private Iterator getSuppliedResources(IPSExtensionDef def, Element extEl,
      File resourceRoot) throws IOException, PSExtensionException
   {
      final List<PSMimeContentAdapter> resources =
            new ArrayList<PSMimeContentAdapter>();
      boolean success = false;
      try
      {
         NodeList resourceNodes = extEl.getElementsByTagName("suppliedResource");
         for (int i = 0; i < resourceNodes.getLength(); i++)
         {
            Element suppliedResource = (Element)resourceNodes.item(i);

            // get the location where this will be installed to (under the handler)
            String deployName = suppliedResource.getAttribute("deployName");
            if (deployName == null || deployName.trim().length() == 0)
            {
               Object[] args =
               {
                  def.getRef().toString()
               };

               throw new PSExtensionException(
                  IPSExtensionErrors.EXT_INSTALLER_DEPLOY_NAME_EXPECTED,
                     args);
            }

            // get the real file location
            URL u = new URL(suppliedResource.getAttribute("href"));
            if (!u.getProtocol().equals("file"))
            {
               Object[] args =
               {
                  def.getRef().toString(),
                  u.toString()
               };

               throw new PSExtensionException(
                  IPSExtensionErrors.EXT_INSTALLER_UNSUPPORTED_RESOURCE,
                     args);
            }

            String file = u.getFile();
            if (file.endsWith("/"))
            {
               // skip this resource URL because it specifies a directory
               continue;
            }

            if (file.length() > 1 && file.indexOf("/") == 0)
            {
               file = file.substring(1); // trim leading slash
            }

            File resourceFile = new File(resourceRoot, file);
            if (!resourceFile.isFile())
            {
               Object[] args =
               {
                  resourceFile
               };

               throw new PSExtensionException(
                  IPSExtensionErrors.EXT_INSTALLER_RESOURCE_NOT_EXITING,
                     args);
            }

            if (!resourceFile.canRead())
            {
               Object[] args =
               {
                  resourceFile
               };

               throw new PSExtensionException(
                  IPSExtensionErrors.EXT_INSTALLER_RESOURCE_NOT_READABLE,
                     args);
            }

            InputStream in = new BufferedInputStream(
               new FileInputStream(resourceFile));

            PSMimeContentAdapter content = new PSMimeContentAdapter(in,
               null, null, null, -1);

            content.setName(deployName);
            resources.add(content);
         }

         success = true;
      }
      finally
      {
         if (!success) // close any opened resources immediately
         {
            for (Iterator i = resources.iterator(); i.hasNext(); )
            {
               try
               {
                  IPSMimeContent content = (IPSMimeContent)i.next();
                  content.getContent().close();
               }
               catch (Throwable t)
               {
                  // ignore
               }
            }
         }
      }

      return resources.iterator();
   }

   private void initManager(File configFile)
      throws PSExtensionException
   {
      Properties extProps = new Properties();
      extProps.put(IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
         configFile.getName());
      m_extMgr = new PSExtensionManager();
      m_extMgr.init(configFile.getParentFile(), extProps);
   }

   private void prepareConfig(File file)
      throws IOException
   {
      if (file.exists())
      {
         if (!file.isFile())
         {
            throw new IOException("Could not create file "
               + file + " because it exists already and is not a file.");
         }

         if (!file.canRead())
         {
            throw new IOException("File " + file + " is not readable.");
         }

         if (!file.canWrite())
         {
            throw new IOException("File " + file + " is not writeable.");
         }

         backupFile(file);
      }
   }

   private File backupFile(File file)
      throws IOException
   {
      File backup = File.createTempFile("ext", ".bak", file.getParentFile());
      InputStream in = null;
      OutputStream out = null;
      try
      {
         in = new BufferedInputStream(new FileInputStream(file));
         out = new BufferedOutputStream(new FileOutputStream(backup));
         IOTools.copyStream(in, out, 8192);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
               // ignore
            }
         }

         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (IOException e)
            {
               // ignore
            }
         }
      }

      return backup;
   }

   /**
    * Closes all resources used in this class.
    */
   @Override
   protected void finalize() throws Throwable
   {
      close();
      super.finalize();
   }

   /**
    * The extension manager instance, initialized during construction, never
    * <code>null</code> after that.
    */
   private IPSExtensionManager m_extMgr;
   private PSExtensionDefFactory m_defFactory;
   private File m_serverDir;

   private static final int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
      | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

   private static final int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
      | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

   /**
    * This extension is a user defined function (UDF) processor.
    */
   private static final int EXT_TYPE_UDF_PROC            = 0x01;

   /**
    * This extension is a request pre-processor.
    */
   private static final int EXT_TYPE_REQUEST_PRE_PROC      = 0x02;

   /**
    * This extension is a result document processor.
    */
   private static final int EXT_TYPE_RESULT_DOC_PROC      = 0x04;
}
