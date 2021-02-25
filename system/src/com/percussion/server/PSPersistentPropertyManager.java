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
package com.percussion.server;

import com.percussion.security.PSUserEntry;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.tools.PSPatternMatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides a mechanism for saving property values across invocations
 * of the server. Each persisted property contains optional meta data and a
 * value. Persisted properties may come from the system or designer. System
 * properties will contain meta data with attributes describing how to process
 * the property. Designer properties may or may not have a meta entry.
 * <p>For system properties, the presence of a meta entry means that that
 * property will be persisted. Designer properties should follow this policy
 * as well.
 * <p>This class uses the PSPersistentPropertyMeta class for managing the
 * property attributes.
 */
public class PSPersistentPropertyManager
{

   /**
    *  Default constructor; private to disallow instantiation.
    */
   private PSPersistentPropertyManager(){}

   /**
    * Get the singleton instance.
    *
    * @return the Singleton instance of the class.  Never <code>null</code>.
    */
   public static PSPersistentPropertyManager getInstance()
   {
      // ms_instance is initialized at class load time, so must be non-null
      // by the time this method is invoked.
      return ms_instance;
   }

   /**
    * A persisted property is a named value that is saved in persistent storage
    * so that it is available across invocations of the server. Each property
    * has certain characteristics that can be modified by the designer. This
    * method returns those attributes.
    *
    * @param category An arbitrary name used for grouping properties. <code>
    *    null</code> and empty are treated the same and stored as <code>NULL
    *    </code> in the repository. Sql wildcards accepted.
    *
    * @param userSession the user session used to get the property meta objects.
    *    It may not be <code>null</code>.
    *
    * @param propertyName The case-sensitive name of the property. Never
    *    <code>null</code> or empty. Sql wildcards accepted.
    *
    * @return A possibly empty collection of PSPersistentPropertyMeta objects
    *    that match the supplied search criteria. If a match isn't found, an
    *    empty collection is returned.
    *
    * @throws IllegalArgumentException if property name and category
    * is not supplied.
    */
   public Collection getPersistedPropertyMeta( String category,
     PSUserSession userSession, String propertyName )
   {
      if ((category == null || category.length() == 0) &&
          (propertyName == null || propertyName.length() == 0))
         throw  new IllegalArgumentException(
          "Category and property name cannot be null at the same time");
      Collection c = null;
      try
      {
         c = new ArrayList<>();
         String userName = getUserName(userSession);
         if (userName.length() == 0)
            return c;

         // We're about to do a number of operations that depend on consistent
         // reads and writes of data contained by 'this'.  Synchronize here
         // to ensure that no updates are done concurrently.
         synchronized(this)
         {
            populateCache(userSession, true);
            ConcurrentHashMap cMap = m_mergedMetaCache.get(userName);
            if (cMap == null)
               return c;
            PSPatternMatcher pm = PSPatternMatcher.SQLPatternMatcher(null);
            Set cSet = cMap.keySet();
            Iterator cItr = cSet.iterator();
            String cat = null;
             ConcurrentHashMap objMap = null;
            if (category != null && category.length() != 0)
            {
               while(cItr.hasNext())
               {
                  cat = (String)cItr.next();
                  if (pm.doesMatchPattern(category, cat))
                  {
                     objMap = (ConcurrentHashMap)cMap.get(cat);
                     matchLeaves(pm, propertyName, objMap, c);
                  }
               }
            }
            else
            {
               while(cItr.hasNext())
               {
                  cat = (String)cItr.next();
                  objMap = (ConcurrentHashMap)cMap.get(cat);
                  matchLeaves(pm, propertyName, objMap, c);
               }
            }
         }
      }
      catch(IllegalArgumentException argEx)
      {
         argEx.printStackTrace(System.out);
      }

     return c;
   }

   public List<PSPersistentPropertyMeta> savePersistentPropertyMeta(List<PSPersistentPropertyMeta> list, PSUserSession user){
       IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();

       List<PSPersistentPropertyMeta> ret = mgr.saveAllPersistentMeta(list);
       resetMetaCache(user);
       return ret;

   }

   public void deletePersistantPropertyMeta(List<PSPersistentPropertyMeta> list, PSUserSession user){
       IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();

       mgr.deleteAllPersistentMeta(list);

       resetMetaCache(user);

   }

   public PSPersistentPropertyMeta savePersistentPropertyMeta(PSPersistentPropertyMeta pm, PSUserSession user){
       IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();

       PSPersistentPropertyMeta ret = mgr.savePersistentPropertyMeta(pm);

       resetMetaCache(user);
        return ret;
   }

   /**
    * Matches property name based on the pattern supplied and returns
    * a Collection of matched values.
    * @param pm pattern matcher.
    * @param pattern to be matched.
    * @param hm container in which the search is to be made.
    * @param rc container in which matched objects will reside.
    * @return collection of matched objects.
    */
   private Collection matchLeaves(PSPatternMatcher pm, String pattern,
                                  ConcurrentHashMap hm, Collection rc)
   {

      Set objSet = hm.keySet();
      Iterator objItr = objSet.iterator();
      while (objItr.hasNext())
      {
         String propName = (String)objItr.next();
         if (pm.doesMatchPattern(pattern, propName))
            rc.add(hm.get(propName));
      }
      return rc;
   }

    /***
     * Attempt to invalidate the meta cache for a given user.
     * @param user
     */
   public void resetMetaCache(PSUserSession user){

       while ( m_mergedMetaCache.entrySet().iterator().hasNext())
       {
           Map.Entry<String, ConcurrentHashMap> entry = m_mergedMetaCache.entrySet().iterator().next();
           if(entry.getKey().equalsIgnoreCase(getUserName(user))){
               m_mergedMetaCache.entrySet().iterator().remove();
           }
       }

       populateCache(user, true);
   }
   /**
    * Populates <code>m_sysMetaCache</code> <code>m_propertyCache</code>
    * with all the default values for a hypothetical user **psxsystem and
    * merged values for an authenticated user/principal.
    * 
    * @param userSession the user session used to get the property meta objects.
    *    It may not be <code>null</code>.
    */
   private void populateCache(PSUserSession userSession, boolean isMeta)
   {
      String userName = getUserName(userSession);
      if (isMeta)
      {
         if (m_mergedMetaCache.get(SYS_USER) == null)
            load(SYS_USER, true);
         if (m_mergedMetaCache.get(userName) == null)
         {
            load(userName, true);
            merge(userName, m_mergedMetaCache, true);
         }
      }
      else
      {
         if (m_propertyCache.get(SYS_USER) == null)
            load(SYS_USER, false);
         if (m_propertyCache.get(userName) == null)
         {
            load(userName, false);
            if (m_propertyCache.get(SYS_USER) != null)
               merge(userName, m_propertyCache, false);
         }
      }
   }

   /**
    * Merges default entries with the user entries,already existing user entries
    * are left untouched.
    */
   private void merge(String userName, Map map, boolean isMeta)
   {
       ConcurrentHashMap sysCategoryMap = (ConcurrentHashMap)map.get(SYS_USER);
       ConcurrentHashMap usrCategoryMap = (ConcurrentHashMap)map.get(userName);
      if (usrCategoryMap == null)
      {
         usrCategoryMap = new ConcurrentHashMap();
         map.put(userName, usrCategoryMap);
      }

      if ( null == sysCategoryMap)
         return;
      Set sysCatSet = sysCategoryMap.keySet();
      Iterator sysCatItr = sysCatSet.iterator();
      while(sysCatItr.hasNext())
      {
         String sysCategory = (String)sysCatItr.next();
         if(!usrCategoryMap.containsKey(sysCategory))
            usrCategoryMap.put(sysCategory, new ConcurrentHashMap<>());
         mergeLeaves(userName, (ConcurrentHashMap)sysCategoryMap.get(sysCategory),
                     (ConcurrentHashMap)usrCategoryMap.get(sysCategory), isMeta);
      }
   }

   /**
    * Merges the leaf map - the map which maintains meta property name and
    * meta object(PSPersistentPropertyMeta) mappings.
    */
   private Map mergeLeaves(String userName, Map src, Map dest, boolean isMeta)
   {
      Set srcSet =  src.keySet();
      Iterator srcItr = srcSet.iterator();
      while (srcItr.hasNext())
      {
         String srcName = (String)srcItr.next();
         //checks if the user mapping for a name already exists
         // to prevent overwrites, if exists don't touch it!!.
         if (dest.get(srcName) == null)
         {
            if (isMeta)
            {
               PSPersistentPropertyMeta obj = (PSPersistentPropertyMeta)
                                         src.get(srcName);
               PSPersistentPropertyMeta newObj =
                     new PSPersistentPropertyMeta(obj, userName);
               dest.put(srcName, newObj);
            }
            else
            {
               PSPersistentProperty obj = (PSPersistentProperty)
                                         src.get(srcName);
               PSPersistentProperty newObj = new PSPersistentProperty(
                     obj, userName, PSPersistentPropertyManager.UPDATE);
               dest.put(srcName, newObj);
            }
         }
      }
      return src;
   }


   /**
    * Populates <code>m_mergedMetaCache</code> and <code>m_propertyCache</code>
    * for an user.
    * @param userName
    * @param loadMetadata indicates whether to load meta properties or
    * properties.  Metadata is loaded if <code>true</code>, data otherwise.
    */
   private void load(String userName, boolean loadMetadata)
   {
      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();

       ConcurrentHashMap categoryMap = new ConcurrentHashMap();

      if (loadMetadata)
         m_mergedMetaCache.put(userName, categoryMap);
      else
         m_propertyCache.put(userName, categoryMap);
      
      List persists;
      if (loadMetadata)
         persists = mgr.findPersistentMetaByName(userName);
      else
         persists = mgr.findPersistentPropertiesByName(userName);
      
      Iterator ps = persists.iterator();
      while (ps.hasNext())
      {
         String propertyName = null;
         String category = null;
         Object obj = ps.next();
         if (loadMetadata)
         {
            category = ((PSPersistentPropertyMeta)obj).getCategory();
            propertyName = ((PSPersistentPropertyMeta)obj).getPropertyName();
         }
         else
         {
            category = ((PSPersistentProperty)obj).getCategory();
            propertyName = ((PSPersistentProperty)obj).getName();

         }
         //if category is null then the key is null and all the metaobj without
         // category go there.
          ConcurrentHashMap map = (ConcurrentHashMap)categoryMap.get(category);
         if (map == null)
         {
            map = new ConcurrentHashMap<>();
            categoryMap.put(category, map);
         }
         map.put(propertyName, obj);
      }
   }

   /**
    * Utility method to get PSPersistentPropertyMeta objects from
    * m_mergedMetaCache.
    */
   private PSPersistentPropertyMeta getFromMetaCache(String category,
       String userName, String propertyName)
   {
      Map catMap = (ConcurrentHashMap)m_mergedMetaCache.get(userName);
      if (catMap != null)
      {
          ConcurrentHashMap objMap = (ConcurrentHashMap)catMap.get(category);
         if(objMap != null)
            return (PSPersistentPropertyMeta)objMap.get(propertyName);
         else
            return null;
      }
      else
      {
         return null;
      }
   }

   /**
    * Gets the first user name from the specified user session.
    * @param userSession the user session which may contains user name(s).
    *    It may not be <code>null</code>.
    * @return user name with "spType/spInstance/username" format.  May be
    * empty, if anonymous access is allowed; never <code>null</code>
    */
   public static String getUserName(PSUserSession userSession)
   {
      if (userSession == null)
         throw new IllegalArgumentException("userSession may not be null.");
      
      PSUserEntry[] pUsr  =
         userSession.getAuthenticatedUserEntries();
      if (pUsr == null || pUsr.length == 0)
         return "";

      return pUsr[0].getName();
   }

   /**
    * Adds or changes the attributes of a persisted property.
    * <p>See {@link #getPersistedPropertyMeta} for more details and a
    * description of the key (search) properties. Sql wildcards should not be
    * used, an error will be thrown if they are found in any of the keys.
    *
    * @param category
    * @param request
    * @param propertyName
    * @param meta The new attribute settings. If <code>null</code>, the entry
    *    is removed.
    *
    * @return <code>true</code> if this is replacing an existing entry,
    *    <code>false</code> otherwise.
    * @throw UnsupportedOperationException.
    */
   public synchronized boolean setPersistedPropertyMeta(
         String category, PSRequest request,
         String propertyName, PSPersistentPropertyMeta meta )
   {
      throw new UnsupportedOperationException("Method not supported yet.");
   }

   /**
    * Returns the value in the persisted property table associated with the
    * supplied category, user and property, or the default object if a match is
    * not found.
    * <p>See {@link #getPersistedPropertyMeta} for more details and a
    * description of the <code>category</code> and <code>propertyName</code>
    * params.  
    * 
    * @param category The category, may not be <code>null</code> or empty if
    * <code>propertyName</code> is <code>null</code> or empty.
    * @param userSession the user session used to get the property meta objects.
    *    It may not be <code>null</code>.
    * @param propertyName Cannot be <code>null</code> or empty if 
    * <code>category</code> is <code>null</code> or empty.
    * 
    * @return Collection of <code>PSPersistentProperty</code> objects, an empty 
    * collection if nothing is found.
    * 
    * @throws IllegalArgumentException If <code>propertyName</code> and 
    * <code>category</code> are both <code>null</code> or empty, or if 
    * <code>request</code> is <code>null</code>.
    */
   public synchronized Collection getPersistedProperty( String category,
      PSUserSession userSession, String propertyName)
   {
      if ((category == null || category.length() == 0) &&
        (propertyName == null || propertyName.length() == 0))
       throw  new IllegalArgumentException(
          "Category and property name cannot be null at the same time.");
          
      if (userSession == null)
         throw new IllegalArgumentException("request may not be null");
         
      Collection c = new ArrayList();
      try
      {
         String userName = getUserName(userSession);
         if (userName.length() == 0)
            return c;

         // We're about to do a number of operations that depend on consistent
         // reads and writes of data contained by 'this'.  Synchronize here
         // to ensure that no updates are done concurrently.
         synchronized (this)
         {
            populateCache(userSession, false);
            if (m_propertyCache.isEmpty())
               return c;
             ConcurrentHashMap cMap = (ConcurrentHashMap)m_propertyCache.get(userName);
            if (cMap == null)
               return c;
            PSPatternMatcher pm = PSPatternMatcher.SQLPatternMatcher(null);
            Set cSet = cMap.keySet();
            Iterator cItr = cSet.iterator();
            if (category != null && category.length() != 0)
            {
               while(cItr.hasNext())
               {
                  String cat = (String)cItr.next();
                  if (pm.doesMatchPattern(category, cat))
                  {
                      ConcurrentHashMap objMap = (ConcurrentHashMap)cMap.get(cat);
                     matchLeaves(pm, propertyName, objMap, c);
                  }
               }
            }
            else
            {
               while(cItr.hasNext())
               {
                  String cat = (String)cItr.next();
                   ConcurrentHashMap objMap = (ConcurrentHashMap)cMap.get(cat);
                  matchLeaves(pm, propertyName, objMap, c);
               }
            }
         }
      }
      catch(IllegalArgumentException argEx)
      {
         argEx.printStackTrace(System.out);
      }

      return c;
   }

   /**
    * Properties are persisted here. If either the user name, property name or
    * property value is null, that property will not be persisted.
    * 
    * @param c Collection of PSPersistentProperty objects which contains objects
    * to be persisted. Only the modified properties are saved.  May not be
    * <code>null</code>.
    * @param userSession the user session used to get the property meta objects.
    *    It may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public synchronized void save(Collection c, PSUserSession userSession)
   {
      if (c == null)
         throw new IllegalArgumentException("c may not be null");
      
      if (userSession == null)
         throw new IllegalArgumentException("userSession may not be null");
         
      try
      {
         Iterator itr = c.iterator();
         String userName = getUserName(userSession);
         if (userName.length() == 0)
            return;
         IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
         while(itr.hasNext())
         {
            PSPersistentProperty ps = (PSPersistentProperty)itr.next();
            String action  = ps.getExtraParam();
            if (action == null )
            {
               if (!itr.hasNext())
                  return;
               continue;
            }
            if (action.equals(INSERT) || action.equals(UPDATE) ||
                  action.equals(DELETE))
            {
               String category = ps.getCategory();
               String name = ps.getName();
               if (category != null)
               {
                  PSPersistentPropertyMeta metaObj = getFromMetaCache(category,
                        userName, name);
                  // if the meta entry is not there just go ahead and write it.
                  // if it's there then evaluate it.
                  if (metaObj == null)
                  {
                     persist(ps, mgr, userSession);
                     continue;
                  }
                  if ((metaObj.getPropertySaveType() == 1) ||
                        (metaObj.getPropertySaveType() == 2) && !action.equals(UPDATE))
                  {
                     persist(ps, mgr, userSession);
                  }
               }
            }
         }
      }
      finally
      {
         // always synchronize the cache, this will also reset the extra param (action)
         // of all properties
         syncPropertyCache(c);
      }
   }
   
   /**
    * Persists the specified persistent property to the backend repository.
    * 
    * @param prop the to be saved property; assumed not <code>null</code>.
    * @param mgr the service used to persist the specified property to the
    *    backend repository; assumed not <code>null</code>.
    * @param session the user session used to get the property meta objects;
    *    assumed not <code>null</code>.
    */
   private void persist(PSPersistentProperty prop, IPSCmsObjectMgr mgr,
         PSUserSession session)
   {
      String action = prop.getExtraParam();
      if (action.equals(INSERT))
      {
         String propName = prop.getName();
         if (getPersistedProperty(prop.getCategory(), session, propName).isEmpty())
         {
            try{
               mgr.savePersistentProperty(prop);
            }catch(org.springframework.dao.DataIntegrityViolationException e){
               // CMS-1018  System displays stack trace in the UI if two people login with the same user name at the same time
               // For now catch the error and display a warning instead. 
               // TODO: Look at this code and make sure that an IP or Session is used to make properties unique.
               ms_log.warn("Unable to save persistent property [" + propName + "] for user session " + prop.getUserName());
            }
         }
         else
         {
            // something is out of sync, trying to insert an existing property, will
            // update instead
            ms_log.warn("Attempting to insert existing persistent property ["
                  + propName + "].  The property will be updated instead.");

            mgr.updatePersistentProperty(prop);
         }
      }
      else if (action.equals(UPDATE))
      {
         mgr.updatePersistentProperty(prop);
      }
      else if (action.equals(DELETE))
      {
         mgr.deletePersistentProperty(prop);
      }
   }

   /**
    * Synchronize properties in <code>m_propertyCache</code>. Preferably called
    * after backend has updated.
    */
   private void syncPropertyCache(Collection c)
   {
      Iterator itr = c.iterator();
      while (itr.hasNext())
      {
         PSPersistentProperty ps = (PSPersistentProperty)itr.next();
         setPropertyCache(ps);
      }
   }

   /**
    * Property is being set here.
    * @param ps property object checked for a possible update.
    */
   private void setPropertyCache(PSPersistentProperty ps)
   {
      // Ensure that the PSPersistentProperty remains consistent while
      // we're reading & updating it.
      synchronized(ps)
      {
         String userName = ps.getUserName();
         String category = ps.getCategory();
         Map catMap = (Map)m_propertyCache.get(userName);
         if (catMap == null)
         {
            catMap = new ConcurrentHashMap();
            m_propertyCache.put(userName, catMap);
         }
         Map objMap = null;
         if (category != null && category.length() == 0)
            category = null;
         objMap = (Map)catMap.get(category);
         if (objMap == null)
         {
            objMap = new ConcurrentHashMap();
            catMap.put(category, objMap);
         }
         String propName = ps.getName();
         String action = ps.getExtraParam();
         PSPersistentProperty obj = (PSPersistentProperty)objMap.get(propName);
         if (action.equals(PSPersistentPropertyManager.INSERT))
         {
            objMap.put(propName, ps);
            //reset the action
            ps.setExtraParam("");
         }
         else if (action.equals(PSPersistentPropertyManager.UPDATE))
         {
            if (obj == null)
               objMap.put(propName, ps);
            else
               obj.setValue(ps.getValue());
            //reset the action
            ps.setExtraParam("");
         }
         else if (action.equals(PSPersistentPropertyManager.DELETE))
         {
            objMap.remove(ps.getName());
            //reset the action
            ps.setExtraParam("");
         }
      }
   }

   /**
    * Singleton instance of the class.  Initialize this statically to ensure
    * thread safety.
    */
   private static final PSPersistentPropertyManager ms_instance =
         new PSPersistentPropertyManager();

   /**
    * Logger for this class.
    */
   private static final Log ms_log = LogFactory.getLog(PSPersistentPropertyManager.class);
   
   /**
    * Maintains a cache of merged <code> PSPersitentPropertyMeta</code>
    * objects. Note there 2 levels of indirection.
    * Username key(s) are mapped to ConcurrentHashMap(s), whose keys are
    * categories and those key(s) are mapped to  ConcurrentHashMap(s) whose key(s) are
    * mapped to meta property objects
    * Pictorially --- (Usernname,(categories,(metaPropertyName,metaObjs)))
    */
   private final Map<String, ConcurrentHashMap> m_mergedMetaCache = new ConcurrentHashMap<String, ConcurrentHashMap>();

   /**
    * Cache for <code>PSPersitentProperty</code> objects.
    */
   private final Map m_propertyCache = new ConcurrentHashMap();

   public  static final String   SYS_USER = "**psxsystem";
   public  static final String   DELETE = "DELETE";
   public  static final String   UPDATE = "UPDATE";
   public  static final String   INSERT = "INSERT";
   public  static final String[] METAELEMENTS =
   {PSPersistentPropertyMeta.ROOT_ELEM,
     PSPersistentPropertyMeta.PROPERTYNAME_ELEM};
   public static final String[] PROPERTYELEMENTS = {
    PSPersistentProperty.PERSISTEDPROPERTYVALUES_ELEM,
     PSPersistentPropertyMeta.PROPERTYNAME_ELEM};
}
