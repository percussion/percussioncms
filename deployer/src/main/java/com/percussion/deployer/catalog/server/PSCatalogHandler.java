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
package com.percussion.deployer.catalog.server;

import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.catalog.PSCatalogResultColumn;
import com.percussion.deployer.catalog.PSCatalogResultSet;
import com.percussion.deployer.catalog.PSCataloger;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSArchiveSummary;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.deployer.objectstore.PSLogSummary;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyManager;
import com.percussion.deployer.server.PSDeploymentHandler;
import com.percussion.deployer.server.PSLogHandler;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.datasource.PSDatasourceMgrLocator;
import com.percussion.util.PSFilenameFilter;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * The handler to handle all deploy catalog requests. Please see {@link 
 * com.percussion.deployer.catalog.PSCataloger PSCataloger} for description of
 * supported request types and results.
 */
public class PSCatalogHandler 
{
   /**
    * Processes the catalog request. This uses the XML document sent as the
    * input data. The results are written to the xml document. 
    *
    * @param request the request object containing the input document for 
    * catalog request, may not be <code>null</code>
    * 
    * @return the document with cataloged results, never <code>null</code>
    * @throws PSDeployException if an error happens processing the request.
    */
   public static Document processRequest(PSRequest request)
      throws PSDeployException
   {
      Document doc = request.getInputDocument();
      Element root = null;
      if (doc == null || (root = doc.getDocumentElement()) == null )
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }

      /* verify this is the appropriate request type */
      String requestTag = root.getTagName();
      int length = PSCataloger.ROOT_PREFIX.length(); 
      if( !requestTag.startsWith(PSCataloger.ROOT_PREFIX) ||
         !PSCataloger.ms_supportedReqTypes.contains(
         requestTag.substring(length)) ) 
      {
         throw new PSDeployException(IPSDeploymentErrors.INVALID_REQUEST_TYPE, 
            root.getTagName());         
      }      

      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      Properties props = new Properties();
      NodeList children = doc.getDocumentElement().getChildNodes();
      for (int i = 0; i < children.getLength(); i++) 
      {
         Node child = children.item(i);
         if(child instanceof Element)
         {
            props.setProperty(((Element)child).getTagName(), 
               tree.getElementData(child));         
         }
      }

      if(ms_depHandler == null)
      {
         ms_depHandler = PSDeploymentHandler.getInstance();
         ms_bundle = PSDeploymentManager.getBundle();
      }
      
      PSCatalogResultSet resultSet = null;
      String requestType = requestTag.substring(length);      
      if(requestType.equals(PSCataloger.TYPE_REQ_ARCHIVES))
      {
         resultSet = catalogArchives(props);
      }
      else if(requestType.equals(PSCataloger.TYPE_REQ_DATASOURCES))
      {
         resultSet = catalogDataSources(props);
      }
      else if(requestType.equals(PSCataloger.TYPE_REQ_CUSTOM_TYPES))
      {
         resultSet = catalogCustomElementTypes(props);
      }
      else if(requestType.equals(PSCataloger.TYPE_REQ_DEPLOY_TYPES))
      {
         resultSet = catalogDeployableElementTypes(props);
      }
      else if(requestType.equals(PSCataloger.TYPE_REQ_DESCRIPTORS))
      {
         resultSet = catalogDescriptors(props);
      }
      else if(requestType.equals(PSCataloger.TYPE_REQ_LITERAL_ID_TYPES))
      {
         resultSet = catalogLiteralIDTypes(props);
      }
      else if(requestType.equals(PSCataloger.TYPE_REQ_TYPE_OBJECTS))
      {
         resultSet = catalogIDTypeObjects(request.getSecurityToken(), props);
      }     
      else if(requestType.equals(PSCataloger.TYPE_REQ_PACKAGELOGS))
      {
         resultSet = catalogPackageLogs(props);
      }     
      else if(requestType.equals(PSCataloger.TYPE_REQ_USER_DEP))
      {
         resultSet = catalogUserDependencies(props);
      }    
             
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      root = resultSet.toXml(respDoc);
      PSXmlDocumentBuilder.replaceRoot(respDoc, root);      
      return respDoc;
   }
   
   /**
    * Catalogs for the archive logs. The returned result set will have columns.
    * The following describes the column metadata(Definition).
    * <table border=1>
    * <tr><th>Column Name</th><th>Column Type</th></tr>
    * <tr><th>Name</th><th>text</th></tr>
    * <tr><th>Installed Date</th><th>date</th></tr>
    * <tr><th>Source Server</th><th>text</th></tr>
    * <tr><th>Version</th><th>text</th></tr>
    * <tr><th>Build</th><th>text</th></tr>
    * </table>
    * Each result will have archive log id as id, and archive name as display 
    * text. Please see {@link com.percussion.deployer.catalog#PSCatalogResult 
    * PSCatalogResult} and {@link 
    * com.percussion.deployer.catalog#PSCatalogResultColumn PSCatalogResultColumn}
    * for more information on column types and identifiers.
    * 
    * @param props the additional parameters to use for the request, may be 
    * <code>null</code>, ignored for this request.
    * 
    * @return the result set, never <code>null</code>
    * 
    * @throws PSDeployException if an error happens cataloging.
    */
   private static PSCatalogResultSet catalogArchives(Properties props)  
      throws PSDeployException 
   {
      PSLogHandler logHandler = ms_depHandler.getLogHandler();
      String serverName = 
         PSServer.getHostName() + ":" + PSServer.getListenerPort();     
      Iterator logs = logHandler.getArchiveSummaries(serverName);
      PSCatalogResultSet resultSet = new PSCatalogResultSet(getArchiveColumns());         
      
      while(logs.hasNext())
      {
         PSArchiveSummary summary = (PSArchiveSummary)logs.next();
         PSArchiveInfo info = summary.getArchiveInfo();

         File archiveFile = ms_depHandler.getImportArchiveFile(
            info.getArchiveRef());
         if (archiveFile.exists())
         {
            PSCatalogResult result = new PSCatalogResult(
               String.valueOf(summary.getId()), info.getArchiveRef());
            result.addTextColumn(info.getArchiveRef());
            result.addDateColumn(summary.getInstallDate().getTime());
            result.addTextColumn(info.getServerName());
            result.addTextColumn(info.getServerVersion());
            result.addTextColumn(info.getServerBuildNumber());         
            resultSet.addResult(result);
         }
      }      
      return resultSet;
   }
   
   /**
    * Gets the columns for the archives catalog request. Caches the columns 
    * list.
    * 
    * @return the columns, never <code>null</code> or empty.
    */
   private static PSCatalogResultColumn[] getArchiveColumns()
   {
      if(ms_archiveColumns == null)
      {
         ms_archiveColumns = new PSCatalogResultColumn[5];
         ms_archiveColumns[0] = new PSCatalogResultColumn(
            ms_bundle.getString("name"), 
            PSCatalogResultColumn.TYPE_TEXT);
         ms_archiveColumns[1] = new PSCatalogResultColumn(
            ms_bundle.getString("installDate"), 
            PSCatalogResultColumn.TYPE_DATE);      
         ms_archiveColumns[2] = new PSCatalogResultColumn(
            ms_bundle.getString("source"), 
            PSCatalogResultColumn.TYPE_TEXT);   
         ms_archiveColumns[3] = new PSCatalogResultColumn(
            ms_bundle.getString("version"), 
            PSCatalogResultColumn.TYPE_TEXT);                  
         ms_archiveColumns[4] = new PSCatalogResultColumn(
            ms_bundle.getString("build"), 
            PSCatalogResultColumn.TYPE_TEXT);         
      }
      return ms_archiveColumns;
   }
   

   
   /**
    * Catalogs for the active datasources. The returned result set will not have 
    * columns. Each result will have datasource name as id and display text. 
    * Please see 
    * {@link com.percussion.deployer.catalog#PSCatalogResult PSCatalogResult} 
    * and {@link 
    * com.percussion.deployer.catalog#PSCatalogResultColumn PSCatalogResultColumn}
    * for more information on column types and identifiers.
    * 
    * @param props the additional parameters to use for the request, may be 
    * <code>null</code>, ignored for this request.
    * 
    * @return the result set, never <code>null</code>
    * 
    * @throws PSDeployException if an error happens cataloging.
    */   
   private static PSCatalogResultSet catalogDataSources(Properties props)
      throws PSDeployException    
   {      
      PSCatalogResultSet dataSources = new PSCatalogResultSet();

      IPSDatasourceManager dsMgr = PSDatasourceMgrLocator.getDatasourceMgr();
      Iterator<String> it = dsMgr.getDatasources().iterator();
      while(it.hasNext())
      {
         String dsName = it.next();
         PSCatalogResult res = new PSCatalogResult(dsName, dsName);
         dataSources.addResult(res);
      }
      return dataSources;
   }

   
   /**
    * Catalogs for the custom element types. The returned result set will not  
    * have columns. Each result will have element type as id and display text. 
    * Please see {@link com.percussion.deployer.catalog#PSCatalogResult 
    * PSCatalogResult} and {@link 
    * com.percussion.deployer.catalog#PSCatalogResultColumn PSCatalogResultColumn}
    * for more information on column types and identifiers.
    * 
    * @param props the additional parameters to use for the request, may be 
    * <code>null</code>, ignored for this request.
    * 
    * @return the result set, never <code>null</code>
    * 
    * @throws PSDeployException if an error happens cataloging.
    */  
   private static PSCatalogResultSet catalogCustomElementTypes(Properties props)
      throws PSDeployException    
   {
      PSDependencyManager mgr = ms_depHandler.getDependencyManager();
      Iterator customTypes = mgr.getCustomElementTypes();
      PSCatalogResultSet customElemTypes = new PSCatalogResultSet();
      while(customTypes.hasNext())
      {
         PSDependencyDef def = (PSDependencyDef)customTypes.next();
         PSCatalogResult result = new PSCatalogResult(def.getObjectType(), 
            def.getObjectTypeName());
         customElemTypes.addResult(result);
      }
      return customElemTypes;
   }
   
   /**
    * Catalogs for the deployable element types. The returned result set will  
    * not have columns. Each result will have element type as id and display 
    * text. Please see {@link com.percussion.deployer.catalog#PSCatalogResult 
    * PSCatalogResult} and {@link 
    * com.percussion.deployer.catalog#PSCatalogResultColumn PSCatalogResultColumn}
    * for more information on column types and identifiers.
    * 
    * @param props the additional parameters to use for the request, may be 
    * <code>null</code>, ignored for this request.
    * 
    * @return the result set, never <code>null</code>
    * 
    * @throws PSDeployException if an error happens cataloging.
    */  
   private static PSCatalogResultSet catalogDeployableElementTypes(
      Properties props)
      throws PSDeployException       
   {
      PSDependencyManager mgr = ms_depHandler.getDependencyManager();
      Iterator elemTypes = mgr.getElementTypes();
      PSCatalogResultSet deplElemTypes = new PSCatalogResultSet();
      while(elemTypes.hasNext())
      {
         PSDependencyDef def = (PSDependencyDef)elemTypes.next();
         PSCatalogResult result = new PSCatalogResult(def.getObjectType(), 
            def.getObjectTypeName());
         deplElemTypes.addResult(result);
      }
      return deplElemTypes;
   }
   
   /**
    * Catalogs for the descriptors. The returned result set will have columns.
    * The following describes the column metadata(Definition).
    * <table border=1>
    * <tr><th>Column Name</th><th>Column Type</th></tr>
    * <tr><th>Name</th><th>text</th></tr>
    * <tr><th>Description</th><th>text</th></tr>
    * <tr><th>Last Modified Date</th><th>date</th></tr>
    * <tr><th>version</th><th>text</th></tr>
    * </table>
    * Each result will have descriptor name as id and as display text. Please 
    * see {@link com.percussion.deployer.catalog#PSCatalogResult 
    * PSCatalogResult} and {@link 
    * com.percussion.deployer.catalog#PSCatalogResultColumn PSCatalogResultColumn}
    * for more information on column types and identifiers.
    * 
    * @param props the additional parameters to use for the request, may be 
    * <code>null</code>, ignored for this request.
    * 
    * @return the result set, never <code>null</code>
    * 
    * @throws PSDeployException if an error happens cataloging.
    */
   private static PSCatalogResultSet catalogDescriptors(Properties props)
      throws PSDeployException    
   {
      PSCatalogResultSet descriptors = 
         new PSCatalogResultSet(getDescriptorColumns());
         
      try {
         File exportDescDir = PSDeploymentHandler.EXPORT_DESC_DIR;      
         if(exportDescDir.exists())
         {
            File[] descFiles = exportDescDir.listFiles(
               new PSFilenameFilter("xml", false));
            if(descFiles != null)
            {
               for (int i = 0; i < descFiles.length; i++) 
               {
                  Document descDoc = PSDeploymentHandler.getDocumentFromFile(
                     descFiles[i], "export descriptor");
                  PSExportDescriptor descriptor = new PSExportDescriptor(
                     descDoc.getDocumentElement(), true);            
                  PSCatalogResult result = 
                     new PSCatalogResult(descriptor.getName(), descriptor.getName());
                  result.addTextColumn(descriptor.getName());
                  result.addTextColumn(descriptor.getDescription()); 
                  result.addDateColumn(descFiles[i].lastModified());
                  result.addTextColumn(descriptor.getVersion()); 
                  descriptors.addResult(result);   
               }
            }
         }
      }
      catch(PSUnknownNodeTypeException ex)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            ex.getLocalizedMessage());      
      }
      return descriptors;
   }
   
   /**
    * Gets the columns for the descriptors catalog request. Caches the columns 
    * list.
    * 
    * @return the columns, never <code>null</code> or empty.
    */
   private static PSCatalogResultColumn[] getDescriptorColumns()
   {
      if(ms_descriptorColumns == null)
      {
         ms_descriptorColumns = new PSCatalogResultColumn[4];
         ms_descriptorColumns[0] = new PSCatalogResultColumn(
            ms_bundle.getString("name"), 
            PSCatalogResultColumn.TYPE_TEXT);
         ms_descriptorColumns[1] = new PSCatalogResultColumn(
            ms_bundle.getString("description"), 
            PSCatalogResultColumn.TYPE_TEXT);      
         ms_descriptorColumns[2] = new PSCatalogResultColumn(
            ms_bundle.getString("modifiedDate"), 
            PSCatalogResultColumn.TYPE_DATE);
         ms_descriptorColumns[3] = new PSCatalogResultColumn(
            ms_bundle.getString("version"), 
            PSCatalogResultColumn.TYPE_TEXT);       
      }
      return ms_descriptorColumns;
   }
   
   /**
    * Catalogs for the literal ID types (The types that are used to represent 
    * the objects having ids). The returned result set will not have columns. 
    * Each result will have id type as id and display text. Please see {@link 
    * com.percussion.deployer.catalog#PSCatalogResult PSCatalogResult} and {@link 
    * com.percussion.deployer.catalog#PSCatalogResultColumn PSCatalogResultColumn}
    * for more information on column types and identifiers.
    * 
    * @param props the additional parameters to use for the request, may be 
    * <code>null</code>, ignored for this request.
    * 
    * @return the result set, never <code>null</code>
    * 
    * @throws PSDeployException if an error happens cataloging.
    */  
   private static PSCatalogResultSet catalogLiteralIDTypes(Properties props)
      throws PSDeployException    
   {
      PSDependencyManager mgr = ms_depHandler.getDependencyManager();
      Iterator idTypes = mgr.getObjectTypes();
      PSCatalogResultSet literalIDTypes = new PSCatalogResultSet();
      while(idTypes.hasNext())
      {
         PSDependencyDef def = (PSDependencyDef)idTypes.next();
         if(def.supportsIdMapping())
         {
            PSCatalogResult result = new PSCatalogResult(def.getObjectType(), 
               def.getObjectTypeName());
            literalIDTypes.addResult(result);
         }
      }
      return literalIDTypes;   
   }
   
   /**
    * Catalogs for the objects with specified type. The returned result 
    * set will not have columns. Each result will have object id as id and name 
    * as display text. Please see {@link 
    * com.percussion.deployer.catalog#PSCatalogResult PSCatalogResult} and {@link 
    * com.percussion.deployer.catalog#PSCatalogResultColumn PSCatalogResultColumn}
    * for more information on column types and identifiers.
    * 
    * @param props the additional parameters to use for the request, may not be 
    * <code>null</code>.
    * 
    * @return the result set, never <code>null</code>
    * 
    * @throws PSDeployException if an error happens cataloging or required 
    * property is missing.
    */  
   private static PSCatalogResultSet catalogIDTypeObjects(PSSecurityToken tok, 
      Properties props) throws PSDeployException    
   {
      if(props == null || props.getProperty("type") == null ||
         ((String)props.getProperty("type")).trim().length() == 0)
         throw new PSDeployException(
            IPSDeploymentErrors.CATALOG_REQD_PROP_NOT_SPECIFIED, "type");
                        
      String type = (String)props.getProperty("type");
      PSCatalogResultSet typeObjects = new PSCatalogResultSet();      
      Iterator objects = 
         ms_depHandler.getDependencyManager().getDependencies(tok, type); 
      while(objects.hasNext())
      {
         PSDependency object = (PSDependency)objects.next();
         PSCatalogResult result = new PSCatalogResult(object.getDependencyId(), 
            object.getDisplayName());
         typeObjects.addResult(result);
      }
      
      return typeObjects;         
   }
   
   /**
    * Catalogs for the installed package logs. The returned result set will have
    * columns. The following describes the column metadata(Definition).
    * <table border=1>
    * <tr><th>Column Name</th><th>Column Type</th></tr>
    * <tr><th>Name</th><th>text</th></tr>
    * <tr><th>Package Type</th><th>text</th></tr>
    * <tr><th>Description</th><th>text</th></tr>
    * <tr><th>Archive Name</th><th>text</th></tr>
    * <tr><th>Installed Date</th><th>date</th></tr>
    * <tr><th>Source Server</th><th>text</th></tr>
    * <tr><th>Version</th><th>text</th></tr>
    * <tr><th>Build</th><th>text</th></tr>
    * </table>
    * Each result will have descriptor name as id and as display text. Please 
    * see {@link com.percussion.deployer.catalog#PSCatalogResult 
    * PSCatalogResult} and {@link 
    * com.percussion.deployer.catalog#PSCatalogResultColumn PSCatalogResultColumn}
    * for more information on column types and identifiers.
    * 
    * @param props the additional parameters to use for the request, may be 
    * <code>null</code>, ignored for this request.
    * 
    * @return the result set, never <code>null</code>
    * 
    * @throws PSDeployException if an error happens cataloging.
    */
   private static PSCatalogResultSet catalogPackageLogs(Properties props)
      throws PSDeployException    
   {
      PSLogHandler logHandler = ms_depHandler.getLogHandler();   
      String serverName = 
         PSServer.getHostName() + ":" + PSServer.getListenerPort();             
      Iterator logs = logHandler.getLogSummaries(serverName);
      PSCatalogResultSet resultSet = 
         new PSCatalogResultSet(getPackageLogColumns());         
      
      while(logs.hasNext())
      {
         PSLogSummary log = (PSLogSummary)logs.next();
         PSArchiveSummary summary = log.getArchiveSummary();
         PSArchiveInfo info = summary.getArchiveInfo();
         PSCatalogResult result = new PSCatalogResult(
            String.valueOf(log.getId()), log.getPackage().getDisplayName());
         result.addTextColumn(log.getPackage().getDisplayName());
         result.addTextColumn(log.getPackage().getObjectTypeName());         
         result.addTextColumn(log.getPackage().getDescription());                  
         result.addTextColumn(info.getArchiveRef());                           
         result.addDateColumn(summary.getInstallDate().getTime());
         result.addTextColumn(info.getServerName());
         result.addTextColumn(info.getServerVersion());
         result.addTextColumn(info.getServerBuildNumber());         
         resultSet.addResult(result);
      }      
      return resultSet;
   }
   
   /**
    * Gets the columns for the package logs catalog request. Caches the columns 
    * list.
    * 
    * @return the columns, never <code>null</code> or empty.
    */
   private static PSCatalogResultColumn[] getPackageLogColumns()
   {
      if(ms_packageLogColumns == null)
      {
         ms_packageLogColumns = new PSCatalogResultColumn[8];
         ms_packageLogColumns[0] = new PSCatalogResultColumn(
            ms_bundle.getString("name"), 
            PSCatalogResultColumn.TYPE_TEXT);
         ms_packageLogColumns[1] = new PSCatalogResultColumn(
            ms_bundle.getString("packageType"), 
            PSCatalogResultColumn.TYPE_TEXT);     
         ms_packageLogColumns[2] = new PSCatalogResultColumn(
            ms_bundle.getString("description"), 
            PSCatalogResultColumn.TYPE_TEXT); 
         ms_packageLogColumns[3] = new PSCatalogResultColumn(
            ms_bundle.getString("archiveName"), 
            PSCatalogResultColumn.TYPE_TEXT);                                               
         ms_packageLogColumns[4] = new PSCatalogResultColumn(
            ms_bundle.getString("installDate"), 
            PSCatalogResultColumn.TYPE_DATE);      
         ms_packageLogColumns[5] = new PSCatalogResultColumn(
            ms_bundle.getString("source"), 
            PSCatalogResultColumn.TYPE_TEXT);   
         ms_packageLogColumns[6] = new PSCatalogResultColumn(
            ms_bundle.getString("version"), 
            PSCatalogResultColumn.TYPE_TEXT);                  
         ms_packageLogColumns[7] = new PSCatalogResultColumn(
            ms_bundle.getString("build"), 
            PSCatalogResultColumn.TYPE_TEXT);         
      }
      return ms_packageLogColumns;
   }
   
   /**
    * Catalogs for the user dependency files relative to rhythmyx root. The 
    * returned result set will not have columns. Each result will have 
    * dependency file/directory path as id and the file/directory name as the 
    * display text. If it is a directory the id string will end with a file 
    * separator. If the 'directory' property is supplied it catalogs for the 
    * files/directories under the specified directory. The directory should be
    * the relative path to rhythtmyx root. Please see {@link 
    * com.percussion.deployer.catalog#PSCatalogResult PSCatalogResult} and {@link 
    * com.percussion.deployer.catalog#PSCatalogResultColumn PSCatalogResultColumn}
    * for more information on column types and identifiers.
    * 
    * @param props the additional parameters to use for the request, if <code>
    * null</code> catalogs for the files and directories under rhythmyx root.
    * 
    * @return the result set, never <code>null</code>
    * 
    * @throws PSDeployException if an error happens cataloging.
    */ 
   private static PSCatalogResultSet catalogUserDependencies(Properties props)
      throws PSDeployException    
   {
      String directory = null;
      if(props != null && props.getProperty("directory") != null)
         directory = props.getProperty("directory");
      
      String curDirectory = PSServer.getRxDir().getAbsolutePath();
      File catalogDir;
      if(directory != null)
         catalogDir = new File(curDirectory, directory);
      else
         catalogDir = new File(curDirectory);
         
      if(!catalogDir.isDirectory() || !catalogDir.exists())
       throw new PSDeployException(
            IPSDeploymentErrors.CATALOG_INVALID_DIRECTORY_SPECIFIED, 
               new String[]{directory});

      PSCatalogResultSet resultSet = new PSCatalogResultSet();             
      File[] dirFiles = catalogDir.listFiles();
      for (int i = 0; i < dirFiles.length; i++) 
      {
         File file = dirFiles[i];
         String path = file.getPath().substring(curDirectory.length()+1);      
         if(file.isDirectory())
            path += IPSDeployConstants.CAT_FILE_SEP;
         PSCatalogResult result = 
            new PSCatalogResult(path, dirFiles[i].getName());
         resultSet.addResult(result);
      }
      
      return resultSet;
   }
      
   /**
    * The deployment handler that is used to collect the data for the catalog
    * request, initialized when the first catalog request is made and never
    * <code>null</code> or modified after that.
    */
   private static PSDeploymentHandler ms_depHandler;
   
   /**
    * The resource bundle to use to get the column names for catalog requests,
    * initialized in <code>processRequest(PSRequest)</code> when first request 
    * is made and never <code>null</code> or modified after that.
    */
   private static ResourceBundle ms_bundle;      
   
   /**
    * The list of result columns for catalog archive request, initialized in 
    * <code>getArchiveColumns()</code> when first catalog archive request is 
    * made and never <code>null</code>, empty or modified after that.
    */
   private static PSCatalogResultColumn[] ms_archiveColumns;
   
   /**
    * The list of result columns for catalog package logs request, initialized  
    * in <code>getPackageLogColumns()</code> when first catalog package log 
    * request is made and never <code>null</code>, empty or modified after that.
    */   
   private static PSCatalogResultColumn[] ms_packageLogColumns;   

   /**
    * The list of result columns for catalog descriptors request, initialized in 
    * <code>getDescriptorColumns()</code> when first catalog descriptors request
    * is made and never <code>null</code>, empty or modified after that.
    */   
   private static PSCatalogResultColumn[] ms_descriptorColumns;   
}
