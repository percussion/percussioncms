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
import com.percussion.extension.PSExtensionRef;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.IPSItemFilterRuleDef;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSContentList;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Class to handle packaging and deploying a ContentList definition.
 * @author vamsinukala
 */

public class PSContentListDefDependencyHandler extends PSDependencyHandler
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

   public PSContentListDefDependencyHandler(PSDependencyDef def,
         PSDependencyMap dependencyMap) 
   {
      super(def, dependencyMap);
   }

   /**
    * Helper method to 
    * 1. find and load all the contentlists
    * 2. generate a map of named contentlists, contentlistbyguid 
    */
   private void init()
   {
      if ( m_publisherHelper == null )
         m_publisherHelper   = new PSPublisherServiceHelper();
   }


   /**
    * Util method to figure out if the ContentList is a legacy one, in which
    * case, there is no generator.  See the other way of accessing the same
    * info:
    * @param e the actual ContentList itself 
    * @return true if legacy
    */
   public boolean isLegacyContentList(IPSCatalogItem e)
   {
      IPSContentList cList = (IPSContentList)e;
      if ( cList == null )
         throw new IllegalArgumentException("ContentList cannot be null");
      String gen = cList.getGenerator();
      return StringUtils.isBlank(gen);
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
      init();
      
      // ContentList ids are GUIDs, make sure the id is a guid.
      IPSContentList cList = findContentListByDependencyID(id);
      PSDependency dep = null;
      if ( cList != null)
         dep = createDependency(m_def, 
               String.valueOf(cList.getGUID().longValue()), cList.getName());
      return dep;
   }

   /**
    * For a legacy content list that has an application on its baggage, pack
    * it's deps
    * @param tok
    * @param dep the contentlist dependency
    * @param cList the actual contentlist
    * @return list of dependencies
    * @throws PSDeployException
    */
   private List<PSDependency> getAppDependenciesForLegacyContentList(
         PSSecurityToken tok, PSDependency dep, IPSContentList cList)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null )
         throw new IllegalArgumentException("dependency may not be null");

      if (cList == null )
         throw new IllegalArgumentException("ContentList may not be null");
     
      List<PSDependency> childDeps = new ArrayList<PSDependency>();
      String url = cList.getUrl();
      if (!StringUtils.isBlank(url))
      {
         PSDependency childDep;

         // get the app child dependency
         String appName = PSDeployComponentUtils.getAppName(url);
         PSDependencyHandler handler = getDependencyHandler(
            PSApplicationDependencyHandler.DEPENDENCY_TYPE);
         childDep = handler.getDependency(tok, appName);
         if (childDep != null)
            childDeps.add(childDep);

         childDeps.addAll(PSIdTypeDependencyHandler.getIdTypeDependencies(tok, 
            dep, this));
         
         return childDeps;
      }
      return childDeps;

   }
   //see base class
   @Override
   @SuppressWarnings("unchecked")
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      init();
      
      PSDependencyHandler handler = null;
      IPSContentList cList = null;
      
      cList = findContentListByDependencyID(dep.getDependencyId());
      Set<PSDependency> childDeps = new HashSet<PSDependency>();
      
      //    Dont forget the stupid idTypes...
      childDeps.addAll(PSIdTypeDependencyHandler.getIdTypeDependencies(tok, 
            dep, this));

      /**
       * STOP HERE, if a legacy content list
       */
      if ( isLegacyContentList(cList))
      {
         List<PSDependency> appList = getAppDependenciesForLegacyContentList(
               tok,  dep, cList);
         childDeps.addAll(appList);
         return childDeps.iterator();
      }
      
      /**
       * package a New Content List deps 
       */
      // Add Expander Exit
      handler = getDependencyHandler(
            PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      PSExtensionRef eRef = m_publisherHelper.getExpanderExtensionRef(
            cList.getExpander());
      PSDependency expExitDep = null;
      if ( eRef != null )
      {
         expExitDep  = handler.getDependency(tok, eRef.toString());
         if ( expExitDep != null && !childDeps.contains(expExitDep) )
            childDeps.add(expExitDep);
      }
      
      // Add Generator Exit
      eRef = m_publisherHelper.getGeneratorExtensionRef(cList.getExpander());
      PSDependency genExitDep = null;
      if ( eRef != null )
      {
         genExitDep  = handler.getDependency(tok, eRef.toString());
         if ( genExitDep != null && !childDeps.contains(genExitDep) )
            childDeps.add(genExitDep);
      }
      
      //Add filter deps
      handler = getDependencyHandler(PSFilterDependencyHandler.DEPENDENCY_TYPE);
      dep = handler.getDependency(tok, 
            String.valueOf(cList.getFilter().getGUID().longValue()));
      childDeps.add(dep);
      return childDeps.iterator();
   }
 
   @SuppressWarnings("unused")
   private Iterator<PSDependency> addFilterDeps(PSSecurityToken tok,
         PSDependency dep, IPSContentList cList) throws PSDeployException
   {
      
      if ( cList == null )
            throw new IllegalArgumentException("contentList may not be null");
      
      IPSItemFilter filter = cList.getFilter();
      Set<PSDependency> childDeps = new HashSet<PSDependency>();
      PSDependencyHandler handler = null;
      // Add Rule's  Exit deps
      handler = getDependencyHandler(
            PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      
      // Add any exits from Item Filters
      Set<IPSItemFilterRuleDef> rules =  filter.getRuleDefs();
      Iterator<IPSItemFilterRuleDef> it = rules.iterator(); 
      while(it.hasNext())
      {
         IPSItemFilterRuleDef rule = it.next();
         try
         {
             PSExtensionRef eRef = 
                PSPublisherServiceHelper.getItemFilterRuleExtensionRef(rule
                  .getRuleName());
             PSDependency ruleDep  = handler.getDependency(tok,
                   eRef.toString());
             if ( ruleDep != null && !childDeps.contains(ruleDep) )
                childDeps.add(ruleDep);
         }
         catch (PSFilterException e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
                  "While creating the Filter dependency, " +
                  "a FilterException occurred: " + e.getLocalizedMessage() );
         }
      }
      
      return childDeps.iterator();
   }
   
   /**
    * From the contentList map, return all the contentList names
    * @return iterator on a set of names
    * @throws PSDeployException 
    */
   @SuppressWarnings("unchecked")
   public Iterator getContentListNames() throws PSDeployException
   {
      init();
      return m_publisherHelper.getNamedContentListMap().keySet().iterator();
   }

   
   // see base class
   // Load all the content lists and return dependencies
   @Override
   @SuppressWarnings("unused")
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
         throws PSDeployException
   {
      init();   
      List<PSDependency> deps = new ArrayList<PSDependency>();
      PSDependency dep;
      IterableMap namedCList = m_publisherHelper.getNamedContentListMap();

      MapIterator it = namedCList.mapIterator();
      while (it.hasNext()) {
         String key = (String)it.next();
        IPSContentList cList = (IPSContentList)namedCList.get(key);
        dep = createDeployableElement(m_def, ""
              + cList.getGUID().longValue(), cList.getName());
        deps.add(dep);
      }
      return deps.iterator();
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
      if ( m_publisherHelper == null )
         init();

      // pack the data into the files
      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();
      IterableMap namedCList = m_publisherHelper.getNamedContentListMap();
      IPSContentList cList = (IPSContentList) namedCList.get(dep
            .getDisplayName());
      files.add(getDepFileFromContentList(cList));
      return files.iterator();
   }

   /**
    * Creates a dependency file from a given dependency data object.
    * @param cList the content list never <code>null</code>
    * @return The dependency file object, it will never be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected PSDependencyFile getDepFileFromContentList(IPSContentList cList)
      throws PSDeployException
   {
      if (cList == null)
         throw new IllegalArgumentException("depData may not be null");
      String str;
      try
      {
         str = cList.toXML();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for Template:"
                     + cList.getName());
      }
      
      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(str));
   }
   
   /**
    * TROLL thru the object and restore the versions of child-lings ;).
    * @param cList the actual content list never <code>null</code>
    * @param ver the version of content list
    * @throws PSDeployException
    */
   private void saveContentList(IPSContentList cList, Integer ver)
         throws PSDeployException
   {
      // nullify and set it to the passed version of the template, can be null
      ((PSContentList) cList).setVersion(null);
      ((PSContentList) cList).setVersion(ver);
      try
      {
         List<IPSContentList> lists = new ArrayList<IPSContentList>();
         lists.add(cList);
         m_publisherSvc.saveContentLists(lists);
      }
      catch (Exception e1)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not save or update the content list:" + cList.getName() +
               "\n" + e1.getLocalizedMessage());
      }
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
      
      if ( m_publisherHelper == null )
         init();
      PSIdMapping clMapping = getIdMapping(ctx, dep);
      IPSContentList cList = null;
      
      // assume the content list has been installed, find its mapping and load
      if (clMapping != null && clMapping.getTargetId() != null)
         cList = findContentListByDependencyID(clMapping.getTargetId());
      else
         cList = findContentListByDependencyID(dep.getDependencyId());    
      
      boolean isNew = (cList == null) ? true : false;
      Integer ver = null;

      if (!isNew)
      {
         cList = m_publisherSvc.loadContentListModifiable(cList.getGUID());
         
         // deserialize on the existing content list
         ver = ((PSContentList) cList).getVersion();
         ((PSContentList) cList).setVersion(null);
      }     
      // retrieve data, followed by its child data if any
      PSDependencyFile depFile = 
         (PSDependencyFile) getContentListDependecyFilesFromArchive(archive,
               dep).next();
      cList = generateContentListFromFile(archive, depFile, cList); 
      doTransforms(dep, ctx, cList, isNew);
      saveContentList(cList, ver);
      
      // add transaction log
      addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.CONTENT_LIST,
            isNew);
   }

   /**
    * Deserialize the contentlist from the dependency file 
    * 
    * @param archive the ArchiveHandler to use to retrieve the files from the 
    * archive, may not be <code>null</code> 
    * @param depFile the PSDependencyFile that was retrieved from the archive
    * may not be <code>null</code>
    * @param cList the actual content list may be <code>null</code>, if null,
    * create a new content list and deserialize on it, else use the given 
    * contentlist from persistence
    * @return the content list
    * @throws PSDeployException
    */
   protected IPSContentList generateContentListFromFile(
         PSArchiveHandler archive, PSDependencyFile depFile,
         IPSContentList cList) throws PSDeployException
   {      
      String tmpStr = PSDependencyUtils.getFileContentAsString(
            archive, depFile);
      if ( cList == null )
         cList = new PSContentList();
      try
      {
         cList.fromXML(tmpStr);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "Could not deserialize the ContentList");
      }
      return cList;
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
   @SuppressWarnings("unchecked")
   protected Iterator getContentListDependecyFilesFromArchive(
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
   
   /**
    * Utility method to find the content list by a given dependency id
    * @param depId the id
    * @return <code>null</code> if content list is not found else get the
    * content list
    */
   private IPSContentList findContentListByDependencyID(String depId)
   {
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency ID may not be null or empty");
      
      IPSContentList cList = null;
      
      List<IPSContentList> cLists = m_publisherSvc.findAllContentLists("");
      for (IPSContentList contentList : cLists)
      {
         if (String.valueOf(contentList.getGUID().longValue()).equals(depId))
         {
            cList = contentList;
            break;
         }
      }
      
      return cList;
   }

   /**
    * get ID Types for the legacy content list
    * @param tok the security token never <code>null</code>
    * @param dep the content list dependency never <code>null</code>
    * @param cList the actual ContentList may not be <code>null</code>
    * @return the ApplicationID Types for this dependency
    */
   @SuppressWarnings("unchecked")
   private PSApplicationIDTypes getIdTypesForLegacyContentList(
         PSSecurityToken tok, PSDependency dep, IPSContentList cList)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (cList == null )
         throw new IllegalArgumentException("contentList may not be null");
      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");
      
         
      PSApplicationIDTypes idTypes = new PSApplicationIDTypes(dep);
      String url = cList.getUrl();
      if (url != null)
      {
         // parse params
         Map paramMap = PSDeployComponentUtils.parseParams(url, null);
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
         
         idTypes.addMappings(dep.getDisplayName(),
            IPSDeployConstants.ID_TYPE_ELEMENT_URL_PARAMS,
               mappings.iterator());
      }
      
      return idTypes;
   }

   // see base class
   @SuppressWarnings("unchecked")
   public PSApplicationIDTypes getIdTypes(PSSecurityToken tok, PSDependency dep)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");
      IPSContentList cList = findContentListByDependencyID(
            dep.getDependencyId());
      if ( cList == null )
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "Could not locate the content list for idTyping");
      if ( isLegacyContentList(cList) )
         return getIdTypesForLegacyContentList(tok, dep, cList);
      
      PSApplicationIDTypes idTypes = new PSApplicationIDTypes(dep);
            
      // ADD ANY EXPANDER PARAMS THAT ARE ID-TYPED
      Map<String, String> paramMap = cList.getExpanderParams();
      List<String> mappings = new ArrayList<String>();
      // check each param for idtypes
      Iterator<Map.Entry<String, String>> entries =
         paramMap.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry<String, String> entry = entries.next();            
         
         // convert to PSParam to leverage existing transformer code
         Iterator params = PSDeployComponentUtils.convertToParams(
            entry).iterator();
         while (params.hasNext())
         {
            PSParam param = (PSParam)params.next();
            PSAppTransformer.checkParam(mappings, param, null);
         } 
      } 
      idTypes.addMappings(EXPANDER_ARGS,
         IPSDeployConstants.ID_TYPE_ELEMENT_CONTENTLIST_EXPANDER_PARAMS,
            mappings.iterator());

      // Add any generator params
      paramMap.clear();
      paramMap = null;
      mappings.clear();
      mappings = null;
      
      paramMap = cList.getGeneratorParams();
      mappings = new ArrayList<String>();
      // check each param for idtypes
      entries = paramMap.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry<String, String> entry = entries.next();            
         
         // convert to PSParam to leverage existing transformer code
         Iterator params = PSDeployComponentUtils.convertToParams(
            entry).iterator();
         while (params.hasNext())
         {
            PSParam param = (PSParam)params.next();
            PSAppTransformer.checkParam(mappings, param, null);
         } 
      } 
      idTypes.addMappings(GENERATOR_ARGS,
         IPSDeployConstants.ID_TYPE_ELEMENT_CONTENTLIST_GENERATOR_PARAMS,
            mappings.iterator());
      
      // Add any filter params
      paramMap.clear();
      paramMap = null;
      mappings.clear();
      mappings = null;
      IPSItemFilter f = cList.getFilter();
      try
      {
         if (f != null)
         {
            // ADD ANY EXPANDER PARAMS THAT ARE ID-TYPED
            Set<IPSItemFilterRuleDef> ruleDefSet = f.getRuleDefs();
            Iterator<IPSItemFilterRuleDef> iter = ruleDefSet.iterator();
            while (iter.hasNext())
            {
               IPSItemFilterRuleDef ruleDef = iter.next();
               paramMap = ruleDef.getParams();

               mappings = new ArrayList<String>();
               // check each param for idtypes
               entries = paramMap.entrySet().iterator();
               while (entries.hasNext())
               {
                  Map.Entry<String, String> entry = entries.next();

                  // convert to PSParam to leverage existing transformer code
                  Iterator params = PSDeployComponentUtils.convertToParams(
                        entry).iterator();
                  while (params.hasNext())
                  {
                     PSParam param = (PSParam) params.next();
                     PSAppTransformer.checkParam(mappings, param, null);
                  }
               }
               idTypes.addMappings(""
                     + PSFilterDefDependencyHandler.RULEDEF_ARGS + ":"
                     + ruleDef.getRuleName(),
                     IPSDeployConstants.ID_TYPE_ELEMENT_RULEDEF_PARAMS,
                     mappings.iterator());
            }
         }
      }
      catch (PSFilterException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "While processing id types for contentlist encountered a"
                     + "a filter exception");
      }

      return idTypes;
   }

   
   // see base class
   // Handles the url params in the case of legacy content lists
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
      {
         throw new IllegalArgumentException("invalid object type");
      }
      
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
                  IPSDeployConstants.ID_TYPE_ELEMENT_CONTENTLIST_EXPANDER_PARAMS) ||
                  element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_CONTENTLIST_GENERATOR_PARAMS))
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
               else if (element.equals(
                     IPSDeployConstants.ID_TYPE_ELEMENT_URL_PARAMS))
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
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (StringUtils.isBlank(id))
         throw new IllegalArgumentException("id may not be null or empty");

      if ( !PSGuid.isValid(PSTypeEnum.CONTENT_LIST, id) )
         return false;

      IPSContentList c = findContentListByDependencyID(id);
      return (c != null) ? true : false;
   }

   // see base class
   @Override
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
         throws PSDeployException
   {
      PSDependencyUtils.reserveNewId(dep, idMap, getType());
   }


   /**
    * Perform the transforms on the contentlist and its expander params and 
    * generator params
    * @param dep the dependency never <code>null</code>  
    * @param ctx import context never <code>null</code>
    * @param cList the contentlist never <code>null</code>
    * @param isNew if <code>true</code> the element does not yet exist on the 
    * target system
    * @throws PSDeployException
    */
   private void doTransforms(PSDependency dep, PSImportCtx ctx,
         IPSContentList cList, boolean isNew)
         throws PSDeployException
   {
      if ( cList == null )
         throw new IllegalArgumentException(
               "Content List cannot be null for idtype mapping");
      
      // Make sure you have no unique index : IX_NAME violation
      if (isNew)
         cList.setName(getUniqueElementName(PSTypeEnum.CONTENT_LIST, cList
               .getName()));

      PSIdMapping clMapping = getIdMapping(ctx, dep);
      if (clMapping != null && clMapping.getTargetId() != null)
      {
         IPSGuid g = new PSGuid(PSTypeEnum.CONTENT_LIST, 
               clMapping.getTargetId());
         cList.setGUID(g);
      }
      
      // Make sure the idtypes on the url are mapped for legacy ContentList
      // NewContentLists have instead GENERATOR/EXPANDER same CRAP
      // either way
      if ( isLegacyContentList(cList))
         transformIdsInURL(cList, ctx);
      else
         transformIdsInArgs(
               transformIdsInArgs(cList, ctx, ARGS_TYPE_GENERATOR), ctx,
               ARGS_TYPE_EXPANDER);
      return;
   }

   
   /**
    * Transform any ids in GeneratorParams or ExpanderParams. Performs
    * transformations on either generator ARGS or EXPANDER_ARGS
    * @param cList the actual content list, never <code>null</code>
    * @param ctx import context never <code>null</code>
    * @param type must be valid type from 
    *    {ARGS_TYPE_EXPANDER,ARGS_TYPE_GENERATOR}
    * @return the contentlist with transformed ids
    * @throws PSDeployException
    */
   private IPSContentList transformIdsInArgs(IPSContentList cList,
         PSImportCtx ctx, int type) throws PSDeployException
   {
      PSIdMap idMap = ctx.getCurrentIdMap();
      
      // nothing to xform
      if ( idMap == null )
         return cList;
      
      Map<String, String> transformedMap = null;
      
      if ( type == ARGS_TYPE_GENERATOR )
         transformedMap = cList.getGeneratorParams();
      else if ( type == ARGS_TYPE_EXPANDER )
         transformedMap = cList.getExpanderParams();
      
      Map<String, String> srcMap = PSDependencyUtils.cloneMap(transformedMap);
      if (!transformedMap.isEmpty())
      {
         //tranform params using idtypes
         transformIds(transformedMap, ctx.getIdTypes(), idMap);
         Set<String> keys = transformedMap.keySet();
         Iterator<String> keyIt = keys.iterator();
         while( keyIt.hasNext() )
         {
            String key = keyIt.next();
            String transformedVal = transformedMap.get(key);
            String srcVal = srcMap.get(key);
            if ( !StringUtils.isBlank(transformedVal) && 
                  !StringUtils.isBlank(srcVal) &&
                  transformedVal.compareTo(srcVal) != 0 )
            {
               if ( type == ARGS_TYPE_GENERATOR )
               {
                  cList.removeGeneratorParam(key);
                  cList.addGeneratorParam(key, transformedVal);
               }
               else if ( type == ARGS_TYPE_EXPANDER )
               {
                  cList.removeExpanderParam(key);
                  cList.addExpanderParam(key, transformedVal);
               }
            }
         }
      }
      return cList;
   }

   /**
    * Performs id mapping transforms for the url of a given content list.
    * 
    * @param cList The content list, assumed not <code>null</code>.
    * @param ctx The import context, assumed not <code>null</code>.
    * 
    * @return The content list with modified url.
    * @throws PSDeployException if an error occurs during id transformation.
    */
   @SuppressWarnings("unchecked")
   private IPSContentList transformIdsInURL(IPSContentList cList,
         PSImportCtx ctx) throws PSDeployException
   {      
      PSIdMap idMap = ctx.getCurrentIdMap();
      String url = cList.getUrl();
      StringBuffer base = new StringBuffer();
      Map params = PSDeployComponentUtils.parseParams(url, base);

      if (!params.isEmpty())
      {
         // tranform params using idtypes
         transformIds(params, ctx.getIdTypes(), idMap);

         // build new url if necessary and reset
         url = PSUrlUtils.createUrl(base.toString(), 
            PSDeployComponentUtils.convertToEntries(params), null);
         cList.setUrl(url);
      }    
      return cList;
   }
   
   /**
    * Content list name has to be unique
    * @param type
    * @param currentName
    * @return a unique content list name
    * @throws IllegalArgumentException
    * @throws PSDeployException
    */
   @SuppressWarnings("unchecked")
   public String getUniqueElementName(PSTypeEnum type, String currentName)
         throws IllegalArgumentException, PSDeployException
   {
      init();
      String uniqueName = currentName;
      Set names = m_publisherHelper.getNamedContentListMap().keySet();
      int count = 1; 
      while ( names.contains(uniqueName) == true )
         uniqueName = currentName + "_" + count++;
      return uniqueName;
   }
      
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "ContentListDef";

   /**
    * The publisher Service
    */
   private IPSPublisherService m_publisherSvc = 
      PSPublisherServiceLocator.getPublisherService();
   
   /**
    * The Publisher Service Helper
    */
   private PSPublisherServiceHelper m_publisherHelper = null;

   
   private static final String EXPANDER_ARGS  = "ExpanderParams";
   private static final String GENERATOR_ARGS = "GeneratorParams";
   
   private static final int ARGS_TYPE_EXPANDER  = 1;
   private static final int ARGS_TYPE_GENERATOR = 2;
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();  
   
   static
   {
      ms_childTypes.add(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
   }
}
