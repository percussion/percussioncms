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


package com.percussion.server.cache;

import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides access to various operations against the Rhythmyx
 * server's cache subsystem.  Currently there are two types of caching: 
 * Assembler and Resource.  Assembler caching handles pages that are returned
 * from queries to DataSets that are specified by Content Variant registrations.
 * Resource caching handles pages cached based on settings within the DataSet. 
 * Methods are provided to flush particular sets of pages cached by either type,
 * all pages cached by either type, all pages returned by an application, all
 * pages cached for a particular session, or the entire cache, regardless of 
 * page type.
 */
public class PSCacheProxy 
{
   /**
    * Flushes all cached pages returned by the specified application, without 
    * considering the cache type.  If the application does not exist, or no 
    * pages are cached for this app, this method has no effect.
    * 
    * @param appName The name of the application for which pages are to be
    * flushed, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>appName</code> is 
    * <code>null</code> or empty.
    * @throws PSCacheException if there are any other errors.
    */
   public static void flushApplication(String appName) throws PSCacheException
   {
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException(" may not be null or empty");
      
      PSCacheManager.getInstance().flushApplication(appName);
   }

   /**
    * Flushes all pages cached for the specified session, without 
    * considering the cache type.  Pages with content that is specific to a 
    * particular user's session will be cached for that session, while others
    * are cached without regard to session.  If the specified session does not 
    * exist, or if no pages are cached for this session, this method has no 
    * effect.
    * 
    * @param sessionId The id of the session for which cached pages should be
    * flushed.  May not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>sessionId</code> is 
    * <code>null</code> or empty.
    * @throws PSCacheException if there are any other errors.
    */
   public static void flushSession(String sessionId) throws PSCacheException
   {
      if (sessionId == null || sessionId.trim().length() == 0)
         throw new IllegalArgumentException(
            "sessionId may not be null or empty");
            
      PSCacheManager.getInstance().flushSession(sessionId);
   }

   /**
    * Flushes all cached Assembler type pages that meet the specified criteria.  
    * If <code>null</code> is supplied for any parameter, it is not considered 
    * to be part of the search criteria.  However, if <code>revision</code> is 
    * not <code>null</code>, then <code>contentId</code> must be supplied. For 
    * example, to flush all assembly pages cached that were requested using
    * sys_variant=301:
    * <pre><code>
    *    PSCacheProxy.flushAssemblers(null, null, null, 301);
    * </code></pre>
    * 
    * Pass <code>null</code> for all arguments to flush all Assembler pages. If 
    * there are no matching pages in the cache, this method has no effect.
    * 
    * @param appName The name of the application for which the pages are cached,
    * may be <code>null</code>, never empty.
    * @param contentId The content id of the item for which the pages are 
    * cached, may be <code>null</code>.
    * @param revision The revision of the item  for which the pages are 
    * cached, may be <code>null</code>.  Must be <code>null</code> if 
    * <code>contentId</code> is <code>null</code>.
    * @param variantId The variant id for which the pages are cached, may be
    * <code>null</code>.
    *  
    * @throws PSCacheException if <code>revision</code> is supplied but
    * <code>contentId</code> is <code>null</code>, if any other parameter is
    * invalid, or if there are any other errors.
    */
   public static void flushAssemblers(String appName, Integer contentId, 
      Integer revision, Integer variantId) throws PSCacheException
   {
      PSCacheManager mgr = PSCacheManager.getInstance();
      IPSCacheHandler handler = mgr.getCacheHandler(
         PSAssemblerCacheHandler.HANDLER_TYPE);
         
      // if caching not enabled, handler will be null
      if (handler == null)
         return;
         
      // create key map with all empty values
      String[] keys = handler.getKeyNames();
      int numKeys = keys.length;
      
      Map keyMap = new HashMap(numKeys);
      for (int i = 0; i < numKeys; i++) 
      {    
         keyMap.put(keys[i], "");
      }
      
      // set supplied key values
      keyMap.put(PSAssemblerCacheHandler.KEY_ENUM[0], appName);
      if (contentId != null)
         keyMap.put(PSAssemblerCacheHandler.KEY_ENUM[1], contentId.toString());
      if (revision != null)
         keyMap.put(PSAssemblerCacheHandler.KEY_ENUM[2], revision.toString());
      if (variantId != null)
         keyMap.put(PSAssemblerCacheHandler.KEY_ENUM[3], variantId.toString());
         
      try 
      {
         handler.validateKeys( keyMap );
      }
      catch(PSSystemValidationException e)
      {
         throw new PSCacheException(IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION,
            e.getLocalizedMessage() );
      }
      
      mgr.flush(keyMap);
   }

   /**
    * Flushes all cached Resource type pages that meet the specified criteria.  
    * If <code>null</code> is supplied for any parameter, it is not considered 
    * to be part of the search criteria.  For example, to flush all pages cached 
    * by a DataSet named "foo", even if requested from different applications:
    * <pre><code>
    *    PSCacheProxy.flushResources(null, "foo");
    * </code></pre>
    * 
    * Pass <code>null</code> for all arguments to flush all Resource pages. If 
    * there are no matching pages in the cache, this method has no effect.
    * 
    * @param appName The name of the application for which the pages are cached,
    * may be <code>null</code>, never empty.
    * @param dataSetName The name of the DataSet for which pages are cached,
    * may be <code>null</code>, never empty.  
    * 
    * @throws PSCacheException if any param is invalid or if there are any 
    * other errors.
    */
   public static void flushResources(String appName, String dataSetName) 
      throws PSCacheException
   {
      PSCacheManager mgr = PSCacheManager.getInstance();
      IPSCacheHandler handler = mgr.getCacheHandler(
         PSResourceCacheHandler.HANDLER_TYPE);
         
      // if caching not enabled, handler will be null
      if (handler == null)
         return;
         
      // create key map with all empty values
      String[] keys = handler.getKeyNames();
      int numKeys = keys.length;
      
      Map keyMap = new HashMap(numKeys);
      for (int i = 0; i < numKeys; i++) 
      {    
         keyMap.put(keys[i], "");
      }
      
      // set supplied key values
      keyMap.put(PSResourceCacheHandler.KEY_ENUM[0], appName);
      keyMap.put(PSResourceCacheHandler.KEY_ENUM[1], dataSetName);
      
      try 
      {
         handler.validateKeys( keyMap );
      }
      catch(PSSystemValidationException e)
      {
         throw new PSCacheException(IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION,
            e.getLocalizedMessage() );
      }
      
      mgr.flush(keyMap);

   }

   /**
    * Flushes all caches, which includes Assemlber, Resource and Folder Cache.
    * 
    * @throws PSCacheException if there are any error.
    */
   public static void flushAll() throws PSCacheException
   {
      PSCacheManager.getInstance().flush();
      flushFolderCache();
      flushHibernateCache();
   }

   /**
    * Flushes and reloads the Folder Cache.
    *  
    * @throws PSCacheException if there are any error.
    */
   public static void flushFolderCache() throws PSCacheException
   {
      PSCacheManager.getInstance().resetFolderCache();
   }
   
   /**
    * Flush hibernate's second level cache
    */
   public static void flushHibernateCache()
   {
      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
      mgr.flushSecondLevelCache();
   }
}
