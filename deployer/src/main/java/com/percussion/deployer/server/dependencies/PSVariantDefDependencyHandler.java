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

import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.deployer.services.IPSDeployService;
import com.percussion.deployer.services.PSDeployServiceException;
import com.percussion.deployer.services.PSDeployServiceLocator;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class to handle packaging and deploying a variant definition.
 */
public class PSVariantDefDependencyHandler extends PSDataObjectDependencyHandler
{

   /**
    * Construct the dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSVariantDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   /**
    */
   private void init()
   {
      if ( m_assemblyHelper == null )
         m_assemblyHelper = new PSAssemblyServiceHelper();
   }

   // see base class
   @Override
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      String variantId = dep.getDependencyId();
      IPSAssemblyTemplate tmp = findVariantByDependencyID(variantId, true);
      if ( tmp == null )
         throw new IllegalArgumentException ("Template may not be null or empty");
      
      // get all content type children for this variant
      List<PSDependency> childDeps = getContentTypeDependencies(tok, tmp);

      // get all slot children for this variant
      List<PSDependency> deps2 = getSlotDependencies(tok, tmp);
      childDeps.addAll(deps2);

      //Acl deps
      addAclDependency(tok, PSTypeEnum.TEMPLATE, dep, childDeps);


      return childDeps.iterator();

   }

   /**
    * Utility method to find the Variant by a given guid(as a STRINGGGGGG)
    * @param depId the guid
    * @return <code>null</code> if Variant is not found
    * 
    */
   protected static PSAssemblyTemplate findVariantByDependencyID(String depId,
                                                                 boolean loadSlots)
   {
      return PSTemplateDefDependencyHandler.findTemplateByDependencyID(depId,
            loadSlots);
   }
   
   /**
    * Transform any IdTypes and ids in Bindings .
    * @param t the template never <code>null</code>
    * @param ctx import context never <code>null</code>
    * @param dep the dependency never <code>null</code>
    * @return do the transforms on the passed in template and return it back
    * @throws PSDeployException
    */
   public PSAssemblyTemplate doTransforms(PSAssemblyTemplate t,
                                          PSImportCtx ctx, PSDependency dep)
         throws PSDeployException
   {    
      if ( t == null )
         throw new IllegalArgumentException(
               "template cannot be null for idtype mapping");      
      PSIdMapping clMapping = getIdMapping(ctx, dep);
      if (clMapping != null && clMapping.getTargetId() != null)
      {
          t = (PSAssemblyTemplate) PSDependencyUtils.transformElementId(t,
                dep, ctx, clMapping);
      }
      return t;
   }

   /**
    * Given the variant id, get the ContentType Deps 
     * @param tok may not be <code>null</code>
    * @param tmp the template may not be <code>null</code>
    * @return List of Dependencies of TEMPLATE type
    * @throws PSDeployException
    */
   private List<PSDependency> getContentTypeDependencies(PSSecurityToken tok,
         IPSAssemblyTemplate tmp) throws PSDeployException
   {
      List<PSDependency> depList = new ArrayList<PSDependency>();
      init();
      List<IPSGuid> ctGuidList = m_assemblyHelper.getContentTypesByTemplate(tmp);
      PSDependencyHandler handler = getDependencyHandler(
                                         PSCEDependencyHandler.DEPENDENCY_TYPE);
      Iterator<IPSGuid> it = ctGuidList.iterator();
      while ( it.hasNext() )
      {
         IPSGuid ctGuid = it.next();
         String id = ""+ctGuid.longValue();
         PSDependency childDep = handler.getDependency(tok, id);
         depList.add(childDep);
      }
      return depList;
   }
   
   /**
    * Given the variant id, figure out the slot deps
    * @param tok may not be <code>null</code>
    * @param tmp may not be <code>null</code>
    * @return List of Dependencies of SLOT type
    * @throws PSDeployException
    */
   private List<PSDependency> getSlotDependencies(PSSecurityToken tok,
         IPSAssemblyTemplate tmp) throws PSDeployException
   {
      List<PSDependency> depList = new ArrayList<PSDependency>();
      
      Set<IPSTemplateSlot> slots = tmp.getSlots();
      
      PSDependencyHandler handler = getDependencyHandler(
            PSSlotDependencyHandler.DEPENDENCY_TYPE);
      
      Iterator<IPSTemplateSlot> it = slots.iterator();
      while ( it.hasNext() )
      {
         IPSTemplateSlot slot = it.next();
         IPSGuid slotGuid = slot.getGUID();
         String id = ""+slotGuid.longValue();
         PSDependency childDep = handler.getDependency(tok, id);
         depList.add(childDep);
      }

      
      return depList;
   }
   // see base class
   @Override
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      init();   
      
      HashMap<String, IPSAssemblyTemplate> legTemplates = m_assemblyHelper
            .getLegacyTemplatesMap();
      
      Iterator variantNames = legTemplates.keySet().iterator(); 
      List<PSDependency> deps = new ArrayList<PSDependency>();
      PSDependency dep;
      while (variantNames.hasNext())
      {
         String name = (String) variantNames.next();
         IPSAssemblyTemplate tmp = legTemplates.get(name);
         dep = createDeployableElement(m_def, ""
               + tmp.getGUID().longValue(), name);
         deps.add(dep);
      }
      return deps.iterator();
   }


   // see base class
   @Override
   public PSDependency getDependency(PSSecurityToken tok, String id)
   {
      PSDependency dep = null;
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (StringUtils.isBlank(id))
         throw new IllegalArgumentException("id may not be null or empty");

      if (Integer.parseInt(id) <= 0)
         return null;
      
      IPSAssemblyTemplate tmp = findVariantByDependencyID(id, false);
      if (tmp != null && tmp.isVariant())
         dep = createDependency(m_def, String
               .valueOf(tmp.getGUID().longValue()), tmp.getName());
      return dep;
    }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Slot</li>
    * <li>ContentTyp</li>
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
      return;
   }

   // see base class
   @Override
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      init();

      // pack the data into the files
      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();

      IPSAssemblyTemplate tmp = findVariantByDependencyID(dep.getDependencyId(), true);

      if ( tmp != null )    
         files.add(PSTemplateDefDependencyHandler.getDepFileFromTemplate(tmp));
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

      init();
      // retrieve datas, variant data followed by its child data if any
      Iterator files = PSTemplateDefDependencyHandler
            .getTemplateDependecyFilesFromArchive(archive, dep);
      PSDependencyFile depFile = (PSDependencyFile) files.next();
      PSIdMapping clMapping = getIdMapping(ctx, dep);
      PSAssemblyTemplate tmp = null;

      // assume the variant has been installed and find its mapping and load
      if (clMapping != null && clMapping.getTargetId() != null)
         tmp = findVariantByDependencyID(clMapping.getTargetId(), false);
      else
         tmp = findVariantByDependencyID(dep.getDependencyId(), false);

      boolean isNew = (tmp == null) ? true : false;
      Integer ver = null;
      if (!isNew)
      {
         // deserialize on the existing variant
         ver = ((PSAssemblyTemplate) tmp).getVersion();
         ((PSAssemblyTemplate) tmp).setVersion(null);
      }
      
      IPSDeployService depSvc = PSDeployServiceLocator.getDeployService();
      try
      {
         depSvc.deserializeAndSaveVariant(tok, archive, dep, depFile, ctx,
               this, tmp, ver);
      }
      catch (PSDeployServiceException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "error occurred while installing template: "
                     + e.getLocalizedMessage());
      }
      // add transaction log
      addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.TEMPLATE, isNew);

   }


   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = 
      IPSDeployConstants.DEP_OBJECT_TYPE_VARIANT_DEF;   
      
   /**
    * Da assembly Service Helper
    */
   private PSAssemblyServiceHelper m_assemblyHelper = null;
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();

   static
   {
      ms_childTypes.add(PSSlotDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSCEDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
   }

}
