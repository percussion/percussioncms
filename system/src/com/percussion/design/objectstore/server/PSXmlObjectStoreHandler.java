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

package com.percussion.design.objectstore.server;

import com.percussion.conn.PSServerException;
import com.percussion.content.IPSMimeContent;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.data.PSDatabaseMetaData;
import com.percussion.data.PSMetaDataCache;
import com.percussion.data.jdbc.PSFileSystemDriver;
import com.percussion.data.vfs.IPSVirtualDirectory;
import com.percussion.data.vfs.PSVirtualApplicationDirectory;
import com.percussion.deploy.server.PSServerJdbcDbmsDef;
import com.percussion.design.objectstore.*;
import com.percussion.design.objectstore.legacy.*;
import com.percussion.error.PSErrorManager;
import com.percussion.error.PSException;
import com.percussion.error.PSRuntimeException;
import com.percussion.extension.*;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerWarning;
import com.percussion.security.*;
import com.percussion.server.*;
import com.percussion.server.config.PSConfigManager;
import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.services.security.data.PSCatalogerConfig;
import com.percussion.tablefactory.*;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSDocVersionConverter2;
import com.percussion.util.PSProperties;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.container.jboss.PSJBossJndiDatasource;
import com.percussion.utils.jdbc.*;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.spring.IPSBeanConfig;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.naming.NamingException;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * The PSXmlObjectStoreHandler class implements XML file object store
 * access for the IPSObjectStoreHandler interface. It allows the E2 server
 * to access the objects from the file system as XML files.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
@SuppressWarnings(value={"unchecked"})
public class PSXmlObjectStoreHandler extends PSObjectFactory
   implements IPSObjectStoreHandler, IPSObjectStoreErrors, IPSValidateSession
{
   /**
    * Constructs a PSXmlObjectStore object, using the specified directory
    * as the object store.
    * <p>
    * <em>NOTE:</em> Only the E2 server should perform this operation.
    * <p>
    * The following connection information should be provided:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>objectDirectory</td>
    *      <td>the directory containing the XML files</td>
    * </tr>
    * </table>
    * 
    * @param      connInfo                     the object store definition
    * 
    * @return the connected object store
    *
    * @todo: make all request root string public and reuse them.
    */
   public PSXmlObjectStoreHandler(java.util.Properties connInfo)
   {
      super();

      if (connInfo == null)
         throw new IllegalArgumentException("Handler properties null");

      String objectDir = (String)connInfo.get(PROP_OBJECT_DIR);
      if (objectDir == null)
         throw new IllegalArgumentException("Handler properties invalid");

      objectDir = PSServer.getRxFile(objectDir);
      
      m_objectDirectory = new File(objectDir);
      if (!m_objectDirectory.isDirectory()) {
         throw new IllegalArgumentException("Handler objectdir invalid: " + 
            m_objectDirectory.toString());
      }
   }

   /**
    * Initializes the object store, returning all applications (both
    * enabled and disabled) that are defined in the object store.
    *
    * @return   PSApplication[]
    *
    * @throws   PSServerException
    * @throws   PSAuthorizationException
    */
   public synchronized PSApplication[] init()
      throws PSServerException,
         PSAuthorizationException
   {
      File lockDir = null;
      try
      {
         // create the locks directory if it does not exist
         lockDir = new File(m_objectDirectory, ".rxlocks");
         lockDir.mkdirs();
         m_lockMgr = new PSXmlObjectStoreLockManager(lockDir);
      }
      catch (IOException ioe)
      {
         String fileName = "";
         if (lockDir != null)
            fileName = lockDir.getName();

         String arg1 = ioe.toString() + " in PSXmlObjectStoreHandler/init";
         Object[] args = { fileName, arg1 };
         throw new PSServerException(HANDLER_IO_ERROR, args);
      }

      PSServerConfiguration conf = getServerConfigurationObject();
      PSLogger logger = conf.getLogger();
      m_LogHandler = new com.percussion.log.PSLogHandler(logger);

      /* build the hash of request handler methods */
      m_requestHandlerMethods = new HashMap();

      // set up legacy converters
      PSComponent.setComponentConverters(createComponentConverters());
      
      // set the legacy updaters
      PSComponent.setComponentUpdaters(createComponentUpdaters());
      
      PSApplication[] apps = getApplicationObjects(false);

      uniquifyAppIds(apps); // after this, apps are sorted by ID

      for (int i = 0; i < apps.length; i++)
      {
         PSApplication app = apps[i];
         // if and only if the app has a non-empty request root, we add a
         // virtual directory mapping for it
         String reqRoot = app.getRequestRoot();
         if (reqRoot != null && reqRoot.length() > 0)
         {
            try
            {
               addVirtualAppDirectory(app.getRequestRoot(), 
                     new File(PSServer.getRxDir(), app.getRequestRoot()));
            }
            catch (IllegalArgumentException ex)
            {
               Object[] args = new Object[] { app.getName(), 
                  ex.getLocalizedMessage() };
               PSLogManager.write(new PSLogServerWarning(APP_LOAD_EXCEPTION,
                  args, true, "ObjectStore"));
               continue;
            }
         }
      } // end loop over all apps

      m_appSums = new PSApplicationSummaryCollection();

      synchronized (m_appSums)
      {
         for (int i = 0; i < apps.length; i++)
         {
            PSApplication app = apps[i];
            m_appSums.addSummary(app, false);
            PSApplicationSummary sum = m_appSums.getSummary(app.getId());
            sum.setFileLastModified(getApplicationFile(
               app.getName()).lastModified());
            PSAcl acl = app.getAcl();
            if (acl != null)
            {
               PSAclHandler aclHandler = new PSAclHandler(acl);
               sum.setAclHandler(aclHandler);
            }

         }
      }

      Class myClass = this.getClass();
//      Method[] aMethod = myClass.getMethods();
//      for (int i=0; i<aMethod.length; i++)
//         System.out.println(aMethod[i].getName());

      try {
         Class[] xmlClass = { org.w3c.dom.Document.class,
                              com.percussion.server.PSRequest.class };

         m_requestHandlerMethods.put(   "design-objectstore-app-lock",
                                       new PSSecuredMethod(
                                             myClass.getMethod(
                                             "extendApplicationLock", xmlClass),
                                             PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-app-load",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "getApplication", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-app-list",
                                       new PSSecuredMethod(
                                       myClass.getMethod(
                                          "getApplicationSummaries", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-app-remove",
                                       new PSSecuredMethod(
                                       myClass.getMethod(
                                          "removeApplication", xmlClass),
                                          PSAclEntry.SACE_DELETE_APPLICATIONS));

         m_requestHandlerMethods.put(   "design-objectstore-app-rename",
                                       new PSSecuredMethod(
                                       myClass.getMethod(
                                          "renameApplication", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-app-save",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "saveApplication", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-userconfig-load",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "getUserConfiguration", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-userconfig-remove",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "removeUserConfiguration", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-userconfig-save",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "saveUserConfiguration", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-serverconfig-load",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "getServerConfiguration", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-serverconfig-save",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "saveServerConfiguration", xmlClass),
                                          PSAclEntry.SACE_ADMINISTER_SERVER));

         m_requestHandlerMethods.put(   "design-objectstore-serverroles-load",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "getRoleConfiguration", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-serverroles-save",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "saveRoleConfiguration", xmlClass),
                                          PSAclEntry.SACE_ADMINISTER_SERVER));

         m_requestHandlerMethods.put(   "design-objectstore-serverconfig-lock",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "extendServerConfigurationLock", 
                                          xmlClass),
                                          PSAclEntry.SACE_ADMINISTER_SERVER));

         m_requestHandlerMethods.put(   "design-objectstore-app-file-save",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "saveApplicationFile", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-app-file-load",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "loadApplicationFile", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put(   "design-objectstore-app-file-remove",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "removeApplicationFile", xmlClass),
                                          PSAclEntry.SACE_DELETE_APPLICATIONS));
         
         m_requestHandlerMethods.put(   "design-objectstore-app-file-rename",
                                        new PSSecuredMethod(
                                        myClass.getMethod(
                                        "renameApplicationFile", xmlClass),
                                        PSAclEntry.SACE_DELETE_APPLICATIONS));

         m_requestHandlerMethods.put("design-objectstore-characterset-map-load",
                                       new PSSecuredMethod(
                                          myClass.getMethod(
                                          "getCharacterSetMap", xmlClass),
                                          PSAclEntry.SACE_ACCESS_DESIGN));

         m_requestHandlerMethods.put( "design-objectstore-extension-save",
            new PSSecuredMethod(myClass.getMethod("saveExtension",
            xmlClass), PSAclEntry.SACE_ACCESS_DESIGN));
         m_requestHandlerMethods.put( "design-objectstore-extension-load",
            new PSSecuredMethod(myClass.getMethod("loadExtension",
            xmlClass), PSAclEntry.SACE_ACCESS_DESIGN));
         m_requestHandlerMethods.put( "design-objectstore-extension-remove",
            new PSSecuredMethod(myClass.getMethod("removeExtension",
            xmlClass), PSAclEntry.SACE_ACCESS_DESIGN));
         m_requestHandlerMethods.put( "design-objectstore-featureset-load",
            new PSSecuredMethod(myClass.getMethod("getSupportedFeatureSet",
            xmlClass), PSAclEntry.SACE_ACCESS_DESIGN));
         m_requestHandlerMethods.put("design-objectstore-tabledefinitions-save",
            new PSSecuredMethod(myClass.getMethod("saveTableDefinitions",
            xmlClass), PSAclEntry.SACE_ACCESS_DESIGN));
         m_requestHandlerMethods.put( "design-objectstore-cesystemdef-load",
               new PSSecuredMethod(
                  myClass.getMethod("getContentEditorSystemDef", xmlClass), 
                  PSAclEntry.SACE_ACCESS_DESIGN));
         m_requestHandlerMethods.put( "design-objectstore-ceshareddef-load",
               new PSSecuredMethod(myClass.getMethod(
                  "getContentEditorSharedDef", xmlClass), 
                  PSAclEntry.SACE_ACCESS_DESIGN));
         m_requestHandlerMethods.put( "design-objectstore-rxconfig-load",
               new PSSecuredMethod(myClass.getMethod("getRxConfiguration",
               xmlClass), PSAclEntry.SACE_ACCESS_DESIGN));
         m_requestHandlerMethods.put( "design-objectstore-rxconfig-save",
               new PSSecuredMethod(myClass.getMethod("saveRxConfiguration",
               xmlClass), PSAclEntry.SACE_ACCESS_DESIGN));
         m_requestHandlerMethods.put("design-objectstore-app-list-files",
            new PSSecuredMethod(myClass.getMethod("getApplicationFiles",
               xmlClass), PSAclEntry.SACE_ACCESS_DESIGN));
         m_requestHandlerMethods.put("design-objectstore-datasource-conndetail",
            new PSSecuredMethod(myClass.getMethod("getConnectionDetail",
               xmlClass), PSAclEntry.SACE_ACCESS_DESIGN));
         m_requestHandlerMethods.put("design-objectstore-jndidatasources-load",
            new PSSecuredMethod(myClass.getMethod("getJndiDatasources",
               xmlClass), PSAclEntry.SACE_ADMINISTER_SERVER));
         m_requestHandlerMethods.put("design-objectstore-jndidatasources-save",
            new PSSecuredMethod(myClass.getMethod("saveJndiDatasources",
               xmlClass), PSAclEntry.SACE_ADMINISTER_SERVER));
         m_requestHandlerMethods.put(
            "design-objectstore-catalogerconfigs-load", new PSSecuredMethod(
               myClass.getMethod("getCatalogerConfigs", xmlClass),
               PSAclEntry.SACE_ADMINISTER_SERVER));
         m_requestHandlerMethods.put(
            "design-objectstore-catalogerconfigs-save", new PSSecuredMethod(
               myClass.getMethod("saveCatalogerConfigs", xmlClass),
               PSAclEntry.SACE_ADMINISTER_SERVER));  
         
         m_requestHandlerMethods.put(
            "design-objectstore-datasourceconfigs-load", new PSSecuredMethod(
               myClass.getMethod("getDatasourceConfigs", xmlClass),
               PSAclEntry.SACE_ADMINISTER_SERVER));
         m_requestHandlerMethods.put(
            "design-objectstore-datasourceconfigs-save", new PSSecuredMethod(
               myClass.getMethod("saveDatasourceConfigs", xmlClass),
               PSAclEntry.SACE_ADMINISTER_SERVER));  
         m_requestHandlerMethods.put(
            "design-objectstore-hibernatedialects-load", new PSSecuredMethod(
               myClass.getMethod("getHibernateDialectConfig", xmlClass),
               PSAclEntry.SACE_ADMINISTER_SERVER));
         m_requestHandlerMethods.put(
            "design-objectstore-hibernatedialects-save", new PSSecuredMethod(
               myClass.getMethod("saveHibernateDialectConfig", xmlClass),
               PSAclEntry.SACE_ADMINISTER_SERVER));      } 
      catch (Exception e) 
      {
         throw new PSRuntimeException(HANDLER_UNEXPECTED_EXCEPTION, 
            e.toString());
      }

      return apps;
   }

   /**
    * Creates the required component converters.
    * 
    * @return The list of converters, never <code>null</code> or empty.
    * 
    * @throws PSServerException If there are any errors. 
    */
   private List<IPSComponentConverter> createComponentConverters() 
      throws PSServerException
   {
      List<IPSComponentConverter> converters = 
         new ArrayList<IPSComponentConverter>();
      
      IPSConfigFileLocator fileLocator = new PSOsConfigFileLocator(
         getServerConfigFile());
      
      IPSRepositoryInfo info = new PSOsRepositoryInfo();
         
      PSConfigurationCtx ctx; 
      try
      {
         ctx = new PSConfigurationCtx(fileLocator, PSServer.getPartOneKey());        
      }
      catch (Exception e)
      {
         throw new PSServerException(e);
      }
         
      converters.add(new PSBackendTableConverter(ctx, info, false));
      converters.add(new PSTableLocatorConverter(ctx, false));
      
      return converters;
   }

   /**
    * Creates the required component updaters.
    * 
    * @return The list of updaters, never <code>null</code> or empty.
    * 
    */
   private List<IPSComponentUpdater> createComponentUpdaters()
   {
      List<IPSComponentUpdater> updaters = 
         new ArrayList<IPSComponentUpdater>();
      
      // CM1RXdiff
	  // updaters.add(new PSContentTypeWorkflowsUpdater());
      updaters.add(new PSAllowAllCtypeWorkflowsUpdater());
      
      return updaters;
   }

   protected void uniquifyAppIds(PSApplication[] allApps)
   {

      // we want to sort the apps by ID
      class IdSorter implements Comparator<Object>
      {
         public final int compare(Object o1, Object o2)
         {
            PSApplication a = (PSApplication)o1;
            PSApplication b = (PSApplication)o2;
            return a.getId() - b.getId();
         }

         @Override
         public final boolean equals(Object o)
         {
            //FB: NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT NC 1-17-16
            if(o==null)
               return false;
            
            return compare(this, o) == 0;
         }

         @Override
         public int hashCode()
         {
            // dummy hash code
            return 0;
         }
      }

      IdSorter idSort = new IdSorter();

      // sort the apps by ID (ascending)
      Arrays.sort(allApps, idSort);

      final int len = allApps.length;

      for (int i = 0; i < len; i++)
      {
         PSApplication app = allApps[i];

         // see if this app needs a unique ID...
         int appId = app.getId();

         int nextId = (len > (i+1)) ? allApps[i+1].getId() : -1 ;

         // does this app need a unique ID... ?
         if (appId < 1 || appId == nextId)
         {
            // search for new IDs, starting at id = 1
            int newId = 0;
            boolean taken = true;
            while (taken)
            {
               newId++;
               // start at beginning, looking for unused ids
               // note that we have to start at the beginning,
               // because we might have violated the sorting order
               // of the subarray in [0,i-1] by assinging a new ID
               // to one of the apps at index < i
               for (int j = 0; j < len; j++)
               {
                  if (allApps[j].getId() == newId)
                     break; // taken, go to next id
                  else if (j == (len - 1))
                     taken = false; // untaken
               }
            }

            com.percussion.server.PSConsole.printMsg(
               "ObjectStore",
               "Assigning unique ID " + newId + " to application \""
               + app.getName() + "\"");

            // update the app object in memory
            app.setId(newId);

            // update the app object on disk
            try
            {
               Document appDoc = loadApplication(app.getName());
               Element appEl = appDoc.getDocumentElement();
               if (appEl != null)
               {
                  appEl.setAttribute("id", "" + newId);

                  // finally, write it to the disk file
                  File appFile = getApplicationFile(app.getName());
                  OutputStream fout =
                     new BufferedOutputStream(lockOutputStream(appFile));
                  try
                  {
                     PSXmlDocumentBuilder.write(appDoc, fout);
                  }
                  finally
                  {
                     releaseOutputStream(fout, appFile);
                  }
               }
               else
               {
                  PSException ex = new PSUnknownDocTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_NULL, "PSXApplication");

                  Object[] args = new Object[] { app.getName(), ex.toString() };
                  PSLogManager.write(new PSLogServerWarning(APP_LOAD_EXCEPTION,
                     args, true, "ObjectStore"));
               }
            }
            catch (IOException ioe)
            {
               Object[] args = new Object[]
               {app.getName(), ioe.toString()};
               PSLogManager.write(new PSLogServerWarning(APP_LOAD_EXCEPTION,
                  args, true, "ObjectStore"));
            }
            catch (PSNotFoundException e)
            {
               Object[] args = new Object[]
               {app.getName(), e.toString()};
               PSLogManager.write(new PSLogServerWarning(APP_LOAD_EXCEPTION,
                  args, true, "ObjectStore"));
            }
            catch (PSServerException e)
            {
               Object[] args = new Object[]
               {app.getName(), e.toString()};
               PSLogManager.write(new PSLogServerWarning(APP_LOAD_EXCEPTION,
                  args, true, "ObjectStore"));
            }
         } // end if needs renumbering
      } // end loop over all apps
   }

   /**
    * Add a listener for changes to application objects.
    *
    * @param listener     the listener object
    *
    * @throws PSAuthorizationException if the user is not permitted to
    *                                       listen for the specified events
    */
   public void addApplicationListener(IPSApplicationListener listener)
      throws PSAuthorizationException
   {
      m_AppListenerVector.add(listener);
   }

   /**
    * Remove a previously installed application event listener.
    *
    * @param listener     the listener object
    */
   public void removeApplicationListener(IPSApplicationListener listener)
   {
      m_AppListenerVector.remove(listener);
   }

   /**
    * Add a listener for changes to server objects.
    *
    * @param  listener    the listener object
    *
    * @throws   PSAuthorizationException   if the user is not permitted to
    *                                             listen for the specified events
    */
   public void addServerListener(IPSServerConfigurationListener listener)
      throws PSAuthorizationException
   {
      m_SrvListenerVector.add(listener);
   }

   /**
    * Remove a previously installed server event listener.
    *
    * @param listener     the listener object
    */
   public void removeServerListener(IPSServerConfigurationListener listener)
   {
      m_SrvListenerVector.remove(listener);
   }

   /**
    * Extend the write lock on an application. Write locks are granted for a
    * maximum of 30 minutes. If the designer needs more time to complete the
    * task, an additional 30 minute extension can be requested.
    *
    * @param      inDoc                        the XML document containing the
    *                                          application data
    *
    *   @param      req                        the request context
    *                                          (for security)
    *
    * @return                                 the XML response document
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSAuthorizationException      if the user is not permitted to
    *                                          create applications on the
    *                                          server
    *
    * @throws PSLockedException            if another user has acquired the
    *                                          application lock. This usually
    *                                          occurs if the application was
    *                                          not previously locked or the
    *                                          lock was lost due to a timeout.
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                          appropriate format for this
    *                                          request type
    *
    * @throws   PSNotFoundException         if an application be that name
    *                                          does not exist
    *
    * @see         com.percussion.design.objectstore.PSApplication
    */
   public Document extendApplicationLock(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException,
         PSLockedException, PSUnknownDocTypeException, PSNotFoundException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootExtendAppLock);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootExtendAppLock);

      //make sure we got the correct root node tag
      if (false == ms_RootExtendAppLock.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootExtendAppLock, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      /* build the response doc */
      try {   /* and process the request */
         PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);

         int applid = Integer.parseInt(walker.getElementData("applid"));

         // how many minutes do we lock for (max of 30, min of 0, default of 30)
         // 0 means unlock
         String lockMinsStr = walker.getElementData("lockMins");
         if (lockMinsStr == null)
            lockMinsStr = "30";
         int lockMins = Integer.parseInt(lockMinsStr);
         if (lockMins > 30 || lockMins < 0)
         {
            lockMins = 30;
         }
         
         String uniqueId = walker.getElementData(ATTR_UNIQUEID);

         boolean overrideSameUser = false;
         String over = walker.getElementData("overrideSameUser");
         if (over != null && over.equals("yes"))
            overrideSameUser = true;

         /* Check security to see if this is allowed */
         if (!checkApplicationSecurity(applid, PSAclEntry.AACE_DESIGN_UPDATE, req)
               &&
             !checkApplicationSecurity(applid, PSAclEntry.AACE_DESIGN_DELETE, req))
         {
            throw new PSAuthorizationException( "lockApplication",
               getApplicationNameFromId(applid), req.getUserSessionId());
         }
            extendApplicationLock(req, applid, uniqueId, lockMins, overrideSameUser);

            Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.createRoot(respDoc, "PSXDesignAppLockResults");
            return respDoc;
      } catch (Exception e) {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Constructs an application object for the specified application. The
    * application is loaded from the object store when this method is called.
    * If the application is being loaded for editing, be sure to lock the
    * application.
    *
    * @param      inDoc                        the XML document containing the
    *                                          application data
    *
    *   @param      req                        the request context
    *                                          (for security)
    *
    * @return                                 the XML response document
    *
    * @throws   PSServerException            if the server is not responding
    *
    * @throws   PSAuthorizationException   if user does not have designer
    *                                          access to the application
    *
    * @throws   PSLockedException            if <CODE>mode</CODE> is <code>edit</code>
    *                                          but another user already has the
    *                                          application locked
    *
    * @throws   PSNotFoundException         if an application be that name
    *                                          does not exist
    *
    * @throws   PSUnknownDocTypeException   if doc does not contain the
    *                                          appropriate format for this
    *                                          request type
    *
    * @see         com.percussion.design.objectstore.PSApplication
    */
   public Document getApplication(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException,
               PSLockedException, PSNotFoundException,
               PSUnknownDocTypeException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppLoad);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppLoad);

      //make sure we got the correct root node tag
      if (false == ms_RootAppLoad.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootAppLoad, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);

      String app = walker.getElementData("name");
      if (app == null) {
         int applid = (new Integer(walker.getElementData("applid"))).intValue();

         app = getApplicationNameFromId(applid);
      }
      String uniqueId = walker.getElementData(ATTR_UNIQUEID);

      // if the app is to be opened in edit mode, attempt to acquire a lock on it
      boolean lockApp = false;
      boolean overrideSameUser = false;
      IPSLockerId lockId = null;
      boolean isLocked = false;
      String mode = walker.getElementData("mode");

      if (mode == null)
      {
         lockApp = false;
      }
      else if (mode.equals("edit"))
      {
         lockApp = true;
         String over = walker.getElementData("overrideSameUser");
         if (over != null && over.equals("yes"))
            overrideSameUser = true;
      }

      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      try
      {
         Document respDoc;
         if(!lockApp)
         {
            respDoc = os.getApplicationDoc(app, req.getSecurityToken());
         }
         else
         {
            lockId = getEffectiveLockerId(req, uniqueId, overrideSameUser);
            os.getApplicationLock(lockId, app, 30);
            isLocked = true;
            respDoc = os.getApplicationDoc(lockId, app, req.getSecurityToken());
         }


         root = respDoc.getDocumentElement();   /* and shift it down */
         Element newRoot = respDoc.createElement("PSXDesignAppLoadResults");
         PSXmlDocumentBuilder.swapRoot(respDoc, newRoot);

         return respDoc;
      }
      catch (Exception e)
      {
         // if locked, we need to release it
         try
         {
            if (isLocked)
               os.releaseApplicationLock(lockId, app);
         }
         catch(Exception ex){/* do nothing - this should always work */}

         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Creates an enumeration containing the requested properties of each
    * application for which the user has designer access.
    * <p>
    * The application properties which can be retrieved are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>name</td>
    *      <td>the application name</td>
    * </tr>
    * <tr><td>description</td>
    *      <td>the application's description</td>
    * </tr>
    * <tr><td>isEnabled</td>
    *      <td>is the application currently enabled</td>
    * </tr>
    * <tr><td>isActive</td>
    *      <td>is the application currently active</td>
    * </tr>
    * <tr><td>createdBy</td>
    *      <td>the name of the user who created the application</td>
    * </tr>
    * <tr><td>createdOn</td>
    *      <td>the date the application was created</td>
    * </tr>
    * <tr><td>lockerName</td>
    *     <td>the name of the locker, null if not locked</td>
    * </tr>
    * <tr><td>lockerSession</td>
    *     <td>the session id of the locker, null if not locked</td>
    * </tr>
    * </table>
    *
    * @param      inDoc                           the XML document containing the
    *                                          application data
    *
    *   @param      req                        the request context
    *                                          (for security)
    *
    * @return                                 the XML response document
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                          appropriate format for this
    *                                          request type
    */
   public Document getApplicationSummaries(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException,
               PSUnknownDocTypeException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppList);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppList);

      //make sure we got the correct root node tag
      if (false == ms_RootAppList.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootAppList, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      String hiddenAtt = root.getAttribute("showHidden");
      req.setShowHiddenApplicationSetting((hiddenAtt != null) &&
                                           hiddenAtt.equals("yes"));

      String appName = root.getAttribute("appName");
      boolean isAppSpecified = !StringUtils.isBlank(appName);
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      
      boolean bGetName = false;
      boolean bGetDesc = false;
      boolean bGetEnabled = false;
      boolean bGetActive = false;
      boolean bGetCreatedBy = false;
      boolean bGetCreatedOn = false;
      boolean bIsEmpty = false;
      boolean bAppType = false;
      boolean bId = false;
      boolean bLockerName = false;
      boolean bLockerSession = false;
      
      PSXmlTreeWalker   walker         = new PSXmlTreeWalker(inDoc);
      String            sTemp;
      while (walker.getNextElement("columnName", true, true) != null) {
         sTemp = walker.getElementData((Element)walker.getCurrent());
         if (sTemp.equalsIgnoreCase("name"))
            bGetName = true;
         else if (sTemp.equalsIgnoreCase("description"))
            bGetDesc = true;
         else if (sTemp.equalsIgnoreCase("isEnabled"))
            bGetEnabled = true;
         else if (sTemp.equalsIgnoreCase("isActive"))
            bGetActive = true;
         else if (sTemp.equalsIgnoreCase("createdBy"))
            bGetCreatedBy = true;
         else if (sTemp.equalsIgnoreCase("createdOn"))
            bGetCreatedOn = true;
         else if (sTemp.equalsIgnoreCase("isEmpty"))
            bIsEmpty = true;
         else if (sTemp.equalsIgnoreCase("appType"))
            bAppType = true;
         else if (sTemp.equalsIgnoreCase("id"))
            bId = true;
         else if (sTemp.equalsIgnoreCase("lockerName"))
            bLockerName = true;
         else if (sTemp.equalsIgnoreCase("lockerSession"))
            bLockerSession = true;
      }

      /* build the response doc */
      Document   respDoc = PSXmlDocumentBuilder.createXmlDocument();
      root = PSXmlDocumentBuilder.createRoot(
         respDoc, "PSXDesignAppListResults");

      boolean showHiddenApps = req.showHiddenApplications();

      try {
         synchronized (m_appSums)
         {
            PSApplicationSummary[] sums = m_appSums.getSummaries();

            for (int i = 0; i < sums.length; i++)
            {
               PSApplicationSummary sum = sums[i];
               if (sum.isHidden() && !showHiddenApps)
                  continue;
               if (isAppSpecified && !appName.equals(sum.getName()))
                  continue;

               if (checkApplicationSecurity(sum.getName(), PSAclEntry.AACE_DESIGN_READ, req))
               {
                  Element baseNode = PSXmlDocumentBuilder.addEmptyElement(
                                          respDoc, root, "PSXApplicationSummary");

                  if (bGetName)
                     PSXmlDocumentBuilder.addElement(
                        respDoc, baseNode, "name", sum.getName());
                  if (bGetDesc)
                     PSXmlDocumentBuilder.addElement(
                        respDoc, baseNode, "description", sum.getDescription());
                  if (bGetEnabled)
                     PSXmlDocumentBuilder.addElement(
                        respDoc, baseNode, "isEnabled", ((sum.isEnabled()) ? "yes" : "no"));
                  if (bGetActive)
                     PSXmlDocumentBuilder.addElement(
                        respDoc, baseNode, "isActive", ((sum.isActive()) ? "yes" : "no"));
                  if (bGetCreatedBy)
                     PSXmlDocumentBuilder.addElement(
                        respDoc, baseNode, "createdBy", sum.getCreatedBy());
                  if (bGetCreatedOn)
                     PSXmlDocumentBuilder.addElement(
                        respDoc, baseNode, "createdOn", "" + sum.getCreatedOn().getTime());
                  if (bIsEmpty)
                     PSXmlDocumentBuilder.addElement(
                        respDoc, baseNode, "isEmpty", "" + sum.isEmpty());
                  if (bAppType)
                     PSXmlDocumentBuilder.addElement(
                        respDoc, baseNode, "appType", "" + sum.getAppType());
                  if (bId)
                     PSXmlDocumentBuilder.addElement(
                           respDoc, baseNode, "id", "" + sum.getId());
                  
                  if (bLockerName || bLockerSession)
                  {
                     PSServerXmlObjectStore os = 
                        PSServerXmlObjectStore.getInstance();
                     IPSLockerId lockId = getEffectiveLockerId(req, uniqueId);
                     Properties lockProps = os.getApplicationLockInfo(lockId, 
                        sum.getName());
                     if (lockProps != null)
                     {
                        Enumeration keys = lockProps.keys();
                        while (keys.hasMoreElements())
                        {
                           String key = (String) keys.nextElement();
                           String val = lockProps.getProperty(key);
                           PSXmlDocumentBuilder.addElement(
                              respDoc, baseNode, key, val);                           
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception e) {
         // all exceptions are fatal here
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         respDoc = fillErrorResponse(e);
      }

      return respDoc;
   }

   /**
    * Get the list of files below an application's root directory.
    * 
    * @param inDoc The document containing the request.  The expected format is:
   * <pre><code>
    * &lt;ELEMENT PSXDesignAppListFiles (EMPTY)>
    * &lt;ATTLIST PSXDesignAppListFiles
    *    appRoot CDATA #REQUIRED
    *    includeDirs (true | false) #REQUIRED
    *    recurse (true | false) #REQUIRED
    * >
    * </code></pre>
    * 
    * @param req The current request object, may not be <code>null</code>.
    * 
    * @return The response doc.  Format is either an error response or:
    * <pre><code>
    * &lt;ELEMENT PSXDesignAppListFilesResults (PSXApplicationFile*)>
    * &lt;ELEMENT PSXApplicationFile (EMPTY)>
    * &lt;ATTLIST PSXApplicationFile
    *    filename     CDATA #REQUIRED
    *    lastModified CDATA #REQUIRED
    *    isFolder     (true | false) #IMPLIED
    * >
    * </code></pre>
    * 
    * @throws PSUnknownDocTypeException if the request is malformed.
    */
   public Document getApplicationFiles(Document inDoc, PSRequest req) 
      throws PSUnknownDocTypeException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      
      if (inDoc == null)
         throw new PSUnknownDocTypeException(XML_ELEMENT_NULL,
            ms_RootAppListFiles);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(XML_ELEMENT_NULL,
            ms_RootAppListFiles);

      // make sure we got the correct root node tag
      if (false == ms_RootAppListFiles.equals(root.getNodeName()))
      {
         Object[] args = { ms_RootAppListFiles, root.getNodeName()};
         throw new PSUnknownDocTypeException(XML_ELEMENT_WRONG_TYPE, args);
      }

      String appRoot = root.getAttribute("appRoot");
      boolean includeDirs = Boolean.parseBoolean(root.getAttribute("includeDirs"));
      boolean recurse = Boolean.parseBoolean(root.getAttribute("recurse"));

      /* build the response doc */
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      root = PSXmlDocumentBuilder.createRoot(respDoc, 
         "PSXDesignAppListFilesResults");

      try
      {
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         Iterator<File> files = 
            os.getAppRootFileList(appRoot, includeDirs, recurse);
         while (files.hasNext())
         {
            File file = files.next();
            File filePath = 
               new File(PSServerXmlObjectStore.getAppRootDir(appRoot),
                  file.getName());
            PSApplicationFile appfile = 
               new PSApplicationFile(file, filePath.isDirectory());
            appfile.setLastModified(filePath.lastModified());
            root.appendChild(appfile.toXml(root.getOwnerDocument()));
         }
      }
      catch (Exception e)
      {
         // all exceptions are fatal here
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         respDoc = fillErrorResponse(e);
      }

      return respDoc;
   }   
   /**
    * Remove the specified application from the object store. This
    * permanently deletes the application, which cannot be recovered.
    *
    * @param      inDoc                           the XML document containing the
    *                                          application data
    *
    *   @param      req                        the request context
    *                                          (for security)
    *
    * @return                                 the XML response document
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSAuthorizationException    if the user does not have delete
    *                                          access to the application
    *
    * @throws PSLockedException            if another user has the
    *                                          application locked
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                          appropriate format for this
    *                                          request type
    *
    * @see         com.percussion.design.objectstore.PSApplication
    */
   public Document removeApplication(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException,
               PSLockedException, PSUnknownDocTypeException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppRemove);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppRemove);

      //make sure we got the correct root node tag
      if (false == ms_RootAppRemove.equals(root.getNodeName()))
      {
         Object[] args = { ms_RootAppRemove, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }


      try {   /* and process the request */
         PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);

         PSApplication application;
         String applicationName = "";
         String uniqueId = walker.getElementData(ATTR_UNIQUEID);
         String app = walker.getElementData("applid");
         if ((app != null) && (app.length() != 0)) 
         {
            int applid = (new Integer(app)).intValue();
            applicationName = getApplicationNameFromId(applid);
            application = getApplicationObject(applicationName);

            doRemoveApplication(applid, req, uniqueId);
         }
         else 
         {
            applicationName = walker.getElementData("name");
            application = getApplicationObject(applicationName);
            doRemoveApplication(applicationName, req, uniqueId);
         }

         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(respDoc, "PSXDesignAppRemoveResults");
         return respDoc;
      } catch (Exception e) {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Rename the specified application defined in this object store. If the
    * current app name and app root are the same, the app root will also be
    * renamed to match the new app name
    *
    * @param      inDoc                        the XML document containing the
    *                                          application data
    *
    *   @param      req                        the request context
    *                                          (for security)
    *
    * @return                                 the XML response document
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSAuthorizationException    if user does not have update
    *                                          access to the application
    *
    * @throws PSLockedException            if another user has the
    *                                          application locked
    *
    * @throws PSNonUniqueException         if an application with the new
    *                                          name already exists
    *
    * @throws PSNotFoundException         if an application with the old
    *                                          name does not exist
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                          appropriate format for this
    *                                          request type
    *
    * @see         com.percussion.design.objectstore.PSApplication
    */
   public Document renameApplication(Document inDoc, PSRequest req)
      throws PSServerException,      PSAuthorizationException,
               PSAuthenticationFailedException,
               PSLockedException,   PSNonUniqueException,
               PSNotFoundException,   PSUnknownDocTypeException
   {

      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppRename);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppRename);

      //make sure we got the correct root node tag
      if (false == ms_RootAppRename.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootAppRename, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      try {   // and process the request
         PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);

         PSApplication application = null;

         String app = walker.getElementData("applid");
         String newName = walker.getElementData("newName");
         String uniqueId = walker.getElementData(ATTR_UNIQUEID);
         String oldName = null;

         if ((app != null) && (app.length() != 0)) {
            oldName = getApplicationNameFromId(Integer.parseInt(app));
         }
         else {
            oldName = walker.getElementData("oldName");
         }

         if (!checkApplicationSecurity(oldName, PSAclEntry.AACE_DESIGN_UPDATE, req))
         {
            throw new PSAuthorizationException(
               "renameApplication", oldName, req.getUserSessionId());
         } else
         {
             application = getApplicationObject(oldName);

            // rename the application on disk, then remove the old summary
            // add the new one

            doRenameApplication(oldName, newName, req, uniqueId);
         }

         /* we need to synchronize on the vector, as new apps may be saved
            * or other apps may be removed. This can change the positioning
            * out from under us, which is a problem. To minimize the impact,
            * we will make a temporary copy of the vector and work from it.
            */
         IPSApplicationListener[] listeners = getApplicationListenerArray();
         int vectorSize = listeners.length;
         for (int index = 0; index < vectorSize; index++){
            listeners[index].applicationRenamed(application, oldName, newName);
         }

         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(respDoc, "PSXDesignAppRenameResults");
         return respDoc;
      } catch (Exception e) {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }


   /**
    * Gets the content editor system definition document. Delegates to <code>
    * PSServerXmlObjectStore</code>
    * <p>Format of the request:
    * <code><pre>
    * &lt;PSXContentEditorSystemDefinitionLoad>
    * &lt;/PSXContentEditorSystemDefinitionLoad>
    * </pre></code>
    * @author James Schultz
    * @param inDoc XML document containing the request data; cannot be
    * <code>null</code>.
    * @param req the request context, used for maintaining statistics;
    * cannot be <code>null</code>.
    * @return a valid Document, either an error or the content editor
    * definition, never <code>null</code>.
    * @throws PSUnknownDocTypeException  if inDoc is <code>null</code> or does
    * not contain the appropriate format for this request type
    * @see PSServerXmlObjectStore#getContentEditorSystemDef
    */
   public Document getContentEditorSystemDef(Document inDoc, PSRequest req)
      throws PSUnknownDocTypeException
   {
      if (null == req)
         throw new IllegalArgumentException("PSRequest cannot be null");

      validateRootElement( inDoc, ms_RootCESystemDefLoad );

      try
      {
         PSContentEditorSystemDef systemDef =
               PSServerXmlObjectStore.getInstance().getContentEditorSystemDef();

         if(systemDef == null)
            throw new PSServerException(
               IPSObjectStoreErrors.CE_SYSTEM_DEF_NOT_FOUND);

         return systemDef.toXml();
      }
      catch (RuntimeException e)
      {
         // let these propagate
         throw e;
      }
      catch (Exception e)
      {
         PSConsole.printMsg("Object Store", e);
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse( e );
      }
   }


   /**
    * Gets the content editor shared definition document. Delegates to <code>
    * PSServerXmlObjectStore</code>
    * <p>Format of the request:
    * <code><pre>
    * &lt;PSXContentEditorSharedDefinitionLoad />
    * </pre></code>
    * @author James Schultz
    * @param inDoc XML document containing the request data; cannot be
    * <code>null</code>.
    * @param req the request context, used for maintaining statistics;
    * cannot be <code>null</code>.
    * @return a valid Document, either an error or the shared content editor
    * definition, never <code>null</code>.
    * @throws PSUnknownDocTypeException  if inDoc is <code>null</code> or does
    * not contain the appropriate format for this request type
    * @see PSServerXmlObjectStore#getContentEditorSharedDef()
    */
   public Document getContentEditorSharedDef(Document inDoc, PSRequest req)
      throws PSUnknownDocTypeException
   {
      if (null == req)
         throw new IllegalArgumentException("PSRequest cannot be null");
      validateRootElement( inDoc, ms_RootCESharedDefLoad );

      try {
         PSContentEditorSharedDef sharedDef =
               PSServerXmlObjectStore.getInstance().getContentEditorSharedDef();

         if(sharedDef == null)
            throw new PSServerException(
               IPSObjectStoreErrors.CE_SHARED_DEF_NOT_FOUND);

         return sharedDef.toXml();
      }  catch (RuntimeException e) {
         // let these propagate
         throw e;
      } catch (Exception e) {
         PSConsole.printMsg("Object Store", e);
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse( e );
      }
   }

   public Document getConnectionDetail(Document inDoc, PSRequest req)
      throws PSUnknownDocTypeException
   {
      if (null == req)
         throw new IllegalArgumentException("PSRequest cannot be null");
      validateRootElement( inDoc, ms_RootGetConnectionDetail );
      
      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(XML_ELEMENT_NULL,
            ms_RootGetConnectionDetail);

      
      String dsName = root.getAttribute("dsName");

      /* build the response doc */
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      root = PSXmlDocumentBuilder.createRoot(respDoc, 
         "PSXGetConnectionDetailsResults");
      
      try
      {
         PSConnectionDetail detail = PSConnectionHelper.getConnectionDetail(
            new PSConnectionInfo(dsName));

         root.setAttribute("driver", detail.getDriver());
         root.setAttribute("datasource", detail.getDatasourceName());
         root.setAttribute("database", detail.getDatabase());
         root.setAttribute("origin", detail.getOrigin());
         root.setAttribute("jdbcUrl", detail.getJdbcUrl());
         
         return respDoc;
      }
      catch (RuntimeException e)
      {
         // let these propagate
         throw e;
      }
      catch (Exception e)
      {
         PSConsole.printMsg("Object Store", e);
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Uses the table factory to create and modify database tables based on
    * the PSTableLocator and PSTableDefintion objects included in the request.
    * It is an error to try to create a table with existing table name unlike
    * the table factory.
    * <p>
    * Format of the request:
    * <code><pre>
    * &lt;PSXDesignTableDefinitionsSave>
    *    &lt;PSXTableLocator>...&lt;/PSXTableLocator>
    *    &lt;tables>
    *       &lt;table>...&lt;/table>
    *    &lt;/tables>
    * &lt;/PSXDesignTableDefinitionsSave>
    * </pre></code>
    * Format of the result document:
    * <br>
    * In case of exception during authorization or connecting to database
    * <code><pre>
    * &lt;PSXDesignTableDefinitionsSaveResults>
    *    &lt;error>error message&lt;/error>
    * &lt;/PSXDesignTableDefinitionsSaveResults>
    * </pre></code>
    * If we didn't get any exception the dtd of result document is:
    * <br>
    * <pre><code>
    *  &lt;!--
    *    Either 'success' or 'error' element for each table schema processed.
    *  --&gt;
    *  &lt;!ELEMENT PSXDesignTableDefinitionsSaveResults (success*, error*)&gt;
    *
    *  &lt;!ELEMENT success  (#PCDATA)&gt;
    *  &lt;!ATTLIST success
    *       tableName CDATA #REQUIRED
    *       create(y|n) "y"&gt;
    *
    *  &lt;!ELEMENT error (#PCDATA)&gt;
    *  &lt;!ATTLIST error
    *       tableName CDATA #REQUIRED
    *       create(y|n) "y" &gt;
    * </code></pre>
    * @author James Schultz
    * @param inDoc XML document containing the request data; cannot be
    * <code>null</code>.
    * @param req the request context, used for checking security access;
    * cannot be <code>null</code>.
    * @return Either an error document or a success document, never
    * <code>null</code>.
    *
    * @throws PSUnknownDocTypeException  if inDoc is <code>null</code> or does
    * not contain the appropriate format for this request type
    */
   public Document saveTableDefinitions(Document inDoc, PSRequest req)
      throws PSUnknownDocTypeException
   {
      if (null == req)
         throw new IllegalArgumentException("PSRequest cannot be null");

      validateRootElement( inDoc, ms_RootTableDefSave );

      // build response
      Document responseDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element responseRoot = PSXmlDocumentBuilder.createRoot(responseDoc,
            ms_RootTableDefSave + "Results");

      // de-serialize objects from the request
      PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);
      Element tableLocElem =
            walker.getNextElement(PSTableLocator.XML_NODE_NAME, true, true);
      if (null == tableLocElem)
      {
         Object[] args = { ms_RootTableDefSave, PSTableLocator.XML_NODE_NAME, "" };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      PSTableLocator loc = null;
      try
      {
         loc = new PSTableLocator( tableLocElem, null, null);
      } catch (PSUnknownNodeTypeException e)
      {
         Object[] args = { ms_RootTableDefSave, PSTableLocator.XML_NODE_NAME, "" };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      // first <tables> is the schema definition
      Element tableDefsElem = walker.getNextElement("tables", true, true);
      if (null == tableDefsElem)
      {
         Object[] args = { ms_RootTableDefSave, "tables", "" };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      Connection conn = null;

      // call the table factory
      try
      {
         Document tablesSchemaDoc = PSXmlDocumentBuilder.createXmlDocument();
         tableDefsElem = (Element)PSXmlDocumentBuilder.copyTree(tablesSchemaDoc,
            tablesSchemaDoc, tableDefsElem, true);

         IPSConnectionInfo connInfo = loc.getCredentials();

         /* Process each table schema at a time from the collection and if error
          * happens in the process log the error and continue with next schema.
          */
         PSJdbcDbmsDef dbmsDef = new PSServerJdbcDbmsDef(connInfo);
         
         PSJdbcDataTypeMap dataTypeMap =
            new PSJdbcDataTypeMap( null, dbmsDef.getDriver(), null );
         PSJdbcTableSchemaCollection tableSchemaCollection =
            new PSJdbcTableSchemaCollection(tablesSchemaDoc, dataTypeMap);

         conn = PSConnectionHelper.getDbConnection(connInfo);

         Iterator tableSchemas = tableSchemaCollection.iterator();
         while(tableSchemas.hasNext())
         {
            PSJdbcTableSchema tableSchema =
               (PSJdbcTableSchema)tableSchemas.next();
            String tableName = tableSchema.getName();
            Element el = null;

            /* If the schema is set for creating new table check for existance
             * before sending it to table factory and log it as error if exists.
             * This behavior differs from the table factory as the table factory
             * drops the existing table and recreates the table with new schema.
             */
            if(tableSchema.isCreate())
            {
               if( PSJdbcTableFactory.catalogTable( conn, dbmsDef, dataTypeMap,
                  tableName, false ) != null )
               {
                  String msg = PSErrorManager.createMessage(
                     IPSObjectStoreErrors.CREATE_TABLE_EXISTS, tableName);
                  el = PSXmlDocumentBuilder.addElement(
                     responseDoc, responseRoot, "error", msg);
                  el.setAttribute("tableName", tableName);
                  el.setAttribute("create", "y");
                  continue;
               }
            }

            // flush the table metadata if schema changes
            tableSchema.addSchemaChangeListener(
               new IPSJdbcTableChangeListener()
               {
                  public void tableChanged(PSJdbcTableChangeEvent e)
                  {
                     /*
                      * make sure its the correct action and that there is
                      * connection info (may not have been constructed this way,
                      * but should have been for our purposes)
                      */
                     if (e.getAction() == 
                           PSJdbcTableChangeEvent.ACTION_SCHEMA_CHANGED &&
                           e.usedConnInfo())
                     {
                        PSDatabaseMetaData dbmd =
                           PSMetaDataCache.getCachedDatabaseMetaData(
                              e.getConnectionInfo());

                        if (dbmd != null)
                        {
                           if (e.usedConnInfo())
                           {
                              try
                              {
                                 PSConnectionDetail detail = PSConnectionHelper.getConnectionDetail(e.getConnectionInfo());
                                 dbmd.flushTableMetaData(e.getTable(), detail.getOrigin());
                              }
                              catch (Exception ex)
                              {
                                 // shouldn't happen if we got this far
                                 throw new RuntimeException(ex);
                              }
                           }
                           else
                           {
                              dbmd.flushTableMetaData(e.getTable(), e.getSchema());
                           }
                        }
                     }
                  }
               }
            );

            try {
               PSJdbcTableFactory.processTable( conn, dbmsDef, tableSchema,
                  null, true );
               el = PSXmlDocumentBuilder.addElement(
                  responseDoc, responseRoot, "success", "true");
            }
            catch ( PSJdbcTableFactoryException je )
            {
               el = PSXmlDocumentBuilder.addElement(responseDoc,
                  responseRoot, "error", je.getLocalizedMessage());
            }

            el.setAttribute("tableName", tableName);
            el.setAttribute("create", tableSchema.isCreate() ? "y" : "n");
         }
      }
      catch( PSJdbcTableFactoryException pje)
      {
         handleException(req, responseDoc, responseRoot, pje);
      }
      catch( SQLException se)
      {
         handleException(req, responseDoc, responseRoot, se);
      }
      catch( IOException ie)
      {
         handleException(req, responseDoc, responseRoot, ie);
      }
      catch( SAXException sxe)
      {
         handleException(req, responseDoc, responseRoot, sxe);
      }
      catch (NamingException ne)
      {
         handleException(req, responseDoc, responseRoot, ne);
      }
      finally
      {
         if(conn != null)
         {
            try { conn.close(); } catch (SQLException e){}
         }
      }

      return responseDoc;
   }

   /**
    * Handles the exception by setting failure to request statistics and adds
    * a new 'error' element with exception message as it's value to the document
    * with specified root.
    *
    * @param req the request object, assumed not to be <code>null</code>
    * @param doc the document to which error should be added, assumed not to be
    * <code>null</code>
    * @param root the root element to which the error element should be added,
    * assumed not to be <code>null</code>
    * @param e the exception, assumed not to be <code>null</code>
    */
   private void handleException(PSRequest req, Document doc, Element root,
      Exception e)
   {
       PSConsole.printMsg("Object Store", e);
       PSRequestStatistics reqStats = req.getStatistics();
       reqStats.setFailure();
       PSXmlDocumentBuilder.addElement( doc, root, "error",
         e.getLocalizedMessage() );
   }

   /**
    * Checks that <code>inDoc</code> is not null or empty, and contains the
    * specified root element.
    *
    * @param inDoc object to be validated
    * @param rootElement name of the expected root element of <code>inDoc</code>
    * @throws PSUnknownDocTypeException if <code>inDoc</code> fails to validate
    */
   private void validateRootElement(Document inDoc, String rootElement)
         throws PSUnknownDocTypeException
   {
      // get the root node and make sure it is the correct type
      if (null == inDoc)
         throw new PSUnknownDocTypeException( XML_ELEMENT_NULL, rootElement );
      Element root = inDoc.getDocumentElement();
      if (null == root)
         throw new PSUnknownDocTypeException( XML_ELEMENT_NULL, rootElement );
      if ( ! rootElement.equals( root.getNodeName() ) )
      {
         Object[] args = { rootElement, root.getNodeName() };
         throw new PSUnknownDocTypeException( XML_ELEMENT_WRONG_TYPE, args );
      }
   }


   /**
    * Saves the specified application to the object store. If the application
    * was newly created, or it references a different object store, it will
    * be created in this object store. If the application represents an
    * existing application in this object store, it will be updated. This
    * behavior can also be overriden by using the createNewApp parameter.
    *
    * @param      inDoc                        the XML document containing the
    *                                          application data
    *
    *   @param      req                        the request context
    *                                          (for security)
    *
    * @return                                 the XML response document
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSAuthorizationException    if creating a new application,
    *                                          the user does not have create
    *                                          access on the server. If
    *                                          updating an existing
    *                                          application, the user does not
    *                                          have update access on the
    *                                          application.
    *
    * @throws PSNotLockedException         when updating an existing
    *                                          application and a lock is not
    *                                          currently held (the timeout
    *                                          already expired or
    *                                          getApplication was not used
    *                                          to lock the application)
    *
    * @throws PSNonUniqueException         if creating an application and
    *                                          an application by the same name
    *                                          already exists
    *
    * @throws PSSystemValidationException         if validate is <code>true</code>
    *                                          and a validation error is
    *                                          encountered
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                          appropriate format for this
    *                                          request type
    *
    * @see         com.percussion.design.objectstore.PSApplication
    */
   public Document saveApplication(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException,
               PSNotLockedException, PSNonUniqueException,
           PSSystemValidationException, PSUnknownDocTypeException
   {

      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppSave);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppSave);

      //make sure we got the correct root node tag
      if (false == ms_RootAppSave.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootAppSave, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      /* build the response doc */
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      root = PSXmlDocumentBuilder.createRoot(respDoc,
                                             "PSXDesignAppSaveResults");

      try {   /* and process the request */
         PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);

         /* get the application XML tree */
         if (walker.getNextElement("PSXApplication", true, true) == null) {
            Object[] args = { ms_RootAppSave, "PSXApplication", "" };
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         /* get the save flags */
         boolean bReleaseLock   = true;
         boolean bValidate      = true;
         boolean bCreateNew   = true;
         String changeDesc = null;

         String sTemp = walker.getElementData("releaseLock");
         if ((sTemp != null) && sTemp.equalsIgnoreCase("no"))
            bReleaseLock = false;

         sTemp = walker.getElementData("validate");
         if ((sTemp != null) && sTemp.equalsIgnoreCase("no"))
            bValidate = false;

         sTemp = walker.getElementData("createNewApp");
         if ((sTemp != null) && sTemp.equalsIgnoreCase("no"))
            bCreateNew = false;

         changeDesc = walker.getElementData("changeDescription");
         if (changeDesc == null)
            changeDesc = "Saved";
         String uniqueId = walker.getElementData(ATTR_UNIQUEID);
         /* promote the app to root so we can do the save easily */
         Element appTree = (Element)walker.getCurrent();
         PSXmlDocumentBuilder.replaceRoot(inDoc, appTree);

         // build an application object from the doc, and add a
         // revision entry to it
         PSApplication app = new PSApplication(inDoc);

         String name = app.getName();
         int id = app.getId();

         // even if validation was not requested, do so for enabled apps
         if (app.isEnabled())
            bValidate = true;

         // make sure the versions match (no conflict...)
         PSRevisionHistory hist = app.getRevisionHistory();
         int inMajor = 1;
         int inMinor = 0;
         if (hist != null)
         {
            inMajor = hist.getLatestMajorVersion();
            inMinor = hist.getLatestMinorVersion();
         }

         int major = inMajor;
         int minor = inMinor;

         synchronized (m_appSums)
         {
            // This will only find and check summary for updates
            PSApplicationSummary sum = m_appSums.getSummary(id);
            if (sum != null)
            {
               major = sum.getMajorVersion();
               minor = sum.getMinorVersion();

               if (major != inMajor || minor != inMinor)
               {
                  // maybe it was changed on disk?
                  updateSummaryEntry(name);
               }

               sum = m_appSums.getSummary(name);
               if (sum != null)
               {
                  major = sum.getMajorVersion();
                  minor = sum.getMinorVersion();
               }
            }
         }

         if ((major != inMajor || minor != inMinor) && !bCreateNew)
         {
            // we have a version conflict
            Object[] args = new Object[]
               {
                  name, "" + inMajor, "" + inMinor, "" + major, "" + minor
               };

            throw new PSVersionConflictException(APP_VERSION_DOES_NOT_MATCH,
               args);
         }

         doSaveApplication(
            app,
            bReleaseLock,
            bValidate,
            bCreateNew,
            req,
            uniqueId,
            changeDesc);


         Element appNode =
               PSXmlDocumentBuilder.addEmptyElement(respDoc, root,
                                                    "PSXApplication");
         appNode.setAttribute("id", "" + app.getId());

         Element historyNode =
               PSXmlDocumentBuilder.addEmptyElement(respDoc,
                                                    root,
                                                    "PSXRevisionHistory");

         // tell the designer what the newest version is
         hist = app.getRevisionHistory();
         if (hist != null)
         {
            PSRevisionEntry entry = hist.getLatestRevision();
            if (entry != null)
               historyNode.appendChild(entry.toXml(respDoc));
         }


         return respDoc;
      } catch (Exception e) {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   public Document saveExtension(Document inDoc, PSRequest req)
      throws PSServerException,
         PSAuthorizationException,
         PSAuthenticationFailedException,
         PSNotLockedException,
           PSSystemValidationException,
         PSExtensionException,
         PSNotFoundException,
         PSUnknownDocTypeException
   {
      try
      {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootExtSave);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootExtSave);

      //make sure we got the correct root node tag
      if (!ms_RootExtSave.equals(root.getNodeName()))
      {
         Object[] args = { ms_RootExtSave, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      // deserialize extension def
      PSXmlTreeWalker tree = new PSXmlTreeWalker(root);
      final int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      final int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element extensionEl = tree.getNextElement("Extension", firstFlag);
      if (extensionEl == null)
      {
         // TODO: better exception type
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, "Extension");
      }

      PSExtensionDefFactory factory = new PSExtensionDefFactory();
      IPSExtensionDef def = factory.fromXml(extensionEl);

      // save all resources
      try
      {
         Element resourceEl = tree.getNextElement("PSXExtensionFile");
         Collection resources = new LinkedList();
         while (resourceEl != null)
         {
            PSExtensionFile file = new PSExtensionFile();
            file.fromXml(resourceEl, null, null);
            resources.add(file.getContent());
            resourceEl = tree.getNextElement("PSXExtensionFile", nextFlag);
         }

         try
         {
            IPSExtensionManager extMgr = PSServer.getExtensionManager(this);
            if (extMgr.exists(def.getRef()))
            {
               extMgr.updateExtension(def, resources.iterator());
            }
            else
            {
               extMgr.installExtension(def, resources.iterator());
            }
         }
         finally
         {
            // close any unclosed resources
            for (Iterator i = resources.iterator(); i.hasNext(); )
            {
               try
               {
                  IPSMimeContent content = (IPSMimeContent)i.next();
                  InputStream in = content.getContent();
                  in.close();
               }
               catch (Throwable t) { /* ignore */ }
            }
         }

         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(
            respDoc, "PSXDesignExtensionSaveResults");

         return respDoc;
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSServerException(e);
      }
      catch (Exception e)
      {
         PSConsole.printMsg("Design", e);
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
      }
      catch (Throwable t)
      {
         PSConsole.printMsg("Design", t);
         return fillErrorResponse(t);
      }
   }

   // returns a doc representation of an IPSExtensionDef
   public Document loadExtension(Document inDoc, PSRequest req)
      throws PSServerException,
         PSAuthorizationException,
         PSAuthenticationFailedException,
         PSNotLockedException,
           PSSystemValidationException,
         PSExtensionException,
         PSNotFoundException,
         PSUnknownDocTypeException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootExtLoad);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootExtLoad);

      //make sure we got the correct root node tag
      if (!ms_RootExtLoad.equals(root.getNodeName()))
      {
         Object[] args = { ms_RootExtLoad, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      try
      {
         String extName = root.getAttribute("name");
         if (extName == null)
         {
            // TODO: better exception type
            throw new PSUnknownDocTypeException(
               XML_ELEMENT_NULL, "name");
         }

         PSExtensionRef ref = new PSExtensionRef(extName);
         IPSExtensionDef def =
            PSServer.getExtensionManager(this).getExtensionDef(ref);

         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element respRoot = PSXmlDocumentBuilder.createRoot(
            respDoc, "PSXDesignExtensionLoadResults");

         PSExtensionDefFactory factory = new PSExtensionDefFactory();
         factory.toXml(respRoot, def);

         return respDoc;
      }
      catch (Exception e)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }


   public Document removeExtension(Document inDoc, PSRequest req)
      throws PSServerException,
         PSAuthorizationException,
         PSAuthenticationFailedException,
         PSNotLockedException,
           PSSystemValidationException,
         PSExtensionException,
         PSNotFoundException,
         PSUnknownDocTypeException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootExtRemove);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootExtRemove);

      //make sure we got the correct root node tag
      if (!ms_RootExtRemove.equals(root.getNodeName()))
      {
         Object[] args = { ms_RootExtRemove, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      try
      {
         String extName = root.getAttribute("name");
         if (extName == null)
         {
            // TODO: better exception type
            throw new PSUnknownDocTypeException(
               XML_ELEMENT_NULL, "name");
         }

         PSExtensionRef ref = new PSExtensionRef(extName);
         PSServer.getExtensionManager(this).removeExtension(ref);

         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(
            respDoc, "PSXDesignExtensionRemoveResults");

         return respDoc;

      }
      catch (Exception e)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }

   }

   /**
    * Loads the previously saved user configuration object for the specified
    * user. If configuration information does not exist on the server,
    * an empty object will be returned.
    *
    * @param      inDoc                           the XML document containing the
    *                                          user configuration data
    *
    *   @param      req                        the request context
    *                                          (for security)
    *
    * @return                                 the XML response document
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSAuthorizationException    if user does not have designer
    *                                          access to the server
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                          appropriate format for this
    *                                          request type
    *
    * @see         com.percussion.design.objectstore.PSUserConfiguration
    */
   public Document getUserConfiguration(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException,
               PSUnknownDocTypeException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootUserConfigLoad);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootUserConfigLoad);

      //make sure we got the correct root node tag
      if (false == ms_RootUserConfigLoad.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootUserConfigLoad, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);

      String name = walker.getElementData("name");
      if ((name == null) || (name.length() == 0)) {
         Object[] args = { ms_RootUserConfigLoad, "PSXUserConfiguration/name", "" };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_INVALID_CHILD, args);
      }

      boolean authenticated = false;
      PSUserSession sess = req.getUserSession();
      PSUserEntry[] entries = sess.getAuthenticatedUserEntries();
      for (int i = 0; i < entries.length; i++)
      {
         if (entries[i].getName().equalsIgnoreCase(name))
         {
            authenticated = true;
            break;
         }
      }

      if (!authenticated)
      {
         throw new PSAuthorizationException(
            "getUserConfiguration", name, req.getUserSessionId());
      }

      try {
         Document respDoc = null;
         try {
            respDoc = loadUserConfig(name);   /* get the cfg file */
         } catch (PSNotFoundException nfe) {
            // this is not treated as an error, create an empty new one
            respDoc = createUserConfiguration(name).toXml();
         }

         root = respDoc.getDocumentElement();   /* and shift it down */
         Element newRoot = respDoc.createElement("PSXDesignUserConfigLoadResults");
         PSXmlDocumentBuilder.swapRoot(respDoc, newRoot);

         return respDoc;
      } catch (Exception e) {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Loads the rx configuration object for the specified name in the request
    * document. If the request is for editing, an attempt will be made to
    * acquire the config lock. Expects the request document in the following
    * format:
    * <pre><code>
    * &lt;ELEMENT PSXDesignRxConfigLoad (EMPTY)>
    * &lt;ATTLIST PSXDesignerRxConfigLoad
    *    name CDATA #REQUIRED
    *    lock (yes|no) "no"
    *    overrideSameUser (yes|no) "no"
    *    overrideDifferentUser (yes|no) "no" >
    * </code></pre>
    *
    * @param inDoc the xml document describing the request, may not be
    * <code>null</code>
    * @param req the request context to check for security, may not be
    * <code>null</code>
    *
    * @return the document that describes the configuration as defined in
    * {@link com.percussion.design.objectstore.PSConfig#toXml(Document)} if the
    * request is successful, otherwise an error document. The error document
    * represents the exception in xml format.
    *
    * @throws PSUnknownDocTypeException if the document is not a recognised
    * document for the request.
    */
   public Document getRxConfiguration(Document inDoc, PSRequest req)
      throws PSUnknownDocTypeException
   {
      if (inDoc == null)
         throw new IllegalArgumentException("inDoc may not be null.");

      if (req == null)
         throw new IllegalArgumentException("req may not be null.");

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootRxConfigLoad);

      // make sure we got the correct root node tag
      if (false == ms_RootRxConfigLoad.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootRxConfigLoad, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      Document respDoc;
      try
      {
         respDoc = PSConfigManager.getInstance().getRxConfiguration(inDoc,
            req.getUserSession().getRealAuthenticatedUserEntry());
      }
      catch (Exception e)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         respDoc = fillErrorResponse(e);
      }

      return respDoc;
   }

   /**
    * Saves/Updates previously locked configuration that is specified in the
    * request document or updates the lock state alone.
    * <pre><code>
    * &lt;ELEMENT PSXDesignRxConfigSave (CONFIGURATION?)>
    * &lt;ATTLIST PSXDesignRxConfigSave
    *    name CDATA #REQUIRED
    *    releaseLock (yes|no) "no" >
    * &lt;ELEMENT CONFIGURATION (#PCDATA)>
    * </code></pre>
    *
    * @param inDoc the XML document containing the save request, may not be
    * <code>null</code>.
    * @param req the request context for security, may not be <code>null</code>.
    *
    * @return the success document if the request is succeeded, otherwise an
    * error document, never <code>null</code>. The xml format of the success
    * document is:
    * <pre><code>
    * &lt;ELEMENT PSXDesignRxConfigSaveResults>
    * </pre></code>
    * The error document represents the exception in xml format.
    *
    * @throws PSUnknownDocTypeException if the document is not a recognised
    * document for the request.
    */
   public Document saveRxConfiguration(Document inDoc, PSRequest req)
      throws PSUnknownDocTypeException
   {
      if (inDoc == null)
         throw new IllegalArgumentException("inDoc may not be null.");

      if (req == null)
         throw new IllegalArgumentException("req may not be null.");

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootRxConfigSave);

      // make sure we got the correct root node tag
      if (false == ms_RootRxConfigSave.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootRxConfigSave, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      Document respDoc;

      try
      {
         PSConfigManager.getInstance().saveRxConfiguration(inDoc,
            req.getUserSession().getRealAuthenticatedUserEntry());

         respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(
            respDoc, "PSXDesignRxConfigSaveResults");
      }
      catch (Exception e)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         respDoc = fillErrorResponse(e);
      }

      return respDoc;
   }

   /**
    * Load the character encoding map from the installation root directory and
    * return the xml document.
    *
    * @param      inDoc                        the XML document containing the
    *                                          character encoding data
    *
    * @param      req                        the request context
    *                                          (for security)
    *
    * @return                                 the XML response document
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSAuthorizationException    if user does not have designer
    *                                          access to the server
    *
    * @throws PSAuthenticationFailedException if the user is not
    *                                            authenticated to perform this
    *                                            command
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                          appropriate format for this
    *                                          request type
   *
   * @throws PSNotFoundException the character encoding map does not exist.
    */
   public Document getCharacterSetMap(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException,
               PSUnknownDocTypeException, PSNotFoundException
   {
      if (inDoc == null)
      {
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootCharacterSetMapLoad);
      }

      Element root = inDoc.getDocumentElement();
      if (root == null)
      {
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootCharacterSetMapLoad);
      }

      //make sure we got the correct root node tag
      if (false == ms_RootCharacterSetMapLoad.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootCharacterSetMapLoad, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      try {
         // get the character set map file
         Document respDoc = loadCharacterSetMap();

         return respDoc;
      } catch (Exception e) {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Remove the user configuration information for the specified user.
    * This permanently deletes all the user configuration information,
    * which cannot be recovered.
    *
    * @param      inDoc                           the XML document containing the
    *                                          user configuration data
    *
    *   @param      req                        the request context
    *                                          (for security)
    *
    * @return                                 the XML response document
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSAuthorizationException    if user does not have designer
    *                                          access to the server
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                          appropriate format for this
    *                                          request type
    *
    * @throws PSNotFoundException         if user configuration
    *                                          information does not exist for
    *                                          the specified user
    *
    * @see         com.percussion.design.objectstore.PSUserConfiguration
    */
   public Document removeUserConfiguration(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException,
               PSUnknownDocTypeException, PSNotFoundException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootUserConfigRemove);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootUserConfigRemove);

      //make sure we got the correct root node tag
      if (false == ms_RootUserConfigRemove.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootUserConfigRemove, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);
      String name = walker.getElementData("name");

      boolean authenticated = false;
      PSUserSession sess = req.getUserSession();
      PSUserEntry[] entries = sess.getAuthenticatedUserEntries();
      for (int i = 0; i < entries.length; i++)
      {
         if (entries[i].getName().equalsIgnoreCase(name))
         {
            authenticated = true;
            break;
         }
      }

      if (!authenticated)
      {
         throw new PSAuthorizationException("removeUserConfiguration", name,
                                             req.getUserSessionId());
      }

      try {   /* and process the request */

         doRemoveUserConfiguration(name);

         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(respDoc, "PSXDesignUserConfigRemoveResults");
         return respDoc;
      } catch (Exception e) {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Saves the user configuration information for the specified user
    * to the object store.
    *
    * @param      inDoc                        the XML document containing the
    *                                          user configuration data
    *
    *   @param      req                        the request context
    *                                          (for security)
    *
    * @return                                 the XML response document
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSAuthorizationException    if user does not have designer
    *                                          access to the server
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                          appropriate format for this
    *                                          request type
    *
    * @see         com.percussion.design.objectstore.PSUserConfiguration
    */
   public Document saveUserConfiguration(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException,
               PSUnknownDocTypeException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootUserConfigSave);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootUserConfigSave);

      //make sure we got the correct root node tag
      if (false == ms_RootUserConfigSave.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootUserConfigSave, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);

      /* build the response doc */
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      root = PSXmlDocumentBuilder.createRoot(
         respDoc, "PSXDesignUserConfigSaveResults");

      try {   /* and process the request */
         /* get the application XML tree */
         if (walker.getNextElement("PSXUserConfiguration", true, true) == null) {
            Object[] args = { ms_RootUserConfigSave, "PSXUserConfiguration", "" };
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         /* promote the cfg to root so we can do the save easily */
         Element cfgTree = (Element)walker.getCurrent();
         PSXmlDocumentBuilder.replaceRoot(inDoc, cfgTree);

         doSaveUserConfiguration(inDoc, req);

         return respDoc;
      } catch (Exception e) {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Loads the server configuration object for this server. If the request
    * is for editing, an attempt will be made to acquire the config lock.
    *
    * @param inDoc The xml document containing the request. Never <code>null
    *    </code>.
    *
    * @param req the request context (for security). Never <code>null</code>.
    *
    * @return An XML response document containing either the serialized
    *    configuration object, or an error document. Never <code>null</code>.
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *    appropriate format for this request type or the input document is
    *    <code>null</code>.
    *
    * @see         com.percussion.design.objectstore.PSServerConfiguration
    */
   public Document getServerConfiguration(Document inDoc, PSRequest req)
      throws PSServerException, PSUnknownDocTypeException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootServerConfigLoad);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootServerConfigLoad);

      //make sure we got the correct root node tag
      if (false == ms_RootServerConfigLoad.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootServerConfigLoad, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      // if the cfg is to be opened in edit mode, attempt to acquire a lock on it
      boolean lockCfg = false;
      String mode = root.getAttribute("mode");
      if (mode != null && mode.equals("edit"))
         lockCfg = true;

      boolean overrideSameUser = false;
      String over = root.getAttribute("overrideSameUser");
      if (over != null && over.equals("yes"))
         overrideSameUser = true;
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      // create the response document
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element respRoot = PSXmlDocumentBuilder.createRoot(
         respDoc, "PSXDesignServerConfigLoadResults");

      boolean isLocked = false;
      IPSLockerId lockId = null;
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      try
      {
         Document cfgDoc;

         if (lockCfg)
         {
            lockId = getEffectiveLockerId(req, uniqueId, overrideSameUser);
            os.getServerConfigLock(lockId, 30);
            isLocked = true;
            cfgDoc = os.getServerConfigDoc(lockId,
               req.getSecurityToken());
         }
         else
         {
            cfgDoc = os.getServerConfigDoc(req.getSecurityToken());
         }


         Element cfgRoot = cfgDoc.getDocumentElement();
         Node importNode = respDoc.importNode(cfgRoot, true);
         respRoot.appendChild(importNode);

         return respDoc;
      }
      catch (Exception e)
      {
         if (lockCfg && isLocked)
            os.releaseServerConfigLock(lockId);
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   // see IPSObjectStoreHandler interface for description
   public Document saveServerConfiguration(Document inDoc, PSRequest req)
      throws PSUnknownDocTypeException, PSLockedException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootServerConfigSave);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootServerConfigSave);

      // make sure we got the correct root node tag
      if (false == ms_RootServerConfigSave.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootServerConfigSave, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);

      // get the save flags
      boolean bReleaseLock   = true;
      String sTemp = walker.getElementData("releaseLock");
      if ((sTemp != null) && sTemp.equalsIgnoreCase("no"))
         bReleaseLock = false;
      String uniqueId = walker.getElementData(ATTR_UNIQUEID);
      
      // build the response doc
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc, "PSXDesignServerConfigSaveResults");

      Element cfgEl = walker.getNextElement("PSXServerConfiguration", true, true);

      // get the server config XML tree
      if (cfgEl == null) {
         Object[] args = { ms_RootServerConfigSave, "PSXServerConfiguration", "" };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      try
      {
         // lock the config, save the file, and unlock the config
         doSaveServerConfig(inDoc, req, uniqueId, bReleaseLock);
         return respDoc;
      }
      catch (Exception e)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }


   /**
    * Saves a previously locked role set to persistent storage. Any changes
    * after successful completion will take effect either immediately or after
    * the user session times out.
    *
    * @param inDoc The XML document containing the save request. The root
    *    element must be PSXDesignServerRoleSave. Never <code>null</code>.
    *
    * @param req The context of the request, never <code>null</code>.
    *
    * @return Either an error document or a success document (root will be
    *    PSXDesignServerRolesSaveResults), never <code>null</code>.
    */
   public Document saveRoleConfiguration(Document inDoc, PSRequest req)
   {
      if ( null == inDoc || null == req )
         throw new IllegalArgumentException( "params can't be null" );

      try
      {
         Element root = inDoc.getDocumentElement();
         if (root == null)
            throw new PSUnknownDocTypeException(
               XML_ELEMENT_NULL, ms_RootRoleConfigSave);

         // make sure we got the correct root node tag
         if (false == ms_RootRoleConfigSave.equals (root.getNodeName()))
         {
            Object[] args = { ms_RootRoleConfigSave, root.getNodeName() };
            throw new PSUnknownDocTypeException(
               XML_ELEMENT_WRONG_TYPE, args);
         }

         PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);

         // get the save flags
         boolean releaseLock   = true;
         String sTemp = walker.getElementData("releaseLock");
         if ((sTemp != null) && sTemp.equalsIgnoreCase("no"))
            releaseLock = false;
         String uniqueId = walker.getElementData(ATTR_UNIQUEID);
         // build the response doc
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(respDoc,
               "PSXDesignServerRolesSaveResults");

         Element cfgEl = walker.getNextElement( ROLE_CONFIG_ROOT_TAGNAME, true,
               true);

         // get the server config XML tree
         if (cfgEl == null) {
            Object[] args = { ms_RootRoleConfigSave, ROLE_CONFIG_ROOT_TAGNAME,
                  "" };
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         // lock the config, save the file, and unlock the config
         PSXmlDocumentBuilder.replaceRoot(inDoc, cfgEl);
         PSRoleConfiguration rcfg = new PSRoleConfiguration(inDoc);

         // Lock the config
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         IPSLockerId lockId = getEffectiveLockerId(req, uniqueId);
         os.getServerConfigLock(lockId, 30);

         try
         {
            os.saveRoleConfiguration(rcfg, lockId, req.getSecurityToken());
         }
         finally
         {
            if (releaseLock)
            {
               os.releaseServerConfigLock(lockId);
            }
         }
         return respDoc;
      }
      catch (Exception e)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }


   /**
    * Extend the write lock on a server configuration. Write locks are granted for a
    * maximum of 30 minutes. If the administrator needs more time to complete the
    * task, an additional 30 minute extension can be requested.
    *
    * @param      inDoc                           the XML document containing the
    *                                          server config data
    *
    *   @param      req                        the request context
    *                                          (for security)
    *
    * @return                                 the XML response document
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSAuthorizationException    if the user is not permitted to
    *                                          create applications on the
    *                                          server
    *
    * @throws PSLockedException            if another user has acquired the
    *                                          application lock. This usually
    *                                          occurs if the application was
    *                                          not previously locked or the
    *                                          lock was lost due to a timeout.
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                          appropriate format for this
    *                                          request type
    *
    * @see         com.percussion.design.objectstore.PSServerConfiguration
    */
   public Document extendServerConfigurationLock(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException,
               PSLockedException, PSUnknownDocTypeException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootServerConfigLock);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootServerConfigLock);

      //make sure we got the correct root node tag
      if (false == ms_RootServerConfigLock.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootServerConfigLock, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);

      // how many minutes do we lock for (max of 30, min of 0, default of 30)
      // 0 means unlock
      String lockMinsStr = walker.getElementData("lockMins");
      if (lockMinsStr == null)
         lockMinsStr = "30";
      int lockMins = Integer.parseInt(lockMinsStr);
      if (lockMins > 30 || lockMins < 0)
      {
         lockMins = 30;
      }
      String uniqueId = walker.getElementData(ATTR_UNIQUEID);
      boolean overrideSameUser = false;
      String over = walker.getElementData("overrideSameUser");
      if (over != null && over.equals("yes"))
         overrideSameUser = true;

      try
      {
         // SECURITY: only administrators can save the config
         PSServer.checkAccessLevel(req, PSAclEntry.SACE_ADMINISTER_SERVER);
      } catch (PSAuthenticationRequiredException e)
      {
         throw new PSAuthorizationException("extendServerConfigurationLock", "",
                                             req.getUserSessionId());
      }

      try
      {
         lockServerConfiguration(req, uniqueId, lockMins, overrideSameUser);
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(respDoc, "PSXDesignServerConfigLockResults");
         return respDoc;
      }
      catch (Exception e)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Saves the specified application file to the object store.
    *
    * @param inDoc The XML document containing the application file
    * data.  May not be <code>null</code>.
    *
    * @param req the request context (for security), may not be
    * <code>null</code>.
    *
    * @return The XML response document, never <code>null</code>.
    *
    * @throws PSServerException if the server is not responding
    * @throws PSAuthenticationFailedException if the user cannot be
    * authenticated.
    * @throws PSAuthorizationException If the user is not authorized to perform
    * this action.
    * @throws PSNotLockedException if the application cannot be locked.
    * @throws PSUnknownDocTypeException  if doc is <code>null</code> or does not
    * contain the appropriate format for this request type
    * @throws PSNotFoundException if the application could not be
    * found.
    *
    * @see       com.percussion.design.objectstore.PSApplicationFile
    */
   public Document saveApplicationFile(Document inDoc, PSRequest req)
      throws   PSServerException, PSAuthorizationException,
               PSNotLockedException, PSUnknownDocTypeException,
               PSAuthenticationFailedException,
               PSNotFoundException
   {

      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppFileSave);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppFileSave);

      // make sure we got the correct root node tag
      if (false == ms_RootAppFileSave.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootAppFileSave, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }

      String appName = root.getAttribute("application");
      if (appName == null || appName.length() == 0)
      {
         Object[] args = { ms_RootAppFileLoad, "application", appName };
         throw new PSUnknownDocTypeException(XML_ELEMENT_INVALID_ATTR, args);
      }

      boolean dontLockApp = false;
      if(root.getAttribute("dontLockApp") != null && root.getAttribute("dontLockApp").equals("yes")) {
         if(ms_serverProps == null) {
            ms_serverProps = PSServer.getServerProps();
         }
         if(ms_serverProps.get(DONT_LOCK_APPLICATION_FLAG) != null 
               && ms_serverProps.get(DONT_LOCK_APPLICATION_FLAG).equals("true")) {
            dontLockApp = true;
         }
      }

      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      boolean overWrite = false;
      {
         String overWriteIfExists = root.getAttribute("overWrite");
         if (overWriteIfExists != null && overWriteIfExists.equals("yes"))
         {
            overWrite = true;
         }
      }

      boolean releaseLock = false;
      {
         String releaseLockIfExists = root.getAttribute("releaseLock");
         if (releaseLockIfExists != null && releaseLockIfExists.equals("yes"))
         {
            releaseLock = true;
         }
      }

      // build an appFile from the request document, so we can get the
      // desired file name and the content
      PSApplicationFile appFile = new PSApplicationFile();
      PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);
      Element appFileEl = walker.getNextElement("PSXApplicationFile");
      if (appFileEl == null)
      {
         if (root == null)
            throw new PSUnknownDocTypeException(
               XML_ELEMENT_NULL, "PSXApplicationFile");
      }
      try
      {
         appFile.fromXml(appFileEl, null, null);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSServerException(e);
      }


      // make sure summaries are up to date
      PSApplicationSummary sum = m_appSums.getSummary(appName);
      if (sum == null)
      {
         synchronized (m_appSums)
         {
            updateSummaryEntry(appName);
            sum = m_appSums.getSummary(appName);
         }
      }

      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();

      boolean exists = false;
      try
      {
         if(dontLockApp) {
            InputStream is = null;
            if(!appFile.isFolder())
               is = appFile.getContent().getContent();
            exists = os.saveApplicationFileWithoutLocking(appName, appFile.getFileName(),
                  is, overWrite, req.getSecurityToken(), appFile.isFolder());
         } else {
         IPSLockerId lockId = getEffectiveLockerId(req, uniqueId);
         os.getApplicationLock(lockId, appName, 30);
         InputStream is = null;
         if(!appFile.isFolder())
            is = appFile.getContent().getContent();
         exists = os.saveApplicationFile(appName, appFile.getFileName(),
            is, overWrite, lockId, req.getSecurityToken(), appFile.isFolder());

         if (releaseLock)
            os.releaseApplicationLock(lockId, appName);
      }
      }
      catch (Exception e)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }


      /* build the response doc */
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element respRoot = PSXmlDocumentBuilder.createRoot(
         respDoc, "PSXDesignAppFileSaveResults");

      respRoot.setAttribute("fileExisted", exists ? "yes" : "no");
      respRoot.setAttribute("fileSaved", (exists && overWrite) ? "yes" : "no");
      return respDoc;
   }


   /**
    * Loads the specified application file from the object store.
    *
    * @param inDoc The XML document containing the application file
    * data.  May not be <code>null</code>.
    *
    * @param req the request context (for security), may not be
    * <code>null</code>.
    *
    * @return The XML response document, never <code>null</code>.
    *
    * @throws PSServerException if the server is not responding
    * @throws PSAuthenticationFailedException if the user cannot be
    * authenticated.
    * @throws PSAuthorizationException If the user is not authorized to perform
    * this action.
    * @throws PSNotLockedException if the application cannot be locked.
    * @throws PSUnknownDocTypeException  if doc is <code>null</code> or does not
    * contain the appropriate format for this request type
    * @throws PSNotFoundException if the application could not be
    * found.
    *
    * @see       com.percussion.design.objectstore.PSApplicationFile
    */
   public Document loadApplicationFile(Document inDoc, PSRequest req)
      throws   PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException,
               PSNotLockedException, PSUnknownDocTypeException,
               PSNotFoundException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppFileLoad);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppFileLoad);

      // make sure we got the correct root node tag
      if (false == ms_RootAppFileLoad.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootAppFileLoad, root.getNodeName() };
         throw new PSUnknownDocTypeException(XML_ELEMENT_WRONG_TYPE, args);
      }

      /* We have to figure out what application has been specified, get
         * its request root, then look for the requested file under that
         * request root. If found, then we build a buffered file input
         * stream for that file and create a PSApplicationFile object
         * with that input stream. Then we create a response document and
         * call PSApplicationFile.toXml() on the document to encode the
         * document data from the file input stream for its trip back over
         * the wire.
         */
      String appName = root.getAttribute("application");
      if (appName == null || appName.length() == 0)
      {
         Object[] args = { ms_RootAppFileLoad, "application", appName };
         throw new PSUnknownDocTypeException(XML_ELEMENT_INVALID_ATTR, args);
      }

      // build an appFile from the request document, so we can get the
      // desired file name
      PSApplicationFile appFile = new PSApplicationFile();
      PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);
      Element appFileEl = walker.getNextElement("PSXApplicationFile");
      try
      {
         appFile.fromXml(appFileEl, null, null);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSServerException(e);
      }

      // make sure summaries are up to date
      PSApplicationSummary sum = m_appSums.getSummary(appName);
      if (sum == null)
      {
         synchronized (m_appSums)
         {
            updateSummaryEntry(appName);
            sum = m_appSums.getSummary(appName);
         }
      }

      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();

      // build the returned appfile doc from the file's input stream
      Document returnDoc = null;
      InputStream in = null;
      try
      {
         in = os.getApplicationFile(appName, appFile.getFileName(),
            req.getSecurityToken());
         PSApplicationFile returnFile = new PSApplicationFile(in,
            appFile.getFileName());
         in = null; // stream has now been closed for us

         returnDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(
            returnDoc, "PSXDesignAppFileLoadResults");
         returnFile.toXml(returnDoc);
      }
      catch (IllegalArgumentException iae)
      {
         Object[] args = new Object[] { appFile.getFileName().getPath(),
            iae.getLocalizedMessage() };
         throw new PSServerException(APP_FILE_IO_ERROR, args);
      }
      finally
      {
         if (in != null)
            try {in.close();} catch (IOException e){}
      }

      return returnDoc;
   }


   /**
    * Removes the specified application file from the object store.
    *
    * @param inDoc The XML document containing the application file
    * data.
    *
    *   @param      req                        the request context
    *                                          (for security)
    *
    * @return The XML response document
    *
    * @throws PSServerException          if the server is not responding
    *
    * @throws PSAuthorizationException
    *
    * @throws PSNotLockedException
    *
    * @throws PSNotFoundException if the application could not be
    * found
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                 appropriate format for this
    *                                 request type
    *
    * @see       com.percussion.design.objectstore.PSApplicationFile
    */
   public Document removeApplicationFile(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException,
         PSNotLockedException, PSUnknownDocTypeException,
         PSNotFoundException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppFileRemove);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(XML_ELEMENT_NULL,
         ms_RootAppFileRemove);

      //make sure we got the correct root node tag
      if (false == ms_RootAppFileRemove.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootAppFileRemove, root.getNodeName() };
         throw new PSUnknownDocTypeException(XML_ELEMENT_WRONG_TYPE, args);
      }

      boolean dontLockApp = false;
      if(root.getAttribute("dontLockApp") != null && root.getAttribute("dontLockApp").equals("yes")) {
         if(ms_serverProps == null) {
            ms_serverProps = PSServer.getServerProps();
         }
         if(ms_serverProps.get(DONT_LOCK_APPLICATION_FLAG) != null 
               && ms_serverProps.get(DONT_LOCK_APPLICATION_FLAG).equals("true")) {
            dontLockApp = true;
         }
      }
      
      // get the name of the application
      String appName = root.getAttribute("application");
      if (appName == null || appName.length() == 0)
      {
         Object[] args = { ms_RootAppFileLoad, "application", appName };
         throw new PSUnknownDocTypeException(XML_ELEMENT_INVALID_ATTR, args);
      }
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      try {
         if (!checkApplicationSecurity(appName, PSAclEntry.AACE_DESIGN_DELETE, req))
         {
            throw new PSAuthorizationException(
               "saveApplication", appName, req.getUserSessionId());
         }

         // fix bug# Rx-99-11-0022 - shouldn't be able to remove files from apps
         // that aren't locked
         if(!dontLockApp) {
         extendApplicationLock(
            req, getApplicationIdFromName(appName), uniqueId, 30, true);
         }

      } 
      catch (Exception e)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }

      // we assume the request root is the application directory
      PSApplicationSummary sum = m_appSums.getSummary(appName);
      if (sum == null)
      {
         synchronized (m_appSums)
         {
            updateSummaryEntry(appName);
            sum = m_appSums.getSummary(appName);
         }
      }

      // the updated summary did not exist for some reason (even though we
      // supposedly created it)
      if (sum == null)
      {
         Object[] args = { appName };
         throw new PSNotFoundException(APP_ROOT_REQD, args);
      }

      String appRoot = sum.getAppRoot();

      File appDir = PSServerXmlObjectStore.getAppRootDir(appRoot);

      // if the application dir does not exist, or it's not a directory,
      // then error
      if (!appDir.exists() || !appDir.isDirectory())
      {
         Object[] args = { ms_RootAppFileLoad, appName, appDir.getPath() };
         throw new PSNotFoundException(APP_DIR_NOT_FOUND, args);
      }

      // build an appFile from the request document, so we can get the
      // desired file name
      PSApplicationFile appFile = new PSApplicationFile();
      PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);
      Element appFileEl = walker.getNextElement("PSXApplicationFile");
      try
      {
         appFile.fromXml(appFileEl, null, null);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSServerException(e);
      }

      // if the app file does not exist under the app dir, then error
      File appFileName = new File(appDir, appFile.getFileName().getPath());
      if (!appFileName.exists())
      {
         Object[] args = { appName, appFileName.getPath() };
         throw new PSNotFoundException(APP_FILE_NOT_FOUND, args);
      }

      printMsg("Deleting application file " + appFileName.toString());

      /* build the response doc */
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(
         respDoc, "PSXDesignAppRemoveSaveResults");

      // delegate to PSServerXmlObjectStore for real deletion
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      PSApplication app = os.getApplicationObject(appName, req
            .getSecurityToken());
      IPSLockerId lockId = getEffectiveLockerId(req, uniqueId);
      boolean isRemoved = false;
      try
      {
         if(dontLockApp) {
         isRemoved = 
            os.removeApplicationFile(app, appFileName, lockId, 
                        req.getSecurityToken(), dontLockApp);
         } else {
         isRemoved = 
            os.removeApplicationFile(app, appFileName, lockId, 
                  req.getSecurityToken());
      }
      }
      catch (Exception e)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
      
      if (!isRemoved)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         String arg1 = "Could not delete " + appFileName.toString();
         Object[] args = { appFileName.getName(), arg1 };
         respDoc = fillErrorResponse(new PSServerException(HANDLER_IO_ERROR, args));
      }

      return respDoc;
   }
   
   /**
    * Renames the specified application file to the new name specified
    * if the file name is not already in use.
    *
    * @param inDoc The XML document containing the application file
    * data.
    *
    * @param req the request context (for security)
    *
    * @return The XML response document
    *
    * @throws PSServerException if the server is not responding
    *
    * @throws PSAuthorizationException if user is not authorized for operation
    *
    * @throws PSNotLockedException if the application is not or could not be 
    * locked. 
    *
    * @throws PSNotFoundException if the application could not be
    * found
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                 appropriate format for this
    *                                 request type    
    */
   public Document renameApplicationFile(Document inDoc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException,
         PSNotLockedException, PSUnknownDocTypeException,
         PSNotFoundException
   {
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, ms_RootAppFileRename);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(XML_ELEMENT_NULL,
         ms_RootAppFileRename);

      //make sure we got the correct root node tag
      if (false == ms_RootAppFileRename.equals (root.getNodeName()))
      {
         Object[] args = { ms_RootAppFileRename, root.getNodeName() };
         throw new PSUnknownDocTypeException(XML_ELEMENT_WRONG_TYPE, args);
      }

      // get the name of the application
      final String appName = getAppNameFromAttribute(root, "application");
      // get the name of the target application
      final String targetAppName = getAppNameFromAttribute(root, "targetApplication");
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);

      boolean dontLockApp = false;
      if(root.getAttribute("dontLockApp") != null && root.getAttribute("dontLockApp").equals("yes")) {
         if(ms_serverProps == null) {
            ms_serverProps = PSServer.getServerProps();
         }
         if(ms_serverProps.get(DONT_LOCK_APPLICATION_FLAG) != null 
               && ms_serverProps.get(DONT_LOCK_APPLICATION_FLAG).equals("true")) {
            dontLockApp = true;
         }
      }

      try {
         if (!checkApplicationSecurity(appName, PSAclEntry.AACE_DESIGN_DELETE, req))
         {
            throw new PSAuthorizationException(
               "renameApplicationFile", appName, req.getUserSessionId());
         }

         if(!dontLockApp) {
         extendApplicationLock(
               req, getApplicationIdFromName(appName), uniqueId, 30, true);
         extendApplicationLock(
               req, getApplicationIdFromName(targetAppName), uniqueId, 30, true);
         }
      } catch (Exception e)
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }

      
      // build an appFile from the request document, so we can get the
      // desired file name
      final PSApplicationFile appFile;
      final PSApplicationFile targetAppFile;
      {
         PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);
         appFile = getNextApplicationFile(walker);
         targetAppFile = getNextApplicationFile(walker);
      }

      // if the app file does not exist under the app dir, then error
      final File appFileName = getAbsoluteFileName(appName, appFile);
      if (!appFileName.exists())
      {
         Object[] args = { appName, appFileName.getPath() };
         throw new PSNotFoundException(APP_FILE_NOT_FOUND, args);
      }

      // cannot rename if an appfile with the new name already exists
      final File targetFileName = getAbsoluteFileName(targetAppName, targetAppFile);
      if (targetFileName.exists())
      {
         Object[] args = { appFileName.toString(), targetFileName.toString()};
         throw new PSServerException(APP_FILE_EXISTS_RENAME_ERROR, args);
      }
      // ... or its parent directory does not exist
      if (!targetFileName.getParentFile().exists())
      {
         Object[] args = { appName, targetFileName.getParentFile().getPath() };
         throw new PSNotFoundException(APP_FILE_NOT_FOUND, args);
      }

      printMsg("Renaming application file " 
         + appFileName.toString() + " to " + targetFileName.toString());

      /* build the response doc */
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(
         respDoc, "PSXDesignAppRenameSaveResults");

      // now attempt to rename the application file
      if (!appFileName.renameTo(targetFileName))
      {
         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         String arg1 = 
            "Could not rename " + appFileName.toString() 
            + "to " + targetFileName.toString();
         Object[] args = { appFileName.getName(), arg1 };
         respDoc = fillErrorResponse(new PSServerException(HANDLER_IO_ERROR, args));
      }

      return respDoc;
   }

   /**
    * Composes absolute file name using application name and file relative to 
    * application directory.
    */
   private File getAbsoluteFileName(final String appName, final PSApplicationFile appFile)
         throws PSNotFoundException, PSServerException
   {
      final File appDir = getApplicationDirectory(appName);
      final File appFileName = new File(appDir, appFile.getFileName().getPath());
      return appFileName;
   }

   /**
    * Gets next application file from the provided tree.
    * @param walker the tree to search.
    */
   private PSApplicationFile getNextApplicationFile(PSXmlTreeWalker walker)
         throws PSServerException
   {
      final PSApplicationFile appFile = new PSApplicationFile();
      try
      {
         final Element appFileEl = walker.getNextElement(PSApplicationFile.ms_nodeType);
         appFile.fromXml(appFileEl, null, null);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSServerException(e);
      }
      return appFile;
   }

   /**
    * Retrieves application name from provided attribute.
    * @throws PSUnknownDocTypeException if nothing was found in the attribute.
    */
   private String getAppNameFromAttribute(Element root, String attributeName)
      throws PSUnknownDocTypeException
   {
      final String  appName = root.getAttribute(attributeName);
      if (appName == null || appName.length() == 0)
      {
         Object[] args = { ms_RootAppFileLoad, attributeName, appName };
         throw new PSUnknownDocTypeException(XML_ELEMENT_INVALID_ATTR, args);
      }
      return appName;
   }

   /**
    * Returns application directory for the given application name.
    */
   private File getApplicationDirectory(String appName) throws PSNotFoundException, PSServerException
   {
      // we assume the request root is the application directory
      final PSApplicationSummary sum = getApplicationSummary(appName);
      final File appDir = PSServerXmlObjectStore.getAppRootDir(sum.getAppRoot());

      // if the application dir does not exist, or it's not a directory,
      // then error
      if (!(appDir.exists() && appDir.isDirectory()))
      {
         Object[] args = { ms_RootAppFileLoad, appName, appDir.getPath() };
         throw new PSNotFoundException(APP_DIR_NOT_FOUND, args);
      }
      return appDir;
   }

   /**
    * Gives application summary for the provided application name.
    * @throws PSNotFoundException if the summary is not found
    * @throws PSServerException on server error
    */
   private PSApplicationSummary getApplicationSummary(String appName) throws PSNotFoundException, PSServerException
   {
      PSApplicationSummary sum = m_appSums.getSummary(appName);
      if (sum == null)
      {
         synchronized (m_appSums)
         {
            updateSummaryEntry(appName);
            sum = m_appSums.getSummary(appName);
         }
      }

      // the updated summary did not exist for some reason (even though we
      // supposedly created it)
      if (sum == null)
      {
         Object[] args = { appName };
         throw new PSNotFoundException(APP_ROOT_REQD, args);
      }
      return sum;
   }

   /**
    * Get the request processing statistics for this object store.
    *
    * @return      the statistics
    */
   public PSObjectStoreStatistics getStatistics()
   {
      return m_statistics;
   }


   /* ************ IPSRequestHandler Interface Implementation ************ */

   /**
    * Process the request using the input context information and data.
    * The results must be written to the specified output stream.
    *
    * @param   request      the request object containing all context
    *                        data associated with the request
    */
   public void processRequest(PSRequest request)
   {
      /* mark that we have a pending event we're working on */
      m_statistics.incrementPendingEventCount();

      // and prepare for statistical reporting
      PSRequestStatistics reqStats = request.getStatistics();

      Throwable thrownError = null;
      try {
         Document inDoc = request.getInputDocument();
         Document respDoc = null;

         if (inDoc == null) {
            thrownError = new IllegalArgumentException("req document null");
            respDoc = fillErrorResponse(thrownError);
         }
         else {
            /* use the request type as the index into our hash */
            String reqType =
                  request.getCgiVariable( IPSCgiVariables.CGI_PS_REQUEST_TYPE );
           PSSecuredMethod rhMethod =
                  (PSSecuredMethod)m_requestHandlerMethods.get( reqType );
            if (rhMethod == null) {
               thrownError = new IllegalArgumentException("req unknown type: " +
                     reqType);
               respDoc = fillErrorResponse(thrownError);
            }
            else {
               /* now invoke it to get the response doc */
               try {
                  Object[] args = { inDoc, request };
                  respDoc = (Document)rhMethod.invoke(request, this, args);
               } catch (Throwable e) {
                  if (e instanceof java.lang.reflect.InvocationTargetException)
                     thrownError = ((java.lang.reflect.InvocationTargetException)e).getTargetException();
                  else
                     thrownError = e;

                  // build the response for the user
                  respDoc = fillErrorResponse(thrownError);
               }
            }
         }

         /* build the response */
         PSResponse resp = request.getResponse();
         if (resp == null) {
            thrownError = new PSServerException(MALFORMED_RESPONSE_DOCUMENT);
            respDoc = fillErrorResponse(thrownError);
         }

         /* and send the doc to the requestor */
         resp.setContent(respDoc,
            IPSMimeContentTypes.MIME_TYPE_TEXT_XML
            + "; charset=" + PSCharSets.rxStdEnc());
      } finally {
         /* mark this as a failure for statistical reporting */
         if (thrownError != null)
            reqStats.setFailure();

         /* Update the app's statistics, even if stats logging is disabled.
          * Since we maintain performance metrics about each application, this
          * is an important piece of information to track.
          */
         m_statistics.update(reqStats);

         // log the request info
         m_LogHandler.logBasicUserActivity(request);
         m_LogHandler.logDetailedUserActivity(request);
         if (thrownError != null) {
            if (thrownError instanceof PSException)
            {
               PSException psex = (PSException) thrownError;
               PSLogManager.write(new PSLogServerWarning(psex.getErrorCode(),
                  psex.getErrorArguments(), true, "ObjectStore"));
            }
            else
            {
               PSLogManager.write(new PSLogServerWarning(HANDLER_IO_ERROR,
                  new Object[]
                  {"processRequest", thrownError.toString()}, true,
                  "ObjectStore"));
            }
         }
      }
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      // we have nothing to shutdown currently
   }

   /**
    * Constructs an application object for the specified application. The
    * application is loaded from the object store when this method is called.
    * If the application is being loaded for editing, be sure to lock the
    * application.
    *
    * @param app the name of the application
    *
    * @return the application object
    *
    * @throws PSNotFoundException if an application be that name
    * does not exist
    *
    * @throws PSServerException   for any other errors encountered
    *
    * @see   com.percussion.design.objectstore.PSApplication
    */
   public PSApplication getApplicationObject(String app)
      throws PSServerException, PSNotFoundException
   {
      Document doc = loadApplication(app);
      try
      {
         PSApplication retApp = createApplication();
         retApp.fromXml(doc);
         return retApp;
      }
      catch (PSUnknownDocTypeException e)
      {
         Object[] args = new Object[] { app, e.toString() };
         throw new PSServerException(APP_LOAD_EXCEPTION, args);
      }
      catch (PSUnknownNodeTypeException e)
      {
         Object[] args = new Object[] { app, e.toString() };
         throw new PSServerException(APP_LOAD_EXCEPTION, args);
      }
   }

   /**
    * Gets the server configuration object for this server.
    *
    * @throws PSServerException   if the server is not responding
    */
   public PSServerConfiguration getServerConfigurationObject()
      throws PSServerException
   {
      Document doc = loadServerConfig();
      PSServerConfiguration config = createServerConfiguration();
      try
      {
         config.fromXml(doc);
         return config;
      }
      catch (PSUnknownDocTypeException e)
      {
         throw new PSServerException(SERVER_CFG_LOAD_EXCEPTION, e.toString());
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSServerException(SERVER_CFG_LOAD_EXCEPTION, e.toString());
      }
   }

   /**
    * Loads the role configuration object for this server. If the request
    * is for editing, an attempt will be made to acquire the config lock.
    *
    * @param inDoc The xml document containing a description of the request.
    *    Throws exception if <code>null</code>.
    *
    * @param req The request context (for security). Throws exception if
    *    <code>null</code>.
    *
    * @return The role configuration object in its XML format, or an error
    *    document if a problem occurs trying to create the config.
    *
    * @see com.percussion.design.objectstore.PSRoleConfiguration
    */
   public Document getRoleConfiguration( Document inDoc, PSRequest req)
   {
      if ( null == inDoc || null == req )
         throw new IllegalArgumentException( "params can't be null" );

      // variables needed in catch block
      boolean lockCfg = false;
      boolean isLocked = false;
      IPSLockerId lockId = null;
      PSServerXmlObjectStore os = null;
      try
      {
         Element root = inDoc.getDocumentElement();
         if (root == null)
            throw new PSUnknownDocTypeException(
               XML_ELEMENT_NULL, ms_RootServerConfigLoad);

         //make sure we got the correct root node tag
         if (false == ms_RootRoleConfigLoad.equals (root.getNodeName()))
         {
            Object[] args = { ms_RootRoleConfigLoad, root.getNodeName() };
            throw new PSUnknownDocTypeException( XML_ELEMENT_WRONG_TYPE, args );
         }

         // if the cfg is to be opened in edit mode, attempt to acquire a lock
         String mode = root.getAttribute("mode");
         if (mode != null && mode.equals("edit"))
            lockCfg = true;

         boolean overrideSameUser = false;
         String over = root.getAttribute("overrideSameUser");
         if (over != null && over.equals("yes"))
            overrideSameUser = true;

         boolean overrideDifferentUser = false;
         over = root.getAttribute("overrideDifferentUser");
         if (over != null && over.equals("yes"))
            overrideDifferentUser = true;
         String uniqueId = root.getAttribute(ATTR_UNIQUEID);
         // create the response document
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element respRoot = PSXmlDocumentBuilder.createRoot(
               respDoc, "PSXDesignServerRolesLoadResults");

         Document cfgDoc;
         os = PSServerXmlObjectStore.getInstance();

         if (lockCfg)
         {
            lockId = getEffectiveLockerId(req, uniqueId, overrideSameUser,
                  overrideDifferentUser );
            os.getServerConfigLock(lockId, 30);
            isLocked = true;
         }

         cfgDoc = os.getRoleConfigurationObject( req.getSecurityToken()).toXml();
         Element cfgRoot = cfgDoc.getDocumentElement();
         Node importNode = respDoc.importNode(cfgRoot, true);
         respRoot.appendChild(importNode);
         return respDoc;
      }
      catch (Exception e)
      {
         try
         {
            if (lockCfg && isLocked)
               os.releaseServerConfigLock(lockId);
         }
         catch ( PSServerException se )
         { /* ignore if we fail to unlock, not much we can do */ }

         PSRequestStatistics reqStats = req.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }


   /**
    * Get all the applications defined in the object store.
    *
    * @param      enabledOnly                  <code>true</code> to return
    *                                          only enabled applications
    *
    * @return                                 an array of PSApplication
    *                                          objects
    *
    * @throws PSServerException            if the server is not responding
    *
    * @throws PSAuthorizationException    if the user does not have design
    *                                          access to the server
    */
   public PSApplication[] getApplicationObjects(boolean enabledOnly)
      throws PSServerException, PSAuthorizationException
   {
      /* *TODO* NOT HONORING SECURITY FOR PHASE I !!! */

      java.util.ArrayList appList = new java.util.ArrayList();
      String[] apps = m_objectDirectory.list();
      for (int i = 0; i < apps.length; i++) {
         if (apps[i].toLowerCase().endsWith(".xml")) {
            String appName = apps[i].substring(0, apps[i].length() - 4);
            try 
            {
               PSApplication app = getApplicationObject(appName);

               // if the caller only wants enabled objects, only return
               // enabled objects
               if (enabledOnly && !app.isEnabled())
                  continue;

               if (!app.getName().equalsIgnoreCase(appName))
               {
                  PSLogManager.write(new PSLogServerWarning(
                     APP_FILE_ROOT_MISMATCH, new Object[]
                     {apps[i], app.getName()}, true, "ObjectStore"));
                  continue;
               }

               appList.add(app);
            }
            catch (Exception e)
            {
               PSLogManager.write(new PSLogServerWarning(APP_LOAD_EXCEPTION,
                  new Object[]
                  {appName, e.toString()}, true, "ObjectStore"));
            }
         }
      }

      PSApplication[] retApps= new PSApplication[appList.size()];
      if (appList.size() > 0)
         appList.toArray(retApps);

      return retApps;
   }

   /**
    * Creates an array of application summary objects. If the user has
    * read access to the application, or is a server administrator, they
    * will be shown the application in the summaries. However, an attempt
    * to access the design of an application they are not readers on will
    * fail.
    *
    * @param req the request context (for security), may not be
    * <code>null</code>.
    *
    * @return an array of PSApplicationSummary objects which the user is allowed
    * to see (may be empty), sorted ascending by application name
    */
   public PSApplicationSummary[] getApplicationSummaryObjects(PSRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      return os.getApplicationSummaryObjects(req.getSecurityToken(),
         req.showHiddenApplications());
   }

   /**
    * Validates the given application object with the default validator
    * for the object store. Returns <CODE>true</CODE> if validation
    * passed, returns <CODE>false</CODE> if validation fails or if validation
    * could not be performed.
    *
    * @author   chadloder
    *
    * @version 1.9 1999/06/30
    *
    * @param   app The application to be validated.
    *
    *
    * @throws   PSServerException
    * @throws   PSAuthorizationException
    * @throws PSSystemValidationException ;
    *
    */
   public boolean validateApplicationObject(PSApplication app)
      throws PSServerException, PSAuthorizationException, PSSystemValidationException
   {
      try
      {
         PSValidatorAdapter validator = new PSValidatorAdapter(this);
         validator.throwOnErrors(true);
         validator.validateApplication(app);
         return true;
      }
      catch (PSSystemValidationException valex)
      {
         // ignore, we simply return false below
      }
      catch (Exception e)
      {
         PSLogManager.write(new PSLogServerWarning(
            VALIDATION_UNEXPECTED_EXCEPTION, new Object[]
            {app.getName(), e.toString()}, true, "ObjectStore"));
      }
      return false;
   }

   /**
    * Extends the application lock for the given application. This method
    * does NO checking on whether the name refers to a valid application.
    *
    * @author   chadloder
    *
    * @version 1.42 1999/07/15
    *
    * @param   req
    * @param   appName
    * @param uniqueId a unique Id supplied by the client used to hold a lock,
    * we default to the user's session id if this value is 
    * <code>null</code> or empty.
    * @param   lockMins
    *
    * @throws   PSServerException
    * @throws   PSLockedException
    * @throws   PSNotFoundException
    * @throws   PSLockAcquisitionException
    */
   private void extendApplicationLock(PSRequest req, String appName, String uniqueId, 
      int lockMins, boolean overrideSameUser)
      throws PSServerException, PSLockedException, PSNotFoundException,
         PSLockAcquisitionException
   {
      // get the locker id from the request's user session
      IPSLockerId locker = getEffectiveLockerId(req, uniqueId, overrideSameUser);

      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      os.getApplicationLock(locker, appName, lockMins);
   }

   /**
    * Extends an application lock on the app with the given id. The app must
    * exist in the summaries.
    *
    * @author   chadloder
    *
    * @version 1.42 1999/07/15
    *
    *
    * @param   req
    * @param   applid
    * @param uniqueId a unique Id supplied by the client used to hold a lock,
    * we default to the user's session id if this value is 
    * <code>null</code> or empty.
    * @param   lockMins
    *
    *
    * @throws   PSServerException
    * @throws   PSLockedException
    * @throws   PSNotFoundException
    * @throws   PSLockAcquisitionException
    */
   private void extendApplicationLock(PSRequest req, int applid, String uniqueId, 
      int lockMins, boolean overrideSameUser)
      throws PSServerException, PSLockedException, PSNotFoundException,
         PSLockAcquisitionException
   {
      PSApplicationSummary sum = m_appSums.getSummary(applid);
      if (sum == null)
      {
         throw new PSNotFoundException(APP_NOT_FOUND, "id=" + applid);
      }

      extendApplicationLock(
         req, sum.getName(), uniqueId, lockMins, overrideSameUser);
   }

   /**
    * Releases the lock for the application with the given name. Does
    * NO validation on whether the name refers to a real application.
    *
    * @author   chadloder
    *
    * @version 1.42 1999/07/15
    *
    * @param   appName
    * @param   req
    * @param uniqueId a unique Id supplied by the client used to hold a lock,
    * we default to the user's session id if this value is 
    * <code>null</code> or empty.
    *
    *
    * @throws   PSServerException if there is an error releasing the lock.
    *
    */
   private void releaseApplicationLock(String appName, PSRequest req, String uniqueId)
      throws PSServerException
   {
      IPSLockerId locker = getEffectiveLockerId(req, uniqueId);

      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      os.releaseApplicationLock(locker, appName);
   }

   /**
    * Releases the lock for the application with the given id.
    *
    * @author   chadloder
    *
    * @version 1.42 1999/07/15
    *
    *
    * @param   applid
    * @param   req
    * @param uniqueId a unique Id supplied by the client used to hold a lock,
    * we default to the user's session id if this value is 
    * <code>null</code> or empty.
    *
    *
    * @throws   PSServerException if there is an error releasing the lock.
    * @throws   PSNotFoundException
    *
    */
   private void releaseApplicationLock(int applid, PSRequest req, String uniqueId)
      throws PSServerException, PSNotFoundException
   {
      PSApplicationSummary sum = m_appSums.getSummary(applid);
      if (sum == null)
      {
         throw new PSNotFoundException(APP_NOT_FOUND, "id=" + applid);
      }

      releaseApplicationLock(sum.getName(), req, uniqueId);
   }

   /**
    *  Locks the server config file
    * @param req
    * @param uniqueId a unique Id supplied by the client used to hold a lock,
    * we default to the user's session id if this value is 
    * <code>null</code> or empty.
    * @param lockMins
    * @param overrideSameUser
    * @throws PSServerException
    * @throws PSLockedException
    * @throws PSLockAcquisitionException
    * @throws PSAuthorizationException
    * @throws PSNotFoundException
    * @throws PSServerException
    */
   private void lockServerConfiguration(
      PSRequest req, String uniqueId, int lockMins, boolean overrideSameUser)
      throws PSServerException, PSLockedException,
         PSLockAcquisitionException, 
         PSAuthorizationException, PSNotFoundException, PSServerException
   {
      // get the locker id from the request's user session
      IPSLockerId locker = getEffectiveLockerId(req, uniqueId, overrideSameUser);

      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      os.getServerConfigLock(locker, lockMins);
   }

   /**
    * Private utility method used to get an effective locker id for a request
    * @author   chadloder
    *
    * @version 1.36 1999/07/07
    *
    *
    * @param   req
    *
    */
   private String getEffectiveUser(PSRequest req)
   {
      // get the locker id from the request's user session
      String locker = "";

      if (req != null)
      {
         PSUserSession sess = req.getUserSession();
         if (sess != null)
         {
            PSUserEntry[] userEntries = sess.getAuthenticatedUserEntries();
            if (userEntries != null)
            {
               for (int i = 0; i < userEntries.length; i++)
               {
                  locker = userEntries[i].getName();
                  break;
               }
            }
         }
      }
      
      return locker;
   }


   /**
    * Convenience method.
    * Calls {@link #getEffectiveLockerId(PSRequest, String, boolean, boolean)
    * getEffectiveLockerId(req, uniqueId, false, false)}.
    */
   private IPSLockerId getEffectiveLockerId(PSRequest req, String uniqueId)
   {
      return getEffectiveLockerId(req, uniqueId, false, false);
   }


   /**
    * Convenience method.
    * Calls {@link #getEffectiveLockerId(PSRequest, String, boolean, boolean)
    * getEffectiveLockerId(req, uniqueId overrideSameUser, false)}.
    */
   private IPSLockerId getEffectiveLockerId(PSRequest req, String uniqueId,
         boolean overrideSameUser)
   {
      return getEffectiveLockerId(req, uniqueId, overrideSameUser, false);
   }

   /**
    * Creates a locker identifier for the user in the supplied request with
    * the supplied flags.
    *
    * @param req The context of the current request. Never <code>null</code>.
    * 
    * @param uniqueId a unique Id supplied by the client used to hold a lock,
    * we default to the user's session id if this value is 
    * <code>null</code> or empty.
    *
    * @param overrideSameUser If <code>true</code>, when the lock is checked
    *    against an existing lock, if it is owned by this user, or this user
    *    in a different session, they will gain the lock. Otherwise, they
    *    will only gain the lock if their particular session currently owns
    *    it.
    *
    * @param overrideDifferentUser If <code>true</code>, when the lock is
    *    checked against an existing lock, no matter who owns it, they will
    *    gain control. This should only be used in very special circumstances.
    *    This flag implies overrideSameUser.
    */
   private IPSLockerId getEffectiveLockerId(PSRequest req, String uniqueId,
         boolean overrideSameUser, boolean overrideDifferentUser )
   {
      String userName = getEffectiveUser(req);
      if(uniqueId == null || uniqueId.trim().length() == 0)
         uniqueId = req.getUserSessionId();     
      return new PSXmlObjectStoreLockerId(userName, overrideSameUser,
            overrideDifferentUser, uniqueId);
   }


   /**
    * Remove an application
    * @param applId
    * @param req
    * @param uniqueId a unique Id supplied by the client used to hold a lock,
    * we default to the user's session id if this value is 
    * <code>null</code> or empty.
    * @throws PSServerException
    * @throws PSLockedException
    * @throws PSNotFoundException
    * @throws PSLockAcquisitionException
    * @throws PSNotLockedException 
    * @throws PSAuthorizationException 
    * @throws PSAuthenticationRequiredException 
    */
   private void doRemoveApplication(int applId, PSRequest req, String uniqueId)
      throws PSServerException, PSLockedException, PSNotFoundException,
         PSLockAcquisitionException, PSAuthenticationRequiredException, PSAuthorizationException, PSNotLockedException
   {
      String appName = getApplicationNameFromId(applId);
      File appFile = getApplicationFile(appName);

      IPSLockerId lockId = null;
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      
      if (appFile.exists()) 
      {
         lockId = getEffectiveLockerId(req, uniqueId);
         os.getApplicationLock(lockId, appName, 30);

         try
         {
            PSServerXmlObjectStore.getInstance().deleteApplication(applId, 
               lockId, req.getSecurityToken());
         }
         finally
         {
            // release the lock, even if we didn't lock it
            releaseApplicationLock(appName, req, uniqueId);
         }
      }
      // silently ignore no such object, etc.
   }

   /**
    * 
    * @param appName
    * @param req
    * @param uniqueId a unique Id supplied by the client used to hold a lock,
    * we default to the user's session id if this value is 
    * <code>null</code> or empty.
    * @throws PSServerException
    * @throws PSLockedException
    * @throws PSNotFoundException
    * @throws PSLockAcquisitionException
    * @throws PSNotLockedException 
    * @throws PSAuthorizationException 
    * @throws PSAuthenticationRequiredException 
    */
   private void doRemoveApplication(
      String appName, PSRequest req, String uniqueId)
      throws PSServerException, PSLockedException, PSNotFoundException,
         PSLockAcquisitionException, PSAuthenticationRequiredException, PSAuthorizationException, PSNotLockedException
   {
      doRemoveApplication(getApplicationIdFromName(appName), req, uniqueId);
   }

   /**
    * Rename the application
    * @param appName
    * @param newName
    * @param req
    * @param uniqueId a unique Id supplied by the client used to hold a lock,
    * we default to the user's session id if this value is 
    * <code>null</code> or empty.
    * @throws PSServerException
    * @throws PSAuthorizationException
    * @throws PSLockedException
    * @throws PSNonUniqueException
    * @throws PSNotFoundException
    * @throws PSUnknownDocTypeException
    * @throws PSUnknownNodeTypeException
    * @throws PSLockAcquisitionException
    * @throws IOException
    * @throws PSAuthenticationFailedException
    */
   private void doRenameApplication(String appName, String newName, 
      PSRequest req, String uniqueId)
      throws   PSServerException, PSAuthorizationException,
               PSLockedException, PSNonUniqueException,
               PSNotFoundException, PSUnknownDocTypeException,
               PSUnknownNodeTypeException,
               PSLockAcquisitionException, IOException,
               PSAuthenticationFailedException
   {
      // get a lock on the application (old-name)
      extendApplicationLock(req, appName, uniqueId, 30, false);

      // save the id to release later since the name may or may not have
      // been changed in the summary
      int appId = getApplicationIdFromName(appName);

      try
      {

         // get the current application object and change its internal name
         PSApplication app = getApplicationObject(appName);
         if (app == null) {
            throw new PSNotFoundException(APP_NOT_FOUND, appName);
         }

         try {
            // set the name
            app.setName(newName);

            // change the root to match the new app name only if
            //   the current root matches the old name
            if (appName.equals(app.getRequestRoot()))
            {
               app.setRequestRoot(newName);
            }
         } catch (IllegalArgumentException e) {
            throw new PSServerException(0, e.getLocalizedMessage());
         }

         doSaveApplication(app,
            false,
            false,
            false,
            req,
            uniqueId,
            "Renamed " + appName + " to " + newName);

      }
      catch (PSNotLockedException e)
      {
         PSLogManager.write(new PSLogServerWarning(
            HANDLER_UNEXPECTED_EXCEPTION, new Object[]
            {e.toString()}, true, "ObjectStore"));
      }
      catch (PSSystemValidationException e)
      {
         PSLogManager.write(new PSLogServerWarning(
            HANDLER_UNEXPECTED_EXCEPTION, new Object[]
            {e.toString()}, true, "ObjectStore"));
      }
      finally
      {
         // release a lock on the old name of the application
         releaseApplicationLock(appId, req, uniqueId);
      }
   }

   private boolean doSaveApplication(PSApplication app,
                                     boolean      releaseLock,
                                     boolean      validate,
                                     boolean      createNewApp,
                                     PSRequest   req,
                                     String uniqueId,
                                     String saveDescription)
      throws   PSServerException, PSAuthorizationException,
               PSNotLockedException, PSNonUniqueException,
           PSSystemValidationException, PSUnknownDocTypeException,
               PSUnknownNodeTypeException, PSLockedException,
               IOException, PSNotFoundException, PSLockAcquisitionException,
               PSAuthenticationFailedException
   {
      String appName = app.getName();
      if ((appName == null) || (appName.length() == 0)) {
         Object[] args = { ms_RootAppSave, "PSXApplication/name", "" };
         throw new PSUnknownDocTypeException(XML_ELEMENT_INVALID_CHILD, args);
      }

      File appFile = getApplicationFile(appName);

      boolean fileExists = appFile.exists();
      PSApplicationSummary oldSum = m_appSums.getSummary(app.getId());
      boolean didExist = ((oldSum != null)  && (!createNewApp));

      IPSLockerId lockId = null;
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();

      try
      {
         /* if we're in create only mode, it better not exist already */
         if (fileExists && (createNewApp || app.getId() < 1)) {
            throw new PSNonUniqueException(APP_NAME_ALREADY_EXISTS, appName);
         }

         lockId = getEffectiveLockerId(req, uniqueId);
         os.getApplicationLock(lockId, appName, 30);

         // and add a new revision, bumping the version number
         app.setRevision(getEffectiveUser(req), saveDescription);

         // finally, write it to the disk file
         os.saveApplication(app, lockId, req.getSecurityToken(), validate);

         // Update summary with new revision
         synchronized (m_appSums)
         {
            PSApplicationSummary sum = m_appSums.getSummary(app.getId());

            // update revision info in summary for all cases
            PSRevisionHistory hist = app.getRevisionHistory();

            sum.setMajorMinorVersion(
               hist.getLatestMajorVersion(),
               hist.getLatestMinorVersion());
            sum.setFileLastModified(appFile.lastModified());
         }

         return !didExist;
      }
      finally
      {
         if (releaseLock && lockId != null)
         {
            // release the lock on the application
            os.releaseApplicationLock(lockId, appName);
            releaseApplicationLock(appName, req, uniqueId);
         }
      }
   }

   /* If they can't change the acl, then don't allow them to save...
      (If the acl has changed...)
   */
   void checkForDisallowedAclModification(
      PSApplication app,
      PSRequest req)
         throws PSAuthorizationException
   {
      PSAcl newAcl = null;
      PSAcl oldAcl = null;

      String appName = app.getName();
      /* Grab the old document */
      try {
         File             appFile    = getApplicationFile(appName);
         Document       doc       = loadApplicationFromFile(appFile);
         PSApplication    oldApp    = new PSApplication(doc);
         oldAcl   = oldApp.getAcl();
         newAcl   = app.getAcl();
      } catch (Exception e)
      {
         throw new PSAuthorizationException("AclModificationCheck", appName,
                                             req.getUserSessionId());
      }
      if (!newAcl.getEntries().equals(oldAcl.getEntries()))
         throw new PSAuthorizationException("AclModification", appName,
                                             req.getUserSessionId());
   }

   Document loadApplication(String app)
      throws PSNotFoundException, PSServerException
   {
      File appFile = getApplicationFile(app);
      try
      {
         Document doc = loadApplicationFromFile(appFile);

         boolean converted = PSDocVersionConverter2.convertApplicationDocument(
            PSServer.getExtensionManager(this),
            doc);

         if (converted)
         {
            OutputStream fout = new BufferedOutputStream(lockOutputStream(appFile));
            try
            {
               PSXmlDocumentBuilder.write(doc, fout);
            }
            finally
            {
               releaseOutputStream(fout, appFile);
            }
         }

         return doc;
      }
      catch (PSServerException e)
      {
         throw e;
      }
      catch (PSNotFoundException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         Object[] args = { appFile.getName(), e.toString() };
         throw new PSServerException(APP_LOAD_EXCEPTION, args);
      }
   }

   private Document loadApplicationFromFile(File appFile)
      throws PSNotFoundException, PSServerException
   {
      Document doc = null;
      InputStream fIn = null;

      try {
         fIn = lockInputStream(appFile);
         doc = PSXmlDocumentBuilder.createXmlDocument(fIn, false);
      } catch (FileNotFoundException e) {
         throw new PSNotFoundException(APP_NOT_FOUND, appFile.getName());
      } catch (Exception e) {
         Object[] args = { appFile.getName(), e.toString() };
         throw new PSServerException(APP_LOAD_EXCEPTION, args);
      } finally {
         if (fIn != null) {
            try { releaseInputStream(fIn, appFile); } catch (Exception e) { /* ignore */ }
         }
      }

      return doc;
   }

   File getApplicationFile(String appName)
   {
      return new File( m_objectDirectory, appName + ".xml");
   }

   private void doSaveUserConfiguration(Document cfgTree, PSRequest req)
      throws   PSServerException, PSAuthorizationException,
               PSNotLockedException, PSNonUniqueException,
           PSSystemValidationException, PSUnknownDocTypeException,
               IOException
   {
      if (cfgTree == null) {
         Object[] args = { ms_RootUserConfigSave, "PSXUserConfiguration", "" };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_INVALID_CHILD, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(cfgTree);

      String cfgName = walker.getElementData("name");
      if ((cfgName == null) || (cfgName.length() == 0)) {
         Object[] args = { ms_RootUserConfigSave, "PSXUserConfiguration/name", "" };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_INVALID_CHILD, args);
      }

      boolean authenticated = false;
      PSUserSession sess = req.getUserSession();
      PSUserEntry[] entries = sess.getAuthenticatedUserEntries();
      for (int i = 0; i < entries.length; i++)
      {
         if (entries[i].getName().equalsIgnoreCase(cfgName))
         {
            authenticated = true;
            break;
         }
      }

      if (!authenticated)
      {
         throw new PSAuthorizationException(
            "saveUserConfiguration", cfgName, req.getUserSessionId());
      }

      File cfgFile = getUserConfigFile(cfgName);
      OutputStream fout = lockOutputStream(cfgFile);
      try
      {
         PSXmlDocumentBuilder.write(cfgTree, fout);
      }
      finally
      {
         releaseOutputStream(fout, cfgFile);
      }
   }

   private void doRemoveUserConfiguration(String name)
      throws PSServerException
   {
      File cfgFile = getUserConfigFile(name);
      if (cfgFile.exists()) {
         cfgFile.delete();
      }

      /* no such object is silently ignored! */
   }

   private Document loadUserConfig(String name)
      throws PSNotFoundException, PSServerException
   {
      File cfgFile = getUserConfigFile(name);
      if (!cfgFile.exists()) {
         throw new PSNotFoundException(
            USER_CFG_NOT_FOUND, name);
      }

      Document doc = null;
      InputStream fIn = null;
      try {
         fIn = lockInputStream(cfgFile);
         doc = PSXmlDocumentBuilder.createXmlDocument(fIn, false);
      } catch (FileNotFoundException e) {
         throw new PSNotFoundException(
            USER_CFG_NOT_FOUND, name);
      } catch (Exception e) {
         Object[] args = { name, e.toString() };
         throw new PSServerException(
            USER_CFG_LOAD_EXCEPTION, args);
      } finally {
         if (fIn != null) {
            try { releaseInputStream(fIn, cfgFile); } catch (Exception e) { /* ignore */ }
         }
      }

      return doc;
   }

   private File getUserConfigFile(String userName)
   {
      File userDir;
      userDir = new File(m_objectDirectory, "UserConfigurations");
      if (!userDir.exists())
         userDir.mkdir();

      return new File( userDir, userName + ".xml");
   }


   private void doSaveServerConfig(
      Document srvCfgDoc,
      PSRequest req,
      String uniqueId,
      boolean releaseLock
      )
      throws   PSServerException, PSAuthorizationException,
               PSNotLockedException, PSNonUniqueException,
           PSSystemValidationException, PSUnknownDocTypeException,
               PSUnknownNodeTypeException, PSNotFoundException,
               PSLockAcquisitionException, PSLockedException,
               IOException
   {
      PSXmlTreeWalker walker = new PSXmlTreeWalker(srvCfgDoc);
      Element cfgEl = walker.getNextElement("PSXServerConfiguration", true, true);
      PSXmlDocumentBuilder.replaceRoot(srvCfgDoc, cfgEl);
      PSServerConfiguration cfg = new PSServerConfiguration(srvCfgDoc);

      // Lock the config
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      IPSLockerId lockId = getEffectiveLockerId(req, uniqueId);
      os.getServerConfigLock(lockId, 30);

      try
      {
         os.saveServerConfig(cfg, lockId, req.getSecurityToken());
      }
      finally
      {
         if (releaseLock)
         {
            os.releaseServerConfigLock(lockId);
         }
      }
   }

   Document loadServerConfig()
      throws PSServerException
   {
      File cfgFile = getServerConfigFile();
      if (!cfgFile.exists()) {
         throw new PSServerException(
            SERVER_CFG_NOT_FOUND);
      }

      Document doc = null;
      InputStream fIn = null;
      try {
         fIn = lockInputStream(cfgFile);
         doc = PSXmlDocumentBuilder.createXmlDocument(fIn, false);
      } catch (FileNotFoundException e) {
         throw new PSServerException(
            SERVER_CFG_NOT_FOUND);
      } catch (Exception e) {
         Object[] args = { e.toString() };
         throw new PSServerException(
            SERVER_CFG_LOAD_EXCEPTION, args);
      } finally {
         if (fIn != null) {
            try { releaseInputStream(fIn, cfgFile); } catch (Exception e) { /* ignore */ }
         }
      }

      return doc;
   }

   File getServerConfigFile()
   {
      return PSProperties.getConfig(PSServer.ENTRY_NAME, CONFIG_FILE,
         PSServer.getRxConfigDir());

   }

   private Document fillErrorResponse(Throwable t)
   {
      return com.percussion.error.PSErrorHandler.fillErrorResponse(t);
   }

   /*
    * Load the character encoding map from the installation root directory
    * into a document.
    *
    * @return Document the character encoding map.
    * @throws PSNotFoundException if the file CSMAPS.XML is not found
    * @throws PSServerException if the character mappings could not be loaded.
    */
   private Document loadCharacterSetMap()
      throws PSNotFoundException, PSServerException
   {
      String fileName =  PSProperties.getConfig(PSServer.ENTRY_NAME,
      PSCharSets.CS_MAP_FILE,PSServer.getRxConfigDir()).getAbsolutePath();

      File csMapFile = new File(fileName);
      if (!csMapFile.exists())
      {
         Object[] args = { fileName };
         throw new PSNotFoundException(CHARACTER_SET_MAP_NOT_FOUND, args);
      }

      Document doc = null;
      InputStream fIn = null;
      try
      {
         fIn = lockInputStream(csMapFile);
         doc = PSXmlDocumentBuilder.createXmlDocument(fIn, false);
      }
      catch (FileNotFoundException e)
      {
         Object[] args = { fileName };
         throw new PSNotFoundException(CHARACTER_SET_MAP_NOT_FOUND, args);
      }
      catch (Exception e)
      {
         Object[] args = { fileName };
         throw new PSServerException(CHARACTER_SET_LOAD_EXCEPTION, args);
      }
      finally
      {
         if (fIn != null)
         {
            try
            {
               releaseInputStream(fIn, csMapFile);
            }
            catch (Exception e)
            { /* ignore */ }
         }
      }

      return doc;
   }

   /**
    * A utility method to get the ACL handler for an application,
    * loading it from disk if necessary.
    *
    * @param   appName The app whose acl handler should be loaded.  Assumed
    * not to be <code>null</code>.
    *
    * @return   PSAclHandler
    */
   PSAclHandler loadAclHandler(String appName)
      throws PSNotFoundException, PSServerException
   {
      Document doc               = null;
      PSApplication app            = null;
      PSAclHandler aclHandler      = null;
      boolean foundSum            = false;
      File appFile               = null;

      /* Try the application summary guys first */
      PSApplicationSummary sum = m_appSums.getSummary(appName);

      if (sum != null)
      {
         aclHandler = sum.getAclHandler();

         /* this should never happen, as we create the aclhandler and set
          * it in the summary when we save any app
          */
         if (aclHandler == null)
         {
            // we might have to do a lazy load
            PSAcl acl = sum.getAcl();

            if (acl != null)
            {
               aclHandler = new PSAclHandler(acl);
               sum.setAclHandler(aclHandler);
            }
         }
      }
      else
      {
         /* we create a summary an populate it with an aclhandler whenever we
          * create a new app, so this should not happen, but just to
          * be safe...
          */
         synchronized (m_appSums)
         {
            updateSummaryEntry(appName);
            sum = m_appSums.getSummary(appName);
         }
         if (sum != null)
            aclHandler = sum.getAclHandler();
      }


      return aclHandler;
   }

   /**
    * Checks that the given session's security matches the given security for
    * the application with the given request root.
    *
    * @author   chadloder
    *
    * @version 1.11 1999/07/12
    *
    * @param   requestRoot The request root for the application.
    * @param   accessLevel The access level.
    * @param   session The user session.
    *
    * @return   boolean <CODE>true</CODE> if the session has the requested
    * permissions for the app with the given request root, <CODE>false</CODE>
    * otherwise.
    *
    * @throws   PSServerException
    * @throws   PSNotFoundException
    *
    */
   public boolean checkApplicationSecurity(
      String requestRoot,
      int accessLevel,
      PSUserSession session
      )
      throws PSNotFoundException, PSServerException
   {
      // find the app name via the request root
      String appName = null;

      synchronized (m_appSums)
      {
         PSApplicationSummary[] sums = m_appSums.getSummaries();
         for (int i = 0; i < sums.length; i++)
         {
            if (sums[i].getAppRoot().equalsIgnoreCase(requestRoot))
            {
               appName = sums[i].getName();
               break; // found it
            }
         }
      }

      if (appName == null)
      {
         throw new PSNotFoundException(APP_NOT_FOUND, "root=" + appName);
      }

      PSAclHandler aclHandler = loadAclHandler(appName);
      if (aclHandler != null)
      {
         try
         {
            int appAccessLevel = aclHandler.getUserAccessLevel(
               session,
               "Permissions: " + accessLevel,
               appName);

            if ((accessLevel & appAccessLevel) == accessLevel)
               return true;

         } catch (PSAuthenticationRequiredException e) {
            // fall through
         } catch (PSAuthorizationException e) {
            // fall through
         }
      }

      return false;
   }

   /**
    * Loads the server's supported feature list document.
    *
    * @param doc The Xml document containing the supported feature data
    * @param request the request context
    * @return the XML response document
    * @throws PSServerException if there is a problem loading the feature set file
    * @roseuid 39FD9995034B
    */
   public Document getSupportedFeatureSet(Document doc, PSRequest request)
      throws PSServerException
   {
      // build the response doc
      File f = PSProperties.getConfig(PSServer.ENTRY_NAME,
      PSFeatureSet.FEATURE_SET_FILE, PSServer.getRxConfigDir());

      FileInputStream fIn = null;
      Document   featureDoc = null;
      Document   respDoc = null;

      try
      {
         respDoc = PSXmlDocumentBuilder.createXmlDocument();

         // build our response node
         Element respRoot = PSXmlDocumentBuilder.createRoot(
            respDoc, "PSXFeatureSetLoadResults");

         /* now try to load the file - if we don't find it, we'll return
          * the empty response node
          */
         if (f.exists())
         {
            fIn = new FileInputStream(f);
            featureDoc = PSXmlDocumentBuilder.createXmlDocument(fIn, false);
            Node importNode = respDoc.importNode(
               featureDoc.getDocumentElement(), true);
            respRoot.appendChild(importNode);
         }
      }
      catch (Exception e)
      {
         // wrap exception
         Object[] args = {e.toString()};
         PSServerException es = new PSServerException(FEATURE_SET_LOAD_EXCEPTION, args);
         throw es;
      }
      finally
      {
         if (fIn != null)
            try { fIn.close(); } catch (Exception e) { /* ignore */ }
      }
      return respDoc;

   }

   /**
    * Gets all JNDI datasource configurations.
    * 
    * @param inDoc The input doc, may not be <code>null</code> and must be in
    * the following format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXGetJndiDatasourcesRequest (EMPTY)>
    *    &lt;ATTLIST PSXGetJndiDatasourcesRequest 
    *       locked (yes | no) "yes"
    *    >
    * </code>
    * </pre> 
    * @param request The request to use, may not be <code>null</code>.
    * 
    * @return The response doc, never <code>null</code> and in the following
    * format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXGetJndiDatasourcesResults (local-tx-datasource*)>
    * </code>
    * </pre> 
    * 
    * @throws PSLockedException If the server config lock cannot be acquired. 
    * @throws PSUnknownDocTypeException If the input document is invalid.
    * @throws PSServerException If there are any errors acquiring the lock. 
    */
   public Document getJndiDatasources(Document inDoc, PSRequest request) 
      throws PSServerException, PSLockedException, PSUnknownDocTypeException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      String rootNodeName = "PSXGetJndiDatasourcesRequest";
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      //make sure we got the correct root node tag
      if (!rootNodeName.equals(root.getNodeName()))
      {
         Object[] args = { rootNodeName, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }
      
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      boolean locked = !"no".equalsIgnoreCase(root.getAttribute("locked"));
      
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      IPSLockerId lockId = null;
      if (locked)
      {
         lockId = getEffectiveLockerId(request, uniqueId);
         os.getServerConfigLock(lockId, 30);
      }
      try
      {
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         root = PSXmlDocumentBuilder.createRoot(respDoc, 
            "PSXGetJndiDatasourcesResults");
         List<IPSJndiDatasource> dsList = 
            os.getJndiDatasources(lockId, request.getSecurityToken());
         
         for (IPSJndiDatasource datasource : dsList)
         {
             PSJBossJndiDatasource jbDatasource = new PSJBossJndiDatasource(datasource);
            root.appendChild(jbDatasource.toXml(respDoc));
         }
         
         return respDoc;
      }
      catch (Exception e)
      {
         if (lockId != null)
            os.releaseServerConfigLock(lockId);
         PSRequestStatistics reqStats = request.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Saves the supplied JNDI datasource configurations.
    * 
    * @param inDoc The input doc, may not be <code>null</code> and must be in
    * the following format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXSaveJndiDatasourcesRequest (local-tx-datasource+)>
    * </code>
    * </pre> 
    * @param request The request to use, may not be <code>null</code>.
    * 
    * @return The response doc, never <code>null</code> and in the following
    * format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXSaveJndiDatasourcesResults (EMPTY)>
    * </code>
    * </pre> 
    * 
    * @throws PSNotLockedException If the server config is not locked.
    * @throws PSLockedException If the server config lock cannot be extended. 
    * @throws PSUnknownDocTypeException If the input document is invalid.
    * @throws PSServerException If there are any errors acquiring the lock. 
    */
   public Document saveJndiDatasources(Document inDoc, PSRequest request) 
      throws PSUnknownDocTypeException, PSLockedException, PSServerException, 
      PSNotLockedException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      String rootNodeName = "PSXSaveJndiDatasourcesRequest";
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      //make sure we got the correct root node tag
      if (!rootNodeName.equals(root.getNodeName()))
      {
         Object[] args = { rootNodeName, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }
      
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      IPSLockerId lockId = getEffectiveLockerId(request, uniqueId);
      
      // Check to see if config is locked
      if (!os.isServerConfigLocked(lockId))
      {
         Object[] args = {"Server Configuration"};
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
            args);
      }
      else
      {
         // extend the lock to be sure we keep it
         os.getServerConfigLock(lockId, 30);
      }
      try
      {
         //TODO: For Jetty - not sure why this is being done here - may be an XML app that needs tweaked.
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(respDoc, 
            "PSXSaveJndiDatasourcesResults");
         String dataSourceNodeName = PSJBossJndiDatasource.DATASOURCE_NODE_NAME;
         PSXmlTreeWalker tree = new PSXmlTreeWalker(root);         
         Element dsEl = tree.getNextElement( dataSourceNodeName, 
            tree.GET_NEXT_ALLOW_CHILDREN);
         
         if (dsEl == null)
            throw new PSUnknownDocTypeException(
               XML_ELEMENT_NULL, dataSourceNodeName);
         
         List<IPSJndiDatasource> dsList = new ArrayList<IPSJndiDatasource>();
         while (dsEl != null)
         {
             PSJBossJndiDatasource psjBossJndiDatasource = new PSJBossJndiDatasource(dsEl);

            dsList.add(psjBossJndiDatasource);
            dsEl = tree.getNextElement(dataSourceNodeName, 
               tree.GET_NEXT_ALLOW_SIBLINGS);
         }
         
         os.saveJndiDatasources(dsList, lockId, request.getSecurityToken());
         
         return respDoc;
      }
      catch (Exception e)
      {
         if (lockId != null)
            os.releaseServerConfigLock(lockId);
         PSRequestStatistics reqStats = request.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Get the datasource resolver that contains the datasource configurations.
    * 
    * @param inDoc The input doc, may not be <code>null</code> and must be in
    * the following format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXGetDatasourceConfigsRequest (EMPTY)>
    *    &lt;ATTLIST PSXGetDatasourceConfigsRequest 
    *       locked (yes | no) "yes"
    *    >
    * </code>
    * </pre> 
    * @param request The request to use, may not be <code>null</code>.
    * 
    * @return The response doc, never <code>null</code> and in the following
    * format, where "bean" is the spring bean XML config for the datasource
    * resolver:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXGetDatasourceConfigsResults (bean)>
    * </code>
    * </pre> 
    * 
    * @throws PSLockedException If the server config lock cannot be acquired. 
    * @throws PSUnknownDocTypeException If the input document is invalid.
    * @throws PSServerException If there are any errors acquiring the lock. 
    */
   public Document getDatasourceConfigs(Document inDoc, PSRequest request)
      throws PSUnknownDocTypeException, PSLockedException, PSServerException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      String rootNodeName = "PSXGetDatasourceConfigsRequest";
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      //make sure we got the correct root node tag
      if (!rootNodeName.equals(root.getNodeName()))
      {
         Object[] args = { rootNodeName, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }
      
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      boolean locked = !"no".equalsIgnoreCase(root.getAttribute("locked"));
      
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      IPSLockerId lockId = null;
      if (locked)
      {
         lockId = getEffectiveLockerId(request, uniqueId);
         os.getServerConfigLock(lockId, 30);
      }
      try
      {
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         root = PSXmlDocumentBuilder.createRoot(respDoc, 
            "PSXGetDatasourceConfigsResults");
         IPSDatasourceResolver resolver = 
            os.getDatasourceConfigs(lockId, request.getSecurityToken());
         
         root.appendChild(resolver.toXml(respDoc));
         
         return respDoc;
      }
      catch (Exception e)
      {
         if (lockId != null)
            os.releaseServerConfigLock(lockId);
         PSRequestStatistics reqStats = request.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Saves the supplied resolver containing the datasource configurations.
    * 
    * @param inDoc The input doc, may not be <code>null</code> and must be in
    * the following format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXSaveDatasourceConfigsRequest (bean)>
    * </code>
    * </pre> 
    * @param request The request to use, may not be <code>null</code>.
    * 
    * @return The response doc, never <code>null</code> and in the following
    * format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXSaveDatasourceConfigsResults (EMPTY)>
    * </code>
    * </pre> 
    * 
    * @throws PSNotLockedException If the server config is not locked.
    * @throws PSLockedException If the server config lock cannot be extended. 
    * @throws PSUnknownDocTypeException If the input document is invalid.
    * @throws PSServerException If there are any errors acquiring the lock. 
    */
   public Document saveDatasourceConfigs(Document inDoc, PSRequest request)
      throws PSUnknownDocTypeException, PSServerException,
      PSNotLockedException, PSLockedException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      String rootNodeName = "PSXSaveDatasourceConfigsRequest";
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      //make sure we got the correct root node tag
      if (!rootNodeName.equals(root.getNodeName()))
      {
         Object[] args = { rootNodeName, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      IPSLockerId lockId = getEffectiveLockerId(request, uniqueId);
      
      // Check to see if config is locked
      if (!os.isServerConfigLocked(lockId))
      {
         Object[] args = {"Server Configuration"};
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
            args);
      }
      else
      {
         // extend the lock to be sure we keep it
         os.getServerConfigLock(lockId, 30);
      }
      try
      {
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(respDoc, 
            "PSXSaveDatasourceConfigsResults");

         PSXmlTreeWalker tree = new PSXmlTreeWalker(root);         
         Element dsEl = tree.getNextElement(IPSBeanConfig.BEAN_NODE_NAME,
            tree.GET_NEXT_ALLOW_CHILDREN);
         
         if (dsEl == null)
            throw new PSUnknownDocTypeException(
               XML_ELEMENT_NULL, IPSBeanConfig.BEAN_NODE_NAME);
         
         IPSDatasourceResolver resolver = PSContainerUtilsFactory.getInstance().getDatasourceResolver();
         resolver.fromXml(dsEl);
         os.saveDatasourceConfigs(resolver, lockId, request.getSecurityToken());
         
         return respDoc;
      }
      catch (Exception e)
      {
         if (lockId != null)
            os.releaseServerConfigLock(lockId);
         PSRequestStatistics reqStats = request.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Get the hibernate dialect configuration.
    * 
    * @param inDoc The input doc, may not be <code>null</code> and must be in
    * the following format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXGetHibernateDialectsRequest (EMPTY)>
    *    &lt;ATTLIST PSXGetHibernateDialectsRequest 
    *       locked (yes | no) "yes"
    *    >
    * </code>
    * </pre> 
    * @param request The request to use, may not be <code>null</code>.
    * 
    * @return The response doc, never <code>null</code> and in the following
    * format, where "bean" is the spring bean XML config for the datasource
    * resolver:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXGetHibernateDialectsResults (bean>
    * </code>
    * </pre> 
    * 
    * @throws PSLockedException If the server config lock cannot be acquired. 
    * @throws PSUnknownDocTypeException If the input document is invalid.
    * @throws PSServerException If there are any errors acquiring the lock.
    */
   public Document getHibernateDialectConfig(Document inDoc, PSRequest request)
      throws PSUnknownDocTypeException, PSLockedException, PSServerException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      String rootNodeName = "PSXGetHibernateDialectsRequest";
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      //make sure we got the correct root node tag
      if (!rootNodeName.equals(root.getNodeName()))
      {
         Object[] args = { rootNodeName, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }
      
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      boolean locked = !"no".equalsIgnoreCase(root.getAttribute("locked"));
      
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      IPSLockerId lockId = null;
      if (locked)
      {
         lockId = getEffectiveLockerId(request, uniqueId);
         os.getServerConfigLock(lockId, 30);
      }
      try
      {
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         root = PSXmlDocumentBuilder.createRoot(respDoc, 
            "PSXGetHibernateDialectsResults");
         PSHibernateDialectConfig config = 
            os.getHibernateDialectConfig(lockId, request.getSecurityToken());
         
         root.appendChild(config.toXml(respDoc));
         
         return respDoc;
      }
      catch (Exception e)
      {
         if (lockId != null)
            os.releaseServerConfigLock(lockId);
         PSRequestStatistics reqStats = request.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Saves the supplied Hibernate dialect configurations.
    * 
    * @param inDoc The input doc, may not be <code>null</code> and must be in
    * the following format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXSaveHibernateDialectsRequest (bean)>
    * </code>
    * </pre> 
    * @param request The request to use, may not be <code>null</code>.
    * 
    * @return The response doc, never <code>null</code> and in the following
    * format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXSaveHibernateDialectsResults (EMPTY)>
    * </code>
    * </pre> 
    * 
    * @throws PSNotLockedException If the server config is not locked.
    * @throws PSLockedException If the server config lock cannot be extended. 
    * @throws PSUnknownDocTypeException If the input document is invalid.
    * @throws PSServerException If there are any errors acquiring the lock. 
    */
   public Document saveHibernateDialectConfig(Document inDoc, PSRequest request)
      throws PSUnknownDocTypeException, PSServerException,
      PSNotLockedException, PSLockedException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      String rootNodeName = "PSXSaveHibernateDialectsRequest";
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      //make sure we got the correct root node tag
      if (!rootNodeName.equals(root.getNodeName()))
      {
         Object[] args = { rootNodeName, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      IPSLockerId lockId = getEffectiveLockerId(request, uniqueId);
      
      // Check to see if config is locked
      if (!os.isServerConfigLocked(lockId))
      {
         Object[] args = {"Server Configuration"};
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
            args);
      }
      else
      {
         // extend the lock to be sure we keep it
         os.getServerConfigLock(lockId, 30);
      }
      try
      {
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(respDoc, 
            "PSXSaveHibernateDialectsResults");

         PSXmlTreeWalker tree = new PSXmlTreeWalker(root);         
         Element cfgEl = tree.getNextElement(IPSBeanConfig.BEAN_NODE_NAME,
            tree.GET_NEXT_ALLOW_CHILDREN);
         
         if (cfgEl == null)
            throw new PSUnknownDocTypeException(
               XML_ELEMENT_NULL, IPSBeanConfig.BEAN_NODE_NAME);
         
         PSHibernateDialectConfig config = new PSHibernateDialectConfig();
         config.fromXml(cfgEl);
         os.saveHibernateDialectConfig(config, lockId, 
            request.getSecurityToken());
         
         return respDoc;
      }
      catch (Exception e)
      {
         if (lockId != null)
            os.releaseServerConfigLock(lockId);
         PSRequestStatistics reqStats = request.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Get the list of cataloger configurations.
    * 
    * @param inDoc The input doc, may not be <code>null</code> and must be in
    * the following format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXGetCatalogerConfigsRequest (EMPTY)>
    *    &lt;ATTLIST PSXGetCatalogerConfigsRequest 
    *       locked (yes | no) "yes"
    *    >
    * </code>
    * </pre> 
    * @param request The request to use, may not be <code>null</code>.
    * 
    * @return The response doc, never <code>null</code> and in the following
    * format, where "bean" is the spring bean XML config for the datasource
    * resolver:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXGetCatalogerConfigsResults (subjectConfigs?, 
    *       roleConfigs?)>
    *    &lt;ELEMENT subjectConfigs (bean*)>
    *    &lt;ELEMENT roleConfigs (bean*)>
    * </code>
    * </pre> 
    * 
    * @throws PSLockedException If the server config lock cannot be acquired. 
    * @throws PSUnknownDocTypeException If the input document is invalid.
    * @throws PSServerException If there are any errors acquiring the lock. 
    */
   public Document getCatalogerConfigs(Document inDoc, PSRequest request)
      throws PSUnknownDocTypeException, PSLockedException, PSServerException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      String rootNodeName = "PSXGetCatalogerConfigsRequest";
      if (inDoc == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_NULL, rootNodeName);

      //make sure we got the correct root node tag
      if (!rootNodeName.equals(root.getNodeName()))
      {
         Object[] args = { rootNodeName, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            XML_ELEMENT_WRONG_TYPE, args);
      }
      
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      boolean locked = !"no".equalsIgnoreCase(root.getAttribute("locked"));
      
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      IPSLockerId lockId = null;
      if (locked)
      {
         lockId = getEffectiveLockerId(request, uniqueId);
         os.getServerConfigLock(lockId, 30);
      }
      
      try
      {
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         root = PSXmlDocumentBuilder.createRoot(respDoc, 
            "PSXGetCatalogerConfigsResults");
         Element subEl = PSXmlDocumentBuilder.addEmptyElement(respDoc, root, 
            "subjectConfigs");
         Element roleEl = PSXmlDocumentBuilder.addEmptyElement(respDoc, root, 
         "roleConfigs");
         
         List<PSCatalogerConfig> configs = os.getCatalogerConfigs(lockId, 
            request.getSecurityToken());
         for (PSCatalogerConfig config : configs)
         {
            Element catEl = config.toXml(respDoc);
            if (config.getConfigType().equals(
               PSCatalogerConfig.ConfigTypes.SUBJECT))
            {
               subEl.appendChild(catEl);
            }
            else
            {
               roleEl.appendChild(catEl);
            }
         }
         
         return respDoc;
      }
      catch (Exception e)
      {
         if (lockId != null)
            os.releaseServerConfigLock(lockId);
         PSRequestStatistics reqStats = request.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }

   /**
    * Saves the supplied cataloger configurations.
    * 
    * @param inDoc The input doc, may not be <code>null</code> and must be in
    * the following format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXSaveCatalogerConfigsRequest (subjectConfigs?, 
    *       roleConfigs?)>
    *    &lt;ELEMENT subjectConfigs (bean*)>
    *    &lt;ELEMENT roleConfigs (bean*)>
    *    
    * </code>
    * </pre> 
    * @param request The request to use, may not be <code>null</code>.
    * 
    * @return The response doc, never <code>null</code> and in the following
    * format:
    * <pre>
    * <code>
    *    &lt;ELEMENT PSXSaveCatalogerConfigsResults (EMPTY)>
    * </code>
    * </pre> 
    * 
    * @throws PSNotLockedException If the server config is not locked.
    * @throws PSLockedException If the server config lock cannot be extended. 
    * @throws PSUnknownDocTypeException If the input document is invalid.
    * @throws PSServerException If there are any errors acquiring the lock. 
    */
   public Document saveCatalogerConfigs(Document inDoc, PSRequest request)
      throws PSUnknownDocTypeException, PSServerException,
      PSNotLockedException, PSLockedException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      String rootNodeName = "PSXSaveCatalogerConfigsRequest";
      if (inDoc == null)
         throw new PSUnknownDocTypeException(XML_ELEMENT_NULL, rootNodeName);

      Element root = inDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(XML_ELEMENT_NULL, rootNodeName);

      // make sure we got the correct root node tag
      if (!rootNodeName.equals(root.getNodeName()))
      {
         Object[] args =
         {rootNodeName, root.getNodeName()};
         throw new PSUnknownDocTypeException(XML_ELEMENT_WRONG_TYPE, args);
      }
      String uniqueId = root.getAttribute(ATTR_UNIQUEID);
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      IPSLockerId lockId = getEffectiveLockerId(request, uniqueId);

      // Check to see if config is locked
      if (!os.isServerConfigLocked(lockId))
      {
         Object[] args =
         {"Server Configuration"};
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
            args);
      }
      else
      {
         // extend the lock to be sure we keep it
         os.getServerConfigLock(lockId, 30);
      }
      try
      {
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(respDoc,
            "PSXSaveCatalogerConfigsResults");

         List<PSCatalogerConfig>configs = new ArrayList<>();
         PSXmlTreeWalker tree = new PSXmlTreeWalker(root);
         Element configEl;
         Element subEl = tree.getNextElement("subjectConfigs", 
            tree.GET_NEXT_ALLOW_CHILDREN);
         if (subEl != null)
         {
            configEl = tree.getNextElement(IPSBeanConfig.BEAN_NODE_NAME,
               tree.GET_NEXT_ALLOW_CHILDREN);
            while (configEl != null)
            {
               configs.add(new PSCatalogerConfig(configEl, 
                  PSCatalogerConfig.ConfigTypes.SUBJECT));
               configEl = tree.getNextElement(IPSBeanConfig.BEAN_NODE_NAME,
                  tree.GET_NEXT_ALLOW_SIBLINGS);
            }
         }
         tree.setCurrent(root);
         Element roleEl = tree.getNextElement("roleConfigs", 
            tree.GET_NEXT_ALLOW_CHILDREN);
         if (roleEl != null)
         {
            configEl = tree.getNextElement(IPSBeanConfig.BEAN_NODE_NAME,
               tree.GET_NEXT_ALLOW_CHILDREN);
            while (configEl != null)
            {
               configs.add(new PSCatalogerConfig(configEl, 
                  PSCatalogerConfig.ConfigTypes.ROLE));
               configEl = tree.getNextElement(IPSBeanConfig.BEAN_NODE_NAME,
                  tree.GET_NEXT_ALLOW_SIBLINGS);
            }
         }
         
         os.saveCatalogerConfigs(configs, lockId, request.getSecurityToken());

         return respDoc;
      }
      catch (Exception e)
      {
         if (lockId != null)
            os.releaseServerConfigLock(lockId);
         PSRequestStatistics reqStats = request.getStatistics();
         reqStats.setFailure();
         return fillErrorResponse(e);
      }
   }
   
   /**
    *
    * @author   davidgennaco
    *
    * @version 1.31 1999/06/23
    *
    * Check an application definition's acl to see if the current
    *      user has the reqested access level.
    *
    * @param   appName   the application name
    *
    * @param   accessLevel   the requested access level to check for
    *
    * @param   req   the current user context
    *
    * @return   <code>true</code> if the user has this authorization
    *            <code>false</code> if the user does not have this authorization
    */
   boolean checkApplicationSecurity(String appName,
                  int accessLevel, PSRequest req)
      throws   PSAuthorizationException, PSAuthenticationFailedException,
               PSNotFoundException, PSServerException
   {
      if (appName == null)
         throw new PSNotFoundException(APP_NOT_FOUND, "null");

      PSAclHandler aclHandler = loadAclHandler(appName);
      if (aclHandler != null)
      {
         try {
            int appAccessLevel = aclHandler.getUserAccessLevel(req);

            if ((accessLevel & appAccessLevel) == accessLevel)
               return true;
         } catch (PSAuthenticationRequiredException e) {
            // fall through
         } catch (PSAuthorizationException e) {
            // fall through
         }
      }

      return false;
   }

   /**
    *
    * @author   davidgennaco
    *
    * @version 1.31 1999/06/24
    *
    * Check an application definition's acl to see if the current
    *      user has the reqested access level, this is the non-disk
    *      version required by creates and new saves...
    *
    * @param   app         the application
    *
    * @param   accessLevel   the requested access level to check for
    *
    * @param   req         the current user context
    *
    * @return   <code>true</code> if the user has this authorization
    *            <code>false</code> if the user does not have this authorization
    */
   boolean checkApplicationSecurity(PSApplication app,
                  int accessLevel, PSRequest req)
      throws PSAuthenticationFailedException, PSAuthorizationException
   {
      PSAcl acl = app.getAcl();

      PSAclHandler aclHandler = new PSAclHandler(acl);

      try {
         int appAccessLevel = aclHandler.getUserAccessLevel(req);

         if ((accessLevel & appAccessLevel) == accessLevel)
         {
            /* The file modification time will have to be set later */
            synchronized (m_appSums)
            {
               m_appSums.addSummary(app, false);
               PSApplicationSummary sum = m_appSums.getSummary(app.getName());
               sum.setAclHandler(aclHandler);
               try
               {
                  String reqRoot = app.getRequestRoot();
                  if (reqRoot != null && reqRoot.length() > 0)
                  {
                     addVirtualAppDirectory(reqRoot, new File(reqRoot));
                  }
               }
               catch (IllegalArgumentException ex)
               {
                  // log this
                  Object[] args =
                  {app.getName(), app.getRequestRoot(),
                     PSException.getStackTraceAsString(ex)};
                  PSLogManager.write(new PSLogServerWarning(
                     IPSServerErrors.ARGUMENT_ERROR,
                     args, true, "ObjectStore"));
               }
               return true;
            }
         }
         else
            return false;
      } catch (PSAuthenticationRequiredException e)
      {
         return false;
      }
   }

   /**
    *
    * @author   davidgennaco
    *
    * @version 1.31 1999/06/24
    *
    * Check an application definition's acl to see if the current
    *      user has the reqested access level, based on application id.
    *
    * @param   id      the application id
    *
    * @param   accessLevel   the requested access level to check for
    *
    * @param   req   the current user context
    *
    * @return   <code>true</code> if the user has this authorization
    *            <code>false</code> if the user does not have this authorization
    */
   boolean checkApplicationSecurity(int id,
                  int accessLevel, PSRequest req)
      throws PSAuthorizationException,
         PSAuthenticationFailedException,
         PSAuthenticationRequiredException,
         PSNotFoundException,
         PSServerException
   {
      String appName = getApplicationNameFromId(id);
      if (appName == null)
         throw new PSNotFoundException(APP_NOT_FOUND, appName);

      return checkApplicationSecurity(appName, accessLevel,
         req);
   }

   /**
    * Replace a summary entry with information loaded from the application
    * file on disk
    *
    * @param   appName The name of the application
    *
    * @throws   com.percussion.design.objectstore.PSNotFoundException
    */
   private void updateSummaryEntry(String appName)
      throws com.percussion.design.objectstore.PSNotFoundException,
         PSServerException
   {
      Document doc               = null;
      PSApplication app            = null;
      File appFile               = null;

      /* Try the application summary guys first */
      appFile = getApplicationFile(appName);
      if (!appFile.exists())
         throw new PSNotFoundException(APP_NOT_FOUND, appName);

      doc = loadApplicationFromFile(appFile);

      if (doc != null)
         try {
            app = new PSApplication(doc);
         }   catch (
            com.percussion.design.objectstore.PSUnknownNodeTypeException e)
         {
            Object[] args = new Object[] { appName, e.toString() };
            throw new PSServerException(APP_LOAD_EXCEPTION, args);
         }   catch (
            com.percussion.design.objectstore.PSUnknownDocTypeException e)
         {
            Object[] args = new Object[] { appName, e.toString() };
            throw new PSServerException(APP_LOAD_EXCEPTION, args);
         }

      PSApplicationSummary sum = m_appSums.getSummary(appName);
      if (sum != null)
      {
         PSAcl acl = app.getAcl();

         if (acl != null)
         {
            PSAclHandler aclHandler = new PSAclHandler(acl);
            sum.setAclHandler(aclHandler);
         }
         sum.setFileLastModified(appFile.lastModified());
         PSRevisionHistory hist = app.getRevisionHistory();
         if (hist != null)
         {
            sum.setMajorMinorVersion(hist.getLatestMajorVersion(),
                                     hist.getLatestMinorVersion());
         }
      }
   }

   /**
    * Returns the application name of the application with the given id.
    * If no app with tha id is found, returns the empty string.
    *
    * @param   id The id of the application.
    *
    * @return   String
    */
   String getApplicationNameFromId(int id)
   {
      PSApplicationSummary sum = m_appSums.getSummary(id);
      if (sum == null)
         return null;
      String appName = sum.getName();
      if (appName == null)
         appName = "";

      return appName;
   }

   /**
    * Gets an application id from the app name. If no app by that
    * name is found, returns 0.
    *
    * @param   name The app name
    *
    * @return   int
    */
   int getApplicationIdFromName(String name)
   {
      PSApplicationSummary sum = m_appSums.getSummary(name);
      if (sum == null)
         return 0;

      return sum.getId();
   }

   public OutputStream lockOutputStream(File f)
      throws IOException
   {
      File canon = f.getCanonicalFile();
      synchronized (m_lockedFiles)
      {
         while (null != m_lockedFiles.get(canon))
         {
            try
            {
               m_lockedFiles.wait();
            }
            catch (InterruptedException e)
            {
               return null;
            }
         }
         m_lockedFiles.put(canon, Boolean.TRUE);
      }

      FileOutputStream out = null;
      try
      {
         out = new FileOutputStream(f);
         return out;
      }
      catch (IOException e)
      {
         releaseOutputStream(out, f);
         throw e;
      }
      catch (Throwable t)
      {
         releaseOutputStream(out, f);
         throw new IOException(t.toString());
      }
   }

   public void releaseOutputStream(OutputStream out, File f)
      throws IOException
   {
      File canon = f.getCanonicalFile();
      synchronized (m_lockedFiles)
      {
         m_lockedFiles.remove(canon);
         m_lockedFiles.notify();
      }
      try
      {
         if (out != null)
            out.close();
      }
      catch (Throwable t)
      {
         // ignore
      }
   }

   public InputStream lockInputStream(File f)
      throws IOException
   {
      File canon = f.getCanonicalFile();
      synchronized (m_lockedFiles)
      {
         while (null != m_lockedFiles.get(canon))
         {
            try
            {
               m_lockedFiles.wait();
            }
            catch (InterruptedException e)
            {
               return null;
            }
         }
         m_lockedFiles.put(canon, Boolean.TRUE);
      }

      FileInputStream in = null;
      try
      {
         in = new FileInputStream(f);
         return in;
      }
      catch (IOException e)
      {
         releaseInputStream(in, f);
         throw e;
      }
      catch (Throwable t)
      {
         releaseInputStream(in, f);
         throw new IOException(t.toString());
      }
   }

   public void releaseInputStream(InputStream in, File f)
      throws IOException
   {
      synchronized (m_lockedFiles)
      {
         File canon = f.getCanonicalFile();
         m_lockedFiles.remove(canon);
         m_lockedFiles.notify();
      }
      try
      {
         if (in != null)
            in.close();
      }
      catch (Throwable t)
      {
         // ignore
      }
   }

   /**
    * Update the virtual directory structure to reflect the addition
    * of this application directory.
    *
    * @author   chadloder
    *
    * @version 1.42 1999/07/15
    *
    *
    * @param   appRoot
    * @param   physicalPath
    *
    */
   void addVirtualAppDirectory(String appRoot, File physicalPath)
   {
      if (physicalPath == null)
         physicalPath = new File(appRoot); // in the objectstore directory

      IPSVirtualDirectory vdir = new PSVirtualApplicationDirectory(
         appRoot, physicalPath, this);

      PSFileSystemDriver.addVirtualDirectory(vdir);
   }

   void removeVirtualAppDirectory(String appRoot)
   {
      PSFileSystemDriver.removeVirtualDirectory(appRoot);
   }

   /**
    * Notifies all listeners registered for application changes.
    * @param app The changed application object
    * @param isNewApp <code>true</code> if app is being created and
    * <code>false</code> if updating an existing app.
    * @param isRemove <code>true</code> if removing an app, <code>false</code>
    * if not, ignored if <code>isNewApp</code> is <code>true</code>.
    * @throws PSSystemValidationException if app fails validation on restart
    * @throws PSNotFoundException if app cannot be located on disk
    * @throws PSServerException for anything else that may go wrong.
    */
   void notifyApplicationListeners(PSApplication app, boolean isNewApp, 
      boolean isRemove)
      throws PSSystemValidationException, PSServerException, PSNotFoundException
   {

      /* we need to synchronize on the vector, as new listeners could be
       * added or removed. This can change the vector
       * out from under us, which is a problem. To minimize the impact,
       * we will make a temporary copy of the vector and work from it.
       */
      IPSApplicationListener[] listeners = getApplicationListenerArray();
      int vectorSize = listeners.length;
      for (int index = 0; index < vectorSize; index++){
         if (isNewApp)
            listeners[index].applicationCreated(app);
         else if (isRemove)
            listeners[index].applicationRemoved(app);
         else
            listeners[index].applicationUpdated(app);
      }
   }

   /**
    * Notifies all listeners registered for server config changes.
    * @param config The server config object
    */
   void notifyServerListeners(PSServerConfiguration config)
   {
      /* we need to synchronize on the vector, as new listeners could be
       * added or removed. This can change the vector
       * out from under us, which is a problem. To minimize the impact,
       * we will make a temporary copy of the vector and work from it.
       */
      ArrayList temp = new ArrayList();
      synchronized(m_SrvListenerVector)
      {
         Enumeration e = m_SrvListenerVector.elements();
         while (e.hasMoreElements())
         {
            temp.add(e.nextElement());
         }
      }

      // now notify everyone in the copy
      Iterator i = temp.iterator();
      while (i.hasNext())
      {
         ((IPSServerConfigurationListener)
            i.next()).configurationUpdated(config);
      }

   }
   
   /**
    * Deletes a file or directory
    * If it is a directory then it recursively deletes the directory
    * and all of its children.
    * @param file the file or directory to be deleted, assumed not 
    * <code>null</code>.
    * @return <code>true</code> if successful.
    */
   private boolean deleteFile(File file)
   {
      if(!file.exists())
         return false;
      if(file.isDirectory())
      {
         File[] children = file.listFiles();
         for(int i = 0; i < children.length; i++)
         {
            if(!deleteFile(children[i]))
               return false;
         }
      }
      return file.delete();      
   }

   private void renameVirtualAppDirectory(
      String oldAppRoot,
      String newAppRoot,
      File physicalPath
      )
   {
      if (physicalPath == null)
         physicalPath = new File(newAppRoot);

      IPSVirtualDirectory vdir = new PSVirtualApplicationDirectory(
         newAppRoot, physicalPath, this);

      PSFileSystemDriver.renameVirtualDirectory(oldAppRoot, vdir);
   }

   void printMsg(String msg)
   {
      com.percussion.server.PSConsole.printMsg("ObjectStore", msg);
   }

   void printMsg(Throwable t)
   {
      com.percussion.server.PSConsole.printMsg("ObjectStore", t);
   }

   /* we need to synchronize on the vector, as new apps may be saved
    * or other apps may be removed. This can change the positioning
    * out from under us, which is a problem. To minimize the impact,
    * we will make a temporary copy of the vector and work from it.
    */
   private IPSApplicationListener[] getApplicationListenerArray()
   {
      synchronized (m_AppListenerVector) {
         int size = m_AppListenerVector.size();
         IPSApplicationListener[] ret = new IPSApplicationListener[size];
         m_AppListenerVector.toArray(ret);
         return ret;
      }
   }



   private static final String   PROP_OBJECT_DIR = "objectDirectory";

   private static final String ms_RootExtendAppLock   = "PSXDesignAppLock";
   private static final String ms_RootAppLoad         = "PSXDesignAppLoad";
   private static final String ms_RootAppList         = "PSXDesignAppList";
   private static final String ms_RootAppListFiles = "PSXDesignAppListFiles";
   private static final String ms_RootAppRemove      = "PSXDesignAppRemove";
   private static final String ms_RootAppRename      = "PSXDesignAppRename";
   private static final String ms_RootAppSave         = "PSXDesignAppSave";
   private static final String ms_RootAppFileSave   = "PSXDesignAppFileSave";
   private static final String ms_RootAppFileLoad   = "PSXDesignAppFileLoad";
   private static final String ms_RootAppFileRemove   = "PSXDesignAppFileRemove";
   private static final String ms_RootAppFileRename   = "PSXDesignAppFileRename";

   private static final String ms_RootUserConfigLoad   = "PSXDesignUserConfigLoad";
   private static final String ms_RootUserConfigRemove   = "PSXDesignUserConfigRemove";
   private static final String ms_RootUserConfigSave   = "PSXDesignUserConfigSave";

   private static final String ms_RootServerConfigLoad   = "PSXDesignServerConfigLoad";
   private static final String ms_RootServerConfigSave   = "PSXDesignServerConfigSave";
   private static final String ms_RootServerConfigLock = "PSXDesignServerConfigLock";

   private static final String ms_RootRoleConfigLoad   = "PSXDesignServerRolesLoad";
   private static final String ms_RootRoleConfigSave   = "PSXDesignServerRolesSave";

   private static final String ms_RootTableDefSave = "PSXDesignTableDefinitionsSave";
   private static final String ms_RootCESystemDefLoad = "PSXContentEditorSystemDefinitionLoad";
   private static final String ms_RootCESharedDefLoad = "PSXContentEditorSharedDefinitionLoad";
   private static final String ms_RootGetConnectionDetail = "PSXGetConnectionDetails";

   public static final String  ms_RootCharacterSetMapLoad   = "PSXCharacterEncodingsLoad";

   public static final String ms_RootExtSave = "PSXDesignExtSave";
   public static final String ms_RootExtLoad = "PSXDesignExtLoad";
   public static final String ms_RootExtRemove = "PSXDesignExtRemove";

   /** The root element of the rx configuration load request. **/
   private static final String ms_RootRxConfigLoad   = "PSXDesignRxConfigLoad";

   /** The root element of the rx configuration save request. **/
   private static final String ms_RootRxConfigSave   = "PSXDesignRxConfigSave";



   private HashMap                           m_requestHandlerMethods   = null;
   private com.percussion.log.PSLogHandler   m_LogHandler            = null;

   private java.util.Vector m_AppListenerVector = new java.util.Vector();
   private java.util.Vector m_SrvListenerVector = new java.util.Vector();

   private Map m_lockedFiles = new HashMap();

   private PSObjectStoreStatistics m_statistics = new PSObjectStoreStatistics();

   /**
    * Constant for the name of the config file.
    */
   private static final String  CONFIG_FILE   = "config.xml";

   /**
    * The name of the root element for the PSRoleConfiguration object.
    */
   private final static String ROLE_CONFIG_ROOT_TAGNAME = "PSXRoleConfiguration";
   
   /**
    * XML attribute for uniqueId used for aquiring a lock
    */
   private static final String ATTR_UNIQUEID = "uniqueId";

   /*
    * The following have package access enabled to allow the
    * PSServerXmlObjectstore class access to them.  Eventually they will be
    * moved to that class and made private once all reliance on them by this
    * class has been moved to the PSServerXmlObjectStore class.
    */
   PSXmlObjectStoreLockManager m_lockMgr;
   java.io.File m_objectDirectory = null;
   PSApplicationSummaryCollection m_appSums = null;

   /**
    * Private implementation of the {@link IPSRepositoryInfo} interface
    */
   private class PSOsRepositoryInfo implements IPSRepositoryInfo
   {
      
      
      
      /**
       * Construct the info object.
       * @throws PSServerException If there are any errors.
       */
      private PSOsRepositoryInfo() throws PSServerException
      {
         init();
      }       
      
      /**
       * Construct the info object.
       * @return 
       * @throws PSServerException If there are any errors.
       */
      private void init() throws PSServerException
      {
         IPSDatasourceResolver resolver =
            PSServerXmlObjectStore.getDatasourceResolver();
         IPSDatasourceConfig config = null;
         String repository = resolver.getRepositoryDatasource();
         for (IPSDatasourceConfig test : 
            resolver.getDatasourceConfigurations())
         {
            if (test.getName().equals(repository))
            {
               config = test;
               break;
            }
         }
         
         if (config == null)
         {
            throw new RuntimeException(
               "No repository datasource config found.");
         }
         
         IPSJndiDatasource datasource = null;
         for (IPSJndiDatasource test : 
            PSServerXmlObjectStore.getJndiDatasources(false))
         {
            if (test.getName().equals(config.getDataSource()))
            {
               datasource = test;
               break;
            }
         }
         
         if (datasource == null)
            throw new RuntimeException(
               "No repository JNDI datasource config found.");
         
         mi_driver = datasource.getDriverName();
         mi_server = datasource.getServer();
         mi_database = config.getDatabase();
         mi_origin = config.getOrigin();
      }
      
      public String getDriver()
      {
         return mi_driver;
      }

      public String getServer()
      {
         return mi_server;
      }

      public String getDatabase()
      {
         return mi_database;
      }

      public String getOrigin()
      {
         return mi_origin;
      }
      
      private String mi_driver;
      private String mi_server;
      private String mi_database;
      private String mi_origin;
   }
   
   /**
    * Private implementation of the {@link IPSConfigFileLocator} interface
    */
   private class PSOsConfigFileLocator implements IPSConfigFileLocator
   {
      /**
       * Construct the locator
       * 
       * @param serverConfigFile File reference to the server config file, 
       * assumed not <code>null</code>.
       */
      private PSOsConfigFileLocator(File serverConfigFile)
      {
         mi_serverConfigFile = serverConfigFile;
         mi_springConfigFile = new File(PSServletUtils.getSpringConfigDir(), 
            PSServletUtils.SERVER_BEANS_FILE_NAME);
      }
      
      public File getServerConfigFile()
      {
         return mi_serverConfigFile;
      }

      public File getSpringConfigFile()
      {
         return mi_springConfigFile;
      }

      private File mi_serverConfigFile;
      private File mi_springConfigFile;

      }

   /**
    * Server properties, should check for null and initialize before use
    */
   private static Properties ms_serverProps = null;

   /**
    * Server property that is set to ignore locking the xml 
    * application when editing the Workbench CMS Filesystem
    */
   private static final String DONT_LOCK_APPLICATION_FLAG = "disableWorkbenchCmsFileApplicationLocking";
}

