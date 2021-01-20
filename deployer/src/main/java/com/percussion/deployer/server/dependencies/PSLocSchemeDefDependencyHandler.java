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
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.sitemgr.data.PSLocationScheme;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class to handle packaging and deploying a location scheme definition.
 */
public class PSLocSchemeDefDependencyHandler
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
   public PSLocSchemeDefDependencyHandler(PSDependencyDef def,
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

      IPSLocationScheme scheme = m_siteMgr.loadScheme(PSGuidUtils.makeGuid(
            dep.getDependencyId(), PSTypeEnum.LOCATION_SCHEME));
      
      // get the VariantDef or TemplateDef child dependency
      PSDependencyHandler varHandler = 
         getDependencyHandler(PSVariantDefDependencyHandler.DEPENDENCY_TYPE);
      PSDependencyHandler tmpHandler = 
         getDependencyHandler(PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
      PSDependency tmpDep = null;
      
      String depId = String.valueOf(scheme.getTemplateId()); 
      IPSAssemblyTemplate t = 
         PSTemplateDefDependencyHandler.findTemplateByDependencyID(depId,
               false);

      if (t.isVariant())
         tmpDep = varHandler.getDependency(tok, depId);
      else
         tmpDep = tmpHandler.getDependency(tok, depId);

      if (tmpDep != null)
         childDeps.add(tmpDep);
           
      // get the ContentType child dependency
      PSDependencyHandler ctHandler = 
         getDependencyHandler(PSCEDependencyHandler.DEPENDENCY_TYPE);
      PSDependency ctDep = ctHandler.getDependency(tok, 
            String.valueOf(scheme.getContentTypeId()));
      if (ctDep != null)
         childDeps.add(ctDep);
      
      // get the extension dependency
      PSDependencyHandler extHandler = 
         getDependencyHandler(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      PSDependency extDep = 
         extHandler.getDependency(tok, scheme.getGenerator());
      if (extDep != null)
         childDeps.add(extDep);
   
      return childDeps.iterator();
   }

   // see base class
   @Override
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok) 
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      List<PSDependency> deps = new ArrayList<PSDependency>();
      
      Set<IPSLocationScheme> schemes = findAllLocationSchemes();
      for (IPSLocationScheme scheme : schemes)
      {
         deps.add(createDependency(m_def, 
               String.valueOf(scheme.getGUID().longValue()), scheme.getName()));
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

      PSDependency sDep = null;
      
      IPSLocationScheme scheme = findLocationScheme(id);
      if (scheme != null)
         sDep = createDependency(m_def, id, scheme.getName());
          
      return sDep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>ContentType</li>
    * <li>VariantDef</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   @Override
   public Iterator<String> getChildTypes()
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
      IPSLocationScheme scheme = findLocationScheme(dep.getDependencyId());
      if (scheme == null)
      {
         Object[] args = {dep.getDependencyId(), dep.getObjectTypeName(),
               dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND,
               args);
      }
      
      files.add(getDepFileFromLocationScheme(scheme));
            
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
               "error occurred while installing context: " +
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
      
      IPSLocationScheme scheme = null;
      PSIdMapping schemeMapping = getIdMapping(ctx, dep); 
      if (schemeMapping != null)
         scheme = findLocationScheme(schemeMapping.getTargetId());
      else
         scheme = findLocationScheme(dep.getDependencyId());
     
      boolean isNew = (scheme == null) ? true : false;
      Integer ver = null;
      if (!isNew)
      {
         scheme = m_siteMgr.loadSchemeModifiable(scheme.getGUID());
         ver = ((PSLocationScheme) scheme).getVersion();
         ((PSLocationScheme) scheme).setVersion(null);
      }
      else
      {
         scheme = m_siteMgr.createScheme();
      }
      
      String packagedSchemeContent = PSDependencyUtils.getFileContentAsString(
            archive, file);
      
      try
      {
         PSLocationScheme ls = (PSLocationScheme) scheme;
         ls.fromXML(packagedSchemeContent);
         
         // Note: "transferIdsInLocationScheme" will try to map the ids for the 
         // Location Scheme's Content Type and Template. If they are not 
         // found on the target system an exception is thrown:
         //     PSDeployException(IPSDeploymentErrors.MISSING_ID_MAPPING).
         // This is acceptable for these associations, and the exception
         // is caught below. This skips over the call to save the scheme below,
         // and the Location Scheme is not added to target.
         transferIdsInLocationScheme(ls, dep, ctx);
         ls.setVersion(null);
         
         if (!isNew)
            ls.setVersion(ver);
                  
         m_siteMgr.saveScheme(ls);
         
         //add transaction log
         addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.LOCATION_SCHEME,
               isNew);
      }
      catch (Exception e)
      {
         PSDeployException psde = (PSDeployException) e;
         Object [] errorArgs = psde.getErrorArguments();
         String obType = (String)errorArgs[0];
         if ( (psde.getErrorCode() == IPSDeploymentErrors.MISSING_ID_MAPPING) &&
             ((obType.equals(PSTemplateDefDependencyHandler.DEPENDENCY_TYPE)) ||
              (obType.equals(PSVariantDefDependencyHandler.DEPENDENCY_TYPE)) ||
              (obType.equals(PSCEDependencyHandler.DEPENDENCY_TYPE))) )
         {
            // The Template or Content Type do not exist on the target system. 
            // The Location Scheme will not be saved (which is what we want).
            // However, later, when we install the Contexts, we must be sure to 
            // any Contexts are not using this Location scheme and remove them 
            // if they are.
         }
         else 
         {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e,
               "error occurred while installing location scheme: " + 
               e.getLocalizedMessage());
         }
      }
   }  
   
   /**
    * Using the idMap in <code>ctx</code> to transfer the ids from the source
    * to target for a given location scheme object and dependency object.
    *
    * @param scheme The location scheme object to be modified, assumed not
    * <code>null</code>.
    * @param dep The location scheme dependency object, assumed not
    * <code>null</code>.
    * @param ctx The import context to aid in the installation, assumed not
    * <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   @SuppressWarnings("unchecked")
   private void transferIdsInLocationScheme(PSLocationScheme scheme,
         PSDependency dep, PSImportCtx ctx)
      throws PSDeployException
   {
      // get id map for the location scheme object
      PSIdMapping locSchemeMapping = getIdMapping(ctx, dep);
      PSIdMap idMap = ctx.getCurrentIdMap();
            
      // xform the ids for LOCSCHEME, CONTEXT, VARIANT and CONTENTTYPE
      if (locSchemeMapping != null)
         scheme.setId(Long.valueOf(locSchemeMapping.getTargetId()));
      
      String tempIdStr = String.valueOf(scheme.getTemplateId());
      String mappedIdStr = null;
      if (idMap != null)    // child id
      {
         if (idMap.getMapping(tempIdStr,
               PSTemplateDefDependencyHandler.DEPENDENCY_TYPE) != null)
         {
            mappedIdStr = mapChildIdForNullableValue(tempIdStr, dep,
                  PSTemplateDefDependencyHandler.DEPENDENCY_TYPE, ctx);
         }
         else
         {
            mappedIdStr = mapChildIdForNullableValue(tempIdStr, dep,
                  PSVariantDefDependencyHandler.DEPENDENCY_TYPE, ctx);
         }

         scheme.setTemplateId(new Long(mappedIdStr));
      }

      scheme.setContentTypeId(new Long(mapChildIdForNullableValue(
            String.valueOf(scheme.getContentTypeId()), dep,
            PSCEDependencyHandler.DEPENDENCY_TYPE, ctx)));

      PSIdMapping contextMapping = getIdMapping(ctx, 
            String.valueOf(scheme.getContextId().longValue()),
            PSContextDefDependencyHandler.DEPENDENCY_TYPE);

      if (contextMapping != null)
      {
         scheme.setContextId(PSGuidUtils.makeGuid(
               contextMapping.getTargetId(), 
               PSTypeEnum.CONTEXT));
      }
   }
   
   /**
    * Retrieves all location schemes.
    * 
    * @return location schemes as a list, never <code>null</code>, may be
    * empty.
    */
   private Set<IPSLocationScheme> findAllLocationSchemes()
   {
      Set<IPSLocationScheme> schemes = new HashSet<IPSLocationScheme>(
            m_siteMgr.findAllSchemes());
        
      return schemes;
   }
   
   /**
    * Retrieves a location scheme for the given id.
    * 
    * @param id the location scheme id, assumed not <code>null</code>.
    * @return location scheme corresponding to the id, <code>null</code> if a
    * match was not found.
    */
   private IPSLocationScheme findLocationScheme(String id)
   {
      IPSLocationScheme scheme = null;
      
      Set<IPSLocationScheme> schemes = findAllLocationSchemes();
      for (IPSLocationScheme s : schemes)
      {
         if (String.valueOf(s.getGUID().longValue()).equals(id))
         {
            scheme = s;
            break;
         }
      }
      
      return scheme;
   }

   /**
    * Creates a dependency file from a given dependency data object.
    * @param scheme the location scheme, assumed not <code>null</code>.
    * 
    * @return The dependency file object, it will never be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   private PSDependencyFile getDepFileFromLocationScheme(
         IPSLocationScheme scheme)
      throws PSDeployException
   {
      String str = "";
      try
      {
         str = ((PSLocationScheme) scheme).toXML();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for Location Scheme:"
                     + scheme.getName());
      }
      
      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(str));
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "LocationSchemeDef";

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();

   /**
    * Get the site manager.
    */
   private static IPSSiteManager m_siteMgr = 
       PSSiteManagerLocator.getSiteManager();
      
   static
   {
      ms_childTypes.add(PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSCEDependencyHandler.DEPENDENCY_TYPE);
   }

}
