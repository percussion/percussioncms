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
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSEdition;
import com.percussion.services.publisher.data.PSEditionContentList;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Class to handle packaging and deploying a edition definition.
 */
public class PSEditionDefDependencyHandler extends PSDataObjectDependencyHandler
   implements IPSServiceDependencyHandler
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
   public PSEditionDefDependencyHandler(PSDependencyDef def,
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

      Set<PSDependency> childDeps = new HashSet<PSDependency>();
      
      String edId = dep.getDependencyId();
      IPSEdition edition = findEditionByDependencyID(edId);
      if (edition != null)
      {
         IPSGuid edtnGuid = edition.getGUID();
         PSDependencyHandler clHandler = getDependencyHandler(
               PSContentListDefDependencyHandler.DEPENDENCY_TYPE);
         PSDependencyHandler ctxtHandler = getDependencyHandler(
               PSContextDefDependencyHandler.DEPENDENCY_TYPE);
         PSDependencyHandler atHandler = getDependencyHandler(
               PSAuthTypeDependencyHandler.DEPENDENCY_TYPE); 
         
         List<IPSEditionContentList> ecls =
            m_pubSvc.loadEditionContentLists(edtnGuid);
         for (IPSEditionContentList ecl : ecls)
         {
            // add ContentList def child dependency
            long clId = ecl.getContentListId().longValue();
            PSDependency clDep = 
               clHandler.getDependency(tok, String.valueOf(clId));
            if (clDep != null)
            {
               if (clDep.getDependencyType() == PSDependency.TYPE_SHARED)
                  clDep.setIsAssociation(false);
               childDeps.add(clDep);               
            }
            
            // add delivery Context def child dependency
            IPSGuid dcId = ecl.getDeliveryContextId();
            PSDependency dcDep =
               ctxtHandler.getDependency(tok, String.valueOf(dcId.longValue()));
            if (dcDep != null)
            {
               if (dcDep.getDependencyType() == PSDependency.TYPE_SHARED)
                  dcDep.setIsAssociation(false);
               childDeps.add(dcDep);
            }
            
            // add assembly Context def child dependency
            IPSGuid acId = ecl.getAssemblyContextId();
            if (acId != null)
            {
               PSDependency acDep =
                  ctxtHandler.getDependency(tok, 
                        String.valueOf(acId.longValue()));
               if (acDep != null)
               {
                  if (acDep.getDependencyType() == PSDependency.TYPE_SHARED)
                     acDep.setIsAssociation(false);
                  childDeps.add(acDep);       
               }
            }
            
            // add authtype dependency
            Integer atId = ecl.getAuthtype();
            PSDependency atDep =
               atHandler.getDependency(tok, String.valueOf(atId));
            if (atDep != null)
            {
               if (atDep.getDependencyType() == PSDependency.TYPE_SHARED)
                  atDep.setIsAssociation(false);               
               childDeps.add(atDep);
            }
         }
         
         // add Edition Task def child dependencies
         PSDependencyHandler tHandler = getDependencyHandler(
               PSEditionTaskDefDependencyHandler.DEPENDENCY_TYPE);
         List<IPSEditionTaskDef> tasks = 
            m_pubSvc.loadEditionTasks(edtnGuid);
         for (IPSEditionTaskDef task : tasks)
         {
            IPSGuid tId = task.getTaskId();
            PSDependency tDep =
               tHandler.getDependency(tok, String.valueOf(tId.longValue()));
            if (tDep != null)
            {
               tDep.setDependencyType(PSDependency.TYPE_LOCAL);
               childDeps.add(tDep);
            }
         }
      }
      
      return childDeps.iterator();
    }

   // see base class
   @Override
   @SuppressWarnings("unchecked")
   public Iterator getDependencies(PSSecurityToken tok)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      List<PSDependency> deps = new ArrayList<PSDependency>();

      List<IPSEdition> editions = m_pubSvc.findAllEditions("");
      for (IPSEdition edition : editions)
      {
         PSDependency dep = createDependency(m_def, 
               String.valueOf(((PSEdition) edition).getId()),
                  edition.getName());
         deps.add(dep);            
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

      PSDependency edDep = null;
      
      IPSEdition edition = findEditionByDependencyID(id);
      if (edition != null)
         edDep = createDependency(m_def, id, edition.getName());
        
      return edDep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>ContentList</li>
    * <li>ContextDef</li>
    * <li>AuthType</li>
    * <li>EditionTaskDef</li>
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

      String edId = dep.getDependencyId();
      IPSEdition edition = findEditionByDependencyID(edId);
      if (edition == null)
      {
         Object[] args = {dep.getDependencyId(), dep.getObjectTypeName(),
               dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND,
               args);
      }
      
      files.add(getDepFileFromEdition(edition));

      // get the data for both ContentList & Context child dependencies data
      // from the relationship with its children
      List<IPSEditionContentList> ecls = m_pubSvc.loadEditionContentLists(
            edition.getGUID());
      String str = "";
      try
      {
         for (IPSEditionContentList ecl: ecls)
         {
            str = ((PSEditionContentList) ecl).toXML();

            files.add(new PSDependencyFile(
                  PSDependencyFile.TYPE_SERVICEGENERATED_XML,
                  createXmlFile(str)));
         }
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e,
               "Unable to generate an edition content list dependency file "
               + "for Edition: " + edition.getName());
      }
          
      return files.iterator();
   }

   // see base class
   @Override
   @SuppressWarnings("unchecked")
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
               "error occurred while installing edition: " +
               e.getLocalizedMessage());
      }
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
      
      IPSEdition edition = null;
      PSIdMapping edtnMapping = getIdMapping(ctx, dep); 
      if (edtnMapping != null)
         edition = findEditionByDependencyID(edtnMapping.getTargetId());
      else
         edition = findEditionByDependencyID(dep.getDependencyId());
     
      boolean isNew = (edition == null) ? true : false;
      Integer ver = null;
      if (!isNew)
      {
         edition = m_pubSvc.loadEditionModifiable(edition.getGUID());
         ver = ((PSEdition) edition).getVersion();
         ((PSEdition) edition).setVersion(null);
      }
      else
      {
         edition = m_pubSvc.createEdition();
      }
      
      String packagedContent = PSDependencyUtils.getFileContentAsString(
            archive, file);
            
      try
      {
         PSEdition edtn = (PSEdition) edition;
         edtn.setId(-1);
         edtn.fromXML(packagedContent);
         transferIdsForEdition(edtn, dep, ctx);
         edtn.setVersion(null);
         
         if (!isNew)
            edtn.setVersion(ver);
                
         m_pubSvc.saveEdition(edtn);
         
         // load edition content lists from target server
         IPSGuid edtnGuid = edition.getGUID();
         List<IPSEditionContentList> targetEcls = 
            m_pubSvc.loadEditionContentLists(edtnGuid);
         
         // delete edition content lists from target server
         for (IPSEditionContentList ecl : targetEcls)
            m_pubSvc.deleteEditionContentList(ecl);
         
         while (files.hasNext())
         {
            file = (PSDependencyFile) files.next();
            String eclFileContent = PSDependencyUtils.getFileContentAsString(
                  archive, file);
            PSEditionContentList ecl = 
               (PSEditionContentList) m_pubSvc.createEditionContentList();
            long eclId = ecl.getEditionContentListPK().getEditionclistid();
            
            ecl.fromXML(eclFileContent);
            ecl.getEditionContentListPK().
            setEditionclistid(eclId);
            
            transferIdsForEditionContentList(ecl, dep, ctx);
            m_pubSvc.saveEditionContentList(ecl);
         }
         
         //add transaction log
         addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.EDITION,
               isNew);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e,
               "error occurred while installing edition: " + 
               e.getLocalizedMessage());
      }
   }  
   
   /**
    * Transfer IDs for a given edition, from the source server to the current /
    * target server.
    *
    * @param edition The data from the source server, assumed not
    * <code>null</code>
    * @param edDep The edition dependency object, assumed not <code>null</code>
    * @param ctx The import context to aid in the installation, assumed not
    * <code>null</code>.
    *
    * @throws PSDeployException if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private void transferIdsForEdition(PSEdition edition, PSDependency edDep,
         PSImportCtx ctx)
      throws PSDeployException
   {
      // get id map and xform ids (flip new entries to not-new)
      PSIdMapping edMapping = getIdMapping(ctx, edDep);

      // xform the ids for EDITION and DEST_SITE
      if (edMapping != null)
         edition.setId(Long.valueOf(edMapping.getTargetId()));
            
      // this is a nullable value
      IPSGuid siteId = edition.getSiteId();
      if (siteId != null)
      {
         PSIdMapping siteMapping = getIdMapping(ctx, 
               String.valueOf(siteId.longValue()),
               PSSiteDefDependencyHandler.DEPENDENCY_TYPE);

         if (siteMapping != null)
         {
            edition.setSiteId(PSGuidUtils.makeGuid(siteMapping.getTargetId(),
                  PSTypeEnum.SITE));
         }
      }
   }
   
   /**
    * Transfer IDs for the children of an Edition dependency, from the
    * source server to the current / target server.
    *
    * @param ecl The edition content list from the source server, assumed not
    * <code>null</code>.
    * @param edDep The edition dependency object, assumed not <code>null</code>.
    * @param ctx The import context to aid in the installation, assumed not
    * <code>null</code>.
    *
    * @throws PSDeployException if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private void transferIdsForEditionContentList(PSEditionContentList ecl,
         PSDependency edDep, PSImportCtx ctx)
      throws PSDeployException
   {
      // xform the ids for EDITION, CONTENTLIST, DELIVERY and ASSEMBLY CONTEXTS
      PSIdMapping edMapping = getIdMapping(ctx,
            String.valueOf(ecl.getEditionId().longValue()),
            DEPENDENCY_TYPE);
      if (edMapping != null)
      {
         ecl.getEditionContentListPK().setEditionid(Integer.valueOf(
               edMapping.getTargetId()).longValue());
      }
      
      PSIdMapping clMapping = getIdMapping(ctx, 
            String.valueOf(ecl.getContentListId().longValue()),
            PSContentListDependencyHandler.DEPENDENCY_TYPE);
      if (clMapping != null)
      {
         ecl.getEditionContentListPK().setContentlistid(Integer.valueOf(
               clMapping.getTargetId()).longValue());
      }

      IPSGuid dcId = ecl.getDeliveryContextId();
      PSIdMapping dcMapping = getIdMapping(ctx, 
            String.valueOf(dcId.longValue()),
            PSContextDefDependencyHandler.DEPENDENCY_TYPE);

      if (dcMapping != null)
      {
         ecl.setDeliveryContextId(PSGuidUtils.makeGuid(
               dcMapping.getTargetId(), PSTypeEnum.CONTEXT));
      }

      // this is a nullable value
      IPSGuid acId = ecl.getAssemblyContextId();
      if (acId != null)
      {
         PSIdMapping acMapping = getIdMapping(ctx, 
               String.valueOf(acId.longValue()),
               PSContextDefDependencyHandler.DEPENDENCY_TYPE);

         if (acMapping != null)
         {
            ecl.setAssemblyContextId(PSGuidUtils.makeGuid(
                  acMapping.getTargetId(), PSTypeEnum.CONTEXT));
         }
      }
   }
   
   /**
    * Utility method to find the Edition by a given dependency id(as a string).
    * @param depId the id, assumed not <code>null</code>.
    * @return <code>null</code> if Edition is not found.
    */
   private IPSEdition findEditionByDependencyID(String depId)
   {
      if (Integer.parseInt(depId) <= 0)
         return null;

      IPSEdition edtn = null;

      List<IPSEdition> editions = m_pubSvc.findAllEditions("");
      for (IPSEdition edition : editions)
      {
         if (String.valueOf(edition.getGUID().longValue()).equals(depId))
         {
            edtn = edition;
            break;
         }
      }
   
      return edtn;
   }
   
   /**
    * Creates a dependency file from a given dependency data object.
    * @param edition the edition never <code>null</code>
    * @return The dependency file object, it will never be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected PSDependencyFile getDepFileFromEdition(IPSEdition edition)
      throws PSDeployException
   {
      if (edition == null)
         throw new IllegalArgumentException("depData may not be null");
      String str = "";
      try
      {
         str = ((PSEdition) edition).toXML();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for Edition:"
                     + edition.getName());
      }
      
      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(str));
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "EditionDef";

   /**
    * Get the publisher service
    */
   private static IPSPublisherService m_pubSvc = 
       PSPublisherServiceLocator.getPublisherService();
   
   /**
    * The schema for EDITIONCLIST_TABLE, initialized by constructor, will never
    * be <code>null</code> or modified after that.
    */
   PSJdbcTableSchema m_editionContentListSchema;

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();

   static
   {
      ms_childTypes.add(PSContentListDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSContextDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSAuthTypeDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSEditionTaskDefDependencyHandler.DEPENDENCY_TYPE);
   }
}
