/******************************************************************************
 *
 * [ PSSiteFolderHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSSaveResults;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * @author RoyKiesler
 *
 * Helper method to create a folder structur within the rhythmyx Folders folder
 * either from an XML file as source or a windows folder root.
 */
public class PSSiteFolderHelper implements IPSRequestPreProcessor
{
   /**
    * This exit creates a folder structure attached to the <code>Folders</code>
    * folder in Rhythmyx either from an XML file supplied in the 
    * <code>folderFileUrl</code> parameter or a folder location from the 
    * windows file system supplied as <code>rootFolder</code> parameter.
    * 
    * @see IPSRequestPreProcessor#preProcessRequest(Object[], IPSRequestContext)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      // validate parameters
      if ((params[0] == null || params[0].toString().trim().equals("")) &&
          (params[1] == null || params[1].toString().trim().equals("")))
      {
         throw new PSParameterMismatchException(
            "at least one parameter (folderFileURL or rootFolder) is " +
            "required.");
      }
      
      PSLocator folders = new PSLocator(3, 1);
      
      if (params[0] != null && !params[0].toString().trim().equals(""))
      {
         Document inputDoc = null;
         try
         {
            InputSource source = new InputSource(new FileReader(
               new File(params[0].toString())));
            inputDoc = PSXmlDocumentBuilder.createXmlDocument(source, false);
         }
         catch (Exception e)
         {
            logger.error("Exception -- " + e.getMessage(), e);
         }
         
         if (inputDoc == null)
            throw new PSExtensionProcessingException(0,
               "Failed to load input doc");
         
         createSiteFolders(inputDoc.getDocumentElement(), folders, request);
      }

      if (params[1] != null && !params[1].toString().trim().equals(""))
      {
         // parse root folder parameter
         String folderParam = params[1].toString();
         File rootFolder = new File(folderParam);
         if (!rootFolder.isDirectory())
         {
            throw new PSExtensionProcessingException(0, "\"" + folderParam +
               "\" is not a valid directory");
         }
         createSiteFolders(rootFolder, folders, request);
      }
   }
   
   /**
    * Builds a site folder hierarchy matching the file system structure starting
    * from <code>rootDir</code> downwards.
    * 
    * @param  rootDir a folder on the current file system, not 
    *    <code>null</code>.
    * @param  folderLocator a <code>PSLocator</code> for the parent folder.
    *    Should be set to <code>null</code> on the first call (recursively
    *    set afterwards). 
    * @param  request the request context used to create the folders, assumed
    *    not <code>null</code>.
    * @throws PSExtensionProcessingException for any error.
    */
   private void createSiteFolders(File rootDir, PSLocator folderLocator,
      IPSRequestContext request) throws PSExtensionProcessingException
   {
      if (rootDir == null)
         throw new PSExtensionProcessingException(0, 
            new IllegalArgumentException(
               "Root element parameter cannot be null").getMessage());
        
      // create a new folder
      PSLocator rootLocator = createFolder(rootDir.getName(), request);
      
      // add folder to parent
      addItemToFolder(rootLocator, folderLocator, request);

      // create sub-folders
      File[] moreFolders = rootDir.listFiles();
      for (int i = 0; i < moreFolders.length; i++)
      {
         if (moreFolders[i].isDirectory())
            createSiteFolders(moreFolders[i], rootLocator, request);
      }     
   }
   
   /**
    * Builds a site folder hierarchy from an XML file. The structure of the XML
    * file should be as follows:
    * <pre>
    * &lt;!ELEMENT folder (folder*)&gt;
    * &lt;!ATTLIST folder
    *    name CDATA #REQUIRED
    *    description CDATA #IMPLIED
    *    ... CDATA #IMPLIED
    * &gt;
    * </pre>
    * The code handles any attribute supplied, whether it makes sense or not.
    * <p>
    * <b>Note:</b>
    * the <code>name</code> attribute is required and is used as the folder 
    * name. Any other attributes are optional and are added as folder property
    * name/value pairs.
    * </p>
    * <p><b>Example document:</b></p>
    * <pre>
    * &lt;folder name="wwwroot" id="foo" description="IIS Root"&gt;
    *    &lt;folder name="aspnet_client"&gt;
    *       &lt;folder name="system_web"&gt;
    *          &lt;folder name="1_1_4322"/&gt;
    *       ;&lt;/folder&gt;
    *    &lt;/folder&gt;
    * &lt;/folder&gt;
    * </pre>
    * 
    * @param  folderElem a &lt;folder&gt; element in a JDOM document similar to
    *    as the above sample. Should be set to the document's root element
    *    on the first call (recursively set afterwards), never <code>null</code>
    *    after that.
    * @param  folderLocator a <code>PSLocator</code> for the parent folder.
    *    Should be set to <code>null</code> on the first call (recursively
    *    set afterwards). 
    * @param  request the request context used to create the folders, assumed
    *    not <code>null</code>.
    * @throws PSExtensionProcessingException for any error.
    */
   private void createSiteFolders(Element folderElem,
      PSLocator folderLocator, IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      if (folderElem == null)
         throw new PSExtensionProcessingException(0, 
            new IllegalArgumentException(
               "Root element parameter cannot be null").getMessage());
      
      // create a new folder
      String folderName = folderElem.getAttribute(NAME_ATTR);
      if (folderName == null || folderName.trim().length() == 0)
         throw new PSExtensionProcessingException(0, 
            new IllegalArgumentException(
               "Folder name cannot be null or empty").getMessage());

      PSLocator rootLocator = createFolder(folderName, request);
      String folderType = PSDbComponent.getComponentType(PSFolder.class);

      PSComponentProcessorProxy cProxy = getComponentProxyInstance(request);
      PSKey[] fKeys = new PSKey[1];
      PSComponentSummary cs = new PSComponentSummary(rootLocator.getId(),
         rootLocator.getRevision(), -1, -1, 
         PSComponentSummary.TYPE_FOLDER, folderName, 2, 1);
      fKeys[0] = (PSKey)cs.getCurrentLocator();
      PSFolder folder = null;
      Element[] items = null;
      try
      {
         items = cProxy.getProcessor(folderType).load(folderType, fKeys);
         folder = new PSFolder(items[0]);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSExtensionProcessingException(0, e);
      }
      catch (PSCmsException e)
      {
         throw new PSExtensionProcessingException(0, e);
      }
      
      // add folder attributes
      NamedNodeMap folderAttribs = folderElem.getAttributes();
      for (int i=0; folderAttribs != null && i<folderAttribs.getLength(); i++)
      {
         Attr attrib = (Attr) folderAttribs.item(i);
         logger.debug("\t" + attrib.getName() + "=" + attrib.getValue());
         
         // ignore the "name" attribute -- already used to create folder
         if (attrib.getName().equals(NAME_ATTR))
            continue;

         folder.setProperty(attrib.getName(), attrib.getValue());
      }
      
      try
      {
         cProxy.save(new IPSDbComponent[] {folder});
      }
      catch (PSCmsException e)
      {
         throw new PSExtensionProcessingException(0, e);
      }
      
      // add folder to parent
      addItemToFolder(rootLocator, folderLocator, request);

      // create sub-folders
      Node child = folderElem.getFirstChild();
      do
      {
         if (child instanceof Element)
            createSiteFolders((Element) child, rootLocator, request);
      } while ((child = child.getNextSibling()) != null);
   }

   // see interface for description
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // setup log4j
      if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
      {
         PropertyConfigurator.configureAndWatch(LOG4J_PROPERTIES);
      }
      
      logger.info("Initialized " + CLASSNAME);
   }

   /**
    * Create a new folder for the supplied name.
    * 
    * @param folderName the name of the new folder, assumed not 
    *    <code>null</code> or empty.
    * @param request the request used to create the folder, assumed not
    *    <code>null</code>.
    * @return the locator of the new created folder, never <code>null</code>.
    * @throws PSExtensionProcessingException for any error.
    */
   private PSLocator createFolder(String folderName, 
      IPSRequestContext request) throws PSExtensionProcessingException
   {
      // create a new folder, with admin permissions, visible to all communities
      PSFolder newFolder = new PSFolder(folderName, -1,
         PSObjectPermissions.ACCESS_ALL, "");

      newFolder.setDisplayFormatPropertyValue("0"); // default

      // save the new folder
      try
      {
         PSComponentProcessorProxy proxy = getComponentProxyInstance(request);
         PSSaveResults results = proxy.save(new IPSDbComponent[] {newFolder});
         newFolder = (PSFolder) results.getResults()[0];

         logger.debug("\tSuccessfully created folder '" + 
            newFolder.getName() + "'");

         PSLocator newFolderLocator = (PSLocator)newFolder.getLocator();
         return newFolderLocator;
      }
      catch (PSCmsException cme)
      {
         logger.fatal(cme);
         throw new PSExtensionProcessingException(0, cme);
      }
   }

   /**
    * Adds the specified item to the specified folder.
    *
    * @param itemLocator the locator of the item to be added, may be
    *    <code>null</code> in which case we default to the <code>Sites</code>
    *    folder.
    * @param folderLocator the locator of the folder to which the item is
    *    added, assumed not <code>null</code>.
    * @param request the request used for the relationship proxy, assumed 
    *    not <code>null</code>.
    * @throws PSExtensionProcessingException for any error.
    */
   private void addItemToFolder(PSLocator itemLocator, PSLocator folderLocator,
      IPSRequestContext request) throws PSExtensionProcessingException
   {
      // check parameters
      if (folderLocator == null)
         folderLocator = new PSLocator(2, -1); // default to Sites root

      // get a remote proxy for folder operations
      PSRelationshipProcessorProxy rProxy = getRelationshipProxyInstance(
         request);

      List locatorList = new ArrayList();
      locatorList.add(itemLocator);

      try
      {
         rProxy.add(PSRelationshipConfig.TYPE_FOLDER_CONTENT, locatorList, 
            folderLocator);
      }
      catch (PSCmsException cme)
      {
         logger.error("\tFailed to add item to folder. " +
            "proxy.getChildren() returned null");

         logger.error(CLASSNAME, cme);
         throw new PSExtensionProcessingException(0, cme);
      }
      catch (Exception e)
      {
         logger.fatal(e);
         throw new PSExtensionProcessingException(0, e);
      }
   }

   /**
    * Get the relationship processor.
    * 
    * @param request the request used to get the relationship processor,
    *    assumed not <code>null</code>.
    * @return the relationship processor, never <code>null</code>.
    * @throws PSExtensionProcessingException for any errors.
    */
   private synchronized PSRelationshipProcessorProxy getRelationshipProxyInstance(
      IPSRequestContext request) throws PSExtensionProcessingException
   {
      PSRelationshipProcessorProxy proxy = (PSRelationshipProcessorProxy) 
         request.getPrivateObject(RELATIONSHIP_PROXY);
      
      if (proxy == null)
      {
         try
         {
            proxy = new PSRelationshipProcessorProxy(
               PSProcessorProxy.PROCTYPE_SERVERLOCAL, request);

            request.setPrivateObject(RELATIONSHIP_PROXY, proxy);
            return proxy;
         }
         catch (PSCmsException cme)
         {
            throw new PSExtensionProcessingException(0, cme);
         }
      }
      
      return proxy;
   }
   
   /**
    * Get the component processor.
    * 
    * @param request the request used to get the component processor,
    *    assumed not <code>null</code>.
    * @return the component processor, never <code>null</code>.
    * @throws PSExtensionProcessingException for any errors.
    */
   private synchronized PSComponentProcessorProxy getComponentProxyInstance(
      IPSRequestContext request) throws PSExtensionProcessingException
   {
      PSComponentProcessorProxy proxy = (PSComponentProcessorProxy) 
      request.getPrivateObject(COMPONENT_PROXY);
   
      if (proxy == null)
      {
         try
         {
            proxy = new PSComponentProcessorProxy(
               PSProcessorProxy.PROCTYPE_SERVERLOCAL, request);

            request.setPrivateObject(COMPONENT_PROXY, proxy);
            return proxy;
         }
         catch (PSCmsException cme)
         {
            throw new PSExtensionProcessingException(0, cme);
         }
      }
      
      return proxy;
   }

   /**
    * The name of this class used for logging purposes.
    */
   private final String CLASSNAME = this.getClass().getName();
   
   /**
    * The log4j properties file.
    */
   private final String LOG4J_PROPERTIES = "log4j.properties";
   
   /**
    * The class specific logger.
    */
   public static Logger logger = Logger.getLogger(PSSiteFolderHelper.class);
   
   /**
    * The constant used to attach and retrieve the component proxy to a 
    * request as private object.
    */
   private static final String COMPONENT_PROXY = "PSComponentProcessorProxy";
   
   /**
    * The constant used to attach and retrieve the relationship proxy to a 
    * request as private object.
    */
   private static final String RELATIONSHIP_PROXY = 
      "PSRelationshipProcessorProxy";
   
   /**
    * The attribute name used for the name attribute in a folder XML.
    */
   private static final String NAME_ATTR = "name";
}
