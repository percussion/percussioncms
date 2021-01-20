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
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyManager;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.data.PSContentList;
import com.percussion.services.publisher.data.PSEdition;
import com.percussion.services.publisher.data.PSEditionTaskDef;
import com.percussion.services.relationship.data.PSRelationshipConfigName;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.sitemgr.data.PSLocationScheme;
import com.percussion.services.sitemgr.data.PSPublishingContext;
import com.percussion.services.sitemgr.data.PSSite;
import com.percussion.util.PSCollection;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.tools.IPSUtilsConstants;
import com.percussion.utils.types.PSPair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Table;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A util class with ALL static methods to help in new GUID type create/install
 * actions
 * @author vamsinukala
 *
 */
public class PSDependencyUtils
{
   /**
    * Given a guid as a string, get the long value 
    * @param depId may not be <code>null</code> or empty
    * @param depTypeName is the dependencyType name
    * @return the long value of the guid
    * @throws PSDeployException
    */
   public static long getGuidValFromString(String depId, String depTypeName) 
      throws PSDeployException
   {
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency ID may not be null or empty");
      if (depTypeName == null || depTypeName.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency type name may not be null or empty");
      
      long guidval = -1;
      try {
         guidval = Long.parseLong(depId);
      }
      catch ( NumberFormatException ne)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               depTypeName + " was expecting a long value: "+depId );
      }
      return guidval;
   }

   
   /**
    * Given a dependency, get the next possible id for the two cases:
    * this will work only for GUID types
    * <ol>
    *    <li> Legacy Element Type
    *    <li> New Element Type
    *    For GUID types, it is handled by IPSGuidManager.createGuid(...)
    * </ol>
    * @param dep
    * @return get the next id <b>may return</b> <code>null</code>
    */
   public static String getNextId(PSDependency dep)
   {
      PSDependencyManager depMgr = PSDependencyManager.getInstance();
      if ( dep == null )
         throw new IllegalArgumentException("dep may not be null");
      
      if (dep.supportsParentId())
         throw new IllegalArgumentException(
            "tgtParentId may not be null if dep supports parent id");     

      PSTypeEnum type = depMgr.getGuidType(dep.getObjectType());
      if (type == null)
         throw new IllegalArgumentException("Dependency not a GUID type");
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid newGuid = guidMgr.createGuid(type);
      return String.valueOf(newGuid.longValue());
   }
   
   /**
    * A generic way of reserving an ID for any of the GUID types. Note for 
    * legacy types that are now GUID types, GUIDManager handles the generation 
    * of IDs accordingly
    * 
    * @param dep Dependency for which we are trying to reserve an id
    *    never <code>null</code>
    * @param idMap reserve an id in the mapping for later use during install
    *    never <code>null</code>
    * @param depType dependencyTpe ( may not be GUID type ) 
    *    never <code>null</code>
    * @throws PSDeployException
    */
   public static void reserveNewId(PSDependency dep, PSIdMap idMap,
         String depType) throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (depType == null || depType.trim().length() == 0)
         throw new IllegalArgumentException("depType may not be null or empty");
      if (!dep.getObjectType().equals(depType))
         throw new IllegalArgumentException("dep wrong type");
      if (!dep.supportsIDMapping())
         throw new IllegalArgumentException("dep must support id mapping");
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSIdMapping mapping;
      if (dep.supportsParentId())
      {
         mapping = idMap.getMapping(dep.getDependencyId(), dep.getObjectType(),
               dep.getParentId(), dep.getParentType());
      }
      else
      {
         mapping = idMap.getMapping(dep.getDependencyId(), dep.getObjectType());
      }
      if (mapping == null)
      {
         Object[] args =
         {dep.getObjectType(), dep.getDependencyId(), idMap.getSourceServer()};
         throw new PSDeployException(IPSDeploymentErrors.MISSING_ID_MAPPING,
               args);
      }

      // this is a new mapping, but a target id was not chosen in the GUI
      if (mapping.isNewObject() && (mapping.getTargetId() == null))
      {
         String nextId = PSDependencyUtils.getNextId(dep);
         if (nextId == null)
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
                  "NextID generator returned null for " + dep.getDisplayName());
         mapping.setTarget(nextId, dep.getDisplayName());
      }
   }

   /**
    * A util to do a quick copy 
    * @param oldMap
    * @return a cloned map
    */
   // 
   public static Map<String, String> cloneMap(Map<String, String> oldMap)
   {
      Map<String, String> newMap = new HashMap<String, String>();
      Set<String> keys = oldMap.keySet();
      Iterator<String> it = keys.iterator();
      while(it.hasNext())
      {
         String key = it.next();
         String val = oldMap.get(key);
         newMap.put(key, val);
      }
      return newMap;
   }

   /**
    * A util to do a quick copy of a map of PSPair
    * 
    * @param oldMap
    * @return a cloned map
    */
   // 
   @SuppressWarnings("unchecked")
   public static Map<String, PSPair<String, String>> clonePSPairMap(
         Map<String, PSPair<String, String>> oldMap)
   {
      Map<String, PSPair<String, String>> newMap = 
         new HashMap<String, PSPair<String, String>>();
      Set<String> keys = oldMap.keySet();
      Iterator<String> it = keys.iterator();
      while (it.hasNext())
      {
         String key = it.next();
         PSPair<String,String> val = oldMap.get(key);
         PSPair<String, String> newVal = new PSPair(val.getFirst(), val.getSecond());
         newMap.put(key, newVal);
      }
      return newMap;
   }
  
   /**
    * Util method to transform the ID of a guid type, given its mapping and 
    * the dependency.
    * @param o the element suchas IPSAssemblyTemplate if GUID type == TEMPLATE
    * never <code>null</code>
    * @param dep the dependency, never <code>null</code>
    * @param ctx never <code>null</code>
    * @param clMapping the id mapping 
    * @return the IPSCatalogItem never <code>null</code>
    * @throws PSDeployException
    */
   public static Object transformElementId(Object o, PSDependency dep,
         PSImportCtx ctx, PSIdMapping clMapping)
         throws PSDeployException
   {
      PSDependencyManager depMgr = PSDependencyManager.getInstance();
      PSTypeEnum type = depMgr.getGuidType(dep.getObjectType());
      if (type == null)
         throw new IllegalArgumentException("Dependency not a GUID type");
      IPSCatalogItem item = null;
      if ( o instanceof IPSCatalogItem )
         item = (IPSCatalogItem)o;
      PSGuid guid = null;
      long guidval = -1;
      try {
         guidval = Long.parseLong(clMapping.getTargetId());
      }
      catch ( NumberFormatException ne)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               " was expecting a long value: " );
      }
      
      guid = new PSGuid(type, guidval);
      
      item.setGUID(guid);
      return o;
   }

   /**
    * A util method to parse an application url and extract the application name
    * 
    * @param appStr the url string 
    * @return the application name
    * @throws PSDeployException
    */
   public static String getColumnAppName(String appStr) throws PSDeployException
      {
          if (StringUtils.isBlank(appStr))
             throw new IllegalArgumentException("String for application name may not be null or empty");
             
         String appName = PSDeployComponentUtils.getAppName(appStr);
            
         if (appName == null || appName.trim().length() == 0)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not extract any application name");
         }
         return appName;
      }

   
   /**
    * A util method to extract the table name from the annotations based on
    * the type of the element
    * @param type the element type
    * @return the table name if the annotation has a table name else 
    * return the key as string
    */
   public static String getTableName(PSTypeEnum type)
   {
      Annotation[] anns = null;

      if ( type == PSTypeEnum.SLOT )
         anns = PSTemplateSlot.class.getAnnotations();
      else if ( type == PSTypeEnum.TEMPLATE)
         anns = PSAssemblyTemplate.class.getAnnotations();
      else if ( type == PSTypeEnum.ITEM_FILTER )
         anns = PSItemFilter.class.getAnnotations();
      else if ( type == PSTypeEnum.CONTENT_LIST )
         anns = PSContentList.class.getAnnotations();
      else if ( type == PSTypeEnum.ACL )
         anns = PSAclImpl.class.getAnnotations();
      else if ( type == PSTypeEnum.NODEDEF )
         anns = PSNodeDefinition.class.getAnnotations();
      else if ( type == PSTypeEnum.KEYWORD_DEF )
         anns = PSKeyword.class.getAnnotations();
      else if ( type == PSTypeEnum.LOCATION_SCHEME )
         anns = PSLocationScheme.class.getAnnotations();
      else if ( type == PSTypeEnum.SITE )
         anns = PSSite.class.getAnnotations();
      else if ( type == PSTypeEnum.RELATIONSHIP_CONFIGNAME )
         anns = PSRelationshipConfigName.class.getAnnotations();
      else if ( type == PSTypeEnum.AUTO_TRANSLATIONS )
         anns = PSAutoTranslation.class.getAnnotations();
      else if ( type == PSTypeEnum.EDITION )
         anns = PSEdition.class.getAnnotations();
      else if ( type == PSTypeEnum.EDITION_TASK_DEF )
         anns = PSEditionTaskDef.class.getAnnotations();
      else if ( type == PSTypeEnum.CONTEXT )
         anns = PSPublishingContext.class.getAnnotations();
            
      Table table = null;
      for (Annotation annotation : anns)
      {
         String name = annotation.annotationType().getCanonicalName();
         if ( name.compareTo("javax.persistence.Table") == 0)
         {
            table = (Table)annotation;
            return table.name();
         }
      }
      // don't return null, instead return the string
      return type.toString();
   }
   
   /**
    * Util to read the dependency file and return the contents as a String
    * @param archive the archive in which this dependencyFile exists never <code>null</code>
    * @param depFile the dependency File never <code>null</code>
    * @return file content as a String
    * @throws PSDeployException
    */
   public static String getFileContentAsString(PSArchiveHandler archive,
         PSDependencyFile depFile) throws PSDeployException
   {
      String s = "";
      BufferedInputStream in = null;
      try
      {
         in = new BufferedInputStream(archive
               .getFileData(depFile));
         int sz = archive.getFileSize(depFile);
         byte[] bytes = new byte[sz];
         in.read(bytes, 0, sz);
         s = new String(bytes, 0, sz, IPSUtilsConstants.RX_JAVA_ENC);
         
      }
      catch (IOException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Problem reading the dependency file: "
                     + depFile.getArchiveLocation().getName());
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
            }
         }
      }
      return s;
   }
   
   /**
    * Helper method to provide the correct mapping based on two attempts:
    * first try it as template if it does not exist, then try again as 
    * variant defintion. If there is no mapping, then throws an exception
    * @param dep the dependency handler never <code>null</code>
    * @param ctx the import context never <code>null</code>
    * @param tmpId the template id never <code>null</code>
    * @return the mapping if it exists else throws an exception
    * @throws PSDeployException 
    */
   public static PSIdMapping getTemplateOrVariantMapping(
         PSDependencyHandler dep, PSImportCtx ctx, String tmpId)
         throws PSDeployException
   {
      PSIdMapping m = null;
      if (dep == null)
         throw new IllegalArgumentException("Dependency may not be null"); 
      if (ctx == null)
         throw new IllegalArgumentException("context may not be null");
      if ( StringUtils.isBlank(tmpId))
         throw new IllegalArgumentException("template id may not be null");
      try
      {
         m = dep.getIdMapping(ctx, tmpId,
               PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
      }
      catch (PSDeployException dex)
      {
         if (dex.getErrorCode() == IPSDeploymentErrors.MISSING_ID_MAPPING)
         {
            // try as a variant . . .
            m = dep.getIdMapping(ctx, tmpId,
                  PSVariantDefDependencyHandler.DEPENDENCY_TYPE);
         }
      }
      return m;
   }

   /**
    * Gets all tables for the active or inactive Content Types.
    * 
    * @param tok the security token, may not be <code>null</code>.
    * 
    * @return the Content Types tables, never <code>null</code> or empty
    * for a proper configured server.
    * 
    * @throws PSDeployException if an error occurs.
    */
   public static List<String> getAllContentTypeTables(PSSecurityToken tok)
      throws PSDeployException
   {
      List<String> tableNames = new ArrayList<String>();
      
      List<IPSNodeDefinition> nodes = PSContentTypeHelper.loadNodeDefs("");
      for (IPSNodeDefinition node : nodes)
      {
         tableNames.addAll(getContentTypeTables(tok, node));
      }
      return tableNames;
   }
   
   /**
    * Gets the table names for the specified Content Type.
    * 
    * @param tok the security token, may not be <code>null</code>.
    * @param node the Content Type, may not be <code>null</code>.
    * 
    * @return the list of table names of the Content Type, may not empty,
    * never <code>null</code..
    * 
    * @throws PSDeployException if an error occurs. 
    */
   public static List<String> getContentTypeTables(PSSecurityToken tok,
         IPSNodeDefinition node) throws PSDeployException
   {
      List<String> tableNames = new ArrayList<String>();
      
      String appName = getColumnAppName(((PSNodeDefinition) node).getNewRequest());

      PSApplication app = PSAppObjectDependencyHandler
            .getApplication(tok, appName);

      PSCollection dataSetColl = app.getDataSets();
      if (dataSetColl == null)
         return Collections.emptyList();
      
      Iterator datasets = dataSetColl.iterator();
      while (datasets.hasNext())
      {
         PSDataSet ds = (PSDataSet) datasets.next();
         if (!(ds instanceof PSContentEditor))
            continue;
         
         PSContentEditor ce = (PSContentEditor) ds;
         PSContentEditorPipe cePipe = (PSContentEditorPipe) ce.getPipe();

         // check for tables
         tableNames.addAll(getLocatorTables(cePipe.getLocator()));
      }
      return tableNames;
   }
   
   /**
    * Get all tables from the supplied container locator
    *
    * @param locator The locator to check, may not be <code>null</code>.
    *
    * @return Iterator over zero or more table names as <code>String</code>
    * objects, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   public static List<String> getLocatorTables(PSContainerLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      List<String> tables = new ArrayList<String>();

      Iterator tableSets = locator.getTableSets();
      while (tableSets.hasNext())
      {
         PSTableSet tableSet = (PSTableSet)tableSets.next();
         Iterator refs = tableSet.getTableRefs();
         while (refs.hasNext())
         {
            PSTableRef ref = (PSTableRef)refs.next();
            tables.add(ref.getName());
         }
      }

      return tables;
   }
   
   /**
    * Get the shared def.
    *
    * @return The def, never <code>null</code>.
    *
    * @throws PSDeployException if the def cannot be loaded.
    */
   public static PSContentEditorSharedDef getSharedDef()
      throws PSDeployException
   {
      PSContentEditorSharedDef sharedDef = PSServer.getContentEditorSharedDef();
      if (sharedDef == null)
      {
         // result of shared def not loading, server will have already logged
         // an error for this.
         Object[] args = {"Cannot load shared def"};
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            args);
      }
      return sharedDef;
   }

   /**
    * Gets the table names used by all shared groups.
    *  
    * @return the table names, never <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if an error occurs.
    */
   public static List<String> getSharedGroupTables() throws PSDeployException
   {
      List<String> tables = new ArrayList<String>();
      
      List<PSSharedFieldGroup> groups = new ArrayList<PSSharedFieldGroup>();
      CollectionUtils.addAll(groups, getSharedDef().getFieldGroups());
      for (PSSharedFieldGroup group : groups)
      {
         tables.addAll(getLocatorTables(group.getLocator()));
      }
      return tables;
   }
   
   
}
