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

package com.percussion.deploy.server.dependencies;

import com.percussion.conn.PSServerException;
import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSAppEnabledPolicySetting;
import com.percussion.deploy.objectstore.PSAppPolicySettings;
import com.percussion.deploy.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSDatasourceMap;
import com.percussion.deploy.objectstore.PSDbmsInfo;
import com.percussion.deploy.objectstore.PSDbmsMap;
import com.percussion.deploy.objectstore.PSDbmsMapping;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSDeployableObject;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSLogPolicySetting;
import com.percussion.deploy.objectstore.PSTracePolicySetting;
import com.percussion.deploy.objectstore.PSTransactionSummary;
import com.percussion.deploy.server.IPSIdTypeHandler;
import com.percussion.deploy.server.PSAppTransformer;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDbmsHelper;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationType;
import com.percussion.design.objectstore.PSBackEndDataTank;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataSelector;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSFunctionCall;
import com.percussion.design.objectstore.PSLogger;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSResultPage;
import com.percussion.design.objectstore.PSResultPageSet;
import com.percussion.design.objectstore.PSWhereClause;
import com.percussion.design.objectstore.server.PSApplicationSummary;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.design.objectstore.server.PSXmlObjectStoreLockerId;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSServer;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.util.PSCollection;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Class to handle packaging and deploying an application.
 */
public class PSApplicationDependencyHandler
   extends PSContentEditorObjectDependencyHandler implements IPSIdTypeHandler
{

   /**
    * Construct a dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    */
   public PSApplicationDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      // use set to ensure we don't add dupes
      Set childDeps = new HashSet<>();

      // don't get dependencies if a system app
      if (isSystemApp(dep.getDependencyId()))
         return childDeps.iterator();

      PSApplication app = getApplication(tok, dep.getDependencyId());

      // get dependencies specified by id type map
      childDeps.addAll(getIdTypeDependencies(tok, dep));

      childDeps.addAll(getStyleSheetDependencies(tok, dep));

      // get app deps - make a new set so we can avoid adding the parent app
      Set appDepSet = new HashSet();
      Document appDoc = app.toXml();
      addApplicationDependencies(tok, appDepSet, appDoc.getDocumentElement());
      Iterator appDeps = appDepSet.iterator();
      while (appDeps.hasNext())
      {
         PSDependency appDep = (PSDependency)appDeps.next();
         if (!dep.getKey().equals(appDep.getKey()))
            childDeps.add(appDep);
      }

      // get roles
      PSDependencyHandler roleHandler = getDependencyHandler(
         PSRoleDefDependencyHandler.DEPENDENCY_TYPE);
      Iterator aclEntries = app.getAcl().getEntries().iterator();
      while (aclEntries.hasNext())
      {
         PSAclEntry aclEntry = (PSAclEntry)aclEntries.next();
         if (aclEntry.isRole())
         {
            PSDependency roleDep = roleHandler.getDependency(tok,
               aclEntry.getName());
            if (roleDep != null)
               childDeps.add(roleDep);
         }
      }


      // walk each dataset
      PSDependencyHandler wfHandler = getDependencyHandler(
         PSWorkflowDependencyHandler.DEPENDENCY_TYPE);
      PSDependencyHandler schemaHandler = getDependencyHandler(
         PSSchemaDependencyHandler.DEPENDENCY_TYPE);
      boolean gotCE = false;
      PSCollection dataSetColl = app.getDataSets();
      if (dataSetColl != null)
      {
         Iterator datasets = dataSetColl.iterator();
         while (datasets.hasNext())
         {
            PSDataSet ds = (PSDataSet) datasets.next();
            // get tables from tank
            PSPipe pipe = ds.getPipe();
            if (pipe == null)
               continue;

            PSBackEndDataTank tank = pipe.getBackEndDataTank();
            if (tank == null)
               continue;

            Iterator tables = tank.getTables().iterator();
            while (tables.hasNext())
            {
               PSBackEndTable table = (PSBackEndTable) tables.next();
               PSDependency schemaDep = schemaHandler.getDependency(tok, table
                     .getTable());
               if (schemaDep != null)
                  childDeps.add(schemaDep);
            }

            // check for function defs and child resource cache dependencies
            if (pipe instanceof PSQueryPipe)
            {
               PSQueryPipe qPipe = (PSQueryPipe) pipe;
               Iterator childResources = qPipe.getCacheSettings()
                     .getDependencies();
               while (childResources.hasNext())
               {
                  String child = (String) childResources.next();
                  PSDependency appDep = getDepFromPath(tok, child);
                  if (!dep.getKey().equals(appDep.getKey()))
                     childDeps.add(appDep);
               }

               PSDataSelector selector = qPipe.getDataSelector();
               if (selector != null)
               {
                  PSCollection whereColl = selector.getWhereClauses();
                  if (whereColl != null)
                  {
                     Iterator wheres = whereColl.iterator();
                     while (wheres.hasNext())
                     {
                        PSWhereClause where = (PSWhereClause) wheres.next();
                        IPSReplacementValue[] vars =
                        {where.getValue(), where.getVariable()};

                        PSDependencyHandler funcHandler = getDependencyHandler(PSDbFunctionDefDependencyHandler.DEPENDENCY_TYPE);
                        for (int i = 0; i < vars.length; i++)
                        {
                           if (vars[i] != null
                                 && vars[i] instanceof PSFunctionCall)
                           {
                              PSFunctionCall func = (PSFunctionCall) vars[i];
                              String funcName = func.getDatabaseFunctionName();
                              PSDependency funcDep = funcHandler.getDependency(
                                    tok, funcName);
                              if (funcDep != null)
                                 childDeps.add(funcDep);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return childDeps.iterator();
   }

   // see base class
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      try
      {
         List files = new ArrayList();
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         Document doc = os.getApplicationDoc(dep.getDependencyId(), tok);
         File appDocFile = createXmlFile(doc);
         files.add(new PSDependencyFile(PSDependencyFile.TYPE_APPLICATION_XML,
            appDocFile));

         Iterator appFiles = getAppFiles(tok, dep.getDependencyId());
         while (appFiles.hasNext())
         {
            File appFile = (File)appFiles.next();

            // write app file out to a temp file
            File tmpFile = getFileFromApp(tok,
               dep.getDependencyId(), appFile);

            // add the dependency using temp file, but supply appfile as
            // original file
            files.add(new PSDependencyFile(
               PSDependencyFile.TYPE_APPLICATION_FILE, tmpFile, appFile));
         }

         return files.iterator();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   // see base class
   public List getExternalDbmsInfoList(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type for this handler");

      List<PSDatasourceMap> infoList = new ArrayList<>();

      try
      {
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         PSApplication app = os.getApplicationObject(dep.getDependencyId(),
            tok);

         // locate backend tables in the app
         PSCollection dataSetColl = app.getDataSets();
         if (dataSetColl == null)
            return infoList;

         Iterator datasets = dataSetColl.iterator();
         while (datasets.hasNext())
         {
            PSDataSet ds = (PSDataSet)datasets.next();

            PSPipe pipe = ds.getPipe();
            if (pipe == null)
               continue;

            PSBackEndDataTank tank = pipe.getBackEndDataTank();
            if (tank == null)
               continue;

            Iterator tables = tank.getTables().iterator();
            while (tables.hasNext())
            {
               PSBackEndTable table = (PSBackEndTable)tables.next();
               String dataSrc = table.getDataSource();

               // see if datasource is not blank
               if (StringUtils.isNotBlank(dataSrc))
               {
                  
                  infoList.add(new PSDatasourceMap(dataSrc,null));
               }
            }
         }

         return infoList;
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }


   // see base class
   public void installDependencyFiles(PSSecurityToken tok,
      PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      String appName = dep.getDependencyId();
      Document appDoc = null;
      List appFileList = new ArrayList();
      Iterator files = archive.getFiles(dep);
      while (files.hasNext())
      {
         PSDependencyFile file = (PSDependencyFile)files.next();
         if (file.getType() == PSDependencyFile.TYPE_APPLICATION_XML)
            appDoc = createXmlDocument(archive.getFileData(file));
         else if (file.getType() == PSDependencyFile.TYPE_APPLICATION_FILE)
         {
            appFileList.add(file);
         }
      }

      // must at least have an app doc
      if (appDoc == null)
      {
         Object[] args =
         {
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_APPLICATION_XML],
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()
         };
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }

      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      PSXmlObjectStoreLockerId lockId = null;
      try
      {
         PSApplication app = new PSApplication(appDoc);

         // get app id, determine if app exists, and set id on app object
         int appId = getAppId(tok, appName);
         boolean exists = appId != -1;
         app.setId(appId);

         // transform app ids if necessary
         PSIdMap idMap = ctx.getCurrentIdMap();
         if (idMap != null)
            transformApp(app, ctx);

         // shutdown the app
         PSServer.shutdownApplication(app.getName());

         // save it disabled so it won't start till we save the files
         boolean wasEnabled = app.isEnabled();
         app.setEnabled(false);

         // create lock, stealing lock if required
         lockId = new PSXmlObjectStoreLockerId(ctx.getUserId(), true, true,
            tok.getUserSessionId());
         os.getApplicationLock(lockId, appName, 30);
         os.saveApplication(app, lockId, tok, true);

         // set enabled state back
         app.setEnabled(wasEnabled);

         // get log handler
         addTransactionLogEntry(dep, ctx, appName + ".xml",
            PSTransactionSummary.TYPE_FILE, exists ?
               PSTransactionSummary.ACTION_MODIFIED :
               PSTransactionSummary.ACTION_CREATED);

         // if app exists, get current app files to determine if adding or
         // overwriting files
         List curFileList = new ArrayList();
         if (exists)
         {
            Iterator curFiles = getAppFiles(tok, app.getName());
            while (curFiles.hasNext())
               curFileList.add(curFiles.next());
         }

         // save the application files
         Iterator appFiles = appFileList.iterator();
         while (appFiles.hasNext())
         {
            int transAction = PSTransactionSummary.ACTION_CREATED;

            PSDependencyFile appFile = (PSDependencyFile)appFiles.next();
            File origFile = normalizePathSep(appFile.getOriginalFile());
            os.saveApplicationFile(appName, origFile,
               archive.getFileData(appFile), true, lockId, tok);

            // check to see if overwriting
            if (exists && curFileList.contains(origFile))
            {
               transAction = PSTransactionSummary.ACTION_MODIFIED;
               // filter out the existing files that were modified
               curFileList.remove(origFile);
            }

            addTransactionLogEntry(dep, ctx, origFile.getPath(),
               PSTransactionSummary.TYPE_FILE, transAction);
         }
         
         // remaining files have to be deleted and logged
         Iterator it = curFileList.iterator();
         File appDir = new File(PSServer.getRxDir(), appName);
         while (it.hasNext())
         {
            File f = (File) it.next();
            File appFile = new File(appDir, f.getPath());
            
            if (os.removeApplicationFile(app, appFile, lockId, tok))
               addTransactionLogEntry(dep, ctx, f.getPath(),
                     PSTransactionSummary.TYPE_FILE,
                     PSTransactionSummary.ACTION_DELETED);
         }

         // apply policy settings and resave
         PSAppPolicySettings policySettings = ctx.getAppPolicySettings();
         PSAppEnabledPolicySetting appEnabled =
            policySettings.getEnabledSetting();
         if (appEnabled.useSetting())
            app.setEnabled(appEnabled.isAppEnabled());
         PSLogPolicySetting logSetting = policySettings.getLogSetting();
         if (logSetting.useSetting())
         {
            boolean enabled = logSetting.isLoggingEnabled();
            PSLogger logger = app.getLogger();
            if (logger != null)
            {
               logger.setAppStartStopLoggingEnabled(enabled);
               logger.setAppStatisticsLoggingEnabled(enabled);
               logger.setBasicUserActivityLoggingEnabled(enabled);
               logger.setDetailedUserActivityLoggingEnabled(enabled);
               logger.setErrorLoggingEnabled(enabled);
               logger.setExecutionPlanLoggingEnabled(enabled);
               logger.setFullUserActivityLoggingEnabled(enabled);
               logger.setMultipleHandlerLoggingEnabled(enabled);
               logger.setServerStartStopLoggingEnabled(enabled);
            }
         }
         PSTracePolicySetting traceSetting = policySettings.getTraceSetting();
         if (traceSetting.useSetting())
            app.getTraceInfo().setTraceEnabled(traceSetting.isTraceEnabled());

         // resave app with policy settings
         os.saveApplication(app, lockId, tok, true);


      }
      catch (PSDeployException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (lockId != null)
         {
            try
            {
               os.releaseApplicationLock(lockId, appName);
            }
            catch(PSServerException e)
            {
               // not fatal
            }
         }
      }


   }

   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // get all applications
      List deps = new ArrayList();
      PSApplicationSummary[] sums =
         PSServerXmlObjectStore.getInstance().getApplicationSummaryObjects(tok,
            false);
      for (int i = 0; i < sums.length; i++)
      {
         String appName = sums[i].getName();
         // dont show CE apps in this list
         if ( sums[i].getAppType() ==  PSApplicationType.CONTENT_EDITOR ) 
            continue;
         PSDependency dep = createDependency(m_def, appName, appName);
         if (isSystemApp(appName))
            dep.setDependencyType(PSDependency.TYPE_SYSTEM);
         deps.add(dep);
      }

      return deps.iterator();
   }


   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      // even though all we need is the id, we have to make sure it exists
      PSDeployableObject dep = null;

      PSApplicationSummary[] sums =
         PSServerXmlObjectStore.getInstance().getApplicationSummaryObjects(tok,
            false);
      for (int i = 0; i < sums.length && dep == null; i++)
      {
         if (sums[i].getName().equals(id))
         {
           dep = createDependency(m_def, id, id);
            if (isSystemApp(id))
               dep.setDependencyType(PSDependency.TYPE_SYSTEM);
         }
      }

      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Keyword</li>
    * <li>UserControl</li>
    * <li>SystemControl</li>
    * <li>TableSchema</li>
    * <li>Exit</li>
    * <li>SharedGroup</li>
    * <li>SystemDefElement</li>
    * <li>SystemDef</li>
    * <li>Role</li>
    * <li>Application</li>
    * <li>StylesheetIncludes</li>
    * <li>Workflow</li>
    * <li>Any ID Type</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   /**
    * Get the type of depedency supported by this handler.
    *
    * @return the type, never <code>null</code> or empty.
    */
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getAppId(tok, id) != -1;
   }

   // see base class
   public boolean shouldDeferInstallation()
   {
      return true;
   }

   // see IPSIdTypeHandler interface
   public PSApplicationIDTypes getIdTypes(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      PSApplicationIDTypes idTypes = new PSApplicationIDTypes(dep);
      try
      {
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         PSApplication app = os.getApplicationObject(dep.getDependencyId(),
            tok);

         PSCollection dataSetColl = app.getDataSets();
         if (dataSetColl == null)
            return idTypes;

         Iterator datasets = dataSetColl.iterator();
         while (datasets.hasNext())
         {
            List mappings = new ArrayList();
            PSDataSet ds = (PSDataSet)datasets.next();
            String reqName = getResourceName(ds);

            // check page selection/validation properties
            PSRequestor requestor =  ds.getRequestor();
            mappings.clear();
            PSAppTransformer.checkConditionals(mappings,
               requestor.getSelectionCriteria().iterator(), null);
            idTypes.addMappings(reqName,
               IPSDeployConstants.ID_TYPE_ELEMENT_REQUEST_PROPERTIES,
               mappings.iterator());

            mappings.clear();
            PSAppTransformer.checkConditionals(mappings,
               requestor.getValidationRules().iterator(), null);
            idTypes.addMappings(reqName,
               IPSDeployConstants.ID_TYPE_ELEMENT_REQUEST_VALIDATIONS,
               mappings.iterator());

               PSPipe dsPipe = ds.getPipe();
            if (dsPipe instanceof PSQueryPipe)
            {
               PSQueryPipe queryPipe = (PSQueryPipe) dsPipe;
               PSDataSelector selector = queryPipe.getDataSelector();
               mappings.clear();
               PSAppTransformer.checkConditionals(mappings, selector
                     .getWhereClauses().iterator(), null);
               idTypes.addMappings(reqName,
                     IPSDeployConstants.ID_TYPE_ELEMENT_SELECTOR, mappings
                           .iterator());

               PSResultPageSet pageSet = ds.getOutputResultPages();
               if (pageSet != null)
               {
                  PSCollection pageColl = pageSet.getResultPages();
                  if (pageColl != null)
                  {
                     mappings.clear();
                     Iterator pages = pageColl.iterator();
                     while (pages.hasNext())
                     {
                        PSAppTransformer.checkResultPage(mappings,
                              (PSResultPage) pages.next(), null);
                     }

                     idTypes.addMappings(reqName,
                           IPSDeployConstants.ID_TYPE_ELEMENT_RESULT_PAGES,
                           mappings.iterator());
                  }
               }
            }

            // the mapper
            PSDataMapper dataMapper = dsPipe.getDataMapper();
            if (dataMapper != null)
            {
               mappings.clear();
               PSAppTransformer.checkDataMapper(mappings, dataMapper, null);
               idTypes.addMappings(reqName,
                     IPSDeployConstants.ID_TYPE_ELEMENT_DATA_MAPPER, mappings
                           .iterator());
            }


            // plus regular dataset pre/post exits
            PSExtensionCallSet inputCallSet =
               ds.getPipe().getInputDataExtensions();
            if (inputCallSet != null)
            {
               mappings.clear();
               PSAppTransformer.checkExtensionCalls(mappings,
                  inputCallSet.iterator(), null);
               idTypes.addMappings(reqName,
                  IPSDeployConstants.ID_TYPE_ELEMENT_INPUT_DATA_EXITS,
                  mappings.iterator());
            }

            PSExtensionCallSet resultCallSet =
               ds.getPipe().getResultDataExtensions();
            if (resultCallSet != null)
            {
               mappings.clear();
               PSAppTransformer.checkExtensionCalls(mappings,
                  resultCallSet.iterator(), null);
               idTypes.addMappings(reqName,
                  IPSDeployConstants.ID_TYPE_ELEMENT_RESULT_DATA_EXITS,
                  mappings.iterator());
            }
         }
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }

      return idTypes;
   }

   // see IPSIdTypeHandler interface
   public void transformIds(Object object, PSApplicationIDTypes idTypes,
      PSIdMap idMap) throws PSDeployException
   {
      if (object == null)
         throw new IllegalArgumentException("object may not be null");

      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (!(object instanceof PSApplication))
      {
         throw new IllegalArgumentException("invalid object type");
      }

      PSApplication app = (PSApplication)object;

      // walk id types and perform any transforms
      Iterator resources = idTypes.getResourceList(false);
      while (resources.hasNext())
      {
         String resource = (String)resources.next();
         PSDataSet ds = getResource(app, resource);
         if (ds == null)
            continue;

         Iterator elements = idTypes.getElementList(resource, false);
         while (elements.hasNext())
         {
            String element = (String)elements.next();
            Iterator mappings = idTypes.getIdTypeMappings(
                  resource, element, false);
            while (mappings.hasNext())
            {
               PSApplicationIDTypeMapping mapping =
                  (PSApplicationIDTypeMapping)mappings.next();

               if (mapping.getType().equals(
                  PSApplicationIDTypeMapping.TYPE_NONE))
               {
                  continue;
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_INPUT_DATA_EXITS))
               {
                  PSExtensionCallSet inputCallSet =
                     ds.getPipe().getInputDataExtensions();
                  if (inputCallSet != null)
                     PSAppTransformer.transformExtensionCalls(
                        inputCallSet.iterator(), mapping, idMap);
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_REQUEST_PROPERTIES))
               {
                  PSAppTransformer.transformConditionals(
                     ds.getRequestor().getSelectionCriteria().iterator(),
                     mapping, idMap);
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_REQUEST_VALIDATIONS))
               {
                  PSAppTransformer.transformConditionals(
                     ds.getRequestor().getValidationRules().iterator(),
                     mapping, idMap);
               }               
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_RESULT_DATA_EXITS))
               {
                  PSExtensionCallSet resultCallSet =
                     ds.getPipe().getResultDataExtensions();
                  if (resultCallSet != null)
                     PSAppTransformer.transformExtensionCalls(
                        resultCallSet.iterator(), mapping, idMap);
               }
               else
               {
                  PSPipe dsPipe = ds.getPipe();

                  if (element.equals(
                     IPSDeployConstants.ID_TYPE_ELEMENT_DATA_MAPPER))
                  {
                     PSAppTransformer.transformDataMapper(
                        dsPipe.getDataMapper(), mapping, idMap);
                  }
                  else if (dsPipe instanceof PSQueryPipe)
                  {

                     if (element.equals(
                        IPSDeployConstants.ID_TYPE_ELEMENT_RESULT_PAGES))
                     {
                        PSResultPageSet pageSet = ds.getOutputResultPages();
                        if (pageSet != null)
                        {
                           PSCollection pageColl = pageSet.getResultPages();
                           if (pageColl != null)
                           {
                              Iterator pages = pageColl.iterator();
                              while (pages.hasNext())
                              {
                                 PSAppTransformer.transformResultPage(
                                    (PSResultPage)pages.next(), mapping, idMap);
                              }
                           }
                        }
                     }
                     else if (element.equals(
                        IPSDeployConstants.ID_TYPE_ELEMENT_SELECTOR))
                     {
                        PSQueryPipe queryPipe = (PSQueryPipe)dsPipe;
                        PSDataSelector selector = queryPipe.getDataSelector();
                        PSAppTransformer.transformConditionals(
                           selector.getWhereClauses().iterator(), mapping,
                           idMap);
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Get the current application id for the specified application.
    *
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param appName The name of the app, assumed not <code>null</code> or
    * empty.
    *
    * @return The id, or <code>-1</code> if an application by that name does
    * not exist.
    */
   private int getAppId(PSSecurityToken tok, String appName)
   {
      int appId = -1;

      PSApplicationSummary[] sums =
         PSServerXmlObjectStore.getInstance().getApplicationSummaryObjects(tok,
            false);
      for (int i = 0; i < sums.length && appId == -1; i++)
      {
         if (sums[i].getName().equals(appName))
           appId = sums[i].getId();
      }

      return appId;
   }

   /**
    * Transforms all ids in the app.
    *
    * @param app The app to transform, assumed not <code>null</code>.
    * @param ctx The import context, assumed not <code>null</code>, and that it
    * has a current id Map.
    *
    * @throws PSDeployException if any errors occur.
    */
   private void transformApp(PSApplication app, PSImportCtx ctx)
      throws PSDeployException
   {
      PSIdMap idMap = ctx.getCurrentIdMap();

      // translate id's using idTypes and idMap
      transformIds(app, ctx.getIdTypes(), idMap);

      // translate dbms credentials using the dbms map
      transformDbms(app, ctx);
   }

   /**
    * Transforms all dbms entries in the application that match the source
    * repository or that have mappings in the <code>dbmsMap</code> in the 
    * supplied <code>ctx</code>.
    *
    * @param app The app to transform, assumed not <code>null</code>.
    * @param ctx The import context, assumed not <code>null</code>.
    *
    * @throws PSDeployException if any errors occur
    */
   private static void transformDbms(PSApplication app, PSImportCtx ctx)
      throws PSDeployException
   {
      PSDbmsMap dbmsMap = ctx.getdbmsMap();
      PSDbmsInfo srcRepInfo = ctx.getSourceRepository();
      PSDbmsInfo tgtRepInfo =
         PSDbmsHelper.getInstance().getServerRepositoryInfo();

      transformDbms(app, srcRepInfo, tgtRepInfo, dbmsMap);
   }

   /**
    * Transforms dbms entries in the supplied application
    *  
    * @param app The app to transform, may not be <code>null</code>.
    * @param srcRepInfo The source repository info, optional.  If supplied, then
    * only entries that match this source are transformed to the target.  
    * Entries not matching this source are transformed using the 
    * <code>dbmsMap</code> if supplied and it contains a mapping with a matching
    * source, otherwise the entry is not transformed.  If this parameter is 
    * <code>null</code>, then all entries are transformed to the target info.
    * @param tgtRepInfo The target repository info to use, may not be 
    * <code>null</code>.
    * @param dbmsMap Optional mappings to use to select target info for entires
    * that don't match the <code>srcRepInfo</code>.  Ignored if 
    * <code>srcRepInfo</code> is <code>null</code>.  Otherwise entries that
    * don't match the source are transformed using this map if supplied.  If 
    * <code>null</code>, entries are transformed only if they match the source, 
    * or if no source info is supplied.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public static void transformDbms(PSApplication app, PSDbmsInfo srcRepInfo, 
         PSDbmsInfo tgtRepInfo, PSDbmsMap dbmsMap) throws PSDeployException
   {
      if (app == null)
         throw new IllegalArgumentException("app may not be null");
      
      if (tgtRepInfo == null)
         throw new IllegalArgumentException("tgtRepInfo may not be null");
      
      String srcRep = null;
      
      if (srcRepInfo != null)
         srcRep = srcRepInfo.getDbmsIdentifier();     
      
      // get all mappings and locate backend tables in the app
      PSCollection dataSetColl = app.getDataSets();
      if (dataSetColl == null)
         return;

      Iterator datasets = dataSetColl.iterator();
      while (datasets.hasNext())
      {
         PSDataSet ds = (PSDataSet)datasets.next();
         PSPipe pipe  = ds.getPipe();
         if (pipe == null)
            continue;

         PSBackEndDataTank tank = pipe.getBackEndDataTank();
         if (tank == null)
            continue;

         Iterator tables = tank.getTables().iterator();
         while (tables.hasNext())
         {
            PSBackEndTable table = (PSBackEndTable)tables.next();
            if (srcRep != null && dbmsMap != null &&
                StringUtils.isNotEmpty(table.getDataSource()))
            {
               Iterator mappings = dbmsMap.getMappings();
               while (mappings.hasNext())
               {
                  PSDbmsMapping mapping = (PSDbmsMapping)mappings.next();
                  String srcInfo = mapping.getSourceInfo();
                  String tgtInfo = mapping.getTargetInfo();
                  if (tgtInfo == null)
                     continue;
   
                  // ??? is this right ? -vamsi
                  if (StringUtils.isNotBlank(srcInfo))
                  {
                     // do the xform
                     transformTable(tgtInfo, table);
                     break; // found our mapping
                  }
               }
            }
         }
      }
   }

   /**
    * Determine if the specified app is a system app.
    *
    * @param appName The name, it may not be <code>null</code> or empty.
    *
    * @return <code>true</code> if it is a system app, <code>false</code>
    * otherwise
    */
   public static boolean isSystemApp(String appName)
   {
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");

      return appName.toLowerCase().startsWith(SYS_PREFIX) ||
         appName.equalsIgnoreCase("administration") ||
         appName.equalsIgnoreCase("docs") ||
         appName.equalsIgnoreCase("dtd") ||
         appName.equalsIgnoreCase("rx_resources") ||
         appName.equalsIgnoreCase("web_resources") ;
   }


   
   /**
    * Transforms the provided table and credentials using the target info.
    * Changes the datasource. 
    * @param tgtInfo The new dbms info, assumed not <code>null</code>.
    * @param table The table to transform, assumed not <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   private static void transformTable(String tgtInfo,
      PSBackEndTable table) throws PSDeployException
   {
      // do the xform
      table.setDataSource(tgtInfo);
      // clear the connection detail, if any
      table.setConnectionDetail(null);
   }

   /**
    * Gets all dependencies from any stylesheets owned by the application.
    *
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param dep The dependency to check, assumed not <code>null</code>.
    *
    * @return The list of child depenendencies, never <code>null</code>, may
    * be empty.
    *
    * @throws PSDeployException if there are any errors.
    */
   private List getStyleSheetDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      List deps = new ArrayList();

      String appName = dep.getDependencyId();
      Iterator files = getAppFiles(tok, appName);
      while (files.hasNext())
      {
         File file = (File)files.next();
         if (PSStylesheetDependencyHandler.isStylesheet(file.getName()) &&
            !"src".equalsIgnoreCase(getRootDir(file)))
         {
            Document sheetDoc = getXmlFileFromApp(tok, appName, file);
            Iterator sheetDeps = getStylesheetDependencies(tok, sheetDoc);
            while (sheetDeps.hasNext())
            {
               PSDependency sheetDep = (PSDependency)sheetDeps.next();
               if (!sheetDep.getKey().equals(dep.getKey()))
                  deps.add(sheetDep);
            }
         }
      }

      return deps;
   }

   /**
    * Gets the resource name used to store id type mappings for the supplied
    * resource.
    *
    * @param ds The dataset to store mappings for, assumed not
    * <code>null</code>.
    *
    * @return The name, assumed not <code>null</code> or empty.
    */
   public static String getResourceName(PSDataSet ds)
   {
      return ds.getRequestor().getRequestPage() + " (" + ds.getName() +
         ")";
   }

   /**
    * Gets the dataset from the app represented by the supplied resourceName.
    *
    * @param app The application to check, assumed not <code>null</code>.
    * @param resourceName A resource name returned by
    * , assumed not
    * <code>null</code> or empty.
    *
    * @return The dataset, or <code>null</code> if not found.
    */
   public static PSDataSet getResource(PSApplication app, String resourceName)
   {
      PSDataSet ds = null;

      PSCollection dataSetColl = app.getDataSets();
      if (dataSetColl != null)
      {
         Iterator datasets = dataSetColl.iterator();
         while (datasets.hasNext() && ds == null)
         {
            PSDataSet test = (PSDataSet)datasets.next();
            if (getResourceName(test).equals(resourceName))
               ds = test;
         }
      }

      return ds;
   }


   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "Application";

   /**
    * Constant for system app prefix.
    */
   private static final String SYS_PREFIX = "sys_";

   /**
    * List of child types supported by this handler, never <code>null</code> or
    * empty.
    */
   private static List ms_childTypes = new ArrayList();

   /**
    * Set of all child types supported by this handler, including any type
    * supporting idType mapping, never <code>null</code> or empty.
    */
   private Set m_childTypes = new HashSet();


   static
   {
      ms_childTypes.add(DEPENDENCY_TYPE);
      ms_childTypes.add(PSControlDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSKeywordDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSRoleDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSchemaDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSharedGroupDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSStylesheetDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSupportFileDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSystemDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSystemDefElementDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSWorkflowDependencyHandler.DEPENDENCY_TYPE);
   }


}
