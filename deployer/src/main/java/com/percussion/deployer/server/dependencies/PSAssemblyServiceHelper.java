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
import com.percussion.extension.IPSExtension;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A helper class for the new templates, slots, bindings, contentlists etc to 
 * use
 * @author vamsinukala
 *
 */
public class PSAssemblyServiceHelper
{
   
   /**
    * CTOR
    */
   public PSAssemblyServiceHelper()
   {
      if ( m_namedTemplatesMap != null )
      {
         m_namedTemplatesMap.clear();
         m_namedTemplatesMap = null;
      }
      if ( m_guidTemplatesMap != null )
      {
         m_guidTemplatesMap.clear();
         m_guidTemplatesMap = null;
      }
      if ( m_legacyTemplatesMap != null )
      {
         m_legacyTemplatesMap.clear();
         m_legacyTemplatesMap = null;
      }
      if ( m_templateCTMap != null )
      {
         m_templateCTMap.clear();
         m_templateCTMap = null;
      }
      if ( m_templateSlotMap != null )
      {
         m_templateSlotMap.clear();
         m_templateSlotMap = null;
      }
      if ( m_slotContentTypeMap != null )
      {
         m_slotContentTypeMap.clear();
         m_slotContentTypeMap = null;
      }
      if ( m_slotTemplatesMap != null )
      {
         m_slotTemplatesMap.clear();
         m_slotTemplatesMap = null;
      }
      if ( m_slots != null )
      {
         m_slots.clear();
         m_slots = null;
      }
         
      m_namedTemplatesMap = new HashMap<String, IPSAssemblyTemplate>();
      m_guidTemplatesMap  = new HashMap<IPSGuid, IPSAssemblyTemplate>();
      m_legacyTemplatesMap= new HashMap<String, IPSAssemblyTemplate>();
      m_templateSlotMap   = new HashMap<String, Set<IPSTemplateSlot>>();
      m_slotContentTypeMap= new HashMap<IPSGuid, Set<IPSNodeDefinition>>();
      m_slotTemplatesMap  = new HashMap<IPSGuid, Set<IPSAssemblyTemplate>>();  
      m_slots             = new ArrayList<IPSTemplateSlot>();
   }

   
   /**
    * Helper method to 
    * 1. find and load all the **NEW** templates: List<IPSAssemblyTemplate>
    * 2. generate a map m_templatesMap
    * @param doLegacyTmps if <code>true</code> build legacyTemplatesMap else
    *        build the new templates map
    * @throws PSDeployException 
    */
   private void doCatalogTemplatesMap(boolean doLegacyTmps) throws PSDeployException
   {
      Set<IPSAssemblyTemplate> templates = null;
      /*
       * Get all the templates Assembler does not distinguish between old 
       * variants and the new tempatles
       */
      try
      {
         templates = m_assemblySvc.findAllTemplates();
      }
      catch (PSAssemblyException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "Assembly exception occurred while cataloging Templates");
      }
      
      Iterator<IPSAssemblyTemplate> it = templates.iterator();
      
      while ( it.hasNext() )
      {
         IPSAssemblyTemplate tmp = (IPSAssemblyTemplate)it.next();
         // separate the legacy(variants) from new templates
         if ( doLegacyTmps == false)
         {
            if ( tmp.getAssembler().compareTo(IPSExtension.LEGACY_ASSEMBLER) == 0 )
               continue;
            m_namedTemplatesMap.put(tmp.getName(), tmp);
            m_guidTemplatesMap.put(tmp.getGUID(), tmp);
         }
         else if ( doLegacyTmps == true )
         {
            if ( !tmp.getAssembler().equals(IPSExtension.LEGACY_ASSEMBLER) )
               continue;
            m_legacyTemplatesMap.put(tmp.getName(), tmp);
         }
      } 
   }
   
   /**
    * Utility Method to init LegacyTemplatesMap
    * @throws PSDeployException
    */
   private void catalogLegacyTemplatesMap() throws PSDeployException
   {
      doCatalogTemplatesMap(true);
   }
   
   private void catalogTemplatesMap() throws PSDeployException
   {
      doCatalogTemplatesMap(false);
   }
  
   
   /** Get slots associated with a template
    * @param name the template name 
    * @return a list of all the slots associated with this template(name)
    */
   public Set<IPSTemplateSlot> getSlotsByTemplate(String name)
   {
      return m_templateSlotMap.get(name);
   }
   /**
    * Get all the ContentType GUIDs that reference this template, 
    * <b>dynamically.</b>  
    * @param t the template definition never <code>null</code>
    * @return the content type guids associated with the template
    */
   public List<IPSGuid> getContentTypesByTemplate(IPSAssemblyTemplate t)
   {
      List<IPSGuid> cTypes = new ArrayList<IPSGuid>();
      try
      {
         List<IPSNodeDefinition> nodeDefs = m_contentMgr
               .findNodeDefinitionsByTemplate(t.getGUID());
         Iterator<IPSNodeDefinition> it = nodeDefs.iterator();
         while (it.hasNext())
         {
            IPSNodeDefinition def = it.next();
            cTypes.add(def.getGUID());
         }
      }
      catch (RepositoryException e)
      {
         e.printStackTrace();
      }
      return cTypes;
   }

   /**
    * Catalog templates which match a pattern in ASSEMBLYURL.
    * Pattern such as "../appname/%" etc
    * @param pattern never <code>null</code>
    * @return the list of templates that satisfy the pattern in assemblyurl. May
    * return <code>null</code>
    */
   public List<IPSAssemblyTemplate> findTemplatesByAssemblyURL(String pattern)
   {
      if ( StringUtils.isBlank(pattern) )
         throw new IllegalArgumentException("pattern may not be null");
      List<IPSAssemblyTemplate> tmps = null;
      try
      {
         tmps = m_assemblySvc.findTemplatesByAssemblyUrl(pattern, false);
      }
      catch (PSAssemblyException e)
      { // dont care
      }
      
      return tmps;
   }
   
   /**
    * A helper method for catalogTemplateSlotsAssociations
    * @param tmpMap
    */
   private void doCatalogTemplateSlotAssociations(
         HashMap<String, IPSAssemblyTemplate> tmpMap)
   {
      Set<String> nameSet = tmpMap.keySet();
      Iterator<String> it = nameSet.iterator();
      while( it.hasNext() )
      {
         String tmpName = it.next();
         IPSAssemblyTemplate tmp = tmpMap.get(tmpName);
         m_templateSlotMap.put(tmp.getName(), tmp.getSlots());
      }
   }
   /**
    * Create a map of list of all slots(their GUIDs) associated with a template
    * TemplateName <--> Set of SLOTS
    *
    */
   private void catalogTemplateSlotsAssociations()
   {
      // only set here
      doCatalogTemplateSlotAssociations(m_namedTemplatesMap);
      doCatalogTemplateSlotAssociations(m_legacyTemplatesMap);
   }

   /**
    * Utility method to return all <templateName, ListOfSlots> Map
    * @return <templateName, ListOfSlots>
    */
   public HashMap<String, Set<IPSTemplateSlot>> getTemplateSlotsMap()
   {
      m_templateSlotMap.clear();
      catalogTemplateSlotsAssociations();
      return m_templateSlotMap;
   }
   
   /**
    * Utility method to return all <slotGUID, ListOfTemplates> Map
    * @return <slotGUID, ListOfTemplates>
    */
   public HashMap<IPSGuid, Set<IPSAssemblyTemplate>>  getSlotTemplatesMap()
   {
      return m_slotTemplatesMap;
   }

 
   /**
    * Utility method to return all <slotGUID, ListOfContentTypeDefs> Map
    * @return <slotGUID, ListOfContentTypeDefs> Map
    */
   public HashMap<IPSGuid, Set<IPSNodeDefinition>>   getSlotContentTypesMap()
   {
      return m_slotContentTypeMap;
   }

   
   /**
    * Utility method to get TemplatesMap
    * @return the hashmap of templates
    * @throws PSDeployException 
    */
   public HashMap<String, IPSAssemblyTemplate> getNamedTemplatesMap()
         throws PSDeployException
   {
      if (m_namedTemplatesMap != null )
      {
         m_namedTemplatesMap.clear();
         m_namedTemplatesMap = new HashMap<String, IPSAssemblyTemplate>();
      }
      
      if ( m_guidTemplatesMap != null )
      {
         m_guidTemplatesMap.clear();
         m_guidTemplatesMap  = new HashMap<IPSGuid, IPSAssemblyTemplate>();
      }
      catalogTemplatesMap();
      return m_namedTemplatesMap;
   }

   /**
    * Utility method to get the template <==> ContentType map
    * @return this association template <==> ContentType map
    */
   public HashMap<String, List<IPSNodeDefinition>> getTemplateCTMap()
   {
      return m_templateCTMap;
   }


   /**
    * Convenience method to return all VARIANTS to be known as Legacry Templates
    * @return a map of <template name, Template>
    * @throws PSDeployException
    */
   public HashMap<String, IPSAssemblyTemplate> getLegacyTemplatesMap()
         throws PSDeployException
   {
      m_legacyTemplatesMap.clear();
      m_legacyTemplatesMap = null;
      m_legacyTemplatesMap = new HashMap<String, IPSAssemblyTemplate>();
      catalogLegacyTemplatesMap();
      return m_legacyTemplatesMap;
   }
   
   @SuppressWarnings("unchecked")
   private void catalogSlots()
   {
      List<IPSCatalogSummary> sumList = null;
      List<IPSTemplateSlot> allSlots = null;
      try
      {
         sumList = m_assemblySvc.getSummaries(PSTypeEnum.SLOT);
         if ( sumList != null && sumList.size() != 0)
         {
            m_slots = new ArrayList<IPSTemplateSlot>();
            Iterator<IPSCatalogSummary> it = sumList.iterator();
            List<IPSGuid> slotGuids = new ArrayList<IPSGuid>();
            while(it.hasNext())
            {
               IPSCatalogSummary slot = it.next();
               slotGuids.add(slot.getGUID());
            }
            allSlots = m_assemblySvc.loadSlots(slotGuids);
            for (IPSTemplateSlot slot : allSlots)
            {
                  m_slots.add(slot);
            }
        }
      }
      catch (PSCatalogException e)
      {
         e.printStackTrace();
      }
      // sort the collection
      Collections.sort(m_slots, new SlotsComparer());
   }
   
   /**
    * method name says it all
    * @return assembly service
    */
   public IPSAssemblyService getAssemblySvc()
   {
      return m_assemblySvc;
   }

   /**
    * clear the list and re-catalog to return all the slots
    * @return List of all slots
    */
   public List<IPSTemplateSlot> getSlots()
   {
      m_slots.clear();
      m_slots = null;
      m_slots  = new ArrayList<IPSTemplateSlot>();
      catalogSlots();
      return m_slots;
   }
   

   /**
    * Da assembly Service
    */
   private static IPSAssemblyService m_assemblySvc = 
                           PSAssemblyServiceLocator.getAssemblyService();
   
   /**
    * ContentManagerService
    */
   private static  IPSContentMgr m_contentMgr = 
                           PSContentMgrLocator.getContentMgr();
   /**
    * Map of a list of all the Content Types associated with a template
    */
   private HashMap<String, List<IPSNodeDefinition>> m_templateCTMap = null;

   /**
    * Map of all the legacy templates name aka VARIANTS,IPSAssemblyTemplate
    */
   private HashMap<String, IPSAssemblyTemplate> m_legacyTemplatesMap = null;

   /**
     * Map of all the assembly templates name,IPSAssemblyTemplate
     */
    private HashMap<String, IPSAssemblyTemplate> m_namedTemplatesMap = null;
    
    /**
     * Map of all the assembly templates name,IPSAssemblyTemplate
     */
    private HashMap<IPSGuid, IPSAssemblyTemplate> m_guidTemplatesMap = null; 
   
   /**
    * Map of all the slots associated to a template
    */
   private HashMap<String, Set<IPSTemplateSlot>> m_templateSlotMap = null;

   /**
    * given a slot guid, find all the templates that refer this slot
    */
   private HashMap<IPSGuid, Set<IPSAssemblyTemplate>> m_slotTemplatesMap = null;
   /**
    * Slot <--> Contenttype List
    */
   private HashMap<IPSGuid, Set<IPSNodeDefinition>> m_slotContentTypeMap = null;

   
   /**
    * Listing of TemplateSlots 
    */
   private List<IPSTemplateSlot> m_slots  = null;
}


/** 
 * Utility sorter for list
 * @author vamsinukala
 *
 */
class SlotsComparer implements Comparator { 
   public int compare(Object obj1, Object obj2) 
   { 
     return (int) (Math.abs(((PSTemplateSlot) obj1).getGUID().longValue()) - 
           Math.abs(((PSTemplateSlot) obj1).getGUID().longValue())); 
   } 
}
