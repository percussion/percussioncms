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
package com.percussion.deployer.server.dependencies;


import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.server.IPSServiceDependencyHandler;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.deployer.services.IPSDeployService;
import com.percussion.deployer.services.PSDeployServiceException;
import com.percussion.deployer.services.PSDeployServiceLocator;
import com.percussion.extension.PSExtensionRef;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSEditionTaskDef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class to handle packaging and deploying an edition task.
 */
public class PSEditionTaskDefDependencyHandler
   extends PSDataObjectDependencyHandler implements IPSServiceDependencyHandler
{

   /**
    * Construct a dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSEditionTaskDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   @Override
   @SuppressWarnings("unchecked")
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      List<PSDependency> childDeps = new ArrayList<PSDependency>();

      // get the extension dependency
      List<PSDependency> extDeps = getExtensionDependencies(tok, dep);
      childDeps.addAll(extDeps);      

      return childDeps.iterator();
   }

   /**
    * Package the extension dependencies
    * @param tok the security token never <code>null</code>
    * @param dep the dependency for which child dependencies are returned
    * @return the extension child dependencies
    * @throws PSDeployException
    */
   private List<PSDependency> getExtensionDependencies(
         PSSecurityToken tok, PSDependency dep) throws PSDeployException
   {
      List<PSDependency> childDeps = new ArrayList<PSDependency>();
      List<String> exts = new ArrayList<String>();
      
      IPSEditionTaskDef task = findEditionTask(dep.getDependencyId());
      if (task != null)
         exts.add(task.getExtensionName());
      
      Iterator<String> it = exts.iterator();
      PSDependencyHandler eHandler = 
         getDependencyHandler(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      PSDependency tmpDep = null;
      while (it.hasNext())
      {
         String ext = it.next();
         tmpDep = eHandler.getDependency(tok, ext);
         if (tmpDep != null)
         {
            if (tmpDep.getDependencyType() == PSDependency.TYPE_SHARED)
            {
               tmpDep.setIsAssociation(false);
            }
            childDeps.add(tmpDep);            
         }
      }
            
      return childDeps;
   }
   
   // see base class
   @Override
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      List<PSDependency> deps = new ArrayList<PSDependency>();
      
      Set<IPSEditionTaskDef> tasks = findAllEditionTasks();
      for (IPSEditionTaskDef task : tasks)
      {
         deps.add(
               createDependency(
                     m_def,
                     String.valueOf(task.getTaskId().longValue()),
                     (new PSExtensionRef(task.getExtensionName())).
                     getExtensionName()));
      }
      
      return deps.iterator();
   }

   // see base class
   @Override
   public PSDependency getDependency(PSSecurityToken tok, String id)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSDependency tDep = null;
      
      IPSEditionTaskDef task = findEditionTask(id);
      if (task != null)
      {
         tDep = createDependency(m_def, id, 
               (new PSExtensionRef(task.getExtensionName()).
                     getExtensionName()));
      }

      return tDep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>ExitDef</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   @Override
   @SuppressWarnings("unchecked")
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   @Override
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   @Override
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSDependencyUtils.reserveNewId(dep, idMap, getType());
   }

   // see base class
   @Override
   @SuppressWarnings("unchecked")
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();

      // package the dep itself
      IPSEditionTaskDef task = findEditionTask(dep.getDependencyId());
      if (task == null)
      {
         Object[] args = {dep.getDependencyId(), dep.getObjectTypeName(),
               dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND,
               args);
      }
      
      files.add(getDepFileFromEditionTask(task));

      return files.iterator();
   }

   // see base class
   @Override
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

      IPSDeployService depSvc = PSDeployServiceLocator.getDeployService();
      try
      {
         depSvc.installDependencyFiles(tok, archive, dep, ctx, this);
      }
      catch (PSDeployServiceException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "error occurred while installing edition task: " +
               e.getLocalizedMessage());
      } 
   }
      
   /**
    * Retrieves all edition tasks.
    * 
    * @return edition tasks as a set, never <code>null</code>, may be
    * empty.
    */
   private Set<IPSEditionTaskDef> findAllEditionTasks()
   {
      Set<IPSEditionTaskDef> tasks = new HashSet<IPSEditionTaskDef>();
      
      List<IPSEdition> editions = m_pubSvc.findAllEditions("");
      for (IPSEdition edition : editions)
         tasks.addAll(m_pubSvc.loadEditionTasks(edition.getGUID()));
              
      return tasks;
   }

   /**
    * Retrieves an edition task for the given id.
    * 
    * @param id the edition task id, assumed not <code>null</code>.
    * 
    * @return edition task corresponding to the id, <code>null</code> if a
    * match was not found.
    */
   private IPSEditionTaskDef findEditionTask(String id)
   {
      return m_pubSvc.findEditionTaskById(PSGuidUtils.makeGuid(id,
            PSTypeEnum.EDITION_TASK_DEF));
   }
   
   /**
    * Creates a dependency file from a given dependency data object.
    * @param task the edition task, assumed not <code>null</code>.
    * 
    * @return The dependency file object, it will never be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   private PSDependencyFile getDepFileFromEditionTask(
         IPSEditionTaskDef task)
      throws PSDeployException
   {
      String str = "";
      try
      {
         str = ((PSEditionTaskDef) task).toXML();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for Edition Task:"
                     + task.getExtensionName());
      }
      
      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(str));
   }
   
   /**
    * Transfer IDs for the Edition Task dependency, from the source server to
    * the current / target server.
    *
    * @param task The edition task to be modified, assumed not
    * <code>null</code>.
    * @param taskDep The edition task dependency object,
    * assumed not <code>null</code>.
    * @param ctx The import context to aid in the installation, assumed not
    * <code>null</code>.
    *
    * @throws PSDeployException if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private void transferIdsForEditionTask(PSEditionTaskDef task,
         PSDependency taskDep, PSImportCtx ctx)
      throws PSDeployException
   {
      PSIdMapping taskMapping = getIdMapping(ctx, taskDep);
      if (taskMapping != null)
      {
         task.setTaskId(PSGuidUtils.makeGuid(taskMapping.getTargetId(),
               PSTypeEnum.EDITION_TASK_DEF));
      }
      
      PSIdMapping edMapping = getIdMapping(ctx, 
            String.valueOf(task.getEditionId().longValue()),
            PSEditionDefDependencyHandler.DEPENDENCY_TYPE);
      if (edMapping != null)
      {
         task.setEditionId(PSGuidUtils.makeGuid(edMapping.getTargetId(),
               PSTypeEnum.EDITION));
      }
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "EditionTaskDef";

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();

   /**
    * Get the publisher service.
    */
   private static IPSPublisherService m_pubSvc = 
      PSPublisherServiceLocator.getPublisherService();
   
   static
   {
      ms_childTypes.add(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
   }

   /**
    * See {@link IPSServiceDependencyHandler#doInstallDependencyFiles(
    * PSSecurityToken, PSArchiveHandler, PSDependency, PSImportCtx)} for
    * details.
    */
   @SuppressWarnings("unchecked")
   public void doInstallDependencyFiles(PSSecurityToken tok,
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

      // retrieve the data from the archive
      Iterator files = getDependecyDataFiles(archive, dep);
      PSDependencyFile file = (PSDependencyFile) files.next();
      
      IPSEditionTaskDef task = null;
      PSIdMapping taskMapping = getIdMapping(ctx, dep); 
      if (taskMapping != null)
         task = findEditionTask(taskMapping.getTargetId());
      else
         task = findEditionTask(dep.getDependencyId());
     
      IPSPublisherService pubSvc = 
         PSPublisherServiceLocator.getPublisherService();
      
      boolean isNew = (task == null) ? true : false;
      Integer ver = null;
      if (!isNew)
      {
         ver = ((PSEditionTaskDef) task).getVersion();
         ((PSEditionTaskDef) task).setVersion(null);
      }
      else
      {
         task = pubSvc.createEditionTask();
      }
      
      String packagedTaskContent = PSDependencyUtils.getFileContentAsString(
            archive, file);
      
      try
      {
         PSEditionTaskDef etd = (PSEditionTaskDef) task;
         etd.fromXML(packagedTaskContent);
         transferIdsForEditionTask(etd, dep, ctx);
         etd.setVersion(null);
         
         if (!isNew)
            etd.setVersion(ver);
                  
         pubSvc.saveEditionTask(etd);
         
         //add transaction log
         addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.EDITION_TASK_DEF,
               isNew);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e,
               "error occurred while installing edition task: " + 
               e.getLocalizedMessage());
      }
   }  

}
