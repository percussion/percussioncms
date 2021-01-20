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

package com.percussion.deployer.server;

import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.deployer.objectstore.PSArchiveDetail;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyContext;
import com.percussion.deployer.objectstore.PSDependencyTreeContext;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.objectstore.PSUserDependency;
import com.percussion.deployer.objectstore.PSValidationResult;
import com.percussion.deployer.objectstore.PSValidationResults;
import com.percussion.deployer.server.dependencies.PSAclDefDependencyHandler;
import com.percussion.deployer.server.dependencies.PSCustomDependencyHandler;
import com.percussion.deployer.server.dependencies.PSDependencyHandler;
import com.percussion.deployer.server.dependencies.PSUserDependencyHandler;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.utils.PSIdNameHelper;
import com.percussion.util.PSIteratorUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Manager class for all dependency handlers. The {@link PSDeploymentHandler}
 * creates and holds an instance when initialized. All other classes should call
 * {@link #getInstance()} to obtain an instance of this class.
 */
@SuppressWarnings(value = { "unchecked" })
public class PSDependencyManager
{
   /**
    * Loads the <code>PSDependencyMap</code> using the XML document found in
    * the <code>server/cfg</code> directory below the deployment root.
    * 
    * @throws PSDeployException if there are any errors.
    * 
    * @throws IllegalStateException if an instance has already been created. The
    * {@link PSDeploymentHandler} creates and holds an instance when
    * initialized. All other classes should call {@link #getInstance()} to
    * obtain an instance of this class.
    */
   PSDependencyManager() throws PSDeployException
   {
      if (ms_instance != null)
         throw new IllegalStateException("Instance has already been created");

      String configDir = ms_configDir;
      boolean buildDepMaps = false;
      try
      {
         if (ms_configDir == null)
         {
            configDir = PSDeploymentHandler.CFG_DIR.getPath();
            buildDepMaps = true;
         }
         loadConfigFiles(configDir, buildDepMaps);
      }
      catch (Exception e)
      {
         ms_log.error("Failed to load configure file from " + configDir, e);
         throw new PSDeployException(IPSDeploymentErrors.DEPENDENCY_MGR_INIT,
               e.toString());
      }
   }

   /**
    * Set the configuration directory which contains the configure files
    * 
    * @param configDir the configure directory, may not be <code>null</code>
    * or empty.
    */
   public static void setConfigDir(String configDir)
   {
      if (StringUtils.isBlank(configDir))
         throw new IllegalArgumentException("configDir cannot be null.");

      ms_configDir = configDir;
   }

   /**
    * Loads the configure files from the specified directory.
    * 
    * @param dir the configure directory, assumed not <code>null</code> or
    * empty.
    * @throws Exception if an error occurs.
    */
   private void loadConfigFiles(String dir, boolean buildDepMaps)
      throws Exception
   {
      FileInputStream in = null;

      try
      {
         File rootDir = new File(dir);
         File depMapFile = new File(rootDir, CONFIG_FILE_NAME);
         in = new FileInputStream(depMapFile);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         PSPackageConfiguration config = new PSPackageConfiguration(doc
               .getDocumentElement(), buildDepMaps);

         m_deployOrder = config.getDeployOrder();
         m_uninstallIgnoreTypes = config.getUninstallIgnoreTypes();
         m_depMap = config.getDependencyMap();
         createTypeMappings();
         ms_instance = this;
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.DEPENDENCY_MGR_INIT,
               e.toString());
      }
      finally
      {
         if (in != null)
            try
            {
               in.close();
            }
            catch (IOException e)
            {
            }
      }
   }

   /**
    * Gets all child dependencies for the supplied dependency.
    * 
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param dep The dependency whose children should be retrieved, may not be
    * <code>null</code>.
    * 
    * @return an Iterator over zero or more PSDependency objects, never
    * <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any other errors.
    */
   public Iterator getDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      Iterator deps = null;
      deps = m_depCache.getChildDependencies(dep);

      if (deps == null)
      {
         // get child dependencies
         PSDependencyDef def = getDependencyDef(dep.getObjectType());
         PSDependencyHandler handler = m_depMap.getDependencyHandler(def);
         Iterator children = handler.getChildDependencies(tok, dep);

         // get user dependencies
         Iterator userDeps = getUserDependencies(dep);

         deps = PSIteratorUtils.joinedIterator(children, userDeps);
         deps = m_depCache.setChildDependencies(dep, deps);
      }

      return deps;
   }

   /**
    * Gets all dependencies that have the supplied dependency as a child. Will
    * return an empty iterator if the supplied dependency is of type
    * {@link PSDependency#TYPE_USER}
    * 
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param dep The dependency to return ancestors for, may not be
    * <code>null</code>.
    * 
    * @return an iterator over zero or more <code>PSDependency</code> objects.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException for any other errors.
    */
   public Iterator getAncestors(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (dep.getDependencyType() == PSDependency.TYPE_USER)
         return PSIteratorUtils.emptyIterator();

      return getParentDependencies(tok, dep);
   }

   /**
    * Adds the files of the supplied dependency and all of its included
    * dependencies to the supplied archive handler.
    * 
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param dependency The dependency to add, may not be <code>null</code>.
    * @param archiveHandler The archive handler to use, may not be
    * <code>null</code>.
    * @param jobHandle The job handle to use to update the status, may not be
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid or if calling
    * <code>dependency.isIncluded()</code> returns <code>false</code>.
    * @throws PSDeployException if there are any errors.
    */
   public void addToArchive(PSSecurityToken tok, PSDependency dependency,
         PSArchiveHandler archiveHandler, IPSJobHandle jobHandle)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dependency == null)
         throw new IllegalArgumentException("dependency may not be null");

      if (archiveHandler == null)
         throw new IllegalArgumentException("archiveHandler may not be null");

      if (jobHandle == null)
         throw new IllegalArgumentException("jobHandle may not be null");

      PSDependencyDef def = getDependencyDef(dependency.getObjectType());
      PSDependencyHandler handler = m_depMap.getDependencyHandler(def);

      if (dependency.isIncluded())
      {
         updateJobStatus(dependency, jobHandle);
         if (!archiveHandler.hasDependencyFiles(dependency))
         {
            Iterator files = handler.getDependencyFiles(tok, dependency);
            if (files.hasNext())
               archiveHandler.addFiles(dependency, files);

            if (dependency.supportsIdTypes())
            {
               // Get id type map and store it in the archive.
               PSApplicationIDTypes idTypes = PSIdTypeManager
                     .loadIdTypes(dependency.getKey());
               if (idTypes == null)
               {
                  Object[] args = { dependency.getObjectTypeName(),
                        dependency.getDependencyId() };
                  throw new PSDeployException(
                        IPSDeploymentErrors.MISSING_ID_TYPES, args);
               }

               archiveHandler.addIdTypes(dependency, idTypes);
            }

            List dbmsInfoList = handler.getExternalDbmsInfoList(tok,
                  dependency);
            if (dbmsInfoList != null)
               archiveHandler.addDbmsInfoList(dependency, dbmsInfoList);
         }
      }

      Iterator deps = dependency.getDependencies();
      if (deps != null)
      {
         while (deps.hasNext())
         {
            PSDependency dep = (PSDependency) deps.next();
            if (!(dep instanceof PSDeployableElement))
            {
               addToArchive(tok, dep, archiveHandler, jobHandle);
            }
         }
      }
   }

   /**
    * This is used to reorder the packaged elements, separate and group the
    * elements by their object-type.
    */
   private class OrderedElement
   {
      /**
       * The object type of the ordered element.
       */
      String mi_objType;

      /**
       * A list of ordered elements with the {@link #mi_objType} object type.
       */
      List<PSImportPackage> mi_elements = new ArrayList<PSImportPackage>();
   }

   /**
    * Creates a base array for grouping the packaged elements by their object
    * type.
    * 
    * @return the base array, never <code>null</code> or empty.
    */
   private OrderedElement[] getInitialOrderedElements()
   {
      List<OrderedElement> result = new ArrayList<OrderedElement>();
      for (String type : m_deployOrder)
      {
         OrderedElement elem = new OrderedElement();
         elem.mi_objType = type;
         result.add(elem);
      }
      OrderedElement[] array = new OrderedElement[result.size()];
      return result.toArray(array);
   }

   /**
    * Returns the list of type enum names of the types that can be ignored for
    * unistall.
    * 
    * @return never <code>null</code> may be empty.
    */
   public List<String> getUninstallIgnoreTypes()
   {
      return m_uninstallIgnoreTypes;
   }

   /**
    * Gets the object type of the given package element for all elements, but it
    * returns the child object type (which is in the dependency ID of the
    * element) for "Custom" element.
    * 
    * @param pkgElement the package element in question, it may not be
    * <code>null</code>.
    * 
    * @return the object type of the package element as described above.
    * 
    * @throws PSDeployException if an error occurs.
    */
   public String getObjectType(PSImportPackage pkgElement)
      throws PSDeployException
   {
      if (pkgElement == null)
         throw new IllegalArgumentException("pkgElement may be not null.");

      String objType = pkgElement.getPackage().getObjectType();
      PSDependencyDef def = getDependencyDef(objType);
      if (def.isDeployableElement()
            && (!PSCustomDependencyHandler.DEPENDENCY_TYPE
                  .equalsIgnoreCase(objType)))
      {
         return objType;
      }

      if (!PSCustomDependencyHandler.DEPENDENCY_TYPE.equals(objType))
      {
         throw new IllegalStateException("The undeployable object type, "
               + objType + ", is not expected.");
      }
      // the actual object type of the "Custom" element is from the object ID.
      String depId = pkgElement.getPackage().getDependencyId();
      if (PSUserDependencyHandler.DEPENDENCY_TYPE.equals(depId))
         return depId;

      String[] keys = PSDependency.parseKey(depId);
      return keys[0];
   }

   /**
    * Adds a given package element into the supplied order list.
    * 
    * @param orderList the order list used to collect and group package element,
    * assumed not <code>null</code> or empty.
    * @param pkgElement the package element, assumed not <code>null</code>.
    * 
    * @throws PSDeployException if an error occurs.
    */
   private void addPkgElement(OrderedElement[] orderList,
         PSImportPackage pkgElement) throws PSDeployException
   {
      String objType = getObjectType(pkgElement);

      for (OrderedElement elem : orderList)
      {
         if (elem.mi_objType.equals(objType))
            elem.mi_elements.add(pkgElement);
      }
   }

   /**
    * Reorder the packaged elements in the order specified in the configuration
    * file.
    * 
    * @param deployList the original packaged element, may not be
    * <code>null</code> or empty.
    * 
    * @return the reordered elements, never <code>null</code> or empty.
    * 
    * @throws PSDeployException if an error occurs.
    */
   public List<PSImportPackage> reorderDeployedElements(
         List<PSImportPackage> deployList) throws PSDeployException
   {
      if (deployList == null || deployList.isEmpty())
         throw new IllegalArgumentException(
               "deployList may not be null or empty.");

      logPackageElementOrder(deployList, true);

      OrderedElement[] orderList = getInitialOrderedElements();
      for (PSImportPackage elem : deployList)
      {
         addPkgElement(orderList, elem);
      }

      List<PSImportPackage> result = new ArrayList<PSImportPackage>();
      for (OrderedElement elem : orderList)
      {
         if (!elem.mi_elements.isEmpty())
         {
            result.addAll(elem.mi_elements);
         }
      }

      logPackageElementOrder(result, false);

      return result;
   }

   /**
    * Logs the order of the given package element list.
    * 
    * @param elements the list of package elements, assumed not
    * <code>null</code> or empty.
    * @param isOriginal <code>true</code> if the list is pre-ordered list.
    * 
    * @throws PSDeployException if an error occurs.
    */
   private void logPackageElementOrder(List<PSImportPackage> elements,
         boolean isOriginal) throws PSDeployException
   {
      if (!ms_log.isDebugEnabled())
         return;

      if (isOriginal)
         ms_log.debug("Original package element order:");
      else
         ms_log.debug("New package element order:");

      for (PSImportPackage elem : elements)
      {
         ms_log.debug("\t objectType=\"" + getObjectType(elem)
               + "\", displayName=\"" + elem.getPackage().getDisplayName()
               + "\".");
      }
   }

   /**
    * Restores the deployable element and all of its children from the archive
    * to the Rhythmyx server installation.
    * 
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param dependency The deployable element to restore, may not be
    * <code>null</code>.
    * @param archiveHandler The archive handler to use to retrieve files from
    * the archive, may not be <code>null</code>.
    * @param ctx The import context that provides access to some of the runtime
    * context and managers, may not be <code>null</code>.
    * @param jobHandle The job handle to use to update the status, may not be
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any other errors.
    */
   public void restoreFromArchive(PSSecurityToken tok,
         PSDeployableElement dependency, PSArchiveHandler archiveHandler,
         PSImportCtx ctx, IPSJobHandle jobHandle) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dependency == null)
         throw new IllegalArgumentException("dependency may not be null");

      if (archiveHandler == null)
         throw new IllegalArgumentException("archiveHandler may not be null");

      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      if (jobHandle == null)
         throw new IllegalArgumentException("jobHandle may not be null");

      // if transforming id's, update id map and set on the context
      PSIdMap curMap = ctx.getCurrentIdMap();
      if (curMap != null)
         reserveNewIds(dependency, curMap);

      List deferList = new ArrayList();

      try
      {
         // now install children
         Iterator deps = dependency.getDependencies();
         if (deps != null)
         {
            while (deps.hasNext() && !jobHandle.isCancelled())
            {
               PSDependency child = (PSDependency) deps.next();
               restoreDependencyFromArchive(tok, child, dependency,
                     archiveHandler, ctx, deferList, jobHandle);
            }
         }

         // now install deferred deps
         deps = deferList.iterator();
         while (deps.hasNext() && !jobHandle.isCancelled())
         {
            PSDependency dep = (PSDependency) deps.next();
            PSDependencyDef def = getDependencyDef(dep.getObjectType());
            PSDependencyHandler depHandler = m_depMap
                  .getDependencyHandler(def);
            installDependency(tok, dep, depHandler, archiveHandler, ctx,
                  jobHandle, dependency);
         }
      }
      finally
      {
         // now save idMap if we had one
         if (curMap != null)
            ctx.getIdMapMgr().saveIdMap(curMap);
      }

   }

   /**
    * Restores the dependency and all of its children from the archive to the
    * Rhythmyx server installation.
    * 
    * @param tok The security token to use if objectstore access is required,
    * assumed not <code>null</code>.
    * @param dependency The dependency to restore, assumed not <code>null</code>.
    * @param root The root dependency of this dependency's tree, assumed not
    * <code>null</code>.
    * @param archiveHandler The archive handler to use to retrieve files from
    * the archive, assumed not <code>null</code>.
    * @param ctx The import context that provides access to some of the runtime
    * context and managers, assumed not <code>null</code>.
    * @param deferList A List of dependencies whose installation should be
    * deferred until all others have been installed, assumed not
    * <code>null</code>.
    * @param jobHandle The job handle to use to update the status, assumed not
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any other errors.
    */
   private void restoreDependencyFromArchive(PSSecurityToken tok,
         PSDependency dependency, PSDeployableElement root,
         PSArchiveHandler archiveHandler, PSImportCtx ctx, List deferList,
         IPSJobHandle jobHandle) throws PSDeployException
   {
      // if we hit an element down in the tree, we are done
      if (dependency instanceof PSDeployableElement)
         return;

      boolean isLocal = dependency.getDependencyType() == PSDependency.TYPE_LOCAL;

      // validation results may indicate an included shared dependency should be
      // skipped
      boolean isIncluded = dependency.isIncluded();
      if (isIncluded && !isLocal)
      {
         PSValidationResults valResults = ctx.getCurrentValidationResults();
         if (valResults != null)
         {
            PSValidationResult result = valResults.getResult(dependency);
            if (result != null && result.isSkip())
               isIncluded = false;
         }
      }

      // local deps will appear included, but don't try to restore them
      // if the first non-local ancestor is not included (only included with
      // parent)
      if (isIncluded
            && (!isLocal || root.includesDependency(dependency, true)))
      {
         PSDependencyDef def = getDependencyDef(dependency.getObjectType());
         PSDependencyHandler depHandler = m_depMap.getDependencyHandler(def);
         if (depHandler.shouldDeferInstallation())
            deferList.add(dependency);
         else if (!jobHandle.isCancelled())
         {
            installDependency(tok, dependency, depHandler, archiveHandler,
                  ctx, jobHandle, root);
         }
      }

      // now install children
      Iterator deps = dependency.getDependencies();
      if (deps != null)
      {
         while (deps.hasNext() && !jobHandle.isCancelled())
         {
            PSDependency child = (PSDependency) deps.next();
            restoreDependencyFromArchive(tok, child, root, archiveHandler,
                  ctx, deferList, jobHandle);
         }
      }
   }

   /**
    * Installs the supplied dependency.
    * 
    * @param tok The security token to use if objectstore access is required,
    * assumed not <code>null</code>.
    * @param dependency The dependency to restore, assumed not <code>null</code>.
    * @param depHandler The handler for this dependency, assumed to be not
    * <code>null</code> and of the correct type.
    * @param archiveHandler The archive handler to use to retrieve files from
    * the archive, assumed not <code>null</code>.
    * @param ctx The import context that provides access to some of the runtime
    * context and managers, assumed not <code>null</code>.
    * @param jobHandle The job handle to use to update the status, assumed not
    * <code>null</code>.
    * @param root The root dependency of this dependency's tree, assumed not
    * <code>null</code>.
    * 
    * @throws PSDeployException if any errors occur.
    */
   private void installDependency(PSSecurityToken tok,
         PSDependency dependency, PSDependencyHandler depHandler,
         PSArchiveHandler archiveHandler, PSImportCtx ctx,
         IPSJobHandle jobHandle, PSDeployableElement root)
      throws PSDeployException
   {
      updateJobStatus(dependency, jobHandle);

      // if included multiple times in package, just skip
      if (!ctx.isDependencyInstalled(dependency, root))
      {
         // if already installed at all, add skip to log
         if (ctx.isDependencyInstalled(dependency))
         {
            depHandler.addTransactionLogEntry(dependency, ctx,
                  PSTransactionSummary.TYPE_SKIPPED,
                  PSTransactionSummary.TYPE_SKIPPED,
                  PSTransactionSummary.ACTION_SKIPPED_ALREADY_INSTALLED);
         }
         else
         {
            // Dependency not yet installed, hold it.
            ctx.setCurrentDependency(dependency);
            try
            {
               PSApplicationIDTypes idTypes = null;
               if (dependency.supportsIdTypes())
               {
                  // get id types from archive, set on context
                  idTypes = archiveHandler.getIdTypes(dependency);
                  if (idTypes == null)
                  {
                     Object[] args = { dependency.getObjectTypeName(),
                           dependency.getDependencyId() };
                     throw new PSDeployException(
                           IPSDeploymentErrors.MISSING_ID_TYPES, args);
                  }
                  ctx.setIdTypes(idTypes);
               }
               depHandler.installDependencyFiles(tok, archiveHandler,
                     dependency, ctx);
               // Check for null pkgGuid
               if (ctx.getPkgGuid() == null)
               {
                  throw new PSDeployException(
                        IPSDeploymentErrors.MISSING_PKG_GUID);
               }

               // Transform dependency id if necessary
               String depId = dependency.getDependencyId();
               PSIdMap idMap = ctx.getCurrentIdMap();
               Boolean supportsIdMapping = dependency.supportsIDMapping();

               if (idMap != null && supportsIdMapping)
               {
                  PSIdMapping depMapping = getIdMapping(dependency, ctx
                        .getCurrentIdMap());
                  if (depMapping != null)
                     depId = depMapping.getTargetId();
               }

               if (!dependency.supportsParentId() && !dependency.getObjectType().equals(PSAclDefDependencyHandler.DEPENDENCY_TYPE))
               {
                  // Save package element data to database
                  IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
                        .getPkgInfoService();
                  PSPkgElement pkgElem = pkgService.createPkgElement(ctx
                        .getPkgGuid());

                  IPSGuid pkgElemGuid = PSIdNameHelper.getGuid(depId, 
                        getGuidType(dependency.getObjectType()));
                  pkgElem.setObjectGuid(pkgElemGuid);
                  pkgService.savePkgElement(pkgElem);
               }

               // now save id types if we have them - they should be transformed
               if (idTypes != null)
               {
                  // Attempt to reset dependencyId to the mapped target id
                  if (idMap != null && supportsIdMapping)
                  {
                     String targetId = getIdMapping(dependency, idMap)
                           .getTargetId();
                     if (targetId != null)
                     {
                        PSDependency clonedDependency = (PSDependency) idTypes
                              .getDependency().clone();
                        clonedDependency.setDependencyId(targetId);
                        idTypes.setDependency(clonedDependency);
                     }
                  }
                  PSIdTypeManager.saveIdTypes(idTypes);
               }

               // save user dependency if not child of custom element
               if (dependency instanceof PSUserDependency)
               {
                  PSUserDependency userDep = (PSUserDependency) dependency;
                  if (!userDep.getParentType().equals(
                        IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM))
                  {
                     saveUserDependency(userDep);
                  }
               }
            }
            catch (PSDeployException e)
            {
               // log the specific dependency that failed, to aid debugging
               ms_log.error("failure while processing: "
                     + formatDependencyString(dependency));
               throw e;
            }
            catch (RuntimeException e)
            {
               // log the specific dependency that failed, to aid debugging
               ms_log.error("failure while processing: "
                     + formatDependencyString(dependency));
               throw e;
            }
         }

         // mark as installed for this package
         ctx.addInstalledDependency(dependency, root);
      }
   }

   /**
    * Returns the dependency's identifying information formatted for output.
    * 
    * @param dependency The dependency to use, assumed not <code>null</code>.
    * 
    * @return The string, never <code>null</code> or empty.
    */
   private String formatDependencyString(PSDependency dependency)
   {
      ResourceBundle bundle = PSDeploymentManager.getBundle();
      Object[] args = { dependency.getObjectTypeName(),
            dependency.getDependencyId(), dependency.getDisplayName() };
      return MessageFormat.format(bundle.getString("depString"), args);
   }

   /**
    * Gets all existing dependencies of the specified type.
    * 
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param type The type to get, may not be <code>null</code> or empty. For
    * Custom types, supply the custom object type ({@link IPSDeployConstants#DEP_OBJECT_TYPE_CUSTOM})
    * concatenated with the supported local dependency type using a forward
    * slash as a delimeter (e.g. "Custom/Application"). For each instance of the
    * child dependency type that exists, a custom deployable element will be
    * returned with the child dependency as a local child. If "Custom/User" is
    * specified, a single custom deployable element with no children will be
    * returned.
    * 
    * @return an Iterator over zero or more PSDependency objects.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any other errors.
    */
   public Iterator getDependencies(PSSecurityToken tok, String type)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      Iterator result = m_depCache.getDependencies(type);
      if (result == null)
      {
         // see if it is a Custom type
         String custType = null;
         StringTokenizer toker = new StringTokenizer(type, "/");
         if (toker.hasMoreTokens())
         {
            String tmpType = toker.nextToken();
            if (tmpType.equals(IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM)
                  && toker.hasMoreTokens())
            {
               custType = toker.nextToken();
               type = tmpType;
            }
         }

         PSDependencyDef def = getDependencyDef(type);
         PSDependencyHandler handler = m_depMap.getDependencyHandler(def);

         if (custType != null)
         {
            List customDeps = new ArrayList();
            if (custType.equals(PSUserDependency.USER_DEPENDENCY_TYPE))
            {
               PSDependency userDep = handler.getDependency(tok, custType);
               if (userDep != null)
                  customDeps.add(userDep);
            }
            else
            {
               // get all child deps of the specified custom type
               PSDependencyHandler childHandler = m_depMap
                     .getDependencyHandler(getDependencyDef(custType));
               Iterator childDeps = childHandler.getDependencies(tok);
               while (childDeps.hasNext())
               {
                  // create a custom deployable element with the child
                  PSDependency childDep = (PSDependency) childDeps.next();
                  PSDependency dep = handler.getDependency(tok, childDep
                        .getKey());
                  if (dep != null)
                     customDeps.add(dep);
               }
            }
            result = customDeps.iterator();
         }
         else
         {
            result = handler.getDependencies(tok);
         }

         result = m_depCache.setDependencies(type, result);
      }

      return result;
   }

   /**
    * Gets all existing dependencies of the specified type with the specified
    * parent id.
    * 
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param type The type to get, may not be <code>null</code> or empty. Must
    * support parent ids.
    * @param parentId Specifies the parent of all dependencies to return, may
    * not be <code>null</code> or empty.
    * 
    * @return an Iterator over zero or more PSDependency objects, never
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any other errors.
    */
   public Iterator getDependencies(PSSecurityToken tok, String type,
         String parentId) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      if (parentId == null || parentId.trim().length() == 0)
         throw new IllegalArgumentException(
               "parentId may not be null or empty");

      // see if it is a Custom type
      PSDependencyDef def = getDependencyDef(type);
      if (!def.supportsParentId())
         throw new IllegalArgumentException("type does not support parent id");

      PSDependencyHandler handler = m_depMap.getDependencyHandler(def);

      return handler.getDependencies(tok, handler.getParentType(), parentId);
   }

   /**
    * Get a map of child and parent types for dependency types that support
    * parent ids.
    * 
    * @return A Map where the key is the child type and the value is the parent
    * type, both as non-<code>null</code>, non-empty <code>String</code>
    * objects, never <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public Map getParentTypes() throws PSDeployException
   {
      if (m_parentTypeMap == null)
      {
         Map types = new HashMap();
         Iterator defs = m_depMap.getDefs();
         while (defs.hasNext())
         {
            PSDependencyDef def = (PSDependencyDef) defs.next();
            if (def.supportsParentId())
            {
               PSDependencyHandler handler = PSDependencyHandler
                     .getHandlerInstance(def, m_depMap);
               types.put(def.getObjectType(), handler.getParentType());
            }
         }
         m_parentTypeMap = types;
      }

      return m_parentTypeMap;
   }

   /**
    * Get the singleton instance of this class
    * 
    * @return the instance, may be <code>null</code> if it has not yet been
    * created.
    */
   public static PSDependencyManager getInstance()
   {
      return ms_instance;
   }

   /**
    * Recursively ensures that there is at least one level of child dependencies
    * below any included child dependency of the supplied deployable element,
    * setting the child dependencies if they have not already been set. Also
    * adds all loadable handlers as immediate server dependencies.
    * <p>
    * Calls {@link PSDependency#setIsAutoDependency(boolean) 
    * setIsAutoDependency(true)} for all dependencies added.
    * 
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param pkg The deployable element to check, may not be <code>null</code>.
    * @param treeCtx The tree context to use to determine inclusion of any
    * dependencies added, may not be <code>null</code> and must contain all
    * dependencies in the supplied <code>pkg</code>.
    * @param jobHandle The current job handle, used to check if the job has been
    * cancelled, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any other errors.
    */
   public void addMissingDependencies(PSSecurityToken tok,
         PSDeployableElement pkg, PSDependencyTreeContext treeCtx,
         IPSJobHandle jobHandle) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");

      if (treeCtx == null)
         throw new IllegalArgumentException("treeCtx may not be null");

      if (jobHandle == null)
         throw new IllegalArgumentException("jobHandle may not be null");

      // IMPORTANT: must call setIsAutoDependency(true) for any dependency
      // added.
      Set loaded = new HashSet();
      addMissingDependencies(tok, pkg, true, loaded, treeCtx, jobHandle);

   }

   /**
    * Validate missing and modified packages in a given export descriptor. The
    * packages in the export descriptor will be modified if there are missing or
    * modified packages and updates the display names of all dependencies in
    * case they have changed.
    * 
    * @param tok The security token, it may not be <code>null</code>.
    * @param exportDesc The export descriptor, it may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if any error occurs.
    */
   public void validatePackages(PSSecurityToken tok,
         PSExportDescriptor exportDesc) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (exportDesc == null)
         throw new IllegalArgumentException("exportDesc may not be null");

      setIsDependencyCacheEnabled(true);
      try
      {
         Iterator pkgs = exportDesc.getPackages();

         // handle the missing and modified packages
         List exportPkgs = new ArrayList();
         List missingPkgNames = new ArrayList();
         List modPkgNames = new ArrayList();
         while (pkgs.hasNext())
         {
            PSDeployableElement pkg = (PSDeployableElement) pkgs.next();
            PSDependencyDef depDef = getDependencyDef(pkg.getObjectType());
            PSDependencyHandler depHandler = PSDependencyHandler
                  .getHandlerInstance(depDef, m_depMap);

            PSDependency curPkg = depHandler.getDependency(tok, pkg
                  .getDependencyId());
            if (curPkg == null)
               missingPkgNames.add(pkg.getDisplayName());
            else
            {
               // update the def in case it has changed
               pkg.updateDependencyDefinition(curPkg);
               // update display name in case it has changed
               pkg.setDisplayName(curPkg.getDisplayName());
               if (checkModifiedDependencies(tok, pkg))
                  modPkgNames.add(pkg.getDisplayName());
               exportPkgs.add(pkg);
            }
         }

         // reset descriptor packages with fixed up pkg list
         exportDesc.setPackages(exportPkgs.iterator());

         if (!missingPkgNames.isEmpty())
            exportDesc.addMissingPackages(missingPkgNames.iterator());

         if (!modPkgNames.isEmpty())
            exportDesc.setModifiedPackages(modPkgNames.iterator());
      }
      finally
      {
         setIsDependencyCacheEnabled(false);
      }
   }

   /**
    * Loads the export descriptor from the supplied archive info and transforms
    * all IDs.
    * 
    * @param info The archive info containing the descriptor. May not be
    * <code>null</code>. <code>info.getArchiveDetail()</code> must not
    * return <code>null</code>.
    * @param logHandler The log handler to use, may not be <code>null</code>.
    * @param idMap The idMap to use to transform ids, <code>null</code> if no
    * transformation is required.
    * 
    * @return The converted descriptor, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public PSExportDescriptor convertExportDescriptor(PSArchiveInfo info,
         PSLogHandler logHandler, PSIdMap idMap) throws PSDeployException
   {
      if (info == null)
         throw new IllegalArgumentException("info may not be null");

      if (logHandler == null)
         throw new IllegalArgumentException("logHandler may not be null");

      PSArchiveDetail detail = info.getArchiveDetail();
      if (detail == null)
         throw new IllegalArgumentException(
               "supplied archive info's detail may not be null");

      PSExportDescriptor desc = detail.getExportDescriptor();

      // transforming IDs
      Iterator pkgs = desc.getPackages();
      while (pkgs.hasNext())
      {
         PSDeployableElement pkg = (PSDeployableElement) pkgs.next();
         {
            transformDeps(pkg, idMap);
         }
      }

      return desc;
   }

   /**
    * Saves the supplied user dependency to the file system.
    * 
    * @param dep The user dependency to save, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>dep</code> is
    * <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public void saveUserDependency(PSUserDependency dep)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      FileOutputStream out = null;
      try
      {
         // save dep as Xml file in directory named using its parent's key
         String name = "UserDep" + dep.getKey();
         File depDir = new File(USER_DEP_DIR, dep.getParentKey());
         depDir.mkdirs();
         File depFile = new File(depDir, name + ".xml");
         out = new FileOutputStream(depFile);
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.write(dep.toXml(doc), out);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
               .getLocalizedMessage());
      }
      finally
      {
         if (out != null)
            try
            {
               out.close();
            }
            catch (IOException ex)
            {
            }
      }
   }

   /**
    * Deletes the supplied user dependency from the file system if it exists.
    * 
    * @param dep The user dependency to delete, may not be <code>null</code>.
    * If the specified user dependency is not found, the method simply returns
    * without error.
    */
   public void deleteUserDependency(PSUserDependency dep)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      // check for saved dep as Xml file in directory named using its parent's
      // key
      String name = "UserDep" + dep.getKey();
      File depDir = new File(USER_DEP_DIR, dep.getParentKey());

      if (depDir.exists())
      {
         File depFile = new File(depDir, name + ".xml");
         if (depFile.exists())
            depFile.delete();
      }
   }

   /**
    * Find and return the specified dependency.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param type The dependency type, may not be <code>null</code> or empty
    * and must be a valid type.
    * @param depId The id, may not be <code>null</code> or empty.
    * 
    * @return The dependency, or <code>null</code> if it is not found.
    * @throws PSDeployException if there are any errors.
    */
   public PSDependency findDependency(PSSecurityToken tok, String type,
         String depId) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (StringUtils.isBlank(type))
         throw new IllegalArgumentException("type may not be null or empty");
      if (StringUtils.isBlank(depId))
         throw new IllegalArgumentException("depId may not be null or empty");

      PSDependencyDef depDef = getDependencyDef(type);
      PSDependencyHandler handler = PSDependencyHandler.getHandlerInstance(
            depDef, m_depMap);
      return handler.getDependency(tok, depId);
   }

   /**
    * Get all dependencies with the specified attributes.
    * 
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param flags One or more <code>TYPE_xxx</code> flags or'd together,
    * specifying the attributes to check.
    * 
    * @return An iterator over zero or more <code>PSDependency</code> objects
    * with the specified attributes, never <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public Iterator getDependencies(PSSecurityToken tok, int flags)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      List depList = new ArrayList();
      Iterator defs = m_depMap.getDefs();
      while (defs.hasNext())
      {
         PSDependencyDef def = (PSDependencyDef) defs.next();

         // skip non-id types if supports id types is specified
         if (((flags & TYPE_SUPPORTS_ID_TYPES) == TYPE_SUPPORTS_ID_TYPES)
               && !def.supportsIdTypes())
         {
            continue;
         }

         Iterator deps = getDependencies(tok, def.getObjectType());
         while (deps.hasNext())
         {
            PSDependency dep = (PSDependency) deps.next();

            // skip non-deployable if type deployable is specified
            if (((flags & TYPE_DEPLOYABLE) == TYPE_DEPLOYABLE)
                  && !(dep.canBeIncludedExcluded() || dep.isIncluded()))
            {
               continue;
            }

            depList.add(dep);
         }
      }

      return depList.iterator();
   }

   /**
    * Get a list of deployable element types.
    * 
    * @return An iterator over zero or more <code>PSDependencyDef</code>
    * objects, never <code>null</code>.
    */
   public Iterator getElementTypes()
   {
      List types = new ArrayList();
      Iterator defs = m_depMap.getDefs();
      while (defs.hasNext())
      {
         PSDependencyDef def = (PSDependencyDef) defs.next();
         if (def.isDeployableElement()
               && !def.getObjectType().equals(
                     IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM))
         {
            types.add(def);
         }
      }
      return types.iterator();
   }

   /**
    * Get a list of deployable object types.
    * 
    * @return An iterator over zero or more <code>PSDependencyDef</code>
    * objects, never <code>null</code>.
    */
   public Iterator getObjectTypes()
   {
      List types = new ArrayList();
      Iterator defs = m_depMap.getDefs();
      while (defs.hasNext())
      {
         PSDependencyDef def = (PSDependencyDef) defs.next();
         if (!def.isDeployableElement())
            types.add(def);
      }
      return types.iterator();
   }

   /**
    * Get a list of possible object types that could be used to id type the
    * specified id. This means the deployable object types that support id
    * mapping and that return an existing dependency for the specified id.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param id The dependency id to use, may not be <code>null</code> or
    * empty.
    * 
    * @return An iterator over zero or more <code>PSDependencyDef</code>
    * objects, never <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public Iterator getPossibleIdTypes(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      List types = new ArrayList();
      Iterator defs = m_depMap.getDefs();
      while (defs.hasNext())
      {
         PSDependencyDef def = (PSDependencyDef) defs.next();
         if (!def.isDeployableElement() && def.supportsIdMapping())
         {
            PSDependencyHandler handler = m_depMap.getDependencyHandler(def);
            if (handler != null)
            {
               boolean existsForType = false;
               if (def.supportsParentId())
               {
                  // get all parents, then check combination of the id w/each
                  // possible parent
                  PSDependencyDef parentDef = m_depMap
                        .getDependencyDef(handler.getParentType());
                  if (parentDef != null)
                  {
                     PSDependencyHandler parentHandler = m_depMap
                           .getDependencyHandler(parentDef);
                     Iterator parentDeps = parentHandler.getDependencies(tok);
                     while (parentDeps.hasNext() && !existsForType)
                     {
                        PSDependency parentDep = (PSDependency) parentDeps
                              .next();
                        existsForType = handler.doesDependencyExist(tok, id,
                              parentDep.getDependencyId());
                     }
                  }
               }
               else
               {
                  existsForType = handler.doesDependencyExist(tok, id);
               }

               if (existsForType)
                  types.add(def);
            }
         }
      }
      return types.iterator();
   }

   /**
    * Get a list of custom deployable element types.
    * 
    * @return An iterator over zero or more <code>PSDependencyDef</code>
    * objects, never <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public Iterator getCustomElementTypes() throws PSDeployException
   {
      List types = new ArrayList();
      PSDependencyDef custDef = m_depMap
            .getDependencyDef(IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM);
      if (custDef != null) // should never be null
      {
         PSDependencyHandler custHandler = PSDependencyHandler
               .getHandlerInstance(custDef, m_depMap);
         Iterator childTypes = custHandler.getChildTypes();
         while (childTypes.hasNext())
         {
            String type = (String) childTypes.next();
            PSDependencyDef childDef = m_depMap.getDependencyDef(type);
            if (childDef != null)
               types.add(childDef);
         }
      }
      return types.iterator();
   }

   /**
    * Get an instance of the id type handler for the supplied dependency type.
    * 
    * @param dep The dependency to get an ID Type handler for, may not be
    * <code>null</code>.
    * 
    * @return The handler, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if the dependency's type does not support
    * ID types.
    * @throws IllegalStateException if the dependency's type supports ID types,
    * but an ID type handler is not found for this type.
    * @throws PSDeployException if there are any other errors.
    */
   IPSIdTypeHandler getIdTypeHandler(PSDependency dep)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.supportsIdTypes())
         throw new IllegalArgumentException("dep must support id types: "
               + dep.getKey());

      PSDependencyDef def = getDependencyDef(dep.getObjectType());
      PSDependencyHandler handler = m_depMap.getDependencyHandler(def);

      if (!(handler instanceof IPSIdTypeHandler))
         throw new IllegalStateException("No ID Type handler found for type: "
               + dep.getObjectType());

      return (IPSIdTypeHandler) handler;
   }

   /**
    * Enables or disables the dependency cache. Dependency lists of each type
    * and child dependencies of each dependency will be lazily cached as they
    * are requested if enabled. Disabling will clear the cache and prevent
    * caching of new requests until it is enabled again. Enabling while it is
    * already enabled will have no effect on any currently cached lists. The
    * cache is disabled by default.
    * 
    * @param isEnabled If <code>true</code>, caching will be enabled, if
    * <code>false</code>, caching will be disabled.
    */
   public void setIsDependencyCacheEnabled(boolean isEnabled)
   {
   // todo: re-enable when MSM dependencies are supported, make thread safe
   // m_depCache.setIsCacheEnabled(isEnabled);
   }

   /**
    * Transforms all IDs in the supplied dependency and its child dependencies.
    * 
    * @param dep The dependency to transform, assumed not <code>null</code>.
    * @param idMap The map to use, assumed not <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void transformDeps(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {
      PSDependencyDef def = getDependencyDef(dep.getObjectType());
      PSDependencyHandler handler = m_depMap.getDependencyHandler(def);

      if (idMap != null
            && (dep.supportsIDMapping() || handler.delegatesIdMapping()))
      {
         PSIdMapping mapping = getIdMapping(dep, idMap);
         String targetId = handler.getTargetId(mapping, dep.getDependencyId());
         if (targetId == null)
         {
            // must have id set
            Object[] args = { mapping.getObjectType(), mapping.getSourceId(),
                  idMap.getSourceServer() };
            throw new PSDeployException(
                  IPSDeploymentErrors.MISSING_ID_MAPPING, args);
         }

         dep.setDependencyId(targetId);

         if (dep.supportsParentId())
            dep
                  .setParent(mapping.getTargetParentId(), mapping
                        .getParentType());
      }

      // handle child dependencies
      Iterator childDeps = dep.getDependencies();
      if (childDeps != null)
      {
         while (childDeps.hasNext())
            transformDeps((PSDependency) childDeps.next(), idMap);
      }
   }

   /**
    * Utility method to get a dependency def from a dependency type.
    * 
    * @param type The type of def to get. Assumed not <code>null</code> or
    * empty.
    * 
    * @return The def, never <code>null</code>.
    * 
    * @throws PSDeployException If the def cannot be located
    */
   public PSDependencyDef getDependencyDef(String type)
      throws PSDeployException
   {
      if (StringUtils.isBlank(type))
         throw new IllegalArgumentException("type may not be null");

      PSDependencyDef def = m_depMap.getDependencyDef(type);
      if (def == null)
      {
         throw new PSDeployException(
               IPSDeploymentErrors.DEPENDENCY_DEF_NOT_FOUND, type);
      }

      return def;
   }

   /**
    * Utility method to return the handler given the dependency
    * 
    * @param dep never <code>null</code>
    * @return the dependency handler based on the type never <code>null</code>
    * @throws PSDeployException
    */
   public PSDependencyHandler getDependencyHandler(PSDependency dep)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("Dependency may not be null");
      return getDependencyHandler(dep.getObjectType());
   }

   /**
    * Utility method to return the handler given the dependency type
    * 
    * @param depType dependency type never <code>null</code>
    * @return the dependency handler based on the type never <code>null</code>
    * @throws PSDeployException
    */
   public PSDependencyHandler getDependencyHandler(String depType)
      throws PSDeployException
   {
      if (StringUtils.isBlank(depType))
         throw new IllegalArgumentException("Dependency may not be null");
      PSDependencyDef def = getDependencyDef(depType);
      return m_depMap.getDependencyHandler(def);
   }

   /**
    * Get all user defined child dependencies for the supplied dependency. Any
    * saved user dependencies that no longer reference valid files are deleted
    * and not included in the returned list.
    * 
    * @param dep The dependency whose child user dependencies will be returned,
    * assumed not <code>null</code>.
    * 
    * @return An iterator over zero or more <code>PSUserDependency</code>
    * objects, never <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if there are any other errors.
    */
   private Iterator getUserDependencies(PSDependency dep)
      throws PSDeployException
   {
      List deps = new ArrayList();
      File depDir = new File(USER_DEP_DIR, dep.getKey());
      if (depDir.exists())
      {
         File[] depFiles = depDir.listFiles();
         for (int i = 0; i < depFiles.length; i++)
         {
            FileInputStream in = null;
            try
            {
               in = new FileInputStream(depFiles[i]);
               Document doc = PSXmlDocumentBuilder
                     .createXmlDocument(in, false);
               PSUserDependency userDep = new PSUserDependency(doc
                     .getDocumentElement());
               if (!userDep.getPath().exists())
                  depFiles[i].delete();
               else
               {
                  deps.add(userDep);
               }
            }
            catch (Exception e)
            {
               throw new PSDeployException(
                     IPSDeploymentErrors.UNEXPECTED_ERROR, e
                           .getLocalizedMessage());
            }
            finally
            {
               if (in != null)
                  try
                  {
                     in.close();
                  }
                  catch (IOException e)
                  {
                  }
            }
         }
      }

      return deps.iterator();
   }

   /**
    * For any new item, reserves a new Id for it in the system and updates the
    * id map. Recursively performs this action for each included child
    * dependency.
    * 
    * @param dep The dependency to reserve ids for, assumed not
    * <code>null</code>.
    * @param idMap The ID map for the source repository, assumed not
    * <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void reserveNewIds(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {

      if (dep.isIncluded() && dep.supportsIDMapping())
      {
         PSDependencyDef def = getDependencyDef(dep.getObjectType());
         PSDependencyHandler handler = m_depMap.getDependencyHandler(def);
         handler.reserveNewId(dep, idMap);
      }

      // now process children
      Iterator deps = dep.getDependencies();
      if (deps != null)
      {
         while (deps.hasNext())
         {
            PSDependency childDep = (PSDependency) deps.next();
            reserveNewIds(childDep, idMap);
         }
      }

   }

   /**
    * Ensures that there is at least one level of child dependencies below the
    * supplied dependency if it is included and adds them if required. If a
    * parent type (has a child that supports that type as a parent) and not
    * included, adds all local dependencies. Recursively calls this method on
    * all child dependencies.
    * <p>
    * Calls {@link PSDependency#setIsAutoDependency(boolean) 
    * setIsAutoDependency(true)} on all dependencies added.
    * 
    * @param tok The security token to use if objectstore access is required,
    * assumed not <code>null</code>.
    * @param dep The dependency to check, assumed not <code>null</code>.
    * @param checkElements <code>true</code> to recurse into deployable
    * elements to get local dependencies, <code>false</code> otherwise.
    * @param loaded The key of any dependency that has had its children
    * autoloaded is added to this set to avoid recursion.
    * @param treeCtx The tree context to use to determine inclusion of any
    * dependencies added, assumed not be <code>null</code>, must contain the
    * supplied <code>dep</code> and all of its child dependencies.
    * @param jobHandle The current job handle, used to check if the job has been
    * cancelled, assumed not <code>null</code>.
    * 
    * @throws PSDeployException if there are any other errors.
    */
   private void addMissingDependencies(PSSecurityToken tok, PSDependency dep,
         boolean checkElements, Set loaded, PSDependencyTreeContext treeCtx,
         IPSJobHandle jobHandle) throws PSDeployException
   {
      PSDependencyIdentifier depId = new PSDependencyIdentifier(dep,
            !checkElements);

      if (loaded.contains(depId) || jobHandle.isCancelled())
         return;

      // ensure dependencies for any included dependency. If an element,
      // get its local dependencies to be used on transforming ids on import
      // when a dependency contains its child ids.
      boolean isParentType = getParentTypes().containsValue(
            dep.getObjectType());

      PSDependencyContext depCtx = treeCtx.getDependencyCtx(dep);
      if (depCtx == null)
      {
         // a bug
         throw new IllegalArgumentException(
               "treeCtx must contain the supplied dependency");
      }

      Iterator deps = dep.getDependencies();

      /*
       * see if we have a local dep of an element and checkElements is false
       * (we've recursed into a deployable element within the tree) - if so,
       * keep recursing local deps
       */
      boolean recurseLocal = (dep.getDependencyType() == PSDependency.TYPE_LOCAL && !checkElements);

      if (deps == null
            && (depCtx.isIncluded() || isParentType
                  || (dep instanceof PSDeployableElement && checkElements) || recurseLocal)
            && !jobHandle.isCancelled())
      {
         // IMPORTANT: must call setIsAutoDependency(true) for any dependency
         // added.
         List depList = new ArrayList();
         deps = getDependencies(tok, dep);
         while (deps.hasNext())
         {
            PSDependency child = (PSDependency) deps.next();

            // only include the child if it passes the suppression filter
            if (!treeCtx.shouldSuppressDependency(child))
            {
               child.setIsAutoDependency(true);
               depList.add(child);
            }
         }
         loaded.add(depId);

         // add to the dep and to the ctx
         dep.setDependencies(depList.iterator());

         // if parent type and not included, or if recursing local, only add
         // local child deps
         if ((!dep.isIncluded() && isParentType) || recurseLocal)
         {
            dep.setDependencies(dep.getDependencies(PSDependency.TYPE_LOCAL));
         }

         deps = dep.getDependencies();
         depCtx.addChildDependencies(dep);

         // once we recurse into a deployable element, don't do it again on
         // that branch of the tree - only the first one we hit is needed.
         if (dep instanceof PSDeployableElement)
            checkElements = false;
      }

      if (deps != null && !jobHandle.isCancelled())
      {
         // see if this type has any "unrequired" child types and if so,
         // remove unincluded children of this type if there are no included
         // sub-dependencies of that child.
         PSDependencyDef def = getDependencyDef(dep.getObjectType());
         PSDependencyHandler handler = m_depMap.getDependencyHandler(def);
         List all = PSDeployComponentUtils.cloneList(deps);
         List included = new ArrayList();
         deps = all.iterator();
         while (deps.hasNext() && !jobHandle.isCancelled())
         {
            PSDependency child = (PSDependency) deps.next();
            if (child.isIncluded()
                  || handler.isRequiredChild(child.getObjectType())
                  || child.containsIncludedDependency())
            {
               included.add(child);
            }
            else
            {
               // will be removed, so remove it from the tree context
               treeCtx.removeDependency(child, false);
            }
         }

         if (all.size() != included.size())
         {
            dep.setDependencies(included.iterator());
            deps = dep.getDependencies();
         }
         else
            deps = all.iterator();

         // now recurse children
         while (deps.hasNext() && !jobHandle.isCancelled())
         {
            PSDependency child = (PSDependency) deps.next();
            addMissingDependencies(tok, child, checkElements, loaded, treeCtx,
                  jobHandle);
         }
      }
   }

   /**
    * Updates the job status for the supplied dependency via the supplied job
    * handle.
    * 
    * @param dep The dependency that is being processed, may not be
    * <code>null</code>.
    * @param jobHandle The handle to use to update the status, may not be
    * <code>null</code>.
    */
   public void updateJobStatus(PSDependency dep, IPSJobHandle jobHandle)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (jobHandle == null)
         throw new IllegalArgumentException("jobHandle may not be null");

      ResourceBundle bundle = PSDeploymentManager.getBundle();
      String depString = formatDependencyString(dep);
      String msg = MessageFormat.format(bundle.getString("processing"),
            new Object[] { depString });
      jobHandle.updateStatus(msg);
   }

   /**
    * Determines if the supplied package is missing any dependencies that should
    * be children and adds them, and if it includes any children that are no
    * longer dependencies and removes them.
    * 
    * @param tok The security token, assumed not <code>null</code>.
    * @param pkg The package to check, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the child list is modified,
    * <code>false</code> otherwise.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private boolean checkModifiedDependencies(PSSecurityToken tok,
         PSDependency pkg) throws PSDeployException
   {
      boolean isMod = false;

      Iterator deps = pkg.getDependencies();
      if (deps != null)
      {
         boolean exists = doesDependencyExist(tok, pkg);
         if (exists)
         {
            Iterator childDeps;
            if (pkg.getObjectType().equals(
                  IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM))
            {
               // its custom, so just check to be sure the child deps exist
               List newList = new ArrayList();
               while (deps.hasNext())
               {
                  PSDependency childDep = (PSDependency) deps.next();
                  PSDependency localDep = getActualDependency(tok, childDep);
                  if (localDep == null)
                     isMod = true;
                  else
                  {
                     // def may have changed
                     childDep.updateDependencyDefinition(localDep);
                     // display name and type may have changed.
                     childDep.setDisplayName(localDep.getDisplayName());
                     childDep.setDependencyType(localDep.getDependencyType());
                     newList.add(childDep);
                  }
               }

               // now set the new list on the pkg
               pkg.setDependencies(newList.iterator());

               childDeps = newList.iterator();
            }
            else
            {
               // build map of actual dependencies using key
               List newList = new ArrayList();
               List depList = PSDeployComponentUtils
                     .cloneList(getDependencies(tok, pkg));

               Map depMap = new HashMap();
               childDeps = depList.iterator();
               while (childDeps.hasNext())
               {
                  PSDependency childDep = (PSDependency) childDeps.next();
                  depMap.put(childDep.getKey(), childDep);
               }

               // walk listed dependencies and see if in the actual map
               Set newDepSet = new HashSet();
               while (deps.hasNext())
               {
                  PSDependency childDep = (PSDependency) deps.next();
                  PSDependency curDep = (PSDependency) depMap.get(childDep
                        .getKey());
                  if (curDep == null)
                  {
                     // its not still actually a dependency, leave it out
                     isMod = true;
                  }
                  else
                  {
                     // update def in case it has changed
                     childDep.updateDependencyDefinition(curDep);
                     // update display name and type in case it has changed
                     childDep.setDisplayName(curDep.getDisplayName());
                     childDep.setDependencyType(curDep.getDependencyType());
                     newList.add(childDep);
                     newDepSet.add(childDep.getKey());
                  }
               }

               // now walk actual deps and add any not already listed
               Iterator newDeps = depList.iterator();
               while (newDeps.hasNext())
               {
                  PSDependency childDep = (PSDependency) newDeps.next();
                  if (!newDepSet.contains(childDep.getKey()))
                  {
                     isMod = true;
                     newList.add(childDep);
                  }
               }

               // now set the new list on the pkg
               pkg.setDependencies(newList.iterator());

               childDeps = newList.iterator();
            }

            // now recurse the child deps
            while (childDeps.hasNext())
            {
               PSDependency childDep = (PSDependency) childDeps.next();
               isMod = checkModifiedDependencies(tok, childDep) || isMod;
            }
         }
      }

      return isMod;
   }

   /**
    * Determines if the supplied dependency exists.
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param dep The dependency to check, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the dependency exists, <code>false</code>
    * otherwise.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private boolean doesDependencyExist(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      PSDependencyDef depDef = getDependencyDef(dep.getObjectType());
      PSDependencyHandler depHandler = PSDependencyHandler.getHandlerInstance(
            depDef, m_depMap);
      boolean exists;
      if (depDef.supportsParentId())
         exists = depHandler.doesDependencyExist(tok, dep.getDependencyId(),
               dep.getParentId());
      else
         exists = depHandler.doesDependencyExist(tok, dep.getDependencyId());

      return exists;
   }

   /**
    * Gets the actual dependency from the local system using the supplied
    * dependency.
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param dep The dependency to get. The supplied dependency may or may not
    * exist on the local system. Assumed not <code>null</code>.
    * 
    * @return The actual dependency, may be <code>null</code> if it does not
    * exist on the local system.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private PSDependency getActualDependency(PSSecurityToken tok,
         PSDependency dep) throws PSDeployException
   {
      return getActualDependency(tok, dep, null);
   }

   /**
    * Gets the actual dependency from the local system using the supplied
    * dependency and optional id map.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param dep The dependency to get. The supplied dependency may or may not
    * exist on the local system. May not be <code>null</code>.
    * @param idMap id map to use to transform ids to the local system, may be
    * <code>null</code> if not required.
    * 
    * @return The actual dependency, may be <code>null</code> if it does not
    * exist on the local system.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public PSDependency getActualDependency(PSSecurityToken tok,
         PSDependency dep, PSIdMap idMap) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      PSDependencyDef depDef = getDependencyDef(dep.getObjectType());
      PSDependencyHandler handler = PSDependencyHandler.getHandlerInstance(
            depDef, m_depMap);

      PSDependency result = null;
      String newId = null;
      String newParentId = null;

      if (idMap != null
            && (dep.supportsIDMapping() || handler.delegatesIdMapping()))
      {
         PSIdMapping mapping = getIdMapping(dep, idMap);
         if (!mapping.isNewObject())
         {
            newId = handler.getTargetId(mapping, dep.getDependencyId());

            if (!dep.supportsParentId())
               result = handler.getDependency(tok, newId);
            else
            {
               newParentId = mapping.getTargetParentId();
               result = handler.getDependency(tok, newId, dep.getParentType(),
                     newParentId);
            }
         }
      }
      else
      {
         if (depDef.supportsParentId())
            result = handler.getDependency(tok, dep.getDependencyId(), dep
                  .getParentType(), dep.getParentId());
         else
            result = handler.getDependency(tok, dep.getDependencyId());
      }

      return result;
   }

   /**
    * Gets the id mapping for the specified dependency.
    * 
    * @param dep The dependency, assumed not <code>null</code>.
    * @param idMap The id map to use, assumed not <code>null</code>.
    * 
    * @return The mapping, never <code>null</code>.
    * 
    * @throws PSDeployException if the mapping cannot be located.
    */
   private PSIdMapping getIdMapping(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {
      PSDependencyDef def = getDependencyDef(dep.getObjectType());
      PSDependencyHandler handler = m_depMap.getDependencyHandler(def);

      return handler.getIdMapping(idMap, dep.getDependencyId(), dep
            .getObjectType(), dep.getParentId(), dep.getParentType());
   }

   /**
    * Gets all dependencies that have the supplied child as a dependency.
    * 
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param dep The dependency whose parents are to be returned, may not be
    * <code>null</code>.
    * 
    * @return iterator over zero or more <code>PSDependency</code> objects,
    * never <code>null</code>. Always returns an empty iterator if the
    * supplied dependency's type is
    * {@link IPSDeployConstants#DEP_OBJECT_TYPE_CUSTOM}.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public Iterator getParentDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      List parentDeps = new ArrayList();

      // not supported for custom
      if (dep.getObjectType()
            .equals(IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM))
         return PSIteratorUtils.emptyIterator();

      Iterator defs = m_depMap.getParentDependencyTypes(getDependencyDef(dep
            .getObjectType()));
      while (defs.hasNext())
      {
         PSDependencyDef def = (PSDependencyDef) defs.next();
         if (!def.canBeAncestor())
            continue;

         Iterator deps = getDependencies(tok, def.getObjectType());
         while (deps.hasNext())
         {
            PSDependency parent = (PSDependency) deps.next();
            if (isChild(tok, dep, parent))
               parentDeps.add(parent);
         }
      }

      return parentDeps.iterator();
   }

   /**
    * Determines if the supplied <code>child</code> is a dependency of the
    * supplied <code>parent</code>.
    * 
    * @param tok The security token to use if objectstore access is required,
    * assumed not <code>null</code>.
    * @param child The possible child dependency. Assumed not <code>null</code>.
    * @param parent The parent to check, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if <code>child</code> is a child dependency
    * of the supplied <code>parent</code>. If parent is of type
    * {@link PSDependency#TYPE_SYSTEM}, but child is not, then
    * <code>false</code> is returned without actually checking as an
    * optimization. Always returns <code>false</code> if the parent's type is
    * {@link IPSDeployConstants#DEP_OBJECT_TYPE_CUSTOM}
    * 
    * @throws PSDeployException if there are any errors.
    */
   private boolean isChild(PSSecurityToken tok, PSDependency child,
         PSDependency parent) throws PSDeployException
   {
      boolean isChild = false;

      // not supported for custom
      if (parent.getObjectType().equals(
            IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM))
      {
         return isChild;
      }

      // if child is not a system dependency, assume it's not a child if the
      // parent is a system dependency
      if (parent.getDependencyType() != PSDependency.TYPE_SYSTEM
            || child.getDependencyType() == PSDependency.TYPE_SYSTEM)
      {
         // may have loaded dependencies
         Iterator children = parent.getDependencies();
         if (children == null)
            children = getDependencies(tok, parent);

         while (children.hasNext() && !isChild)
         {
            PSDependency aChild = (PSDependency) children.next();
            isChild = child.getKey().equals(aChild.getKey());
         }
      }

      return isChild;
   }

   /**
    * Get the deployment type from a guid type, which is a list as multiple
    * deployment types may map to the same guid type.
    * 
    * @param type The guid type, may not be <code>null</code>.
    * 
    * @return The list of deployment types, never <code>null</code> or empty.
    */
   public List<String> getDeploymentType(PSTypeEnum type)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");

      if (m_guidToDepTypeMap == null)
         throw new IllegalStateException("type map not initialized");

      List<String> depTypes = m_guidToDepTypeMap.get(type);
      if (depTypes == null)
      {
         throw new RuntimeException(
               "No deployment type mapping found for guid type: "
                     + type.toString());
      }

      return depTypes;
   }

   /**
    * Get the guid type from an deployment type.
    * 
    * @param deploymentType The deployment type, may not be <code>null</code>
    * or empty, must be a valid deployment type.
    * 
    * @return The guid type, may be <code>null</code> if there is no
    * corresponding guid type.
    */
   public PSTypeEnum getGuidType(String deploymentType)
   {
      if (StringUtils.isBlank(deploymentType))
         throw new IllegalArgumentException("var may not be null or empty");

      if (m_depToGuidTypeMap == null)
         throw new IllegalStateException("type map not initialized");

      return m_depToGuidTypeMap.get(deploymentType);
   }

   /**
    * Creates all type mappings between deployment and enum types.
    */
   private void createTypeMappings()
   {
      // create the maps
      m_depToGuidTypeMap = new HashMap<String, PSTypeEnum>();
      m_guidToDepTypeMap = new HashMap<PSTypeEnum, List<String>>();

      // add all mappings
      Iterator defs = m_depMap.getDefs();
      while (defs.hasNext())
      {
         PSDependencyDef def = (PSDependencyDef) defs.next();
         String guidType = def.getGuidType();
         if (guidType != null)
         {
            addTypeMapping(PSTypeEnum.valueOf(guidType), def.getObjectType());
         }
      }
   }

   /**
    * Adds a type mapping between a guid and a dependency type.
    * 
    * @param guidType The guid type, assumed not <code>null</code>.
    * @param depType The dependency type, assumed not <code>null</code> or
    * empty.
    */
   private void addTypeMapping(PSTypeEnum guidType, String depType)
   {
      m_depToGuidTypeMap.put(depType, guidType);
      // could be multiple dep types for same guid type
      List<String> typeList = m_guidToDepTypeMap.get(guidType);
      if (typeList == null)
      {
         typeList = new ArrayList<String>();
         m_guidToDepTypeMap.put(guidType, typeList);
      }
      typeList.add(depType);
   }
   
   /**
    * Flag to indicate a dependency supports Id types. If included, then
    * identifies those dependencies for which
    * {@link PSDependency#supportsIdTypes()} returns <code>true</code>.
    */
   public static final int TYPE_SUPPORTS_ID_TYPES = 0x0001;

   /**
    * Flag to indicate a dependency type that is deployable. If included, then
    * identifies those dependencies can be included in an archive. This includes
    * local and shared types only.
    */
   public static final int TYPE_DEPLOYABLE = 0x0002;

   /**
    * Directory below the objectstore directory containing all user
    * dependencies.
    */
   private static final File USER_DEP_DIR = new File(PSServer.getRxDir()
         .getAbsolutePath()
         + "/" + PSDeploymentHandler.OBJECTSTORE_DIR, "UserDependencies");

   /**
    * Singleton instance of this class. Intialized when first instance of the
    * class is constructed, never <code>null</code> or modified after that.
    */
   private static PSDependencyManager ms_instance = null;

   /**
    * Constant for the name of the configure file used to construct the
    * {@link PSPackageConfiguration} this mananger uses.
    */
   private static final String CONFIG_FILE_NAME = "sys_PackageConfiguration.xml";

   /**
    * The dependency map used to get dependency type defs, child deps, parent
    * deps, etc. Intialzied during construction, never <code>null</code> or
    * modified after that.
    */
   private PSDependencyMap m_depMap;

   /**
    * Map of parent and child types. The key is the child type and the value is
    * the parent type, both as non-<code>null</code>, non-empty
    * <code>String</code> objects, <code>null</code> until first call to
    * {@link #getParentTypes()}, never <code>null</code> or modified after
    * that, may be empty.
    */
   private Map m_parentTypeMap = null;

   /**
    * The deployment order sequence, as a list of object type of the packaged
    * elements. It is initialized by constructor, never <code>null</code>
    * after that.
    */
   private List<String> m_deployOrder;

   /**
    * List of guid types that are ignored for uninstall.
    */
   private List<String> m_uninstallIgnoreTypes;

   /**
    * The dependency cache, used to cache lists of dependencies of each type, as
    * well as child dependencies of a specific dependency. Initialized during
    * construction, never <code>null</code>, initially disabled.
    */
   private PSDependencyCache m_depCache = new PSDependencyCache();

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static Logger ms_log = Logger
         .getLogger("com.percussion.deployer.server.PSDependencyManager");

   /**
    * The directory of the configure/map file. It is primarily used and set by
    * unit test. Default to <code>null</code>.
    */
   private static String ms_configDir = null;

   /**
    * Map of dependency type to type enum, <code>null</code> until first call
    * to {@link #createTypeMappings()}, never <code>null</code> or modified
    * after that.
    */
   private Map<String, PSTypeEnum> m_depToGuidTypeMap = null;

   /**
    * Map of type enum to dependency type, <code>null</code> until first call
    * to {@link #createTypeMappings()}, never <code>null</code> or modified
    * after that.
    */
   private Map<PSTypeEnum, List<String>> m_guidToDepTypeMap = null;
   
   /**
    * Object to handle the caching of dependencies. Cache is disabled by
    * default.
    */
   private class PSDependencyCache
   {
      /**
       * Get the cached dependency list for the supplied type. The returned list
       * contains copies of the cached dependencies. Without caching there may
       * normally be different instances representing the same dependency, so
       * this produces the same behavior. Sometimes the same dependency will be
       * local in one spot in the tree, and shared in another. Also, in some
       * places it may have child dependencies loaded, and not in others, and
       * this will make a difference during validation.
       * 
       * @param objectType The type of dependency, assumed not <code>null</code>
       * or empty.
       * 
       * @return An iterator containing a clone of each
       * <code>PSDependency</code> object in the cached list,
       * <code>null</code> if caching is disabled or if no list has yet been
       * cached for the specified type.
       */
      public Iterator getDependencies(String objectType)
      {
         Iterator deps = null;
         if (m_isEnabled)
         {
            List depList = (List) m_dependenciesMap.get(objectType);
            if (depList != null)
               deps = cloneDepList(depList).iterator();
         }

         return deps;
      }

      /**
       * Get the cached child dependency list for the supplied dependency.
       * 
       * @param dep The dependency for which the list is returned, assumed not
       * <code>null</code>.
       * 
       * @return An iterator containing a clone of each
       * <code>PSDependency</code> object in the cached list,
       * <code>null</code> if caching is disabled or if no list has yet been
       * cached for the supplied dependency.
       */
      public Iterator getChildDependencies(PSDependency dep)
      {
         Iterator deps = null;
         if (m_isEnabled)
         {
            List depList = (List) m_childDependenciesMap.get(dep.getKey());
            if (depList != null)
               deps = cloneDepList(depList).iterator();
         }

         return deps;
      }

      /**
       * Sets the supplied list of dependencies in the cache for the supplied
       * type. If the cache is disabled, the same list is returned with no side
       * effect.
       * 
       * @param objectType The type for which the list is to be cached, assumed
       * not <code>null</code>.
       * @param deps An iterator over zero or more <code>PSDependency</code>
       * objects, assumed not <code>null</code>.
       * 
       * @return An iterator over the same list supplied by the
       * <code>deps</code> parameter, never <code>null</code>, may be the
       * same iterator passed in if caching is not enabled.
       */
      public Iterator setDependencies(String objectType, Iterator deps)
      {
         if (m_isEnabled)
         {
            List depList = PSDeployComponentUtils.cloneList(deps);
            m_dependenciesMap.put(objectType, depList);
            deps = depList.iterator();
         }

         return deps;
      }

      /**
       * Sets the supplied list of dependencies in the cache for the supplied
       * type. If the cache is disabled, the same list is returned with no side
       * effect.
       * 
       * @param dep The dependency for which the list is to be cached, assumed
       * not <code>null</code>.
       * @param deps An iterator over zero or more <code>PSDependency</code>
       * objects, assumed not <code>null</code>.
       * 
       * @return An iterator over the same list supplied by the
       * <code>deps</code> parameter, never <code>null</code>, may be the
       * same iterator passed in if caching is not enabled.
       */
      public Iterator setChildDependencies(PSDependency dep, Iterator deps)
      {
         if (m_isEnabled)
         {
            List depList = PSDeployComponentUtils.cloneList(deps);
            m_childDependenciesMap.put(dep.getKey(), depList);
            deps = depList.iterator();
         }

         return deps;
      }

      /**
       * Enables or disables the cache.
       * 
       * @param isEnabled If <code>true</code>, the cache is enabled,
       * otherwise the cache is disabled and any cached lists are cleared.
       */
      public void setIsCacheEnabled(boolean isEnabled)
      {
         m_isEnabled = isEnabled;
         if (!m_isEnabled)
         {
            m_dependenciesMap.clear();
            m_childDependenciesMap.clear();
         }
      }

      /**
       * Returns a new list, containing a clone of each
       * <code>PSDependency</code> object in the supplied list, without any
       * child dependencies or ancestors.
       * 
       * @param depList A list of zero or more <code>PSDependency</code>
       * objects, assumed not <code>null</code>.
       * 
       * @return The cloned list, never <code>null</code>.
       */
      private List cloneDepList(List depList)
      {
         List copy = new ArrayList();
         Iterator deps = depList.iterator();
         while (deps.hasNext())
         {
            PSDependency dep = (PSDependency) deps.next();
            dep = (PSDependency) dep.clone();
            dep.setDependencies(null);
            dep.setAncestors(null);
            copy.add(dep);
         }

         return copy;
      }

      /**
       * Determines if the cache is enabled. Initially <code>false</code>, is
       * modified by calls to {@link #setIsCacheEnabled(boolean)}.
       */
      private boolean m_isEnabled = false;

      /**
       * Map of cached dependencies by type, where the key is the objectType as
       * a <code>String</code> and the value is a <code>List</code> of
       * <code>PSDependency</code> objects, never <code>null</code>, may be
       * empty.
       */
      private Map m_dependenciesMap = new HashMap();

      /**
       * Map of cached child dependencies by parent, where the key is the parent
       * <code>PSDependency</code> and the value is a <code>List</code> of
       * child <code>PSDependency</code> objects, never <code>null</code>,
       * may be empty.
       */
      private Map m_childDependenciesMap = new HashMap();
   }

   /**
    * Class to identify a dependency by it's key, type, and location within a
    * dependency tree.
    */
   private class PSDependencyIdentifier
   {
      /**
       * Construct an identifier
       * 
       * @param dep The dep for which this identifier is constructed, assumed
       * not <code>null</code>
       * @param isFromSubPackage <code>true</code> if the dependency is a
       * child of an element within another element's package,
       * <code>false</code> if the dependency is actually part of the package.
       */
      public PSDependencyIdentifier(PSDependency dep, boolean isFromSubPackage)
      {
         m_dep = dep;
         m_isFromSubPackage = isFromSubPackage;
      }

      /**
       * Determine if this object is equal to another.
       * 
       * @return <code>true</code> if the supplied object is a
       * {@link PSDependencyIdentifier} for a dependency with a matching key,
       * type, and matching <code>fromSubPackage</code> setting (see ctor for
       * more info), <code>false</code> otherwise.
       */
      @Override
      public boolean equals(Object obj)
      {
         boolean isEqual = true;

         if (!(obj instanceof PSDependencyIdentifier))
         {
            isEqual = false;
         }
         else
         {
            PSDependencyIdentifier depId = (PSDependencyIdentifier) obj;
            if (!depId.m_dep.getKey().equals(m_dep.getKey()))
               isEqual = false;
            else if (depId.m_dep.getDependencyType() != m_dep
                  .getDependencyType())
            {
               isEqual = false;
            }
            else if (depId.m_isFromSubPackage ^ m_isFromSubPackage)
            {
               isEqual = false;
            }
         }

         return isEqual;
      }

      /**
       * See {@link Object#hashCode()} for full description. Overriden to obey
       * contract as {@link #equals(Object)} has been overriden.
       */
      @Override
      public int hashCode()
      {
         return m_dep.getKey().hashCode() + m_dep.getDependencyType()
               + Boolean.valueOf(m_isFromSubPackage).hashCode();
      }

      /**
       * The dependency for which this identifier is constructed.
       */
      private PSDependency m_dep;

      /**
       * Determines if the dependency for which this identifier is constructed
       * is found in a sub-package of another package.
       */
      private boolean m_isFromSubPackage = false;
   }
}
