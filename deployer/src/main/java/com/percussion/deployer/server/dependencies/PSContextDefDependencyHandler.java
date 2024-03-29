/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.deployer.server.dependencies;


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
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.sitemgr.data.PSPublishingContext;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to handle packaging and deploying a context definition.
 */
public class PSContextDefDependencyHandler extends PSDataObjectDependencyHandler
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
   public PSContextDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   @Override
   public Iterator<PSDependency> getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      List<PSDependency> childDeps = getLocationSchemeDependencies(tok, dep);

      return childDeps.iterator();         
    }

   // see base class
   @Override
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok) throws PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      List<PSDependency> deps = new ArrayList<>();
      
      List<IPSPublishingContext> contexts = m_siteMgr.findAllContexts();
      for (IPSPublishingContext context : contexts)
      {
         IPSGuid cId = context.getGUID();
         deps.add(createDependency(m_def, String.valueOf(cId.longValue()),
               context.getName()));
      }
      
      return deps.iterator();
   }

   // see base class
   @Override
   public PSDependency getDependency(PSSecurityToken tok, String id) throws PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSDependency dep = null;
      
      IPSPublishingContext context = findPublishingContext(id);
      if (context != null)
         dep = createDependency(m_def, id, context.getName());
           
      return dep;      
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>LocationSchemeDef</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   @Override
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
   public Iterator getDependencyFiles(
      @SuppressWarnings("unused") PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
         
      List<PSDependencyFile> files = new ArrayList<>();
      
      String depId = dep.getDependencyId();
      IPSPublishingContext context = findPublishingContext(depId);
      if (context == null)
      {
         Object[] args = {dep.getDependencyId(), dep.getObjectTypeName(),
               dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND,
               args);
      }
      
      files.add(getDepFileFromContext(context));

      return files.iterator();
   }

   /**
    * Using the idMap in <code>ctx</code> to transfer the ids from the source
    * to target for a given context.
    *
    * @param context The publishing context to be modified, assumed not
    * <code>null</code>.
    * @param ctx The import context to aid in the installation, assumed not
    * <code>null</code>.
    *
    * @throws PSDeployException if any error occur.
    */
   private void transferIdsInContext(PSSecurityToken tok, PSDependency dep,
         PSPublishingContext context, PSImportCtx ctx) 
   throws PSDeployException
   {
      // xform the ids for CONTEXT, DEFAULTSCHEME
      IPSGuid schId = context.getDefaultSchemeId();
      if (schId != null)
      {
         PSIdMapping schemeMap = getIdMapping(ctx,
                 String.valueOf(schId.longValue()),
                 PSLocSchemeDefDependencyHandler.DEPENDENCY_TYPE);

         if (schemeMap != null)
         {
            context.setDefaultSchemeId(PSGuidUtils.makeGuid(
                    schemeMap.getTargetId(),
                    PSTypeEnum.LOCATION_SCHEME));
         }
      }

      IPSGuid ctxId = context.getGUID();
      if (ctxId != null)
      {
         PSIdMapping ctxMapping = getIdMapping(ctx,
                 String.valueOf(ctxId.longValue()),
                 DEPENDENCY_TYPE);

         if (ctxMapping != null)
            context.setId(new Integer(ctxMapping.getTargetId()));
      }
   }
   
   
   // see base class
   @Override
   public void installDependencyFiles(
      @SuppressWarnings("unused") PSSecurityToken tok, PSArchiveHandler archive, 
      PSDependency dep, PSImportCtx ctx) throws PSDeployException
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
               "error occurred while installing context: " +
               e.getLocalizedMessage());
      } 
    }

   /**
    * See {@link IPSServiceDependencyHandler#doInstallDependencyFiles(
    * PSSecurityToken, PSArchiveHandler, PSDependency, PSImportCtx)} for
    * details.
    */
   public void doInstallDependencyFiles(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
           throws PSDeployException, PSNotFoundException {
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
      
      IPSPublishingContext context = null;
      PSIdMapping ctxtMapping = getIdMapping(ctx, dep); 
      if (ctxtMapping != null)
         context = findPublishingContext(ctxtMapping.getTargetId());
      else
         context = findPublishingContext(dep.getDependencyId());
     
      boolean isNew = (context == null) ? true : false;
      Integer ver = null;
      if (!isNew)
      {
         context = m_siteMgr.loadContextModifiable(context.getGUID());
         ver = ((PSPublishingContext) context).getVersion();
         ((PSPublishingContext) context).setVersion(null);
      }
      else
      {
         context = m_siteMgr.createContext();
      }
      
      String packagedContextContent = PSDependencyUtils.getFileContentAsString(
            archive, file);
      
      try
      {
         PSPublishingContext ctxt = (PSPublishingContext) context;
         ctxt.fromXML(packagedContextContent);
         transferIdsInContext(tok, dep, ctxt, ctx);
         ctxt.setVersion(null);
                           
         if (!isNew)
            ctxt.setVersion(ver);

         m_siteMgr.saveContext(ctxt);
         
         //add transaction log
         addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.CONTEXT, isNew);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e,
               "error occurred while installing context: " + 
               e.getLocalizedMessage());
      }
   }  
   
   /**
    * Package the location scheme dependencies
    * @param tok the security token, assumed not <code>null</code>
    * @param dep the dependency for which child dependencies are returned,
    * assumed not <code>null</code>
    * @return the location scheme child dependencies, never <code>null</code>
    * may be empty
    * @throws PSDeployException
    */
   private List<PSDependency> getLocationSchemeDependencies(
         PSSecurityToken tok, PSDependency dep) throws PSDeployException, PSNotFoundException {
      List<PSDependency> childDeps = new ArrayList<>();
      List<String> ids = new ArrayList<>();

      IPSPublishingContext context = findPublishingContext(
            dep.getDependencyId());
      if (context == null)
      {
         Object[] args = {dep.getDependencyId(), dep.getObjectTypeName(),
               dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND,
               args);
      }

      List<IPSLocationScheme> schemes = m_siteMgr.findSchemesByContextId(
            context.getGUID());
      for (IPSLocationScheme scheme : schemes)
         ids.add(String.valueOf(scheme.getGUID().longValue()));

      Iterator<String> it = ids.iterator();
      PSDependencyHandler lsHandler =
         getDependencyHandler(
               PSLocSchemeDefDependencyHandler.DEPENDENCY_TYPE);

      PSDependency tmpDep = null;
      while (it.hasNext())
      {
         String depId = it.next();
         tmpDep = lsHandler.getDependency(tok, depId);
         if ( tmpDep != null )
            childDeps.add(tmpDep);
      }

      return childDeps;
   }

   /**
    * Creates a dependency file from a given dependency data object.
    * 
    * @param context the publishing context, assumed not <code>null</code>.
    * 
    * @return The dependency file object, it will never be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   private PSDependencyFile getDepFileFromContext(IPSPublishingContext context)
      throws PSDeployException
   {
      String str = "";
      try
      {
         str = ((PSPublishingContext) context).toXML();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for Context:"
                     + context.getName());
      }
      
      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(str));
   }
   
   /**
    * Retrieves a publishing context for the given id.
    * 
    * @param id the publishing context id, assumed not <code>null</code>.
    * @return publishing context corresponding to the id, <code>null</code> if a
    * match was not found.
    */
   private IPSPublishingContext findPublishingContext(String id) throws PSNotFoundException {
      IPSPublishingContext context = null;
      
      List<IPSPublishingContext> contexts = m_siteMgr.findAllContexts();
      for (IPSPublishingContext ctxt : contexts)
      {
         if (String.valueOf(ctxt.getGUID().longValue()).equals(id))
         {
            context = ctxt;
            break;
         }
      }
      
      return context;
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "ContextDef";
   
   /**
    * Get the site manager.
    */
   private static IPSSiteManager m_siteMgr = 
      PSSiteManagerLocator.getSiteManager();
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<>();

   static
   {
      ms_childTypes.add(PSLocSchemeDefDependencyHandler.DEPENDENCY_TYPE);
   }
}
