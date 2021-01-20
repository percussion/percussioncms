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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.PSSearchMultiProperty;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.util.IPSHtmlParameters;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class to handle packaging and deploying search objects.
 */
public abstract class PSSearchObjectDependencyHandler
   extends PSCmsObjectDependencyHandler
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
    * @throws PSDeployException if any other error occurs.
    */
   public PSSearchObjectDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);
   }

   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (! dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");

      Set<PSDependency> childDeps = new HashSet<PSDependency>();
      PSComponentProcessorProxy proc = getComponentProcessor(tok);
      PSSearch search = loadSearch(proc, dep.getDependencyId());
      if (search == null)
      {
         Object[] args = {dep.getDependencyId(), dep.getObjectTypeName(),
            dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND,
            args);
      }

      // get default display format
      try
      {
         Element[] dfEls = proc.load(PSDisplayFormat.getComponentType(
            PSDisplayFormat.class), new PSKey[] {PSDisplayFormat.createKey(
               new String[] {search.getDisplayFormatId()})});

         if (dfEls.length > 0)
         {
            PSDisplayFormat df = new PSDisplayFormat(dfEls[0]);
            PSDependencyHandler dfHandler = getDependencyHandler(
               PSDisplayFormatDependencyHandler.DEPENDENCY_TYPE);
            PSDependency dfDep = dfHandler.getDependency(tok,
               getIdFromKey(df, df.getInternalName()));
            if (dfDep != null)
               childDeps.add(dfDep);
         }
      }
      catch (PSCmsException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }


      // get app
      String url = search.getUrl();
      if (url != null && url.trim().length() > 0)
      {
         String appName = PSDeployComponentUtils.getAppName(url);
         if (appName != null && appName.trim().length() > 0)
         {
            PSDependencyHandler appHandler = getDependencyHandler(
               PSApplicationDependencyHandler.DEPENDENCY_TYPE);
            PSDependency appDep = appHandler.getDependency(tok, appName);
            if (appDep != null)
            {
               if (appDep.getDependencyType() == PSDependency.TYPE_SHARED)
               {
                  appDep.setIsAssociation(false);
               }
               childDeps.add(appDep);               
            }
         }
      }

      // Acl deps
      if ( getType().compareTo(PSViewDefDependencyHandler.DEPENDENCY_TYPE) == 0)
         addAclDependency(tok, PSTypeEnum.VIEW_DEF, dep, childDeps);
      else
         addAclDependency(tok, PSTypeEnum.SEARCH_DEF, dep, childDeps);
      
      // get system field value dependencies
      childDeps.addAll(getSystemFieldDeps(tok, search));

      // get id type dependencies from url params
      childDeps.addAll(getIdTypeDependencies(tok, dep));

      return childDeps.iterator();
    }

   // see base class
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      try
      {
         List<PSDependency> deps = new ArrayList<PSDependency>();

         PSComponentProcessorProxy proc = getComponentProcessor(tok);
         Element[] elements = proc.load(PSSearch.getComponentType(
            PSSearch.class), null);
         for (int i = 0; i < elements.length; i++)
         {
            PSSearch search = new PSSearch(elements[i]);
            if (isDependentType(search))
            {
               String name = search.getInternalName();
               deps.add(createDependency(m_def, getIdFromKey(search, name),
                  name));
            }
         }

         return deps.iterator();
      }
      catch (PSCmsException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");


      PSDependency dep = null;

      PSSearch search = loadSearch(getComponentProcessor(tok), id);
      if (search != null)
         dep = createDependency(m_def, id, search.getInternalName());

      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Application</li>
    * <li>DisplayFormat</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      try
      {
         // create a dummy object
         PSSearch search = new PSSearch();
         reserveNewId(dep, idMap, search);
      }
      catch (PSCmsException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   // see base class
   @Override
   public Iterator<PSDependencyFile> getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");

      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();

      // load the component
      PSSearch search = loadSearch(getComponentProcessor(tok), dep.getDependencyId());
      if (search != null)
         files.add(createDependencyFile(search));

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

      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");

      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      try
      {
         // restore the file
         Iterator<PSDependencyFile> files = getDependencyFilesFromArchive(archive, dep);
         Element root = getElementFromFile(archive, dep, files.next());

         // restore the object and clone it
         PSSearch srcSearch = new PSSearch(root);
         PSSearch newSearch = (PSSearch)srcSearch.clone();

         // create a list for transaction support
         PSDbComponentCollection dbCompList = new PSDbComponentCollection(
            PSSearch.class);

         // look for existing
         String tgtId = dep.getDependencyId();
         PSComponentProcessorProxy proc = getComponentProcessor(tok);
         PSIdMapping idMapping = getIdMapping(ctx, dep);
         if (idMapping != null)
            tgtId = idMapping.getTargetId();

         PSSearch tgtSearch = loadSearch(proc, tgtId);

         // if it already exists on the target, add it to the list marked for
         // delete so we can "replace" it with the source version
         if (tgtSearch != null)
         {
            tgtSearch.markForDeletion();
            dbCompList.add(tgtSearch);
            
            // keep target version information
            newSearch.setVersion(tgtSearch.getVersion());
         }

         newSearch.setLocator(PSSearch.createKey(new String[]{tgtId}));

         // translate ids in the dependent objects of the new version
         Boolean installNewSearch = true;
         if (ctx.getCurrentIdMap() != null)
         {
            installNewSearch = transformIds(tok, proc, dep, ctx, newSearch);
         }
         
         // add it to the list and save
         if (installNewSearch)
            dbCompList.add(newSearch);
         proc.save(new IPSDbComponent[] {dbCompList});

         // be sure to change id mapping to not new if we have one
         if (idMapping != null)
            idMapping.setIsNewObject(false);

         // add the transaction to the log
         int action = (tgtSearch == null) ?
            PSTransactionSummary.ACTION_CREATED :
            PSTransactionSummary.ACTION_MODIFIED;
         addTransactionLogEntry(dep, ctx, newSearch, action);

      }
      catch (PSCmsException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
       
}

   /**
    * Determines if the supplied search object should be treated as a dependency
    * by the derived handler.
    *
    * @param search The search to check, never <code>null</code>.
    *
    * @return <code>true</code> if the supplied search is to be treated as a
    * dependency of the handler's type, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   protected abstract boolean isDependentType(PSSearch search);

   /**
    * Convert the supplied map to a list of properties
    *
    * @param map The map, assumed not <code>null</code>, and to have
    * non-<code>null</code> <code>String</code> objects for the key and value.
    *
    * @return A list of <code>PSProperty</code> objects, never
    * <code>null</code>, may be empty.
    */
   private List<PSProperty> mapToProps(Map map)
   {
      Iterator props = map.entrySet().iterator();
      List<PSProperty> propList = new ArrayList<PSProperty>();
      while (props.hasNext())
      {
         Map.Entry entry = (Map.Entry)props.next();
         PSProperty prop = new PSProperty((String)entry.getKey());
         prop.setValue(entry.getValue());
         propList.add(prop);
      }

      return propList;
   }

   /**
    * Loads the search object specified by the supplied id.
    *
    * @param proc The processor to use, assumed not <code>null</code>.
    * @param id The id of the search, assumed not <code>null</code> or empty.
    *
    * @return The search, or <code>null</code> if no matching object is found.
    *
    * @throws PSDeployException If there are any errors.
    */
   private PSSearch loadSearch(PSComponentProcessorProxy proc, String id)
      throws PSDeployException
   {
      try
      {
         PSSearch search = null;
         PSKey[] locators = new PSKey[] {PSSearch.createKey(new String[] {id})};
         Element[] elements = proc.load(PSSearch.getComponentType(PSSearch.class),
            locators);
         if (elements.length > 0)
         {
            PSSearch test = new PSSearch(elements[0]);
            if (isDependentType(test))
               search = test;
         }

         return search;
      }
      catch (PSCmsException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * Gets the property containing defined communities, unless the property
    * specifies "all" communities.
    *
    * @param search The search object to get the property from, assumed not
    * <code>null</code>.
    *
    * @return The property, or <code>null</code> if "all" is defined or the
    * property does not exist.
    */
   private PSSearchMultiProperty getCommunityProperty(PSSearch search)
   {
      PSSearchMultiProperty result = null;

      // get community dependencies unless "all" is specified
      if (!search.doesPropertyHaveValue(PSSearch.PROP_COMMUNITY,
         PSSearch.PROP_COMMUNITY_ALL))
      {
         // find the community propery
         Iterator props = search.getProperties();
         while (props.hasNext() && result == null)
         {
            PSSearchMultiProperty prop = (PSSearchMultiProperty)props.next();
            if (PSSearch.PROP_COMMUNITY.equals(prop.getName()))
               result = prop;
         }
      }

      return result;
   }

   /**
    * Gets dependencies specified by search criteria using system fields and
    * literal values.
    *
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param search The search to get the dependencies from, assumed not
    * <code>null</code>.
    *
    * @return A list of dependencies, never <code>null</code>, may be empty.
    *
    * @throws PSDeployException
    */
   private List<PSDependency> getSystemFieldDeps(PSSecurityToken tok,
         PSSearch search) throws PSDeployException
   {
      List<PSDependency> deps = new ArrayList<PSDependency>();
      List<String[]> childList = new ArrayList<String[]>();
      Iterator fields = getSystemDefFields(search, false).entrySet().iterator();
      while (fields.hasNext())
      {
         Map.Entry entry = (Map.Entry)fields.next();
         String type = (String)entry.getKey();
         PSSearchField field = (PSSearchField)entry.getValue();

         Iterator vals = field.getFieldValues().iterator();
         while (vals.hasNext())
         {
            String val = (String)vals.next();
            PSDependencyHandler handler = getDependencyHandler(type);
            PSDependencyDef def = m_map.getDependencyDef(type);
            if (def.supportsParentId())
            {
               // save children for end once all possible parents are found
               childList.add(new String[] {type, val, handler.getParentType()});
            }
            else
            {
               PSDependency dep = handler.getDependency(tok, val);
               if (dep != null)
                  deps.add(dep);
            }
         }
      }

      // now handle children - use first parent with matching type found
      Iterator<String[]> children = childList.iterator();
      while (children.hasNext())
      {
         PSDependency dep = null;
         String[] child = children.next();
         Iterator<PSDependency> parents = deps.iterator();
         while (parents.hasNext() && dep == null)
         {
            PSDependency parent = parents.next();
            if (parent.getObjectType().equals(child[2]))
            {
               PSDependencyHandler handler = getDependencyHandler(child[0]);
               dep = handler.getDependency(tok, child[1],
                  parent.getObjectType(), parent.getDependencyId());
            }
         }

         if (dep != null)
            deps.add(dep);
      }

      return deps;
   }

   /**
    * Transforms the child dependency ids in the supplied search. 
    * (Only Display Formats and System Fields for now.)
    *
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param proc The processor to use, assumed not <code>null</code>
    * @param dep The dependency being installed, assumed not <code>null</code>.
    * @param ctx The context to use to get id mappings, assumed not
    * <code>null</code>.
    * @param search The search to transform, assumed not <code>null</code>.
    *
    * @return <code>true</code> if the child dependency ids were transformed,
    * <code>false</code> if the search's display format is not
    * included in the package, does not exist on the system, and the "Default"
    * search does not exist on the system.
    *
    * @throws PSDeployException if there are any errors
    */
   private Boolean transformIds(
         PSSecurityToken tok, PSComponentProcessorProxy proc, PSDependency dep,
         PSImportCtx ctx, PSSearch search) 
   throws PSDeployException
   {
      // Process the Display Format 
      //    First - Locate expected single child dep of Display Format type
      //    (If it's not there something is wrong - bail out).
      String dfDepType = PSDisplayFormatDependencyHandler.DEPENDENCY_TYPE;
      Iterator<PSDependency> deps = dep.getDependencies(dfDepType);
      PSDependency dfDep = null;
      if (deps.hasNext())
      {
         dfDep = deps.next();         
      }
      else
      {
         Object[] args = {search.getDisplayFormatId(), dfDepType,
            dep.getDependencyId(), dep.getObjectTypeName()};
         throw new PSDeployException(IPSDeploymentErrors.CHILD_DEP_NOT_FOUND,
            args);
      }
      
      //    Second - determine if the Display Format is:
      //            a) in the package being installed (by transforming the ID)
      //            b) on the target system (by loading it)
      Boolean isInPackage = dfDep.isIncluded();
      String tgtId = dfDep.getDependencyId();
      PSIdMapping dfIdMapping = getIdMappingOfAssoc(
            tok, ctx, tgtId, dfDepType); 
      if (dfIdMapping != null)
      {
         tgtId = dfIdMapping.getTargetId();
      }
      
      PSDisplayFormatDefDependencyHandler dfDefDepHandler =
         (PSDisplayFormatDefDependencyHandler) getDependencyHandler(
            PSDisplayFormatDefDependencyHandler.DEPENDENCY_TYPE);
      PSDisplayFormat df = 
         dfDefDepHandler.loadDisplayFormat(proc, tgtId);
      Boolean isOnTargetSystem = (df != null ? true : false);
      
      //    Third - Decide how to handle association:
      //     If in the package, and on the target, use the target version.
      //     If in the package, and not on the target, use the package version.
      //     If not in the package, and is on the target, use the target version.
      //     If not in the package, and is not on the target, use a default.
      //     If no defaults are available, don't install the search.
      int id = -1;
      String strId = "-1";
      if (isInPackage && isOnTargetSystem)
      {
         id = df.getGUID().getUUID();
         strId = String.valueOf(id);
      }
      else if (isInPackage && !isOnTargetSystem)
      {
         strId = dfIdMapping.getTargetId();
      }
      else if (!isInPackage && isOnTargetSystem)
      {
         id = df.getGUID().getUUID();
         strId = String.valueOf(id);
      }
      else if (!isInPackage && !isOnTargetSystem)
      {
         id = getDefaultTargetDisplayFormatId(proc, dfDefDepHandler);
         if (id != -1)
         {
            strId = String.valueOf(id);
         }
         else
         {
            return false;
         }
      }
      // Finally - Set the Display Format ID
      search.setDisplayFormatId(strId);            
      
      // Transform system field values
      transformSystemFieldIds(dep, ctx, search);

      return true;
   }



   
   /**
    * Transforms all literal system field values
    *
    * @param dep The dependency being installed, assumed not <code>null</code>.
    * @param ctx The import context to use for id transforms, assumed not
    * <code>null</code>.
    * @param search The search being transformed, assumed not <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   private void transformSystemFieldIds(PSDependency dep, PSImportCtx ctx,
      PSSearch search) throws PSDeployException
   {
      Map fieldMap = getSystemDefFields(search, true);
      Iterator fields = fieldMap.entrySet().iterator();
      while (fields.hasNext())
      {
         Map.Entry entry = (Map.Entry)fields.next();
         String type = (String)entry.getKey();
         PSSearchField field = (PSSearchField)entry.getValue();
         Iterator vals = field.getFieldValues().iterator();
         // build list of new values to replace current list
         List<String> newVals = new ArrayList<String>();
         while (vals.hasNext())
         {
            String val = (String)vals.next();
            // if this was determined to be a dependency on package, we'll find
            // the child somewhere below
            PSDependencyDef def = m_map.getDependencyDef(type);
            if (def.supportsParentId())
            {
               // first get the parent
               PSDependencyHandler handler = getDependencyHandler(type);
               String parentType = handler.getParentType();

               // try each parent field value till we hit a dependency
               Iterator parentVals = field.getFieldValues().iterator();
               while (parentVals.hasNext())
               {
                  String parentVal = (String)parentVals.next();
                  PSDependency parent = doGetChildDependency(dep, parentVal,
                     parentType);
                  if (parent != null)
                  {
                     PSDependency child = parent.getChildDependency(val, type);
                     if (child != null)
                     {
                        PSIdMapping idMapping = getIdMapping(ctx, child);
                        newVals.add(idMapping.getTargetId());
                        break;
                     }
                  }
               }
            }
            else
            {
               PSDependency child = doGetChildDependency(dep, val, type);
               if (child != null)
               {
                  PSIdMapping idMapping = getIdMapping(ctx, dep);
                  newVals.add(idMapping.getTargetId());
               }
            }
            field.setFieldValues(field.getOperator(), newVals);
         }
      }
   }

   /**
    * Discovers all system def fields used by the supplied search with a numeric
    * value or values, and returns them with their corresponding dependency
    * object type.
    *
    * @param search The search to check, assumed not <code>null</code>.
    * @param forTransform <code>true</code> to return types used in id
    * transformations, <code>false</code> to return types used in dependency
    * discovery.
    *
    * @return A map where the key is the object type of the dependency the
    * field represents as a <code>String</code>, and the value is the
    * <code>PSSearchField</code> object.
    */
   private Map getSystemDefFields(PSSearch search, boolean forTransform)
   {
      Map<String, PSSearchField> values = new HashMap<String, PSSearchField>();

      Iterator fields = search.getFields();
      while (fields.hasNext())
      {
         PSSearchField field = (PSSearchField)fields.next();
         String type;
         if (forTransform)
         {
            type = (String)ms_sysTransformDepFieldTypes.get(
               field.getFieldName());
         }
         else
         {
            type = (String)ms_sysChildDepFieldTypes.get(field.getFieldName());
         }

         if (type == null)
            continue;

         if (field.getFieldType() != PSSearchField.TYPE_NUMBER)
            continue;
         List vals = field.getFieldValues();
         if (!vals.isEmpty())
            values.put(type, field);
      }

      return values;
   }


   /**
    * Searches the target system for the "Default" Display Format.  
    * If such a Display Format is not found, then the first 
    * Display Format found is chosen.
    * 
    * @param proc The processor to use, assumed not <code>null</code>.
    * @param dfHandler The display format dependency handler to use to find
    * display formats on the target system, assumed not <code>null</code>.
    * 
    * @return The id of the Display Format to use as default or -1 
    * if a Display Format was not found.
    * @throws PSDeployException if any errors occur. 
    */
   @SuppressWarnings("unchecked")
   private Integer getDefaultTargetDisplayFormatId(
         PSComponentProcessorProxy proc,
         PSDisplayFormatDefDependencyHandler dfHandler)
   throws PSDeployException
   {

      Integer defDispFmt = -1;
      PSDisplayFormat df = null;
      PSDisplayFormat firstDf = null;
      
      Iterator<PSDisplayFormat> itDf = dfHandler.loadAll(proc);
      while (itDf.hasNext())
      {
         if (firstDf == null)
         {
            // save the first one in case there is no default
            firstDf = itDf.next();
            if (firstDf.getInternalName().equals("Default"))
            {
               // first one is the default
               defDispFmt = firstDf.getGUID().getUUID();
               break;
            }
            
            continue;
         }
         
         df = itDf.next();
         if (df.getInternalName().equals("Default"))
         {
            defDispFmt = df.getGUID().getUUID();
            break;
         }
      }
      
      if (defDispFmt == -1 && firstDf != null)
      {
         // use the first display format found
         defDispFmt = firstDf.getGUID().getUUID();
      }
      
      return defDispFmt;
      
   }
   
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();

   /**
    * Map of system fields and the corresponding child dependency type.  Key is
    * the field name as a <code>String</code>, value is the dependency object
    * type as a <code>String</code>.  Never <code>null</code> or empty.  Used to
    * get child dependencies identified by values of system fields in
    * conditions.
    */
   private static Map<String, String> ms_sysChildDepFieldTypes = 
                                                new HashMap<String, String>();

   /**
    * Map of system fields and the corresponding transform dependency type.  Key
    * is the field name as a <code>String</code>, value is the dependency object
    * type as a <code>String</code>.  Never <code>null</code> or empty.  Used to
    * transform values of system fields in conditions when installing.  Usually
    * the child dependency is discovered as an element, but the id is
    * transformed using the child defintion dependency handler (this is a design
    * flaw that should be resolved at some point).
    */
   private static Map<String, String> ms_sysTransformDepFieldTypes = 
                                                new HashMap<String, String>();

   static
   {
      // initialize child types
      ms_childTypes.add(PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSDisplayFormatDependencyHandler.DEPENDENCY_TYPE);

      // initialize system field types
      ms_sysChildDepFieldTypes.put(IPSHtmlParameters.SYS_COMMUNITYID,
         PSCommunityDependencyHandler.DEPENDENCY_TYPE);
      ms_sysTransformDepFieldTypes.put(IPSHtmlParameters.SYS_COMMUNITYID,
         PSCommunityDependencyHandler.DEPENDENCY_TYPE);

      ms_sysChildDepFieldTypes.put(IPSHtmlParameters.SYS_WORKFLOWID,
         PSWorkflowDependencyHandler.DEPENDENCY_TYPE);
      ms_sysTransformDepFieldTypes.put(IPSHtmlParameters.SYS_WORKFLOWID,
         PSWorkflowDependencyHandler.DEPENDENCY_TYPE);

      ms_sysTransformDepFieldTypes.put(IPSHtmlParameters.SYS_CONTENTSTATEID,
         PSStateDefDependencyHandler.DEPENDENCY_TYPE);

      ms_sysChildDepFieldTypes.put(IPSHtmlParameters.SYS_CONTENTTYPEID,
         PSCEDependencyHandler.DEPENDENCY_TYPE);
      ms_sysTransformDepFieldTypes.put(IPSHtmlParameters.SYS_CONTENTTYPEID,
         PSCEDependencyHandler.DEPENDENCY_TYPE);
   }

}
