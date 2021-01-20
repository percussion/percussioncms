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

 
package com.percussion.deployer.server;

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.idtypes.PSAppExtensionCallIdContext;
import com.percussion.deployer.objectstore.idtypes.PSAppExtensionParamIdContext;
import com.percussion.deployer.objectstore.idtypes.PSApplicationIdContext;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Class to manage {@link PSApplicationIDTypes} maps for all applications.  
 * Handles saving and restoring map to and from disk, as well as updating the
 * map to add and remove new and obsolete mappings.
 */
public class PSIdTypeManager 
{

   /**
    * Loads the existing map for the specified dependency key.  
    * 
    * @param depKey The dependency key of the dependency to get the IDTypes map 
    * for.  May not be <code>null</code> or empty.  See 
    * {@link PSDependency#getKey()}.
    * 
    * @return The idTypes map, may be <code>null</code> if a map has not been
    * saved for the provided key.
    * 
    * @throws IllegalArgumentException if <code>depKey</code> is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public static PSApplicationIDTypes loadIdTypes(String depKey)
      throws PSDeployException
   {
      if (depKey == null || depKey.trim().length() == 0)
         throw new IllegalArgumentException("depKey may not be null or empty");
      
      PSApplicationIDTypes idTypeMap = null;
      
      Document idTypeDoc = getIdTypesDoc(depKey);
      
      try
      {
         if (idTypeDoc != null)
            idTypeMap = new PSApplicationIDTypes(
               idTypeDoc.getDocumentElement());
      }
      catch(Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      
      return idTypeMap;
   }
   
   /**
    * Loads id types for the supplied list of dependencies.  
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param deps A list of dependencies for which id types are to be loaded, 
    * may not be <code>null</code>, and must only contain dependencies that
    * support id types. Calls {@link #loadIdTypes(PSSecurityToken, PSDependency, 
    * boolean, Map, Map)} for each dependency in the list, adding filters and 
    * using a filter cache for efficiency.
    * 
    * @return The list of id types for the supplied list of dependencies, never
    * <code>null</code> or empty.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public static Iterator loadIdTypes(PSSecurityToken tok, Iterator deps)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (deps == null)
         throw new IllegalArgumentException("deps may not be null");

      List typeList = new ArrayList();
      Map filterCache = new HashMap();
      Map extensionDefCache = new HashMap();
      
      while (deps.hasNext())
      {
         typeList.add(loadIdTypes(tok, (PSDependency)deps.next(), true, 
            filterCache, extensionDefCache));         
      }
      
      return typeList.iterator();
   }

   /**
    * Convenience method that calls {@link #loadIdTypes(PSSecurityToken, 
    * PSDependency, boolean, Map, Map) 
    * loadIdTypes(tok, dep, false, null, null)}.
    */
   public static PSApplicationIDTypes loadIdTypes(PSSecurityToken tok, 
      PSDependency dep) throws PSDeployException
   {
      return loadIdTypes(tok, dep, false, null, null);
   }
   
   /**
    * Loads the existing map for the supplied dependency.  If not found, 
    * creates a new one.  Scans all possible locations in dependency's files for 
    * literal ids and adds any missing entries to the supplied ID types object.  
    * Also removes any mappings for literals that are no longer defined in the 
    * application.
    * 
    * @param tok The security token to use to access objectstore objects, may
    * not be <code>null</code>.
    * @param dep The dependency to get the IDTypes map for.  May not be
    * <code>null</code> and must support id types.  See 
    * {@link PSDependency#supportsIdTypes()}.
    * @param addDynamicData <code>true</code> to add any dynamic data that is
    * used when setting type values, but is not persited.  This includes 
    * extension param names, and the information necessary to filter possible 
    * type choices for each id in the map, <code>false</code> to omit
    * this information if not required.
    * @param filterCache Current filters that may be reused as a cache when
    * calling this method multiple times for different dependencies.  May be
    * <code>null</code>, otherwise conforms to the filter map returned by
    * {@link PSApplicationIDTypes#getChoiceFilters()}.
    * @param extensionCache Current extension defs that may be reused as a cache 
    * when calling this method multiple times for different dependencies.  May be
    * <code>null</code>, otherwise contents are expected to be populated and
    * used exclusively by this method.
    * 
    * @return The idTypes map, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>tok</code> is <code>null</code>
    * or if <code>dep</code> is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public static PSApplicationIDTypes loadIdTypes(PSSecurityToken tok, 
      PSDependency dep, boolean addDynamicData, Map filterCache, 
      Map extensionCache) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)   
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.supportsIdTypes())
         throw new IllegalArgumentException("dep must support id types");
         
      PSApplicationIDTypes idTypes = null;
      String depKey = dep.getKey();
      Document doc = getIdTypesDoc(depKey);
      if (doc != null)
      {
         try
         {
            idTypes = new PSApplicationIDTypes(doc.getDocumentElement());
         }
         catch (PSUnknownNodeTypeException e)
         {
            Object[] args = {depKey, e.getLocalizedMessage()};
            throw new PSDeployException(IPSDeploymentErrors.ID_TYPE_MAP_LOAD, 
               args);
         }
      }
      else
      {
         // if new map, create
         idTypes = new PSApplicationIDTypes(dep);
      }  

      // get all types identified by the handler as undefined
      IPSIdTypeHandler idTypeHandler = 
         PSDependencyManager.getInstance().getIdTypeHandler(dep);
      PSApplicationIDTypes newTypes = idTypeHandler.getIdTypes(tok, dep);
      
      // add only the new mappings, leaving defined mappings in the current
      // map, and then remove obsolete mappings
      fixupMappings(idTypes, newTypes);

      // save what we've got before we return it
      saveIdTypes(idTypes);
      
      // now add choice filters
      if (addDynamicData)
      {
         addChoiceFilters(tok, idTypes, filterCache);
         addExtensionParamNames(idTypes, extensionCache);
      }
      return idTypes;      
   }


   /**
    * Saves the supplied idTypes map to disk, first removing any included choice
    * filter information.
    * 
    * @param idTypes The idTypes map, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>idTypes</code> is 
    * <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public static void saveIdTypes(PSApplicationIDTypes idTypes)
      throws PSDeployException
   {
      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");
      
      idTypes.setChoiceFilters(null);

      FileOutputStream out = null;
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element mapEl = idTypes.toXml(doc);
         PSXmlDocumentBuilder.replaceRoot(doc, mapEl);

         PSDeploymentHandler.IDTYPE_DIR.mkdirs();
         String depKey = idTypes.getDependency().getKey();

         File mapFile = new File(PSDeploymentHandler.IDTYPE_DIR,
            depKey + ".xml");

         out = new FileOutputStream(mapFile);
         PSXmlDocumentBuilder.write(doc, out);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (out != null)
            try {out.close();} catch(IOException ex){}
      }
      
   }
   
   /**
    * Gets mappings for all idTypes specified for the supplied dependency.
    * 
    * @param tok The security token to use to access objectstore objects, may
    * not be <code>null</code>.
    * @param dep The application dependency to check id types for, may not be
    * <code>null</code>, must support id types.
    * 
    * @return an iterator over zero or more 
    * <code>PSApplicationIDTypeMapping</code> objects, never <code>null</code>.
    * Will not include mappings that are defined as not specifying an id.
    * 
    * @throws IllegalArgumentException If <code>dep</code> is invalid.
    * @throws PSDeployException if any idType mapping found for the specified
    * application is incomplete.
    */
   public static Iterator getIdTypeDependencies(PSSecurityToken tok,
      PSDependency dep) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
         
      // TODO - Always returns an empty list since we do not want to do 
      // ID typing.  We should remove all references to ID Typing.
      List mappingList = new ArrayList();         

      return mappingList.iterator();
   }
   
   /**
    * Get the <code>Document</code>, which contains the specified
    * <code>PSApplicationIDTypes</code> object from disk.  The map is not 
    * validated or updated in any way.
    *
    * @param depKey The dependency key of the dependency to get the IDTypes map 
    * for.  May not be <code>null</code> or empty.  See 
    * {@link PSDependency#getKey()}.
    *
    * @return The doc, may be <code>null</code> if an existing map is not found.
    *
    * @throws IllegalArgumentException if <code>depKey</code> is not valid.
    * @throws PSDeployException if there is an error while getting the
    * <code>Document</code>.
    */
   static Document getIdTypesDoc(String depKey) 
      throws PSDeployException
   {
      if (depKey == null || depKey.trim().length() == 0)
         throw new IllegalArgumentException("depKey may not be null or empty");
      
      File mapFile = new File(PSDeploymentHandler.IDTYPE_DIR, depKey +
         ".xml");
      
      Document resultDoc = null;
      if (mapFile.exists())
      {
         FileInputStream in = null;
         try
         {
            in = new FileInputStream(mapFile);
            resultDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         }
         catch (Exception e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               e.getLocalizedMessage());
         }
         finally
         {
            if (in != null)
               try {in.close();} catch(IOException ex){}
         }
      }
      
      return resultDoc;
   }
   
   /**
    * Adds any type mappings found in the supplied <code>newTypes</code> to the
    * <code>curTypes</code> if not already defined by <code>curTypes</code>.  
    * Any mappings in <code>curTypes</code> not defined by <code>newTypes</code>
    * are removed from <code>curTypes</code>.
    * 
    * @param curTypes The currently defined idtypes, assumed not 
    * <code>null</code>.
    * @param newTypes The newly discovered idtypes, assumed not 
    * <code>null</code>.
    */
   private static void fixupMappings(PSApplicationIDTypes curTypes, 
      PSApplicationIDTypes newTypes)
   {
      PSApplicationIDTypes usedTypes = new PSApplicationIDTypes(
         newTypes.getDependency());
      
      Iterator resources = newTypes.getResourceList(false);
      while (resources.hasNext())
      {
         String resource = (String)resources.next();
         Iterator elements = newTypes.getElementList(resource, false);
         while (elements.hasNext())
         {
            String element = (String)elements.next();
            Iterator mappings = newTypes.getIdTypeMappings(resource, element, 
               false);
            addMappings(mappings, resource, element, curTypes, usedTypes);
         }
      }
      
      // now remove any in current map not "used"
      removeUnusedMappings(curTypes, usedTypes);
   }
   
   /**
    * Adds choicefilters for the types in the map, and then resets any invalid
    * type mappings to undefined based on the filters.
    * 
    * @param tok The security token to use to access objectstore objects, 
    * assumed not <code>null</code>.
    * @param idTypes The idtypes for which filters are determined and to which
    * they are set, assumed not <code>null</code>.
    * @param filterCache Current filters that may be reused as a cache when
    * calling this method multiple times for different id types.  May be
    * <code>null</code>, otherwise conforms to the filter map returned by
    * {@link PSApplicationIDTypes#getChoiceFilters()}.
    * 
    * @throws PSDeployException if there are any errors determining possible
    * types for an id
    */   
   private static void addChoiceFilters(PSSecurityToken tok, 
      PSApplicationIDTypes idTypes, Map filterCache) throws PSDeployException
   {
      Map choiceFilters = new HashMap();
      PSDependencyManager depMgr = PSDependencyManager.getInstance();
      Iterator ids = idTypes.getIds().iterator();
      while (ids.hasNext())      
      {
         String id = ids.next().toString();
         
         // first check the cache if we have one
         List typeList = null;
         if (filterCache != null)
            typeList = (List)filterCache.get(id);
         if (typeList == null)
         {
            // not cached, look it up
            typeList = new ArrayList();
            Iterator types = depMgr.getPossibleIdTypes(tok, id);
            while (types.hasNext())
            {
               PSDependencyDef def = (PSDependencyDef)types.next();
               typeList.add(def.getObjectType());
            }
            
            // cache it if we have one
            if (filterCache != null)
               filterCache.put(id, typeList);
         }
         
         choiceFilters.put(id, typeList);         
      }
      
      if (!choiceFilters.isEmpty())
      {
         idTypes.setChoiceFilters(choiceFilters);                  
      }
   }

   /**
    * Updates the supplied ID types by Adding the extension param names to any 
    * extension param ctx that does not already contain this information.
    * 
    * @param idTypes The ID Types to update, assumed not <code>null</code>.
    * @param extensionCache Current extension defs that may be reused as a cache 
    * when calling this method multiple times.  May be <code>null</code>, 
    * otherwise contents are expected to be populated and used exclusively by 
    * this method.
    */
   private static void addExtensionParamNames(PSApplicationIDTypes idTypes, 
      Map extensionCache)
   {
      Iterator mappings = idTypes.getAllMappings(false);
      while (mappings.hasNext())
      {
         PSApplicationIDTypeMapping mapping = 
            (PSApplicationIDTypeMapping) mappings.next();
         
         Iterator contexts = mapping.getContext().getAllContexts();
         while (contexts.hasNext())
         {
            PSApplicationIdContext ctx = 
               (PSApplicationIdContext) contexts.next();
            if (!(ctx instanceof PSAppExtensionParamIdContext))
               continue;
            
            PSAppExtensionParamIdContext extParamCtx = 
               (PSAppExtensionParamIdContext)ctx;
            if (extParamCtx.getParamName() == null)
            {
               // make sure parent is an extension call, not a db function
               PSApplicationIdContext parentCtx = extParamCtx.getParentCtx();
               if (!(parentCtx instanceof PSAppExtensionCallIdContext))
                  continue;
               
               // get def and add name
               int index = extParamCtx.getIndex();
               PSAppExtensionCallIdContext callCtx = 
                  (PSAppExtensionCallIdContext)parentCtx;
               
               // check the cache for the def
               String ref = callCtx.getExtensionRef(); 
               IPSExtensionDef def = (PSExtensionDef)extensionCache.get(ref);
               if (def == null)
               {
                  // get the def and cache it
                  try
                  {
                     def = PSServer.getExtensionManager(null).getExtensionDef(
                        new PSExtensionRef(ref));
                     extensionCache.put(ref, def);                        
                  }
                  catch (Exception e)
                  {
                     // we tried, not crucial
                     continue;
                  }
               }

               // get the name and set on the ctx
               Iterator paramNames = def.getRuntimeParameterNames();
               int i = 0;
               while (paramNames.hasNext() && i <= index)
               {
                  String name = paramNames.next().toString();
                  if (i++ == index && name != null && 
                     name.trim().length() > 0)
                  {
                     extParamCtx.setParamName(name);
                  }
               }
            }
         }
      }
   }
   
   /**
    * Adds idType mappings to the provided map using the supplied parameters.
    * If a mapping with the same context is found, but a different value, it
    * will be replaced, and the type will need to be reset.  If the exact same
    * mapping is found, the new mapping will not be added.
    * 
    * @param mappings The list of mappings to add, assumed not 
    * <code>null</code>.
    * @param resourceName The resource name to use, assumed not 
    * <code>null</code> or empty.
    * @param elementName The element name to use, may not be <code>null</code> 
    * or empty.
    * @param idTypes The id types map to add to, may not be <code>null</code>.
    * @param usedMappings Mappings that have been added or were found in
    * the map as already existing are added to this map.  Modified by each call 
    * to contain any mappings either added to the supplied <code>idTypes</code>
    * or skipped because they are already defined. May not be <code>null</code>.
    */
   private static void addMappings(Iterator mappings, String resourceName, 
      String elementName, PSApplicationIDTypes idTypes, 
         PSApplicationIDTypes usedMappings)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");
      
      if (resourceName == null || resourceName.trim().length() == 0)
         throw new IllegalArgumentException(
            "resourceName may not be null or empty");
            
      if (elementName == null || elementName.trim().length() == 0)
         throw new IllegalArgumentException(
            "elementName may not be null or empty");
            
      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");
      
      if (usedMappings == null)
         throw new IllegalArgumentException("usedMappings may not be null");
      
      while (mappings.hasNext())
      {
         PSApplicationIDTypeMapping mapping = 
            (PSApplicationIDTypeMapping)mappings.next();
         PSApplicationIdContext ctx = mapping.getContext();
         PSApplicationIDTypeMapping cur = idTypes.getMapping(resourceName, 
            elementName, ctx);
          
         if (cur != null && cur.getValue().equals(mapping.getValue()))
            usedMappings.addMapping(resourceName, elementName, cur);
         else
         {
            if (cur != null)
               idTypes.removeMapping(resourceName, elementName, ctx);
            
            // add new one
            idTypes.addMapping(resourceName, elementName, mapping);
            usedMappings.addMapping(resourceName, elementName, mapping);
         }
      }
   }

   /**
    * Removes any mappings from the supplied idTypes not found in the 
    * <code>usedMappings</code> idTypes.
    * 
    * @param idTypes The mappings to remove unused mappings from, may not be
    * <code>null</code>.
    * @param usedMappings The mappings that have been "used", and so are not 
    * obsolete.  May not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */   
   private static void removeUnusedMappings(PSApplicationIDTypes idTypes, 
      PSApplicationIDTypes usedMappings)
   {
      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");
      
      if (usedMappings == null)
         throw new IllegalArgumentException("usedMappings may not be null");
      
      Iterator resources = idTypes.getResourceList(false);
      while (resources.hasNext())
      {
         String resource = (String)resources.next();
         Iterator elements = idTypes.getElementList(resource, false);
         while (elements.hasNext())
         {
            String element = (String)elements.next();
            Iterator mappings = idTypes.getIdTypeMappings(resource, element, 
               false);
            while (mappings.hasNext())
            {
               PSApplicationIDTypeMapping mapping = 
                  (PSApplicationIDTypeMapping)mappings.next();
               if (usedMappings.getMapping(resource, element, 
                  mapping.getContext()) == null)
               {
                  idTypes.removeMapping(resource, element, 
                     mapping.getContext());
               }
            }
         }
      }
   }
}
