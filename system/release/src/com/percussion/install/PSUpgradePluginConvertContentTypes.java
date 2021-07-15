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
package com.percussion.install;

import com.percussion.cms.objectstore.PSContentType;
import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationType;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.IOTools;
import com.percussion.util.PSCollection;
import com.percussion.util.PSSqlHelper;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * This plugin has been written to convert pre-6.0 content type applications to
 * the new 6.0 format.  Conversion will involve the following steps:
 * 
 * <li>
 * 1. Back-up original application.
 * </li>
 * <li>
 * 2. Find and extract all content editor resources, placing each in a new
 *    application named psx_ce[typename], where typename is the name of the 
 *    content type.  Runtime access will be maintained in the application acl,
 *    however, design access will be turned off for all entries other than the
 *    default user.  It will be ensured that a default user exists with design
 *    access.
 * </li>
 * <li>
 * 3. If the original application does not have any resources other than the 
 *    purge resource, remove it.
 * </li>
 * <li>
 * 4. If the original application does contain resources other than the purge
 *    resource, remove the purge resource and leave the application.
 * </li>
 * <li>
 * 5. Create and persist a new acl with the appropriate permissions to the
 *    database.
 * </li>
 * <li>
 * 6. For each content editor resource, add a mapping entry to the 
 *    rxconfig/Server/resourceMap.properties file which describes the request
 *    mappings for old -> new content editors.  The format of the entry is as
 *    follows:
 *    
 *    appname1/resource1=appname2/resource2
 * </li>
 * <li>
 * 7. Invalid url characters non-alphanumeric or "_"
 *    which are found in a content type name will be converted to underscore.
 * </li>
 * <li>
 * 8. If the previous application had a corresponding directory under Rhythmyx,
 *    create a directory for the newly created content editor application and
 *    copy all files from the original folder to the new folder.
 * </li> 
 */

public class PSUpgradePluginConvertContentTypes extends PSSpringUpgradePluginBase
{
   /**
    * Default constructor
    */
   public PSUpgradePluginConvertContentTypes()
   {
      super();
   }

   /**
    * Implements the process function of IPSUpgradePlugin.  Performs the tasks
    * described above.
    *
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      log("Converting Content Types");
      File appFile = null;
      String appFileName = "";
      PSPluginResponse response = null;
      FileOutputStream out = null;
            
      try
      {
         File objDir = RxUpgrade.getObjectStoreDir();
         File[] appFiles = objDir.listFiles();
         
         if (appFiles == null)
         {
            log("Error occurred accessing objectstore directory " +
                  objDir.getAbsolutePath());
            return response;
         }
         
         for(int i=0; i < appFiles.length; i++)
         {
            appFile = appFiles[i];
            appFileName = appFile.getName();
            
            if (appFile.isDirectory() || !appFileName.endsWith(".xml") ||
                  PSPreUpgradePluginLocalCreds.isSystemApp(appFileName))
               continue;
            if (RxUpgrade.isContentEditorApp(appFile, config.getLogStream()))
            {
               try
               {
                  convert(appFile);
               }
               catch (Exception e)
               {
                  log("Error occurred converting application " +
                        appFileName + ": " + e.getMessage());
               }
            }
         }
         
         if (m_resourceMapProps.size() > 0)
         {
            //Save the resource mappings to disk
            out = new FileOutputStream(RxUpgrade.getRxRoot() + RESOURCE_MAP);
            String header = "This file is for compatibility purposes and not " +
                            "meant for general use.  After you have converted " +
                            "anything (exits, Word template) that references " +
                            "the old content types, this file should be " +
                            "deleted.";
            m_resourceMapProps.store(out, header);
            
            log("Saved " + m_resourceMapProps.size() + " resource mappings to disk");
         }
      }
      catch(Exception e)
      {
         log("Exception caught " + e.getMessage());
         e.printStackTrace(config.getLogStream());
      }
      finally
      {
         try
         {
            if (out != null)
               out.close();
         }
         catch (IOException e)
         {}
      }
      
      log("Finished process() of the plugin Convert Content Types...");
      return response;
   }

   /**
    * Helper function that converts an application to the 6.0 format.
    *
    * @param appFile - pre-6.0 application file to convert, can not be 
    * <code>null</code>.
    */
   public static void convert(File appFile)
    throws Exception
   {
      FileInputStream oldIn = null;
      FileOutputStream oldOut = null;
      FileInputStream newIn = null;
      FileOutputStream newOut = null;
      Document oldDoc = null;
            
      try
      {
         oldIn = new FileInputStream(appFile);
         oldDoc = PSXmlDocumentBuilder.createXmlDocument(oldIn, false);
         
         //Load the application
         PSApplication app = new PSApplication(oldDoc);
                  
         String appName = app.getName();
         
         log("Converting application [" + appName + "]");
         
         PSCollection datasets = app.getDataSets();
         PSCollection datasets2 = new PSCollection(PSDataSet.class);
         PSCollection newDatasets = new PSCollection(PSDataSet.class);
         PSCollection transferAclEntries = new PSCollection(PSAclEntry.class);
         PSCollection newAclEntries = new PSCollection(PSAclEntry.class);
         PSCollection aclOwners = new PSCollection(PSAclEntry.class);
         PSContentEditor ce = null;
         PSDataSet purgeResource = null;
         int contentTypeId = 0;
         String name = "";
         
         //Deal with security
         PSAcl appAcl = app.getAcl();
         PSCollection aclEntries = appAcl.getEntries();
         PSAcl newAcl = new PSAcl();
         PSAclEntry entry;
         int accessLevel;
         
         //Create a default user entry with full runtime, design access
         PSAclEntry defaultEntry = new PSAclEntry(PSAclEntry.DEFAULT_USER_NAME,
               PSAclEntry.ACE_TYPE_USER);
         int defaultAccess = 0;
         defaultAccess |= PSAclEntry.AACE_DATA_CREATE;
         defaultAccess |= PSAclEntry.AACE_DATA_DELETE;
         defaultAccess |= PSAclEntry.AACE_DATA_QUERY;
         defaultAccess |= PSAclEntry.AACE_DATA_UPDATE;
         defaultAccess |= PSAclEntry.AACE_DESIGN_DELETE;
         defaultAccess |= PSAclEntry.AACE_DESIGN_READ;
         defaultAccess |= PSAclEntry.AACE_DESIGN_UPDATE;
         defaultAccess |= PSAclEntry.AACE_DESIGN_MODIFY_ACL;
         defaultEntry.setAccessLevel(defaultAccess);
         newAclEntries.add(defaultEntry);
         
         boolean ownerFound = false;
         //Add acl entries for transfer
         for (int i = 0; i < aclEntries.size(); i++)
         {
            entry = (PSAclEntry) aclEntries.get(i);
            accessLevel = entry.getAccessLevel();
                        
            //Check for owner, only need the first one
            if (!ownerFound && (accessLevel & PSAclEntry.AACE_DESIGN_MODIFY_ACL) ==
               PSAclEntry.AACE_DESIGN_MODIFY_ACL)
            {
               aclOwners.add(entry);
               ownerFound = true;
            }
            
            transferAclEntries.add(entry);
         }
         
         //Set new entries on new acl
         newAcl.setEntries(newAclEntries);
         
         //Save purge resource if it has any pre or post exits
         for (int i = 0; i < datasets.size(); i++)
         {
            Object obj = datasets.get(i);
            
            if (obj instanceof PSContentEditor)
               continue;
            else
            {
               PSDataSet dataSet = (PSDataSet) obj;
               PSRequestor requestor = dataSet.getRequestor();
               
               //check for purge resource
               if (requestor != null && 
                     requestor.getRequestPage().equalsIgnoreCase("purge"))
               {
                  //check for pre or post exits
                  PSPipe pipe = dataSet.getPipe();
                  if (pipe != null)
                  {
                     PSExtensionCallSet extInCallSet = pipe.getInputDataExtensions();
                     PSExtensionCallSet extResCallSet = pipe.getResultDataExtensions();
                     if ((extInCallSet != null && !extInCallSet.isEmpty()) ||
                           (extResCallSet != null && !extResCallSet.isEmpty()))
                     {
                        purgeResource = dataSet;
                        break;
                     }
                  }
               }
            }
         }
         
         //For each content editor in the application, create a new 6.0 app
         for (int i = 0; i < datasets.size(); i++)
         {
            Object obj = datasets.get(i);
            
            if (obj instanceof PSContentEditor)
            {
               ce = (PSContentEditor) obj;
               newDatasets = new PSCollection(PSDataSet.class);
               
               //Add purge resource if found
               if (purgeResource != null)
                  newDatasets.add(purgeResource);
               
               //Get the content type name
               contentTypeId = (int) ce.getContentType();
               String label = findContentTypeName(contentTypeId);
                                                          
               if (label.trim().length() == 0)
               {
                  log("Could not convert application " +  appName + 
                        ", invalid content type id [" + contentTypeId + "]");
               }
               else
               {
                  //Remove spaces from name
                  name = StringUtils.deleteWhitespace(label); 
                  
                  //Ensure unique, valid name
                  name = getValidContentTypeName(name, appFile);
                  
                  //Ensure request page matches content type name
                  PSRequestor ceRequestor = ce.getRequestor();
                  
                  String requestPage = "";
                  if (ceRequestor != null)
                  {
                     //Save the original for resource mapping
                     requestPage = ceRequestor.getRequestPage();
                     
                     ceRequestor.setRequestPage(name);
                  }
                  
                  //Add content editor
                  newDatasets.add(ce);
                  
                  try 
                  {
                     //Create new application
                     createApp(appFile, oldDoc, name, newDatasets, newAcl);
                     
                     //Update CONTENTTYPES table
                     updateRequestColumns(contentTypeId, label, name);
                     
                     //Create and persist new acl
                     createAcl(contentTypeId, transferAclEntries, aclOwners);
                     
                     //Add request mapping entry
                     addResourceMapping(app.getRequestRoot(), name, requestPage);
                  }
                  catch (Exception e)
                  {
                     log("Conversion failed due to the " +
                           "following exception: " + e.getMessage());
                     e.printStackTrace(m_config.getLogStream());
                  }
               }
            }
            else
            {
               PSDataSet dataSet = (PSDataSet) obj;
               PSRequestor requestor = dataSet.getRequestor();
               
               if (requestor != null && 
                     !requestor.getRequestPage().equalsIgnoreCase("purge"))
               {
                  //keep only non-purge, non-content editor resources
                  datasets2.add(dataSet);
               }
            }
         }
         
         //Remove content editor(s) from original app, delete if no other resources
         if (datasets2.size() > 0)
         {
            app.setDataSets(datasets2);
            oldOut = new FileOutputStream(appFile.getAbsolutePath());
            PSXmlDocumentBuilder.write(app.toXml(), oldOut);
         }
         else
            appFile.delete();
       
         log("Converted application [" + appName + "]");
      }
      finally
      {
         if (oldIn != null)
            oldIn.close();
         
         if (newIn != null)
            newIn.close();
         
         if (oldOut != null)
            oldOut.close();
         
         if (newOut != null)
            newOut.close();
      }
      
      
   }
   
   /**
    * Helper function to create a new 6.0 application.  This method will write
    * the new application to disk.  This method also checks to see if the
    * original application has a corresponding folder under the Rhythmyx root
    * for various text files.  If so, a new folder will be created for the new
    * application and the contents of the original folder will be copied to the
    * new location.
    * 
    * @param appFile the original pre-6.0 application file, assumed not <code>null</code>.
    * @param appDoc the original pre-6.0 application document, assumed not <code>null</code>.
    * @param name the content type name of the new application, assumed not <code>null</code>.
    * @param dataSets the datasets of the new application, assumed not <code>null</code>.
    * @param acl the acl of the new application, assumed not <code>null</code>
    */
   private static void createApp(File appFile, Document appDoc, String name, 
         PSCollection dataSets, PSAcl acl) throws Exception
   {
      String newName = PSContentType.createAppName(name);
      
      log("Creating new application [" + newName + "]");
      
      File newAppFile = new File(appFile.getParent(), newName + ".xml");
      FileOutputStream newOut = new FileOutputStream(newAppFile);
      PSApplication newApp = new PSApplication(appDoc);
    
      newApp.setName(newName);
      newApp.setRequestRoot(newName);
      newApp.setApplicationType(PSApplicationType.CONTENT_EDITOR);
      newApp.setDataSets(dataSets);
      newApp.setAcl(acl);
      
      PSXmlDocumentBuilder.write(newApp.toXml(), newOut);
      
      newOut.close();
      
      String origName = appFile.getName();
      origName = origName.substring(0, origName.lastIndexOf(".xml"));
      String rxRoot = RxUpgrade.getRxRoot();
      File origAppDir = new File(rxRoot, origName);
      if (origAppDir.exists())
      {
         File newAppDir = new File(rxRoot, newName);
         newAppDir.mkdir();
         
         log("Copying contents of " + origAppDir.getAbsolutePath()
               + " to " + newAppDir.getAbsolutePath());
         
         File[] origFiles = origAppDir.listFiles();
         for (int i = 0; i < origFiles.length; i++)
            IOTools.copyToDir(origFiles[i], newAppDir);
      }
   }
   
   /**
    * Helper function to locate content type name for a given content type id
    * 
    * @param contentTypeId the content type id, assumed not <code>null</code>
    * @return the name which corresponds to this content type id or empty if one
    * could not be found
    */
   private static String findContentTypeName(int contentTypeId)
    throws Exception
   {
      Connection conn = RxUpgrade.getJdbcConnection();
      PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(RxUpgrade.getRxRepositoryProps());
      
      String qualTableName = 
         PSSqlHelper.qualifyTableName("CONTENTTYPES", dbmsDef.getDataBase(), 
               dbmsDef.getSchema(), dbmsDef.getDriver());
      
      String queryStmt = "SELECT " + qualTableName + ".CONTENTTYPENAME " + 
                         "FROM " + qualTableName + " WHERE " +
                         qualTableName + ".CONTENTTYPEID=" + contentTypeId;
      
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(queryStmt);
      String name = "";
      
      if (rs.next())
         name = rs.getString("CONTENTTYPENAME");
      
      return name;
   }
   
   /**
    * Helper function to ensure a unique content type name that is valid for
    * urls and will result in a unique application name.  The name will also be
    * added to the set of unique, valid content type names.
    * 
    * @param contentTypeName the content type name, assumed not <code>null</code>
    * @param appFile the original pre-6.0 application file, assumed not <code>null</code>
    * @return a unique content type name which is valid for urls and results in
    * a unique application name
    */
   private static String getValidContentTypeName(String contentTypeName, 
         File appFile)
    throws Exception
   {
      boolean altered = false;
      
      // get the valid content type name
      String ctName = PSStringUtils.makeValidContentTypeName(contentTypeName);
      
      if (!ctName.equals(contentTypeName))
      {
         // the content type name has been altered, must make sure it is unique
         ctName = getUniqueContentTypeName(ctName);
         altered = true;
      }
      
      File newAppFile = new File(appFile.getParent(),
            PSContentType.createAppName(ctName) + ".xml");
      
      while (newAppFile.exists())
      {
         ctName += '_';
         ctName = getUniqueContentTypeName(ctName);
         newAppFile = new File(appFile.getParent(),
               PSContentType.createAppName(ctName) + ".xml");
         altered = true;
      }
      
      if (altered)
         log("Converting content type name from " + contentTypeName + " to "
               + ctName);
      
      m_contentNames.add(ctName);
      
      return ctName;
   }
   
   /**
    * Helper function to ensure a unique content type name.  Once a unique name
    * is found, it will be added to the set of content type names.
    * 
    * @param contentTypeName the content type name with all whitespace removed,
    * assumed not <code>null</code>
    * @return a unique name will be created by appending '_' to the name
    */
   private static String getUniqueContentTypeName(String contentTypeName)
    throws Exception
   {
      String ctName = contentTypeName;
      
      while (m_contentNames.contains(ctName))
         ctName += "_";
             
      return ctName;
   }
    
   /**
    * Helper function to update new and query request columns for a given content
    * type id with the new application name.  This function also updates the label
    * column with the content type name.  The name column is updated as well.
    * 
    * @param contentTypeId the content type id, assumed not <code>null</code>
    * @param contentTypeLabel the label of the content type, assumed not <code>null</code>
    * @param contentTypeName the name of the content type, assumed not <code>null</code>
    */
   private static void updateRequestColumns(int contentTypeId, String contentTypeLabel,
         String contentTypeName)
    throws Exception
   {
      log("Updating request urls, label, and name for content type [" + contentTypeLabel + "]");
      
      Connection conn = RxUpgrade.getJdbcConnection();
      PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(RxUpgrade.getRxRepositoryProps());
      
      String qualTableName = 
         PSSqlHelper.qualifyTableName("CONTENTTYPES", dbmsDef.getDataBase(), 
               dbmsDef.getSchema(), dbmsDef.getDriver());
      
      String cLabel = contentTypeLabel.replaceAll("'", "''");
      String cName = contentTypeName.replaceAll("'", "''");
            
      String request = PSContentType.createRequestUrl(cName);
            
      String updateStmt = "UPDATE " + qualTableName + " SET " +
                          qualTableName + ".CONTENTTYPENEWREQUEST='" + request + "', " +
                          qualTableName + ".CONTENTTYPEQUERYREQUEST='" + request + "', " +
                          qualTableName + ".CONTENTTYPELABEL='" + cLabel + "', " +
                          qualTableName + ".CONTENTTYPENAME='" + cName + "' " +
                          " WHERE " + qualTableName + ".CONTENTTYPEID=" + contentTypeId;
      
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(updateStmt);
   }
   
   /**
    * Helper function to create an acl for the node definition represented by the
    * given content type id.  Uses hibernate services to accomplish this.
    * 
    * @param contentTypeId the content type id, assumed not <code>null</code>
    * @param aclEntries the acl entries for this new acl, assumed not <code>null</code>
    * @param aclOwners the owner(s) of this new acl, assumed not <code>null</code>
    */
   private static void createAcl(int contentTypeId, PSCollection aclEntries,
         PSCollection aclOwners)
    throws Exception
   {  
      if (aclEntries.size() == 0 && aclOwners.size() == 0)
         return;
      
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      List<IPSAcl> aclList = new ArrayList<>();
            
      log("Creating acl for content type [" + contentTypeId + "]");
      
      //Load acl, if it already exists
      PSGuid aclGuid = new PSGuid(PSTypeEnum.NODEDEF, contentTypeId);
      PSAclImpl aclImpl = 
         (PSAclImpl) aclService.loadAclForObjectModifiable(aclGuid);
      PSTypedPrincipal principal;
      
      if (aclImpl == null)
      {
         //Must create a new acl
         if (aclOwners.size() == 0)
         {
            principal = new PSTypedPrincipal(PSAclEntry.DEFAULT_USER_NAME,
                  PrincipalTypes.USER);
            aclImpl = (PSAclImpl) aclService.createAcl(aclGuid, principal);
            
            PSAclEntryImpl defaultOwner = 
               (PSAclEntryImpl) aclImpl.findEntry(principal);
            
            defaultOwner.addPermission(PSPermissions.READ);
            defaultOwner.addPermission(PSPermissions.UPDATE);
            defaultOwner.addPermission(PSPermissions.DELETE);
            
            log("Created ACL with " + defaultOwner.getName() + " owner");
         }
         else
         { 
            PSAclEntry owner = (PSAclEntry) aclOwners.get(0);
            PrincipalTypes type = getPrincipalType(owner);
            
            principal = new PSTypedPrincipal(owner.getName(), type);
            aclImpl = (PSAclImpl) aclService.createAcl(aclGuid, principal);
            
            log("Created ACL with " + owner.getName() + " owner");
         }
      }
      else
         principal = (PSTypedPrincipal) aclImpl.getFirstOwner();
      
      //Add/update entries
      for (int i = 0; i < aclEntries.size(); i++)
      {
         PSAclEntry entry = (PSAclEntry) aclEntries.get(i);
         PrincipalTypes type = getPrincipalType(entry);
                  
         PSTypedPrincipal newPrincipal = new PSTypedPrincipal(entry.getName(), type);
         int accessLevel = entry.getAccessLevel();
         
         PSAclEntryImpl newEntry = (PSAclEntryImpl) aclImpl.findEntry(newPrincipal);
         
         if (newEntry == null)
            newEntry = (PSAclEntryImpl) aclImpl.createEntry(newPrincipal);
         
         String newEntryName = newEntry.getName();
         
         if ((accessLevel & PSAclEntry.AACE_DESIGN_DELETE) == PSAclEntry.AACE_DESIGN_DELETE)
         {
            newEntry.addPermission(PSPermissions.DELETE);
            log("Added DESIGN DELETE to " + newEntryName);
         }
         else
            newEntry.removePermission(PSPermissions.DELETE);
         if ((accessLevel & PSAclEntry.AACE_DESIGN_READ) == PSAclEntry.AACE_DESIGN_READ)
         {
            newEntry.addPermission(PSPermissions.READ);
            log("Added DESIGN READ to " + newEntryName);
         }
         else
            newEntry.removePermission(PSPermissions.READ);
         if ((accessLevel & PSAclEntry.AACE_DESIGN_UPDATE) == PSAclEntry.AACE_DESIGN_UPDATE)
         {
            newEntry.addPermission(PSPermissions.UPDATE);
            log("Added DESIGN UPDATE to " + newEntryName);
         }
         else
            newEntry.removePermission(PSPermissions.UPDATE);
         if ((accessLevel & PSAclEntry.AACE_DESIGN_MODIFY_ACL) == PSAclEntry.AACE_DESIGN_MODIFY_ACL)
         {
            newEntry.addPermission(PSPermissions.OWNER);
            log("Added OWNER to " + newEntryName);
         }
         else
         {
            //Only remove owner privileges if it is not the default user entry
            if (!entry.isUser() || !entry.getName().equalsIgnoreCase(PSAclEntry.DEFAULT_USER_NAME))
               newEntry.removePermission(PSPermissions.OWNER);
         }
         
         aclImpl.addEntry(principal, newEntry);
      }
           
      aclList.add(aclImpl);
      aclService.saveAcls(aclList);
   }
   
   /**
    * Helper function to add a resource mapping entry to the resource map 
    * properties.  The entry will be in the form
    *  appname1/resource1=appname2/resource2.
    *
    * @param reqRoot the original application request root
    * @param name the content type name of the new application, assumed not 
    * <code>null</code>
    * @param requestPage the request page of the original application,
    * assumed not <code>null</code> 
    */
   private static void addResourceMapping(String reqRoot, String name,
         String requestPage)
    throws Exception
   {
      if (StringUtils.isEmpty(requestPage))
      {
         log("Request page is empty, a resource mapping will not be added");
         return;
      }
      
      String newUrl = PSContentType.createRequestUrl(name);
      
      //remove ../ prefix and extension from request
      String newResource = newUrl.substring(
            PSContentType.CE_URL_PREFIX.length(), newUrl.lastIndexOf("."));
      String oldResource = reqRoot + '/' + requestPage;
      
      log("Adding resource mapping " + oldResource + "=" + newResource);
      
      m_resourceMapProps.setProperty(oldResource, newResource);
   }
   
   /**
    * Helper function to determine the principal type of a <code>PSAclEntry<code>
    * object.
    * 
    * @param aclEntry The acl entry object, assumed not <code>null</code>
    * @return The principal type of this acl entry in the form of a
    * <code>PrincipalTypes<code> object.
    */
   private static PrincipalTypes getPrincipalType(PSAclEntry aclEntry)
   {
      PrincipalTypes type = PrincipalTypes.UNDEFINED;
      
      if (aclEntry.isUser())
         type = PrincipalTypes.USER;
      else if (aclEntry.isGroup())
         type = PrincipalTypes.GROUP;
      else if (aclEntry.isRole())
         type = PrincipalTypes.ROLE;
      
      return type;
   }
     
   /**
    * Prints message to the log printstream if it exists
    * or just sends it to System.out
    *
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private static void log(String msg)
   {
      if (msg == null)
      {
         return;
      }

      if (m_config != null)
      {
         m_config.getLogStream().println(msg);
      }
      else
      {
         System.out.println(msg);
      }
   }
   
   /**
    * Relative location of resource mapping file,
    * rxconfig/Server/resourceMap.properties
    */
   private static final String RESOURCE_MAP = 
      "rxconfig" + File.separator + "Server" + File.separator + "resourceMap.properties";
   
   /**
    * Resource mapping properties storage. 
    */   
   private static Properties m_resourceMapProps = new Properties();
   
   /**
    * Content type name storage.
    */
   private static Set m_contentNames = new HashSet();
   
   private static IPSUpgradeModule m_config;
      
      
}
