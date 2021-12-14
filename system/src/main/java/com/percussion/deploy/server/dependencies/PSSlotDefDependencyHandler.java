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

import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSDeployComponentUtils;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSIdMapping;
import com.percussion.deploy.server.IPSIdTypeHandler;
import com.percussion.deploy.server.PSAppTransformer;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.design.objectstore.PSParam;
import com.percussion.extension.IPSExtension;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.assembly.data.PSTemplateTypeSlotAssociation;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class to handle packaging and deploying a Slot definition the new way.
 * @author vamsinukala
 */

public class PSSlotDefDependencyHandler extends PSDependencyHandler
      implements
         IPSIdTypeHandler
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

   public PSSlotDefDependencyHandler(PSDependencyDef def,
         PSDependencyMap dependencyMap) 
   {
      super(def, dependencyMap); 
   }

   /**
    * Helper method to init PSAssemblyServiceHelper
    */
   private void init()
   {
      if ( m_assemblyHelper == null)
         m_assemblyHelper = new PSAssemblyServiceHelper();
   }

   /**
    * Util method to figure out if the slot is a legacy slot, in which case,
    * there is no finder. Used to calculate child deps namely the exits in 
    * finder arguments
    * See the other way of accesing the same info:
    * {@link #isLegacySlot(String id)}.
    * @param slot the actual slot itself
    * @return true if legacy slot
    */
   public boolean isLegacySlot(IPSTemplateSlot slot)
   {
      if ( slot == null )
         throw new IllegalArgumentException("slot cannot be null");
      String finder = slot.getFinderName();
      return StringUtils.isBlank(finder);
   }
   
   /**
    * Util method to figure out if the slot is a legacy slot, in which case,
    * there is no finder. Used to calculate child deps namely the exits in 
    * finder arguments
    * @param id  the slot by its id ( GUID or old style id)
    * @return true  if legacy slot
    */
   public boolean isLegacySlot(String id)
   {
      if (StringUtils.isBlank(id))
         throw new IllegalArgumentException("slot ID may not be null or empty");
      return isLegacySlot(findSlotByDependencyID(id));
   }
   /**
    * Utility method to find the slot by a given GUID(as a STRINGGGGGG)
    * @param depId the GUID
    * @return <code>null</code> if slot is not found else get DA SLOT
    */
   private IPSTemplateSlot findSlotByDependencyID(String depId)
   {
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency ID may not be null or empty");
      IPSGuid slotId = new PSGuid(PSTypeEnum.SLOT, depId);
      return m_assemblySvc.findSlot(slotId);
   }

   
   // see base class
   // id is a PSGuid for a template def
   @Override
   public PSDependency getDependency(PSSecurityToken tok, String id)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSDependency dep = null;
      IPSTemplateSlot slot = findSlotByDependencyID(id);
      if ( slot != null )
      {
         dep = createDependency(m_def, String.valueOf(slot.getGUID()
               .longValue()), slot.getName());
      }
      return dep;
   }

   
   //see base class
   @Override
   @SuppressWarnings("unchecked")
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      init();
      
      IPSTemplateSlot slot = findSlotByDependencyID(dep.getDependencyId());
      if ( slot == null )
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "Could not find a slot with id: " + dep.getDependencyId());
      boolean isLegacy = isLegacySlot(slot);
      
      // OK, we have the slot, now package its dependencies..
      List<PSDependency> childDeps = new ArrayList<PSDependency>();
      
      PSTemplateTypeSlotAssociation[] slotRelations = 
         ((PSTemplateSlot)slot).getSlotTypeAssociations();
      long[] tmpList = null;
      long[] ctList  = null;
      if ( slotRelations.length > 0 )
      {
         tmpList = new long[slotRelations.length];
         ctList  = new long[slotRelations.length];
         for ( int i=0; i<slotRelations.length; i++)
         {
            PSTemplateTypeSlotAssociation slotRel = slotRelations[i];
            tmpList[i] = slotRel.getTemplateId();
            ctList[i]  = slotRel.getContentTypeId();            
         }
      }
      
      // Now that we have the SLOT, first add the ContentType deps
      PSDependencyHandler handler = getDependencyHandler(
            PSCEDependencyHandler.DEPENDENCY_TYPE);
      if ( ctList != null )for ( int i=0; i<ctList.length; i++ )
      {
         PSDependency childDep = handler.getDependency(tok, String
               .valueOf(ctList[i]));
         childDeps.add(childDep);
      }
      
      
      // Next add the Templates/VariantDef as deps
      handler = getDependencyHandler(
            PSTemplateDependencyHandler.DEPENDENCY_TYPE);
      PSDependencyHandler varHandler = getDependencyHandler(
            PSVariantDefDependencyHandler.DEPENDENCY_TYPE);
      
      if ( tmpList != null ) for ( int i= 0; i<ctList.length; i++ )
      {
         PSDependency childDep = null;
         // load just the template and not its baggage
         IPSAssemblyTemplate t = null;
         try
         {
            t = m_assemblySvc.loadTemplate(String
                  .valueOf(tmpList[i]), false);
         }
         catch (PSAssemblyException e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
                  "Unable to load template while cataloging slot dependencies\n"+
                  e.getLocalizedMessage());
         }
         if ( t == null )
            continue;
         String guidStr = String.valueOf(t.getGUID().longValue());
         if ( !t.getAssembler().equals(IPSExtension.LEGACY_ASSEMBLER))
            childDep = handler.getDependency(tok, guidStr);
         else
            childDep = varHandler.getDependency(tok, guidStr);

         if ( childDep != null )
            childDeps.add(childDep);
      }
      
      // Acl deps
      addAclDependency(tok, PSTypeEnum.SLOT, dep, childDeps);


      /**
       * STOP!! legacy slots do not have any id mappings and have no exits
       * RETURN 
       */
      if ( isLegacy )
         return childDeps.iterator();
      
      // Next add any exit deps
      String finder = slot.getFinderName();
      handler = getDependencyHandler(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      if (StringUtils.isNotBlank(finder))
      {
         finder = SLOT_CONTENT_FINDER_PREFIX+finder;
         PSDependency childDep = handler.getDependency(tok, finder);
         if (childDep != null)
         {
            childDeps.add(childDep);
         }
      }
      
      // Dont forget the idTypes...
      childDeps.addAll(PSIdTypeDependencyHandler.getIdTypeDependencies(tok, 
            dep, this));
      return childDeps.iterator();
   }
 
   // see base class
   public PSApplicationIDTypes getIdTypes(PSSecurityToken tok, PSDependency dep)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");

      PSApplicationIDTypes idTypes = new PSApplicationIDTypes(dep);
      IPSTemplateSlot slot = findSlotByDependencyID(dep.getDependencyId());
      
      if (slot == null)
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not find a slot with id: " + dep.getDependencyId());
      
      if ( isLegacySlot(slot))
         return idTypes;
      
      
      // add any finder params that are id-mapped
      Map<String, String> paramMap = slot.getFinderArguments();
      List mappings = new ArrayList();
      // check each param for idtypes
      Iterator entries = paramMap.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry)entries.next();            
         
         // convert to PSParam to leverage existing transformer code
         Iterator params = PSDeployComponentUtils.convertToParams(
            entry).iterator();
         while (params.hasNext())
         {
            PSParam param = (PSParam)params.next();
            PSAppTransformer.checkParam(mappings, param, null);
         } 
      }
      
      idTypes.addMappings(SLOT_FINDER_ARGS,
         IPSDeployConstants.ID_TYPE_ELEMENT_SLOT_FINDER_PARAMS,
            mappings.iterator());

      return idTypes;
   }

   
   // see base class
   // Load all the templates and return dependencies
   @Override
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      init();
      List<PSDependency> deps = new ArrayList<PSDependency>();
      List<IPSTemplateSlot> slots = m_assemblyHelper.getSlots();
      PSDependency dep = null;
      for (IPSTemplateSlot slot : slots)
      {
         dep = createDeployableElement(m_def, String.valueOf(slot.getGUID()
               .longValue()), slot.getName());
         deps.add(dep);
      }
      return deps.iterator();
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


      // pack the data into the files
      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();
      IPSTemplateSlot slot  = findSlotByDependencyID(dep.getDependencyId());
      PSDependencyFile f = getDepFileFromSlot(slot);
      files.add(f);
      return files.iterator();
   }

   /**
    * Creates a dependency file from a given dependency data object.
    * @param slot the slot, never <code>null</code>
    * @return The dependency file object, it will never be <code>null</code>.
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   private PSDependencyFile getDepFileFromSlot(IPSTemplateSlot slot)
      throws PSDeployException
   {
      if (slot == null)
         throw new IllegalArgumentException("depData may not be null");
      String str;
      try
      {
         str = slot.toXML();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for Template:"
                     + slot.getName());
      }
      
      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(IPSDeployConstants.XML_HDR_STR + str));
   }
   
   /**
    * Method to figure what the id for this dependency is:
    * if a mapping exists , use that as the target id.
    * If a mapping does not exist, use the source id as mapping 
    * If a mapping exists but has not been installed yet, still use the source
    * id as the mapping
    * @param dep the dependency that is being installed  never <code>null</code>
    * @param ctx the import context never <code>null</code>
    * @return the dependency id that exists on the target system
    * @throws PSDeployException
    */
   private String findTargetId(PSDependency dep, PSImportCtx ctx)
         throws PSDeployException
   {
      String tgtId = dep.getDependencyId();
      PSIdMapping tgtMap = getIdMapping(ctx, dep);
      
      if ( tgtMap == null)
         return tgtId;
      if ( !StringUtils.isBlank(tgtMap.getTargetId()))
      {
         IPSTemplateSlot s = findSlotByDependencyID(tgtId);
         tgtId = tgtMap.getTargetId();
         if (s != null && s.isSystemSlot())
         {
            tgtId = dep.getDependencyId();    
            tgtMap.setTarget(tgtId, tgtMap.getTargetName());
         }
      }
      return tgtId;
   }

   // see base class
   @Override
   public void installDependencyFiles(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
           throws PSDeployException, PSAssemblyException, PSNotFoundException {
      Iterator files = getSlotDependecyFilesFromArchive(archive, dep);
      PSDependencyFile depFile = (PSDependencyFile) files.next();
      String tgtId = findTargetId(dep, ctx);
      PSTemplateSlot slot = (PSTemplateSlot) findSlotByDependencyID(tgtId);
            
      boolean isNew = (slot == null) ? true : false;
      Integer ver = null;
      HashMap<PSTemplateTypeSlotAssociation, Integer> bVer = 
         new HashMap<PSTemplateTypeSlotAssociation, Integer>();
      if (!isNew)
      {
         IPSGuid slotGuid = slot.getGUID();
         slot = (PSTemplateSlot) m_assemblySvc.loadSlotModifiable(slotGuid);
          
         // deserialize on the existing template
         ver = slot.getVersion();
         slot.setVersion(null);
         PSTemplateTypeSlotAssociation assoc[] = slot.getSlotTypeAssociations();
         for (PSTemplateTypeSlotAssociation a : assoc)
         {
            PSPair<IPSGuid, IPSGuid> p = new PSPair<IPSGuid, IPSGuid>(
                  new PSGuid(PSTypeEnum.NODEDEF, a.getContentTypeId()),
                  new PSGuid(PSTypeEnum.TEMPLATE, a.getTemplateId()));
            slot.removeSlotAssociation(p);
         }
           
         saveSlot(slot, ver);
         slot = (PSTemplateSlot) m_assemblySvc.loadSlotModifiable(slotGuid);

         // deserialize on the existing template
         ver = slot.getVersion();
         slot.setVersion(null);
      }
      
      slot = (PSTemplateSlot)generateSlotFromFile(archive, depFile, slot);
      //restore versions, before doing transforms, else the primary key
      // will be different
      PSTemplateTypeSlotAssociation assoc[] = slot
            .getSlotTypeAssociations();
      for (PSTemplateTypeSlotAssociation a : assoc)
      {
         if (bVer.get(a) != null)
            a.setVersion(bVer.get(a));
      }
      doTransforms(tok, archive, dep, ctx, slot);
      saveSlot(slot, ver);
      //    add transaction log
      addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.SLOT, isNew);
   }
   
   
   /**
    * TROLL thru the object and restore the versions of child-lings ;).
    * 
    * @param s
    * @param ver the version of template
    * @throws PSDeployException
    */
   private void saveSlot(IPSTemplateSlot s, Integer ver)
         throws PSDeployException
   {
      // nullify and set it to the passed version of the template, can be null
      ((PSTemplateSlot) s).setVersion(null);
      ((PSTemplateSlot) s).setVersion(ver);

      try
      {
         m_assemblySvc.saveSlot(s);
      }
      catch (Exception e1)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not save or update the slot:" + s.getName() + "\n"
                     + e1.getLocalizedMessage());
      }
   }
   
   
   /**
    * This is the first pass at transforming slot, template and contenttype ids.
    * It assumes all the elements for association are present and does the 
    * transforms
    * @param dep the dependency never <code>null</code>
    * @param ctx import context never <code>null</code>
    * @param slot template slot never <code>null</code>
    * @throws PSDeployException
    */
   private void transformElementIds(PSDependency dep,
         PSImportCtx ctx, IPSTemplateSlot slot) throws PSDeployException
   {
      if (slot == null)
         throw new IllegalArgumentException("Slot Definition may not be null");
      
      if (dep == null)
         throw new IllegalArgumentException("dep cannot be null");
   
      if (ctx == null)
         throw new IllegalArgumentException("ctx cannot be null");

      PSIdMapping clMapping = getIdMapping(ctx, dep);
      if (clMapping != null)
      {
         if (!slot.isSystemSlot())
         {
            PSDependencyUtils.transformElementId(slot,
                  dep, ctx, clMapping);
         }
         PSTemplateTypeSlotAssociation assoc[] = ((PSTemplateSlot) slot)
               .getSlotTypeAssociations();
         removeAllAssociationsOnSlot(slot);
         int slotId = slot.getGUID().getUUID();
         for (PSTemplateTypeSlotAssociation a : assoc)
         {
            String ctId = String.valueOf(a.getContentTypeId());
            a.setSlotId(slotId);
            PSIdMapping ctMap = getIdMapping(ctx, ctId,
                  PSCEDependencyHandler.DEPENDENCY_TYPE);
            if (ctMap != null)
               ctId = ctMap.getTargetId();
            a.setContentTypeId(Long.parseLong(ctId));

            PSIdMapping tmpMap = null;
            String tmpId = String.valueOf(a.getTemplateId());
            try
            {
               tmpMap = getIdMapping(ctx, String.valueOf(a.getTemplateId()),
                     PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
            }
            catch (PSDeployException dex)
            {
               if (dex.getErrorCode() == IPSDeploymentErrors.MISSING_ID_MAPPING)
               {
                  tmpMap = getIdMapping(ctx, String.valueOf(a.getTemplateId()),
                        PSVariantDefDependencyHandler.DEPENDENCY_TYPE);
               }
            }
            if (tmpMap != null)
               tmpId = tmpMap.getTargetId();
            a.setTemplateId(Long.parseLong(tmpId));
         }
         addSlotAssociations(slot, Arrays.asList(assoc));
      }
   }
   
   
   /**
    * Remove any invalid associations and warn. This can happen when the slot
    * exists but either the template or contenttype does not exist.
    * Done as follows:
    * 1. Go thru the list of associations and add to a list only the valid ones
    * 2. On the slot remove **all** the associations
    * 3. Add back only the valid associations
    * This will leave the slot in a valid state.
    * @param tok the security token, never <code>null</code>
    * @param slot the template slot never <code>null</code>
    * @throws PSDeployException
    * @throws PSDeployException
    */
   private void removeInvalidAssociations(PSSecurityToken tok,
         IPSTemplateSlot slot) throws PSDeployException, PSNotFoundException {
      if (slot == null)
         throw new IllegalArgumentException(
               "Slot Definition cannot be null for idtype mapping");

      PSTemplateTypeSlotAssociation assoc[] = ((PSTemplateSlot) slot)
            .getSlotTypeAssociations();
      PSDependencyHandler ceHandler = 
         getDependencyHandler(PSCEDependencyHandler.DEPENDENCY_TYPE);
      PSDependencyHandler tmpHandler = 
         getDependencyHandler(PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
      
      //Filter the association list by picking up only the valid ones
      List<PSTemplateTypeSlotAssociation> aList = Arrays.asList(assoc);
      Iterator<PSTemplateTypeSlotAssociation> it = aList.iterator();
      List<PSTemplateTypeSlotAssociation> addList = 
         new ArrayList<PSTemplateTypeSlotAssociation>();
      while (it.hasNext())
      {
         PSTemplateTypeSlotAssociation a = it.next();
         String ctId = String.valueOf(a.getContentTypeId());
         if (!ceHandler.doesDependencyExist(tok, ctId))
         {
            m_log
              .warn("Removing Slot <==>Template/ContentType relationship{Slot:"
                        + slot.getGUID()
                        + ",ContentType:"
                        + ctId
                        + "}, because ContentType is not found.");
            continue;
         }
         
         String tmpId = String.valueOf(a.getTemplateId());
         if (!tmpHandler.doesDependencyExist(tok, tmpId))
         {
            m_log
               .warn("Removing Slot <==>Template/ContentType relationship{Slot:"
                        + slot.getGUID()
                        + ",Template:"
                        + tmpId
                        + "}, because Template is not found.");
            continue;
         }
         addList.add(a);
      }
      // remove all the associations and return the slot with only valid assocs.
      removeAllAssociationsOnSlot(slot);
      addSlotAssociations(slot, addList);
   }

   /**
    * Add the supplied slot associations to the slot
    * @param slot the slot to add to, never <code>null</code>
    * @param aList the list of associations to add, never <code>null</code>
    * may be empty
    */
   private void addSlotAssociations(IPSTemplateSlot slot,
         Collection<PSTemplateTypeSlotAssociation> aList)
   {
      if (slot == null)
         throw new IllegalArgumentException("Slot Definition may not be null");
      Iterator<PSTemplateTypeSlotAssociation> it = aList.iterator();
      while (it.hasNext())
      {
         ((PSTemplateSlot)slot).addSlotTypeAssociation(it.next());
      }
   }

   /** Removes all the slot associations
    * @param slot never <code>null</code>
    */
   private void removeAllAssociationsOnSlot(IPSTemplateSlot slot)
   {
      if (slot == null)
         throw new IllegalArgumentException("Slot Definition may not be null");
      PSTemplateTypeSlotAssociation assoc[] = ((PSTemplateSlot) slot)
            .getSlotTypeAssociations();
      for (PSTemplateTypeSlotAssociation a : assoc)
      {
         PSPair<IPSGuid, IPSGuid> ctPair = 
            new PSPair<IPSGuid, IPSGuid>
            (new PSGuid(PSTypeEnum.NODEDEF, a.getContentTypeId()),
               new PSGuid(PSTypeEnum.TEMPLATE, a.getTemplateId()));
         ((PSTemplateSlot)slot).removeSlotAssociation(ctPair);
      }
   }
   
   /**
    * Transform any IdTypes and ids in Bindings Remove any 
    * slot<-->Template/ContentType associations if they are not yet existing 
    * on the target system
    * 
    * @param tok the security token, never <code>null</code>
    * @param archive the archive, never <code>null</code>
    * @param dep the dependency
    * @param ctx import context never <code>null</code>
    * @param slot 
    * @throws PSDeployException
    */
   private void doTransforms(PSSecurityToken tok, PSArchiveHandler archive,
         PSDependency dep, PSImportCtx ctx, IPSTemplateSlot slot)
           throws PSDeployException, PSNotFoundException {
      if (slot == null)
         throw new IllegalArgumentException(
               "Slot Definition cannot be null");
      if (ctx == null)
         throw new IllegalArgumentException(
               "ctx cannot be null");
      if (archive == null)
         throw new IllegalArgumentException(
               "archive cannot be null");
      transformElementIds(dep, ctx, slot);
      removeInvalidAssociations(tok, slot);
      transformSlotFinderParams(tok, archive, dep, ctx, slot);
   }

   
   /**
    * Helper method to perform the actual transforms of IDs
    * 
    * @param tok PSSecurity Token may not be <code>null</code>
    * @param archive the archive handler may not be <code>null</code>
    * @param dep the dependency for which the transformation has to happen
    * @param ctx ImportContext may not be <code>null</code>
    * @param slot TemplateSlot in which idtypes are to be transformed
    * @throws PSDeployException
    */
   private void transformSlotFinderParams(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx,
         IPSTemplateSlot slot) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (slot == null)
         throw new IllegalArgumentException("template Slot may not be null");
      
      PSIdMap idMap = ctx.getCurrentIdMap();
      if (idMap != null)
      {
         Map<String, String> paramMap = slot.getFinderArguments();
         if (!paramMap.isEmpty())
         {
            // tranform params using idtypes
            transformIds(paramMap, ctx.getIdTypes(), idMap);
            ((PSTemplateSlot) slot).setFinderArguments(paramMap);
         }
      }
   }
   
   //see base class
   @Override
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (StringUtils.isBlank(id))
         throw new IllegalArgumentException("id may not be null or empty");

      if ( !PSGuid.isValid(PSTypeEnum.SLOT, id) )
         return false;

      IPSTemplateSlot slot = findSlotByDependencyID(id);
      return (slot!=null)? true:false;
   }

   /**
    * Given the serialized data for a TemplateSlot, create such TemplateSlot
    * @param archive the ArchiveHandler to use to retrieve the files from the 
    * archive, may not be <code>null</code> 
    * @param depFile the PSDependencyFile that was retrieved from the archive
    * may not be <code>null</code>
    * @param slot the actual slot may be <code>null</code>
    * @return the actual template
    * @throws PSDeployException
    */
   protected IPSTemplateSlot generateSlotFromFile(
         PSArchiveHandler archive, PSDependencyFile depFile, IPSTemplateSlot slot)
         throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");

      if (depFile == null)
         throw new IllegalArgumentException("depFile may not be null");

      File f = depFile.getFile();
     
      String tmpStr = PSDependencyUtils.getFileContentAsString(
            archive, depFile);
      
      if ( slot == null )
         slot = m_assemblySvc.createSlot();
      try
      {
         slot.fromXML(tmpStr);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "Could not create template from file:" + f.getName());
      }
      return slot;
   }
   /**
    * Return an iterator for dependency files in the archive
    * @param archive The archive handler to retrieve the dependency files from,
    *           may not be <code>null</code>.
    * @param dep The dependency object, may not be <code>null</code>.
    * 
    * @return An iterator one or more <code>PSDependencyFile</code> objects.
    *         It will never be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there is no dependency file in the archive
    *            for the specified dependency object, or any other error occurs.
    */
   protected Iterator getSlotDependecyFilesFromArchive(
         PSArchiveHandler archive, PSDependency dep) throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      Iterator files = archive.getFiles(dep);

      if (!files.hasNext())
      {
         Object[] args =
         {PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_SERVICEGENERATED_XML],
               dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()};
         throw new PSDeployException(
               IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
      return files;
   }

   // see base class
   @Override
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
         throws PSDeployException
   {
      PSDependencyUtils.reserveNewId(dep, idMap, getType());
   }

   // see base class
   @SuppressWarnings("unchecked")
   public void transformIds(Object object, PSApplicationIDTypes idTypes,
         PSIdMap idMap) throws PSDeployException
   {
      if (object == null)
         throw new IllegalArgumentException("object may not be null");

      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");
      
      if (!(object instanceof Map))
         throw new IllegalArgumentException("invalid object type");
      
      Map paramMap = (Map)object;
      // walk id types and perform any transforms
      Iterator resources = idTypes.getResourceList(false);
      while (resources.hasNext())
      {
         String resource = (String)resources.next();
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

               if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_SLOT_FINDER_PARAMS))
               {
                  // transform the params
                  Iterator entries = paramMap.entrySet().iterator();
                  while (entries.hasNext())
                  {
                     // convert to PSParam(s) to leverage existing code
                     List<String> valList = new ArrayList<String>();
                     Map.Entry entry = (Map.Entry)entries.next();
                     List paramList = PSDeployComponentUtils.convertToParams(
                        entry);
                     Iterator params = paramList.iterator();
                     while (params.hasNext())
                     {
                        PSParam param = (PSParam) params.next();
                        
                        // transform
                        PSAppTransformer.transformParam(param, mapping, idMap);
                        valList.add(param.getValue().getValueText());
                     }
                     Object newVal;
                     if (valList.size() > 1)
                        newVal = valList;
                     else
                        newVal = valList.get(0);
                     
                     entry.setValue(newVal);                      
                  }
               }
            }
         }
      } 
      
   }
   
   // see base class
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
   
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = 
      IPSDeployConstants.DEP_OBJECT_TYPE_SLOT_DEF;

   /**
    * Da assembly Service
    */
   private IPSAssemblyService m_assemblySvc = 
                           PSAssemblyServiceLocator.getAssemblyService();
   
   /**
    * logger 
    */
   private static final Logger m_log = LogManager.getLogger(PSSlotDefDependencyHandler.class);

   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();
   
   /**
    * Assembly Helper instance
    */
   private PSAssemblyServiceHelper m_assemblyHelper;
   
   private static final String SLOT_CONTENT_FINDER_PREFIX="Java/global/percussion/slotcontentfinder/";
   
   private static final String SLOT_FINDER_ARGS = "Slot-FinderArguments";
   static
   {
      ms_childTypes.add(PSCEDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSTemplateDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
   }
}
