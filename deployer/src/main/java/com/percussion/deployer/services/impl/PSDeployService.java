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
package com.percussion.deployer.services.impl;

import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.server.IPSServiceDependencyHandler;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyManager;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.deployer.server.dependencies.PSDependencyHandler;
import com.percussion.deployer.server.dependencies.PSFilterDefDependencyHandler;
import com.percussion.deployer.server.dependencies.PSSiteDefDependencyHandler;
import com.percussion.deployer.server.dependencies.PSTemplateDefDependencyHandler;
import com.percussion.deployer.server.dependencies.PSVariantDefDependencyHandler;
import com.percussion.deployer.services.IPSDeployService;
import com.percussion.deployer.services.PSDeployServiceException;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An MSM related service which delineates the transaction boundaries for 
 * specific assembly elements. 
 * @author vamsinukala
 *
 */
//todo: reconcile with system/src/main/java/com/percussion/deploy/services/impl/PSDeployService.java
@Transactional (propagation = Propagation.REQUIRED, noRollbackFor=Exception.class)
public class PSDeployService
   implements IPSDeployService
{
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }



    /**
    * A helper method for installing dependency files within a transaction
    * boundary. 
    * Hibernate does not like child entities loaded in one session and
    * then trying to save, again in a different session. This service handles
    * deserialization, applying transforms, persisting/updating via 
    * HibernateTransactionManager
    * @param tok the security token never <code>null</code>
    * @param archive the ArchiveHandler to use to retrieve the files from the 
    *        archive, may not be <code>null</code>
    * @param dep the dependency never <code>null</code>
    * @param depFile the dependency file never <code>null</code>
    * @param ctx the import context never <code>null</code>
    * @param depHandler the dependency handler which performs the transform ID
    *        Types and mappings never <code>null</code>
    * @param s PSSite, may or may not be <code>null</code>
    * @param ver version  of the existing site, this needs to be persisted so
    *        that hibernate mananges the versioning for optimistic locking
    * @throws PSDeployServiceException
    * 
    */
   public void deserializeAndSaveSite(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSDependencyFile depFile,
         PSImportCtx ctx, PSDependencyHandler depHandler, IPSSite s,
         Integer ver) throws PSDeployServiceException
   {
      //TODO: convert site def dependency handler to use the
      // installDependencyFiles(...) method below and remove this method.
      
      if ( tok == null )
         throw new IllegalArgumentException("security token may not be null");
      if ( archive == null )
         throw new IllegalArgumentException("archive may not be null");
      if ( dep == null )
         throw new IllegalArgumentException("dependency may not be null");
      if ( depFile == null )
         throw new IllegalArgumentException(
               "deserialization file may not be null");
      if ( ctx == null )
         throw new IllegalArgumentException(
               "dependency context may not be null");
      if ( depHandler == null )
         throw new IllegalArgumentException(
               "dependency handler may not be null");
      
      
      PSSiteDefDependencyHandler dh = (PSSiteDefDependencyHandler)depHandler;
      IPSSite desSite;
      try
      {
         desSite = dh.generateSiteFromFile(tok,archive, depFile, s, ctx);
         dh.transformSiteData(tok, dep, ctx, desSite);
         dh.saveDeserializedObject(desSite, ver);
      }
      catch (PSDeployException e)
      {
         throw new PSDeployServiceException(e);
      }
   }

   /**
    * A helper method for installing dependency files within a transaction
    * boundary. 
    * Hibernate does not like child entities loaded in one session and
    * then trying to save, again in a different session. This service handles
    * deserialization, applying transforms, persisting/updating via 
    * HibernateTransactionManager
    * @param tok security token never <code>null</code>
    * @param archive the ArchiveHandler to use to retrieve the files from the
    *        archive, may not be <code>null</code>
    * @param dep never <code>null</code>
    * @param depFile file never <code>null</code>
    * @param depHandler which performs the transform ID Types and
    *        mappings never <code>null</code>
    * @param t the template  may or may not be <code>null</code>
    * @param ver version of the existing site, this needs to be persisted so
    *        that hibernate manages the versioning for optimistic locking
    * @param bVer the versions of the bindings, hibernate is again sensitive
    *        to the version numbers when updating existing bindings
    * @throws PSDeployServiceException
    * 
    */

   public void deserializeAndSaveTemplate(PSSecurityToken tok,
                                          PSArchiveHandler archive, PSDependency dep, PSDependencyFile depFile,
                                          PSImportCtx ctx, PSDependencyHandler depHandler,
                                          PSAssemblyTemplate t, Integer ver, HashMap<Long, Integer> bVer)
         throws PSDeployServiceException
   {
      //TODO: convert template def dependency handler to use the
      // installDependencyFiles(...) method below and remove this method.
      
      PSTemplateDefDependencyHandler dh = 
         (PSTemplateDefDependencyHandler)depHandler;
      try
      {
         t = dh.generateTemplateFromFile(archive, depFile, t, ctx);
         dh.doTransforms(t, ctx, dep);
         PSTemplateDefDependencyHandler.saveTemplate(t, ver, bVer);
      }
      catch (PSDeployException e)
      {
         throw new PSDeployServiceException(e);
      } 
   }
   
   /**
    * A helper method for installing dependency files within a transaction
    * boundary. 
    * Hibernate does not like child entities loaded in one session and
    * then trying to save, again in a different session. This service handles
    * deserialization, applying transforms, persiting/updating via 
    * HibernateTransactionManager
    * @param tok the security token never <code>null</code>
    * @param archive the ArchiveHandler to use to retrieve the files from the 
    *        archive, may not be <code>null</code>
    * @param dep the dependency never <code>null</code>
    * @param depFile the dependency file never <code>null</code>
    * @param ctx the import context never <code>null</code>
    * @param depHandler the dependency handler which performs the transform ID
    *        Types and mappings never <code>null</code>
    * @throws PSDeployServiceException
    * 
    */
   public void deserializeAndSaveFilter(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSDependencyFile depFile,
         PSImportCtx ctx, PSDependencyHandler depHandler)
         throws PSDeployServiceException
   {
      //TODO: convert filter def dependency handler to use the 
      //  installDependencyFiles(...) method below and remove this method.
      
      if ( tok == null )
         throw new IllegalArgumentException("security token may not be null");
      if ( archive == null )
         throw new IllegalArgumentException("archive may not be null");
      if ( dep == null )
         throw new IllegalArgumentException("dependency may not be null");
      if ( depFile == null )
         throw new IllegalArgumentException(
               "deserialization file may not be null");
      if ( ctx == null )
         throw new IllegalArgumentException(
               "dependency context may not be null");
      if ( depHandler == null )
         throw new IllegalArgumentException(
               "dependency handler may not be null");
      
      
      PSFilterDefDependencyHandler dh =
         (PSFilterDefDependencyHandler)depHandler;
      try
      {
         IPSItemFilter filter = dh.findFilterByDependencyID(
               dep.getDependencyId());
         
         boolean isNew = (filter == null) ? true : false;
         String fName = "";
         Integer lver = null;
         
         if ( !isNew )
         {
            fName = filter.getName();
            List<IPSGuid> ids = new ArrayList<>();
            ids.add(filter.getGUID());
            IPSFilterService filterSvc = 
               PSFilterServiceLocator.getFilterService();
            try
            {
               filter = filterSvc.loadFilter(ids).get(0);
            }
            catch (PSNotFoundException e)
            {
               throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
                     "Could not load the existing filter: " + fName);
            }

                // deserialize on the existing filter
                lver = ((PSItemFilter) filter).getVersion();
                ((PSItemFilter) filter).setVersion(null);
                filter = null;
         }
         
         filter = dh.generateFilterFromFile(tok, archive, dep, depFile, ctx,
               filter);
         filter = dh.doTransforms(filter, dep, ctx, isNew);
         dh.saveFilter(filter, lver);         
      }
      catch (PSDeployException e)
      {
         throw new PSDeployServiceException(e);
      }
   }
   
   /**
    * A helper method for installing dependency files within a transaction
    * boundary. 
    * Hibernate does not like child entities loaded in one session and
    * then trying to save, again in a different session. This service handles
    * deserialization, applying transforms, persisting/updating via 
    * HibernateTransactionManager
    * @param tok security token never <code>null</code>
    * @param archive the ArchiveHandler to use to retrieve the files from the 
    *        archive, may not be <code>null</code>    
    * @param dep never <code>null</code>
    * @param depFile file never <code>null</code>
    * @param depHandler which performs the transform ID Types and 
    *        mappings never <code>null</code>
    * @param t the template  may or may not be <code>null</code>
    * @param ver version of the existing variant, this needs to be 
    *        persisted so that hibernate manages the versioning for 
    *        optimistic locking
    * @throws PSDeployServiceException
    * 
    */
   public void deserializeAndSaveVariant(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSDependencyFile depFile,
         PSImportCtx ctx, PSDependencyHandler depHandler,
         PSAssemblyTemplate t, Integer ver)
         throws PSDeployServiceException
   {
      //TODO: convert variant def dependency handler to use the 
      //  installDependencyFiles(...) method below and remove this method.
      
      try
      {
         PSVariantDefDependencyHandler dh = 
                     (PSVariantDefDependencyHandler) depHandler;
         
         PSTemplateDefDependencyHandler th = 
               (PSTemplateDefDependencyHandler) PSDependencyManager
               .getInstance().getDependencyHandler(
                     PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
         
         t = th.generateTemplateFromFile(archive, depFile, t, ctx);
         t = dh.doTransforms(t, ctx, dep);
         PSTemplateDefDependencyHandler.saveTemplate(t, ver,
               new HashMap<>());
      }
      catch (PSDeployException e)
      {
         throw new PSDeployServiceException(e);
      } 
   }

   /**
    * See {@link IPSDeployService#installDependencyFiles(PSSecurityToken, 
    * PSArchiveHandler, PSDependency, PSImportCtx, IPSServiceDependencyHandler)}
    * for details.  Invokes 
    * {@link IPSServiceDependencyHandler#doInstallDependencyFiles(
    * PSSecurityToken, PSArchiveHandler, PSDependency, PSImportCtx)}
    */
   public void installDependencyFiles(PSSecurityToken tok, 
         PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx,
         IPSServiceDependencyHandler service)
   throws PSDeployServiceException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      if (service == null)
         throw new IllegalArgumentException("service may not be null");
      
      try
      {
         service.doInstallDependencyFiles(tok, archive, dep, ctx);
      }
      catch (PSDeployException | PSNotFoundException e)
      {
         throw new PSDeployServiceException(e);
      }
   }
}


