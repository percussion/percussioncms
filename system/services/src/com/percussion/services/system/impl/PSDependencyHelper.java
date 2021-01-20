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
package com.percussion.services.system.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.server.PSDependencyManager;
import com.percussion.deploy.server.PSDeploymentHandler;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.server.PSApplicationSummary;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.error.PSException;
import com.percussion.i18n.PSLocale;
import com.percussion.search.IPSExecutableSearch;
import com.percussion.search.PSExecutableSearchFactory;
import com.percussion.search.PSSearchException;
import com.percussion.search.PSWSSearchResponse;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.services.system.data.PSDependent;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

/**
 * Provides utility methods for locating dependencies and converting between
 * guid types and MSM dependency types.
 */
public class PSDependencyHelper
{
   /**
    * Construct a dependency helper.
    */
   public PSDependencyHelper()
   {
      PSDeploymentHandler depHandler = PSDeploymentHandler.getInstance();
      if (depHandler == null)
         throw new IllegalStateException(
            "Deployment Handler must be initialized");
      m_depMgr = depHandler.getDependencyManager();

      // todo: restore once we need to support apps via MSM
//      m_guidConverters.put(PSTypeEnum.LEGACY_APPLICATION, 
//         new PSAppGuidConverter());
      m_guidConverters.put(PSTypeEnum.LOCALE, 
         new PSLocaleGuidConverter());
      m_guidConverters.put(PSTypeEnum.DISPLAY_FORMAT, 
         new PSDisplayFormatGuidConverter());
   }
   
   /**
    * Enable dependency caching to improve performance.  This will enable the 
    * MSM dependency cache.
    */
   public void enableCache()
   {
      m_depMgr.setIsDependencyCacheEnabled(true);
   }

   /**
    * Clear the dependency cache, must be called when finished using an instance
    * of this class if {@link #enableCache()} was called.
    */
   public void disableCache()
   {
      m_depMgr.setIsDependencyCacheEnabled(false);
   }

   
   /**
    * Find all dependencies of the specified design object.
    *  
    * @param guid The guid of the design object, may not be <code>null</code>.
    * 
    * @return The list of dependents, never <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if there are any MSM errors. 
    * @throws PSAssemblyException If there is an error using the assembly
    * service
    * @throws PSException If a search fails.
    * @throws PSFilterException 
    */
   @SuppressWarnings(value={"unchecked"})
   public List<PSDependent> findDependents(IPSGuid guid) 
      throws PSDeployException, PSAssemblyException, 
      PSException, PSFilterException
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      List<PSDependent> dependents;

      // use a set to keep results unique
      Set<PSDependent> dependentSet = new HashSet<PSDependent>();
      
      // check for deps not handled by MSM
      dependentSet.addAll(checkContentDependencies(guid)); 
      
      // now check for MSM dependencies - todo: restore once optimized
      if (false) // allow reference to code
         dependentSet.addAll(checkMSMDependencies(guid));
      
      dependents = new ArrayList<PSDependent>(dependentSet);
      
      return dependents;
   }

   /**
    * Find all objects dependent on a set of two or more design objects.
    *  
    * @param guids The guids of the design objects, may not be 
    * <code>null</code>, and must contain at least 2 entries.
    * 
    * @return The list of dependents, never <code>null</code>, may be empty.
    * 
    * @throws PSException If a search fails.
    */
   public List<PSDependent> findDependents(IPSGuid[] guids) 
      throws PSException
   {
      if (guids == null || guids.length < 2)
         throw new IllegalArgumentException(
            "guids may not be null and must contain at least 2 values");
      
      for (IPSGuid guid : guids)
      {
         if (guid == null)
         {
            throw new IllegalArgumentException(
               "guids may not contain null values");
         }
      }
      
      List<PSDependent> results = new ArrayList<PSDependent>();
      
      // currently only supports 2 guids, a content type and a template
      if (guids.length != 2)
         return results;
      
      IPSGuid ctypeGuid = null;
      IPSGuid templateGuid = null;
      for (IPSGuid guid : guids)
      {
         short type = guid.getType();
         if (type == PSTypeEnum.NODEDEF.getOrdinal())
            ctypeGuid = guid;
         else if (type == PSTypeEnum.TEMPLATE.getOrdinal())
            templateGuid = guid;
      }
      
      if (ctypeGuid == null || templateGuid == null)
         return results;
      
      // find relationships using the template
      List<PSRelationship> rels = findDependentRelationships(templateGuid);
      if (rels.isEmpty())
         return results;
      
      // find matching items based on dependent and content type
      List<PSSearchField> flds = new ArrayList<PSSearchField>();
      flds.add(getContentTypeSearchField(ctypeGuid));
      
      Set<String> ids = new HashSet<String>();
      for (PSRelationship rel : rels)
      {
         ids.add(String.valueOf(rel.getDependent().getId()));
      }
      PSSearchField fld = new PSSearchField(IPSHtmlParameters.SYS_CONTENTID, 
         "ci", "i", PSSearchField.TYPE_NUMBER, null);
      fld.setFieldValues(PSSearchField.OP_IN, new ArrayList<String>(ids));
      flds.add(fld);
      
      searchItemDependencies(results, flds.iterator());
      
      return results;
   }

   /**
    * Uses MSM to check for dependencies (which are really ancestors in MSM).
    * 
    * @param guid the guid to check, assumed not <code>null</code>.
    * 
    * @return A list of dependents, never <code>null</code>, may be empty.
    *  
    * @throws PSDeployException If a deployment error occurs.
    * @throws PSException if a server error occurs.
    */
   @SuppressWarnings(value={"unchecked"})
   private Collection<PSDependent> checkMSMDependencies(IPSGuid guid) 
      throws PSDeployException, PSException, PSFilterException
   {
      List<PSDependent> dependents = new ArrayList<PSDependent>();
      
      // TODO: This is not the best place for this. It should be in the
      // MSM checking instead 
      // check item filter dependencies
      if (guid.getType() == PSTypeEnum.ITEM_FILTER.getOrdinal())
      {
         dependents.addAll(checkItemFilterDependencies(guid));
      }
      
      PSSecurityToken tok = PSRequest.getContextForRequest().getSecurityToken();
      
      String depId = String.valueOf(guid.longValue());
      boolean foundMatch = false;
      for (String depType : m_depMgr.getDeploymentType(PSTypeEnum.valueOf(
         guid.getType())))
      {
         // convert id if necessary
         IPSGuidConverter guidConverter;
         guidConverter = m_guidConverters.get(PSTypeEnum.valueOf(
            guid.getType()));
         if (guidConverter != null)
         {
            depId = guidConverter.convertToName(tok, guid.longValue());
            if (depId == null)
            {
               throw new IllegalArgumentException(
                  "Could not locate object identified by id: " + 
                  guid.toString());
            }  
         }
         PSDependency dep = m_depMgr.findDependency(tok, depType, depId);
         if (dep == null)
         {
            continue;
         }
         
         foundMatch = true;
         
         Iterator deps = m_depMgr.getAncestors(tok, dep);
         while (deps.hasNext())
         {
            PSDependency child = (PSDependency) deps.next();
            PSTypeEnum childType = m_depMgr.getGuidType(child.getObjectType());
            
            try
            {
               String strChildId = child.getDependencyId();
               // convert to id if necessary
               guidConverter = m_guidConverters.get(childType);
               if (guidConverter != null)
               {
                  strChildId = guidConverter.convertToID(tok, strChildId);
                  if (strChildId == null)
                  {
                     throw new RuntimeException(
                        "Could not locate object identified by dependency: " + 
                        child.toString());
                  }                       
               }
               long childId = Long.parseLong(strChildId);
               PSDesignGuid childGuid = new PSDesignGuid(childType, childId);

               /*
                * Don't add a dependent that has the same guid we are check 
                * dependents for (this is possible if we checked a "def" and
                * got the "package" type back, or in the rare case of a circular
                * dependency 
                */
               if (childGuid.equals(new PSDesignGuid(guid)))
                  continue;
               
               PSDependent dependent = new PSDependent();
               dependent.setId(childGuid.getValue());
               dependent.setType(childType.toString());
               dependents.add(dependent);
            }
            catch (NumberFormatException e)
            {
               // not a referencable design object, skip
            }
         }
      }
      
      if (!foundMatch)
      {
         throw new IllegalArgumentException(
            "Could not locate object identified by id: " + guid.toString());         
      }
      
      return dependents;
   }

   /**
    * For certain types, MSM is not able to provide the required dependency 
    * information, so it is obtained by this method in those cases.
    *  
    * @param guid The guid for which dependents are to be checked, assumed not 
    * <code>null</code>.
    *  
    * @return The list of dependents, possibly empty, that were discovered by
    * this method, never <code>null</code>.
    * @throws PSAssemblyException If there is an error converting a global
    * template guid to its name.
    * @throws PSException If a search fails.
    * @throws PSFilterException 
    */
   private Collection<PSDependent> checkContentDependencies(IPSGuid guid) 
      throws PSAssemblyException, PSException
   {
      List<PSDependent> deps = new ArrayList<PSDependent>();

      // check for content item and folder ancestors
      // do folder
      Iterator<PSSearchField> searchFields = null; 
      searchFields = getFolderSearchFields(guid);
      if (searchFields != null)
      {
         searchItemDependencies(deps, searchFields);
      }
      
      // do item, can skip if added folder dep 
      if (deps.isEmpty())
      {
         searchFields = getItemSearchFields(guid);
         if (searchFields != null)
         {
            searchItemDependencies(deps, searchFields);
         }
      }


      // check for relationship ancestors, can skip if already added a dep
      if (deps.isEmpty())
      {
         if (!findDependentRelationships(guid).isEmpty())
         {
            addAnonymousItemDep(deps);
         }
      }
      
      return deps;
   }

   /**
    * Search for item dependencies using the specified search fields, and add 
    * dependencies for those found to the supplied set.
    * 
    * @param deps The set to which the dependencies are added, assumed not 
    * <code>null</code>.
    * @param searchFields The search fields to use, assumed not 
    * <code>null</code>.
    * 
    * @throws PSSearchException if there is an error performing the search
    * @throws PSCmsException If there are any other errors.
    */
   private void searchItemDependencies(List<PSDependent> deps, 
      Iterator<PSSearchField> searchFields) 
      throws PSCmsException, PSSearchException
   {
      PSRequest req = PSRequest.getContextForRequest();
      List<String> names = new ArrayList<String>();
      names.add(IPSHtmlParameters.SYS_CONTENTID);      
      PSSearch searchObj = new PSSearch("findDeps");
      searchObj.setMaximumNumber(1);         

      while (searchFields.hasNext())
         searchObj.getFieldContainer().add(searchFields.next());
      
      // do search
      IPSExecutableSearch exsearch = 
         PSExecutableSearchFactory.createExecutableSearch(req, names, 
            searchObj);      
      PSWSSearchResponse searchResp = exsearch.executeSearch();
      if (!searchResp.getRowList().isEmpty())
      {
         addAnonymousItemDep(deps);
      }
   }

   /**
    * Adds an item dependency to the supplied list that does not specify an id.
    * 
    * @param deps The list to which the dependency is added, assumed not 
    * <code>null</code>.
    */
   private void addAnonymousItemDep(List<PSDependent> deps)
   {
      // add single "anonymous" dependency
      PSDependent itemDep = new PSDependent();
      itemDep.setId(-1);
      itemDep.setType(PSTypeEnum.LEGACY_CONTENT.name());
      deps.add(itemDep);
   }

   /**
    * Find all relationships that depend upon the object specified by the 
    * supplied guid.
    * 
    * @param guid The guid to check, assumed not <code>null</code>.
    * 
    * @return The list of relationships, never <code>null</code>, may be empty.
    *  
    * @throws PSException if there are any errors.
    */
   private List<PSRelationship> findDependentRelationships(IPSGuid guid) 
      throws PSException
   {
      PSRelationshipFilter filter = getRelationshipFilter(guid);
      if (filter != null)
      {
         IPSRelationshipService relservice = 
            PSRelationshipServiceLocator.getRelationshipService();
         return relservice.findByFilter(filter);
      }
      
      return new ArrayList<PSRelationship>();
   }

   /**
    * Check what the dependents are for the passed item filter. 
    * @param guid the item filter guid, assumed a reference to an item filter
    * and not <code>null</code>
    * @return a collection of dependents, may be empty but not <code>null</code>
    * @throws PSFilterException 
    */
   private Collection<PSDependent> checkItemFilterDependencies(IPSGuid guid)
   throws PSFilterException
   {
      Collection<PSDependent> rval = new ArrayList<PSDependent>();
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      
      IPSItemFilter filter = fsvc.loadFilter(guid);
      
      List<IPSItemFilter> filters = fsvc.findAllFilters();
      for (IPSItemFilter f : filters)
      {
         if (filter.equals(f.getParentFilter()))
         {
            PSDependent dep = new PSDependent();
            dep.setId(f.getGUID().longValue());
            dep.setType(PSTypeEnum.ITEM_FILTER.name());
            rval.add(dep);
         }
      }
      IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();
      List<IPSContentList> lists = pub.findAllContentLists("%");
      for (IPSContentList list : lists)
      {
         if (filter.equals(list.getFilter()))
         {
            PSDependent dep = new PSDependent();
            dep.setId(list.getGUID().longValue());
            dep.setType(PSTypeEnum.CONTENT_LIST.name());
            rval.add(dep);
         }
      }
      
      return rval;
   }

   /**
    * Get the filter to use to search on relationship dependencies.
    * 
    * @param guid The guid to use for the search, assumed not <code>null</code>.
    *  
    * @return The filter, or <code>null</code> if no search should be performed.
    */
   private PSRelationshipFilter getRelationshipFilter(IPSGuid guid)
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setCategory(
         PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
      filter.setCommunityFiltering(false);
      
      PSTypeEnum type = PSTypeEnum.valueOf(guid.getType());
      // check variant id
      if (PSTypeEnum.TEMPLATE.equals(type))
      {
         filter.setProperty(IPSHtmlParameters.SYS_VARIANTID, String.valueOf(
            guid.getUUID()));
      }
      // check slotid
      else if (PSTypeEnum.SLOT.equals(type))
      {
         filter.setProperty(IPSHtmlParameters.SYS_SLOTID, String.valueOf(
            guid.getUUID()));
      }
      // check site id
      else if (PSTypeEnum.SITE.equals(type))
      {
         filter.setProperty(IPSHtmlParameters.SYS_SITEID, String.valueOf(
            guid.getUUID()));
      }
      else
      {
         // no search necessary
         return null;
      }
      
      return filter;
   }

   /**
    * Get search fields to search for dependent items.
    * 
    * @param guid The guid to check, assumed not <code>null</code>.
    * 
    * @return The iterator of one or more fields, or <code>null</code> if the
    * supplied guid is not a type that can be dependent on an item, in which 
    * case no search should be performed.
    */
   private Iterator<PSSearchField> getItemSearchFields(IPSGuid guid)
   {
      List<PSSearchField> flds = new ArrayList<PSSearchField>();
      
      // add criteria based on guid type
      PSTypeEnum type = PSTypeEnum.valueOf(guid.getType());
      // locale
      if (PSTypeEnum.LOCALE.equals(type))
      {
         PSSearchField localeFld = getLocaleSearchField(guid);
         flds.add(localeFld);
      }      
      // community
      else if (PSTypeEnum.COMMUNITY_DEF.equals(type))
      {
         PSSearchField commFld = getCommunitySearchField(guid);
         flds.add(commFld);
      }
      // content type
      else if (PSTypeEnum.NODEDEF.equals(type))
      {
         PSSearchField fldct = getContentTypeSearchField(guid);
         flds.add(fldct);
      }
      // workflow
      else if (PSTypeEnum.WORKFLOW.equals(type))
      {
         PSSearchField fldwf = new PSSearchField(
            IPSHtmlParameters.SYS_WORKFLOWID, "wf", "w",
            PSSearchField.TYPE_NUMBER, null);
         fldwf.setFieldValue(PSSearchField.OP_EQUALS, String.valueOf(
            guid.getUUID()));
         flds.add(fldwf);         
      }
      else
      {
         // don't bother searching
         return null;
      }
      
      return flds.iterator();      
   }

   /**
    * Get search fields to search for dependent folders.
    * 
    * @param guid The guid to check, assumed not <code>null</code>.
    * 
    * @return The iterator of one or more fields, or <code>null</code> if the
    * supplied guid is not a type that can be dependent on an folder, in which 
    * case no search should be performed.
    * 
    * @throws PSAssemblyException If there is an error obtaining the name of a
    * global template guid. 
    */
   private Iterator<PSSearchField> getFolderSearchFields(IPSGuid guid) 
      throws PSAssemblyException
   {
      List<PSSearchField> flds = new ArrayList<PSSearchField>();
      PSSearchField fldct = new PSSearchField(
         IPSHtmlParameters.SYS_CONTENTTYPEID, "ct", "o", 
         PSSearchField.TYPE_NUMBER, null);
      fldct.setFieldValue(PSSearchField.OP_EQUALS, 
         String.valueOf(PSFolder.FOLDER_CONTENT_TYPE_ID));
      flds.add(fldct);
      PSSearchField fldtyp = new PSSearchField("sys_objecttype", "ot", "o", 
         PSSearchField.TYPE_NUMBER, null);
      fldtyp.setFieldValue(PSSearchField.OP_EQUALS, "2");
      flds.add(fldtyp);
      
      // now add criteria based on guid type
      PSTypeEnum type = PSTypeEnum.valueOf(guid.getType());
      // locale
      if (PSTypeEnum.LOCALE.equals(type))
      {
         PSSearchField localeFld = getLocaleSearchField(guid);
         flds.add(localeFld);
      }
      // community
      else if (PSTypeEnum.COMMUNITY_DEF.equals(type))
      {
         PSSearchField commFld = getCommunitySearchField(guid);
         flds.add(commFld);
      }      
      else
      {
         // handle criteria represented as properties
         String propName = null;
         String propVal = null;      
         // display format
         if (PSTypeEnum.DISPLAY_FORMAT.equals(type))
         {
            propName = PSFolder.PROPERTY_DISPLAYFORMATID;
            propVal = String.valueOf(guid.getUUID());
         }
         // global template - lookup
         else if (PSTypeEnum.TEMPLATE.equals(type))
         {
            // see if it's a global template, and if so, search on its name
            IPSAssemblyService assemblySvc = 
               PSAssemblyServiceLocator.getAssemblyService();
            IPSAssemblyTemplate template = assemblySvc.loadTemplate(guid, 
               false);
            if (IPSAssemblyTemplate.OutputFormat.Global.equals(
               template.getOutputFormat()))
            {
               propName = PSFolder.PROPERTY_GLOBALTEMPLATE;
               propVal = template.getName();
            }
         }
         
         if (propName != null)
         {
            PSSearchField fldp = new PSSearchField(
               PSServerFolderProcessor.PROP_NAME, "propName", "n",
               PSSearchField.TYPE_TEXT, null);
            fldp.setFieldValue(PSSearchField.OP_EQUALS, propName);
            flds.add(fldp);
            PSSearchField fldv = new PSSearchField(
               PSServerFolderProcessor.PROP_VALUE, "propVal", "v",
               PSSearchField.TYPE_TEXT, null);
            fldv.setFieldValue(PSSearchField.OP_EQUALS, propVal);
            flds.add(fldv);            
         }
         else
         {
            // don't bother searching
            return null;
         }
      }

      return flds.iterator();
   }


   /**
    * Get a community search field using the supplied guid.
    * 
    * @param guid The guid, assumed not <code>null</code> and to represent a
    * community.
    * 
    * @return The search field, never <code>null</code>.
    */
   private PSSearchField getCommunitySearchField(IPSGuid guid)
   {
      PSSearchField commFld = new PSSearchField(
         IPSHtmlParameters.SYS_COMMUNITYID, "community", "c",
         PSSearchField.TYPE_NUMBER, null);
      commFld.setFieldValue(PSSearchField.OP_EQUALS, String.valueOf(
         guid.getUUID()));
      return commFld;
   }
   
   /**
    * Get a locale search field using the supplied guid.
    * 
    * @param guid The guid, assumed not <code>null</code> and to represent a
    * locale.
    * 
    * @return The search field, never <code>null</code>.
    */
   private PSSearchField getLocaleSearchField(IPSGuid guid)
   {
      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
      PSLocale locale = objMgr.loadLocale(guid.getUUID());  // safe cast
      if (locale == null)
         throw new IllegalArgumentException(
            "Could not locate object identified by id: " + guid.toString());

      PSSearchField localeFld = new PSSearchField(IPSHtmlParameters.SYS_LANG, 
         "locale", "l", PSSearchField.TYPE_TEXT, null);
      localeFld.setFieldValue(PSSearchField.OP_EQUALS, 
         locale.getLanguageString());
      return localeFld;
   }
   

   /**
    * Get a content type search field using the supplied guid.
    * 
    * @param guid The guid, assumed not <code>null</code> and to represent a
    * content type.
    * 
    * @return The search field, never <code>null</code>.
    */
   private PSSearchField getContentTypeSearchField(IPSGuid guid)
   {
      PSSearchField fldct = new PSSearchField(
         IPSHtmlParameters.SYS_CONTENTTYPEID, "ct", "o",
         PSSearchField.TYPE_NUMBER, null);
      fldct.setFieldValue(PSSearchField.OP_EQUALS, String.valueOf(
         guid.getUUID()));
      return fldct;
   }   

   /**
    * The dependency manager to use to obtain dependency information from MSM,
    * not <code>null</code> after consruction.
    */
   private PSDependencyManager m_depMgr;
   
   /**
    * Simple interface for guid converters
    */
   private interface IPSGuidConverter
   {
      /**
       * Convert the supplied id to a name.
       * 
       * @param tok The security token to use, assumed not <code>null</code>.
       * @param id The id to convert.
       * 
       * @return The name, may be <code>null</code> if a matching object is not
       * found.
       * 
       * @throws PSException If there are any errors.
       */
      public String convertToName(PSSecurityToken tok, long id)
         throws PSException;
      
      /**
       * Convert the supplied name to an id.
       * 
       * @param tok The security token to use, assumed not <code>null</code>.
       * @param name The name to convert, assumed not <code>null</code> or 
       * empty.
       * 
       * @return The id as a string, or <code>null</code> if no matching object
       * is found.
       * 
       * @throws PSException If there are any errors.
       */
      public String convertToID(PSSecurityToken tok, String name) 
         throws PSException;
   }
   
   /**
    * Guid converter for applications - not currently used, but may be if MSM
    * is every optimized
    */
   @SuppressWarnings("unused")
   private class PSAppGuidConverter implements IPSGuidConverter
   {
      // see IPSGuidConverter
      public String convertToName(PSSecurityToken tok, long id)
      {
         String appName = null;
         
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         PSApplicationSummary[] appsums = os.getApplicationSummaryObjects(tok, 
            true);
         
         for (PSApplicationSummary summary : appsums)
         {
            if (id == summary.getId())
            {
               appName = summary.getName();
               break;
            }
         }
         
         return appName;

      }

      // see IPSGuidConverter      
      public String convertToID(PSSecurityToken tok, String name)
      {
         String appId = null;
         
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         PSApplicationSummary[] appsums = os.getApplicationSummaryObjects(tok, 
            true);
         
         for (PSApplicationSummary summary : appsums)
         {
            if (name.equals(summary.getName()))
            {
               appId = String.valueOf(summary.getId());
               break;
            }
         }
         
         return appId;
      }
   }
   
   /**
    * Guid converter for PSLocale objects 
    */   
   private class PSLocaleGuidConverter implements IPSGuidConverter
   {
      // see IPSGuidConverter   
      @SuppressWarnings("unused")
      public String convertToName(PSSecurityToken tok, long id) 
         throws PSException
      {
         if (tok == null);
         String name = null;
         IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
         PSLocale locale = objMgr.loadLocale((int)id);  // safe cast
         if (locale != null)
         {
            name = locale.getLanguageString();
         }
         
         return name;
      }
      
      // see IPSGuidConverter   
      @SuppressWarnings("unused")
      public String convertToID(PSSecurityToken tok, String name) 
         throws PSException
      {
         if (tok == null);
         String id = null;
         IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
         PSLocale locale = objMgr.findLocaleByLanguageString(name);
         if (locale != null)
         {
            id = String.valueOf(locale.getLocaleId());
         }
         
         return id;
      }
      
   }
   
   /**
    * Guid converter for PSDisplayFormat objects 
    */
   private class PSDisplayFormatGuidConverter implements IPSGuidConverter
   {
      // see IPSGuidConverter    
      public String convertToName(PSSecurityToken tok, long id) 
         throws PSException
      {
         String name = null;
         PSComponentProcessorProxy proc = new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_SERVERLOCAL, 
            new PSRequest(tok));
         PSKey[] key = new PSKey[]
         {
            PSDisplayFormat.createKey(new String[] {String.valueOf(id)})
         };
         
         Element[] elements = proc.load(PSDisplayFormat.getComponentType(
            PSDisplayFormat.class), key);
         if (elements.length > 0)
            name = (new PSDisplayFormat(elements[0])).getInternalName();
         
         return name;
      }
      
      // see IPSGuidConverter    
      public String convertToID(PSSecurityToken tok, String name) 
         throws PSException
      {
         String id = null;
         PSComponentProcessorProxy proc = new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_SERVERLOCAL, 
            new PSRequest(tok));
         
         Element[] elements = proc.load(PSDisplayFormat.getComponentType(
            PSDisplayFormat.class), null);
         for (int i = 0; i < elements.length; i++)
         {
            PSDisplayFormat df = new PSDisplayFormat(elements[i]);
            if (df.getInternalName().equals(name))
            {
               id = df.getLocator().getPart();
               break;
            }
         }
         
         return id;
      }
   }
   
   /**
    * Map of guid converters by type, never <code>null</code> or modified after 
    * construction.
    */
   private Map<PSTypeEnum, IPSGuidConverter> m_guidConverters = 
      new HashMap<PSTypeEnum, IPSGuidConverter>();
}

