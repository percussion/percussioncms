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

import com.percussion.cms.IPSConstants;
import com.percussion.data.IPSDataExtractor;
import com.percussion.data.IPSTableChangeListener;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataExtractorFactory;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSQueryHandler;
import com.percussion.data.PSTableChangeEvent;
import com.percussion.data.PSUpdateHandler;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSNamedReplacementValue;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSResourceCacheSettings;
import com.percussion.design.objectstore.PSServerCacheSettings;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;
import com.percussion.util.PSIteratorUtils;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Handles the caching of query resource pages.  Will only cache pages returned
 * by query resource with caching enabled.  Manages the automatic flushing of
 * stale or dirty pages in the cache by monitoring all update resources
 * for insert, update and delete requests matching tables used by the query
 * resource or any query resources it lists as depedencies, recursively.
 */
public class PSResourceCacheHandler extends PSCacheHandler
   implements IPSTableChangeListener
{

   /**
    * Constructs an instance of this handler.
    *
    * @param cacheSettings The server cache settings.  May not be
    * <code>null</code>.
    *
    * @throws IllegalStateException if the <code>PSCacheManager</code> has not
    * been initialized.
   */
   public PSResourceCacheHandler(PSServerCacheSettings cacheSettings)
   {
      super(KEY_SIZE, cacheSettings);
   }

   @SuppressWarnings("unused") void start() throws PSCacheException
   {
      /*
       * add listener for hibernate eviction and flush based on table associated
       * to that type
       */
      IPSNotificationService svc = 
         PSNotificationServiceLocator.getNotificationService();
      svc.addListener(EventType.OBJECT_INVALIDATION, 
         new IPSNotificationListener() {

         public void notifyEvent(PSNotificationEvent notification)
         {
            IPSGuid g = (IPSGuid) notification.getTarget();

            String table = ms_tableTypeMap.get(PSTypeEnum.valueOf(
               g.getType()));
            
            if (table != null)
            {
               tableChanged(new PSTableChangeEvent(table, 
                  PSTableChangeEvent.ACTION_UPDATE, 
                  new HashMap<String, String>()));
            }            
         }});
   }


   // see IPSCacheHandler interface.
   public String getType()
   {
      return HANDLER_TYPE;
   }

   /**
    * See interface {@link IPSCacheHandler#getKeyNames} and {@link #flush(Map)}
    * for more information.
    */
   public String[] getKeyNames()
   {
      return KEY_ENUM;
   }

   /**
    * Validates that the supplied keys represent all keys required by this
    * handler to flush cached items. See {@link #flush(Map) flush} for
    * description of keys.
    * <br>
    * This method validates the following cases.
    * <ol>
    * <li>The number of keys supplied is at least the size of {@link #KEY_ENUM}.
    * </li>
    * <li>Should have entries for all the keys required for this handler.</li>
    * </ol>
    *
    * @param keys A map of key names and their values. May not be <code>null
    * </code> or empty.
    *
    * @throws PSSystemValidationException if the validation fails.
    */
   public void validateKeys(Map keys)
      throws PSSystemValidationException
   {
      if(keys == null)
         throw new IllegalArgumentException("keys may not be null.");

      int numKeys = KEY_ENUM.length;

      //validates the minimum number of keys.
      if(keys.size() < numKeys )
      {
         throw new PSSystemValidationException(
            IPSServerErrors.INSUFFICIENT_NUM_CACHE_KEYS,
            new Object[] { String.valueOf(numKeys) });
      }

      //validate all required keys are present
      for(int i=0; i<numKeys; i++)
      {
         String keyName =  KEY_ENUM[i];

         if( !keys.containsKey( keyName ) )
         {
            throw new PSSystemValidationException(
               IPSServerErrors.MISSING_CACHE_KEY,
               new Object[] { keyName } );
         }
      }
   }

   /**
    * Determines if the request in the supplied cache context may be cached.
    * Only requests that identify query resources with caching enabled may be
    * cached.
    * <p>
    * It always returns <code>false</code> if the HTML parameter,
    * {@link IPSHtmlParameters#SYS_IS_RESOURCE_CACHE_OFF} equals
    * {@link IPSConstants#BOOLEAN_TRUE}.
    *
    * See base class for more info.
    *
    */
   public boolean isRequestCacheable(PSCacheContext context)
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");

      boolean result = false;

      result = (m_depTree.getSettings(context.getAppName(),
         context.getDataSetName()) != null);

      if (result)
      {
         // see if the resource cache is off for this request
         String isCacheOffParam =
            context.getRequest().getParameter(
               IPSHtmlParameters.SYS_IS_RESOURCE_CACHE_OFF);
         result = !IPSConstants.BOOLEAN_TRUE.equals(isCacheOffParam);
      }

      return result;
   }

   /**
    * Used to register for the appropriate change events so that dirty cache
    * items may be detected and automatically flushed.  Will register with any
    * <code>PSUpdateHandler</code> to be informed of all update events.  Builds
    * list of query resources to cache, and a tree of parent-child query
    * resource dependencies relationships for automatic flushing of parent pages
    * when child data is updated.
    *
    * See base class for more info.
    */
   void initHandler(IPSRequestHandler requestHandler)
   {
      if (requestHandler == null)
         throw new IllegalArgumentException("requestHandler may not be null");

      if (requestHandler instanceof PSQueryHandler)
      {
         PSQueryHandler qh = (PSQueryHandler)requestHandler;
         PSDataSet ds = qh.getDataSet();

         m_depTree.addResource(qh.getAppName(), ds);
      }
      else if (requestHandler instanceof PSUpdateHandler)
      {
         // we always want to be notified
         PSUpdateHandler uh = (PSUpdateHandler)requestHandler;
         uh.addTableChangeListener(this);
      }
   }

   // see base class
   void shutdownHandler(IPSRequestHandler requestHandler)
   {
      if (requestHandler instanceof PSQueryHandler)
      {
         PSQueryHandler qh = (PSQueryHandler)requestHandler;
         String appName = qh.getAppName();
         String dsName = qh.getDataSetName();
         m_depTree.removeResource(appName, dsName);
      }
   }

   /**
    * Flushes all cached responses.
    */
   void flush()
   {
      logFlushMessage(null);
      super.flush();
   }


   /**
    * Flushes cache items based on the supplied keys.  The keys supplied may
    * identify one or more cached items.  Any items identified by the supplied
    * keys are flushed.  The keys supplied to this handler must include each of
    * the following keys or else the request is ignored and the method simply
    * returns.
    *
    * <table>
    * <tr>
    * <th>Name</th><th>Description</th>
    * </tr>
    * <tr>
    * <td>AppName</td><td>The name of the application for which cached pages
    * should be flushed. </td>
    * <td>DatasetName</td><td>Pages cached for the specified dataset name should
    * be flushed.  If AppName is not supplied, and datasets in more than one
    * application have this name, pages cached for all of them will be flushed.
    * </td>
    * </tr>
    * </table>
    *
    * To omit a key, use <code>null</code> or an empty <code>String</code> for
    * the value of the entry.
    *
    * See base class for more info.
    */
   void flush(Map keys)
   {
      if (keys == null)
         throw new IllegalArgumentException("keys may not be null");

      //Check that the supplied keys are valid for this handler and flush them
      //only if they are valid.
      try {
         validateKeys( keys );

         Object[] keyset = new Object[KEY_SIZE];
         for(int i = 0; i < KEY_ENUM.length; i++)
         {
            Object value = keys.get( KEY_ENUM[i] );
            if(value != null && value.toString().trim().length() > 0)
               keyset[i] = value.toString().trim();
         }

         PSMultiLevelCache cache = getCache();
         if (cache != null)
         {
            logFlushMessage(keyset);
            cache.flush(keyset);
         }
      }
      catch(PSSystemValidationException e)
      {
         //ignore this as the keys may not be valid for this handler.
      }
   }

   // see base class
   void flushApplication(String appName)
   {
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");

      // if server is not case sensitive, appname keys are lowercased
      if (!PSServer.isCaseSensitiveURL())
         appName = appName.toLowerCase();

      Object[] keyset = new Object[KEY_SIZE];
      keyset[KEY_APP_INDEX] = appName;

      PSMultiLevelCache cache = getCache();
      if (cache != null)
      {
         logFlushMessage(keyset);
         cache.flush(keyset);
      }
   }

   /**
    * Returns a non-<code>null</code> empty iterator so all table changes
    * will trigger a notification, but will not cause the update handler to
    * generate column values. See {@link IPSTableChangeListener} for more
    * information.
    */
   public Iterator getColumns(@SuppressWarnings("unused") String tableName, 
      @SuppressWarnings("unused") int actionType)
   {
      return PSIteratorUtils.emptyIterator();
   }

   // see IPSTableChangeListener interface
   public void tableChanged(PSTableChangeEvent e)
   {
      flushDependencies(getCache(), m_depTree.getDatasetKeys(e.getTableName()));
   }

   /**
    * Extracts the required keyset from the provided context.  Key values
    * returned are as follows. For case-insensitive entries, they are lowercased
    * before they are added:
    * <ol>
    * <li>App name (case-sensitivity determined by server setting)</li>
    * <li>Dataset name </li>
    * <li>The following values concatenated together, delimited by a "/":
    * <ol>
    * <li>Protocol  (case-insensitive)
    * <li>Server/port (case-insensitive) - if port is 80, will not be added</li>
    * <li>Rhythmyx root (case-sensitivity determined by server's setting)</li>
    * <li>Page extension (case-insensitive)</li>
    * <li>all HTML params (case-sensitive), sorted by name in form name=value
    * <li>Any additional keys specified by the resource's cache settings.
    * </li>
    * </ol>
    * </li>
    *</ol>
    *
    * See base class for more info.
    */
   @SuppressWarnings("unchecked")
   protected Object[] getKeys(PSCacheContext context) throws PSCacheException
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");

      PSRequest request = context.getRequest();

      Object[] keys = new Object[KEY_SIZE];

      String appName = request.getAppName();
      String datasetName = context.getDataSetName();

      keys[KEY_APP_INDEX] = appName;
      keys[KEY_DATASET_INDEX] = datasetName;

      // those are the required keys.  if any are null, return null
      if (keys[KEY_APP_INDEX] == null ||
          keys[KEY_APP_INDEX].toString().trim().length() == 0 ||
          keys[KEY_DATASET_INDEX] == null)
      {
         return null;
      }

      // now build the composite key
      StringBuffer buf = new StringBuffer();

      // add protocol
      buf.append(request.getServletRequest().getScheme());
      buf.append(":");
      buf.append(KEY_SEP);

      // add host
      PSUserSession session = request.getUserSession();
      String host = session.getOriginalHost().toLowerCase();
      buf.append(host);
      buf.append(KEY_SEP);

      // add port if not default http port
      int port = session.getOriginalPort();
      if (port != 80);
      {
         buf.append(String.valueOf(port));
         buf.append(KEY_SEP);
      }

      // add rx root
      buf.append(PSServer.getRequestRoot());  // will be lowercase if required
      buf.append(KEY_SEP);

      // add resource name and extension, case insensitive
      buf.append(request.getRequestPage().toLowerCase());
      buf.append(KEY_SEP);

      // add all other html params, sorted by name, case-sensitive
      Map sortedParams = new TreeMap();
      sortedParams.putAll(request.getParameters());
      Iterator params = sortedParams.entrySet().iterator();
      while(params.hasNext())
      {
         Map.Entry entry = (Map.Entry)params.next();
         String key = entry.getKey().toString();

         buf.append(key);
         buf.append(PARAM_SEP);
         if (entry.getValue() != null)
            buf.append(entry.getValue().toString());
         buf.append(KEY_SEP);
      }

      // add any addtional keys specified
      try
      {
         PSExecutionData data = context.getExecutionData();
         Iterator extractors = m_depTree.getExtractors(appName, datasetName);
         while (extractors.hasNext())
         {
            IPSDataExtractor extractor = (IPSDataExtractor)extractors.next();
            IPSReplacementValue[] vals = extractor.getSource();
            if (vals != null)
            {
               for (int i = 0; i < vals.length; i++)
               {
                  buf.append(vals[i].getValueDisplayText());
                  buf.append(PARAM_SEP);
               }
            }

            buf.append(extractor.extract(data));
            buf.append(KEY_SEP);
         }
      }
      catch (PSDataExtractionException e)
      {
         throw new PSCacheException(IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION,
            e.getLocalizedMessage());
      }

      keys[KEY_COMPOSITE_INDEX] = buf.toString();

      return keys;
   }

   /**
    * Flushes all items specified by the provided list of dataset keys.
    *
    * @param cache The cache to flush, assumed not <code>null</code>.
    * @param dsKeys A list of <code>PSDataSetKey</code> objects.  Assumed not
    * <code>null</code>, may be empty.
    */
   private void flushDependencies(PSMultiLevelCache cache, Iterator dsKeys)
   {
      Object keys[] = new Object[KEY_SIZE];

      while (dsKeys.hasNext())
      {
         PSDataSetKey dsKey = (PSDataSetKey)dsKeys.next();
         keys[KEY_APP_INDEX] = dsKey.getAppName();
         keys[KEY_DATASET_INDEX] = dsKey.getDataSetName();
         logFlushMessage(keys);
         cache.flush(keys);
      }
   }

   /**
    * Type of request this handler will cache.  See {@link #getType()} for more
    * information.
    */
   public static final String HANDLER_TYPE = "Resource";


   /**
    * Static array of key names, each at the position in the array of its
    * corresponding index in a valid keyset.  Never <code>null</code>.
    */
   public static final String[] KEY_ENUM =
   {
         "appname",
         "datasetname"
   };

   /**
    * Constant for the key size this handler's cache will use.  Value is
    * currently <code>6</code>.   All <code>KEY_XXX_INDEX</code> values must be
    * between <code>0</code> and <code>KEY_SIZE - 1</code>, inclusive.
    */
   private static final int KEY_SIZE = 3;

   /**
    * Index into the keyset array for the application name.
    */
   private static final int KEY_APP_INDEX = 0;

   /**
    * Index into the keyset array for the dataset name
    */
   private static final int KEY_DATASET_INDEX = 1;

   /**
    * Index into the keyset array for the composite key value. This is all other
    * key values concatenated together.
    */
   private static final int KEY_COMPOSITE_INDEX = 2;

   /**
    * Constant for the separator used when building the composite key value.
    */
   private static final String KEY_SEP = "/";

   /**
    * Constant for the separator used between parameter names and values when
    * building the composite key value.
    */
   private static final String PARAM_SEP = "=";

   /**
    * Constant for the separator used when building the composite names.
    */
   private static final String NAME_SEP = "/";

   /**
    * Represents all runtime data for query resources that will be cached.
    * Never <code>null</code>, modified by calls to <code>initHandler()</code>
    * and <code>tableChangeShutdown()</code>
    */
   private PSResourceDependencyTree m_depTree = new PSResourceDependencyTree();
   
   /**
    * Map of view name to list of table names used by the view, never 
    * <code>null</code> or modified after construction.
    */
   private static Map<String, Collection<String>> ms_viewMap = 
      new HashMap<String, Collection<String>>();
   
   /**
    * Map of object type to associated table name to flush if an object of that
    * type is evicted from the hibernate cache
    */
   private static Map<PSTypeEnum, String> ms_tableTypeMap = 
      new HashMap<>();
   
   
   static
   {
      ms_tableTypeMap.put(PSTypeEnum.ACL, "PSX_ACLS");
      
      Collection<String> communityViewTableNames = new ArrayList<>();
      
      communityViewTableNames.add("PSX_ACLS");
      communityViewTableNames.add("PSX_ACLENTRIES");
      communityViewTableNames.add("PSX_ACLENTRYPERMISSIONS");
      communityViewTableNames.add("RXCOMMUNITY");
      ms_viewMap.put("PSX_COMMUNITY_PERMISSION_VIEW", communityViewTableNames);
      
      Collection<String> tableNames = new ArrayList<>();
      tableNames.add("PSX_DISPLAYFORMATPROPERTIES");
      tableNames.addAll(communityViewTableNames);
      ms_viewMap.put("PSX_DISPLAYFORMATPROPERTY_VIEW", tableNames);
      
      tableNames.clear();
      tableNames.add("RXMENUVISIBILITY");
      tableNames.addAll(communityViewTableNames);
      ms_viewMap.put("PSX_MENUVISIBILITY_VIEW", tableNames);
      
      tableNames.clear();
      tableNames.add("PSX_SEARCHPROPERTIES");
      tableNames.addAll(communityViewTableNames);
      ms_viewMap.put("PSX_SEARCHPROPERTIES_VIEW", tableNames);
      
      ms_viewMap.put("RXCONTENTTYPECOMMUNITY", communityViewTableNames);
      
      ms_viewMap.put("RXSITECOMMUNITY", communityViewTableNames);
      
      ms_viewMap.put("RXVARIANTCOMMUNITY", communityViewTableNames);
      
      ms_viewMap.put("RXWORKFLOWCOMMUNITY", communityViewTableNames);      
   }

   /**
    * Class to encapsulate the unique key for a dataset, its app name and
    * dataset name.
    */
   private class PSDataSetKey
   {
      /**
       * Construct a key.
       *
       * @param appName The app name, may not be <code>null</code> or empty.
       * @param dsName The dataset name, may not be <code>null</code> or empty.
       */
      public PSDataSetKey(String appName, String dsName)
      {
         if (appName == null || appName.trim().length() == 0)
            throw new IllegalArgumentException(
               "appName may not be null or empty");

         if (dsName == null || dsName.trim().length() == 0)
            throw new IllegalArgumentException(
               "dsName may not be null or empty");

         m_appName = appName;
         m_dsName = dsName;
      }

      /**
       * Get the appname portion of this key.
       *
       * @return The appname, never <code>null</code> or empty.
       */
      public String getAppName()
      {
         return m_appName;
      }

      /**
       * Get the dataset name portion of this key.
       *
       * @return The dataset name, never <code>null</code> or empty.
       */
      public String getDataSetName()
      {
         return m_dsName;
      }

      /**
       * Determines if the supplied object is equal to this one.
       *
       * @param obj An object, may be <code>null</code>.
       *
       * @return <code>true</code> if the supplied object is an instance of
       * <code>PSDataSetKey</code> with the same member values.
       */
      public boolean equals(Object obj)
      {
         boolean isEqual = true;
         if (!(obj instanceof PSDataSetKey))
            isEqual = false;
         else
         {
            PSDataSetKey other = (PSDataSetKey)obj;
            if (!m_appName.equals(other.m_appName))
               isEqual = false;
            else if (!m_dsName.equals(other.m_dsName))
               isEqual = false;
         }

         return isEqual;
      }

      /**
       * Returns a hash code value for the object. See
       * {@link java.lang.Object#hashCode() Object.hashCode()} for more info.
       */
      public int hashCode()
      {
         return m_appName.hashCode() + m_dsName.hashCode();
      }

      /**
       * Get the string representation of this object.
       *
       * @return A string in the format <appName>/<dsName>, never
       * <code>null</code> or empty.
       */
      public String toString()
      {
         return m_appName + "/" + m_dsName;
      }

      /**
       * The name of the application containing this dataset, initialized by the
       * ctor, never <code>null</code> or empty or modified after that.
       */
      private String m_appName;

      /**
       * The name of the dataset, initialized by the ctor, never
       * <code>null</code> or empty or modified after that.
       */
      private String m_dsName;
   }


   /**
    * Class to encapsulate runtime information about cached resources, and
    * dependencies between query resources and their child resource and table
    * depenedencies.
    */
   private class PSResourceDependencyTree
   {
      /**
       * Add a resource to this tree.
       *
       * @param appName The name of the app containing the dataset, may not be
       * <code>null</code> or empty
       * @param ds The dataset to add, may not be <code>null</code>.
       */
      public void addResource(String appName, PSDataSet ds)
      {
         if (appName == null || appName.trim().length() == 0)
            throw new IllegalArgumentException(
               "appName may not be null or empty");

         if (ds == null)
            throw new IllegalArgumentException("ds may not be null");

         PSDataSetKey dsKey = new PSDataSetKey(appName, ds.getName());

         // Synchronize access to all data while adding a resource
         synchronized (m_resourceMonitor)
         {
            // build map of table names to datasets
            PSCollection tableCol =
               ds.getPipe().getBackEndDataTank().getTables();

            Iterator tables = tableCol.iterator();
            while (tables.hasNext())
            {
               PSBackEndTable table = (PSBackEndTable)tables.next();
               Collection<String> allTables = expandViews(table.getTable());
               for (String tableName : allTables)
               {
                  List<PSDataSetKey> dataSets = m_tableMap.get(tableName);
                  if (dataSets == null)
                  {
                     dataSets = new ArrayList<>();
                     m_tableMap.put(tableName, dataSets);
                  }

                  dataSets.add(dsKey);
               }
            }

            // build map of dataset to resource name
            String resourceName = getResourceName(appName,
               ds.getRequestor().getRequestPage());
            m_resourceMap.put(dsKey, resourceName);


            PSPipe pipe = ds.getPipe();
            if (pipe instanceof PSQueryPipe) // should always be true
            {
               PSResourceCacheSettings settings =
                  ((PSQueryPipe)ds.getPipe()).getCacheSettings();
               if (settings.isCachingEnabled())
               {
                  // build map of dataset to cache settings
                  m_settingsMap.put(dsKey, settings);
                  initExtractors(settings, dsKey);

                  // build map of any child resource names to list of parent
                  // datasets names
                  Iterator children = settings.getDependencies();
                  while (children.hasNext())
                  {
                     String childName = (String)children.next();
                     List<PSDataSetKey> parents = m_parentMap.get(childName);
                     if (parents == null)
                     {
                        parents = new ArrayList<>();
                        m_parentMap.put(childName, parents);
                     }
                     parents.add(dsKey);
                  }
               }
            }
         }
      }

      /**
       * If the supplied table name represents a view, returns a list of tables
       * used by the view, otherwise returns a list with only the supplied
       * table name.
       *  
       * @param tableName The name of the table or view to check, assumed not 
       * <code>null</code> or empty, match is case insensitive.
       * 
       * @return The resulting list of table names, never <code>null</code> or 
       * empty.
       */
      private Collection<String> expandViews(String tableName)
      {
         Collection<String> result = new ArrayList<>();
         Collection<String> viewTables = ms_viewMap.get(
            tableName.toUpperCase());
         if (viewTables == null || viewTables.isEmpty())
            result.add(tableName);
         else
            result.addAll(viewTables);
         
         return result;
      }

      /**
       * Remove all stored information for a dataset.
       *
       * @param appName The name of the app containing the dataset.  May not be
       * <code>null</code> or empty.
       * @param datasetName The name of the datatset, may not be
       * <code>null</code> or empty.
       */
      public void removeResource(String appName, String datasetName)
      {
         if (datasetName == null || datasetName.trim().length() == 0)
            throw new IllegalArgumentException(
               "appName may not be null or empty");
         if (datasetName == null || datasetName.trim().length() == 0)
            throw new IllegalArgumentException(
               "appName may not be null or empty");

         PSDataSetKey dsKey = new PSDataSetKey(appName, datasetName);

         // Synchronize access to all data while removing a resource
         synchronized (m_resourceMonitor)
         {
            m_settingsMap.remove(dsKey);
            m_keyExtractors.remove(dsKey);
            String resourceName = m_resourceMap.remove(dsKey);

            // remove as child
            if (resourceName != null)
               m_parentMap.remove(resourceName);

            // remove as parent
            removeFromMapEntryList(m_parentMap, dsKey);

            // remove from tablemap
            removeFromMapEntryList(m_tableMap, dsKey);
         }
      }

      /**
       * Get keys for all datasets affected by updates to the specified
       * table.  Also includes keys for parent datasets of child resources
       * that are affected by the updates, recursively.
       *
       * @param tableName The name of the table that was updated.
       *
       * @return An interator over zero or more <code>PSDataSetKey</code>
       * objects, never <code>null</code>.
       */
      public Iterator getDatasetKeys(String tableName)
      {
         List<PSDataSetKey> result = new ArrayList<>();

         // get list of dataset names to flush for given table update.
         List<PSDataSetKey> dsList = new ArrayList<>();

         /*
            Check map of table to dataset names.  For each dataset, add to list.
            Recursively check for and add all parent datasets.  Synchronize
            use of lists to avoid modifications while walking.
         */
         synchronized (m_resourceMonitor)
         {
            List<PSDataSetKey> resources = m_tableMap.get(tableName);
            if (resources != null)
               dsList.addAll(resources);
         }

         // need separate list to avoid concurrent modifications
         List<PSDataSetKey> processed = new ArrayList<>();
         Iterator keys = dsList.iterator();
         while (keys.hasNext())
         {
            // add dataset if its cached
            PSDataSetKey dsKey = (PSDataSetKey)keys.next();
            if (m_settingsMap.containsKey(dsKey))
               result.add(dsKey);

            // now add parents.  Synchronize to avoid modifications of lists
            // stored in maps while walking them.
            synchronized(m_resourceMonitor)
            {
               result.addAll(getParents(dsKey, processed));
            }
         }

         return result.iterator();
      }

      /**
       * Get cache settings for the specified dataset.
       *
       * @param appName The name of the app containing the dataset, may not be
       * <code>null</code> or empty.
       * @param dsName The name of the dataset, may not be <code>null</code> or
       * empty.
       *
       * @return The settings, or <code>null</code> if caching not enabled for
       * the specified dataset.
       */
      public PSResourceCacheSettings getSettings(String appName, String dsName)
      {
         if (appName == null || appName.trim().length() == 0)
            throw new IllegalArgumentException(
               "appName may not be null or empty");

         if (dsName == null || dsName.trim().length() == 0)
            throw new IllegalArgumentException("dsName may not be null or empty");

         return m_settingsMap.get(new PSDataSetKey(
            appName, dsName));
      }

      /**
       * Get all extractors used to provide additional key values for the
       * specified resource.
       *
       * @param appName The name of the app containing the dataset, may not be
       * <code>null</code> or empty.
       * @param dsName The name of the dataset, may not be <code>null</code> or
       * empty.
       *
       * @return An iterator over zero or more <code>IPSDataExtractor</code>
       * objects, never <code>null</code>.
       */
      public Iterator getExtractors(String appName, String dsName)
      {
         if (appName == null || appName.trim().length() == 0)
            throw new IllegalArgumentException(
               "appName may not be null or empty");

         if (dsName == null || dsName.trim().length() == 0)
            throw new IllegalArgumentException(
               "dsName may not be null or empty");

         // return copy of list to avoid concurrent modification exceptions
         List<IPSDataExtractor> extractors = new ArrayList<>();
         PSDataSetKey dsKey = new PSDataSetKey(appName, dsName);
         synchronized(m_resourceMonitor)
         {
            List<IPSDataExtractor> intList = m_keyExtractors.get(dsKey);
            if (intList != null)
               extractors.addAll(intList);
         }

         return extractors.iterator();
      }

      /**
       * Recursively get parent datasets for the suplied dataset key.
       *
       * @param dsKey The key, assumed not <code>null</code>.
       * @param processed List of <code>PSDataSetKey</code> objects for datasets
       * whose parents have already been retrieved, to avoid infinite loops.
       * Assumed not <code>null</code>.
       *
       * @return List of parent <code>PSDataSetKey</code> objects, only those
       * that have caching enabled. Never <code>null</code>, may be empty.
       */
      private List<PSDataSetKey> getParents(PSDataSetKey dsKey, 
         List<PSDataSetKey> processed)
      {
         List<PSDataSetKey> result = new ArrayList<>();
         if (!processed.contains(dsKey)) // avoid infinite loops
         {
            processed.add(dsKey);

            // get resource name
            String resourceName = m_resourceMap.get(dsKey);
            if (resourceName != null)
            {
               // get parent datasets
               List parentList = m_parentMap.get(resourceName);
               if (parentList != null)
               {
                  Iterator parents = parentList.iterator();
                  while (parents.hasNext())
                  {
                     PSDataSetKey parent = (PSDataSetKey)parents.next();

                     // add if it is cached
                     if (m_settingsMap.containsKey(parent))
                        result.add(parent);

                     // recurse even if not cached
                     result.addAll(getParents(parent, processed));
                  }
               }
            }
         }

         return result;
      }

      /**
       * Removes the specified object from any map entry that contains it in its
       * value list.  If the map entry's value list is then empty, the entry is
       * removed from the map.
       *
       * @param map The map to process, where the key is a <code>String</code>
       * object, and the value is a <code>List</code> of objects, assumed not
       * <code>null</code>.
       * @param value The object to remove from the list contained by the value
       * of each entry in the supplied <code>map</code>.  May be
       * <code>null</code>.
       */
      @SuppressWarnings("unchecked")
      private void removeFromMapEntryList(Map map, Object value)
      {
         List removalList = new ArrayList();
         Iterator entries = map.entrySet().iterator();
         while (entries.hasNext())
         {
            Map.Entry entry = (Map.Entry)entries.next();
            List values = (List)entry.getValue();
            values.remove(value);
            if (values.isEmpty())
              removalList.add(entry.getKey());  // avoid concurrent mods
         }

         Iterator removals = removalList.iterator();
         while (removals.hasNext())
            map.remove(removals.next());
      }

      /**
       * Initializes extractors for any additional keys specified by the
       * supplied cache settings so they are available at runtime.
       *
       * @param settings The settings which may specify additional keys,
       * assumed not <code>null</code>.
       * @param dsKey The key to use to store and retrive the extractors,
       * assumed not <code>null</code>.
       */
      private void initExtractors(PSResourceCacheSettings settings,
         PSDataSetKey dsKey)
      {
         List<IPSDataExtractor> extractors = new ArrayList<>();
         Iterator extraKeys = settings.getAdditionalKeys();
         while (extraKeys.hasNext())
         {
            PSNamedReplacementValue val =
               (PSNamedReplacementValue)extraKeys.next();

            try
            {
               extractors.add(
                  PSDataExtractorFactory.createReplacementValueExtractor(val));
            }
            catch (IllegalArgumentException e)
            {
               throw new IllegalArgumentException(e.getLocalizedMessage());
            }
         }

         m_keyExtractors.put(dsKey, extractors);
      }

      /**
       * Get a full resource name (app and resource) to use as a key when
       * storing this value in a collection.
       *
       * @param appName The name of the app containing the resource, assumed not
       * <code>null</code> or empty.
       * @param resourceName The name of the resource, assumed not
       * <code>null</code> or empty.
       *
       * @return The full resource name, never <code>null</code> or empty.
       */
      private String getResourceName(String appName, String resourceName)
      {
         return appName + NAME_SEP + resourceName;
      }

      /**
       * Monitor object used synchronize all map entry access to avoid
       * concurrent modification exceptions on contained lists.  Never
       * <code>null</code> or modified.
       */
      private Object m_resourceMonitor = new Object();

      /**
       * Map of cache settings for each dataset that has caching enabled, where
       * key is a <code>PSDataSetKey</code> object, and the value is the
       * <code>PSResourceCacheSettings</code>.  Never <code>null</code>,
       * modified by calls to <code>addResource()</code> and
       * <code>removeResource()</code>.
       */
      private Map<PSDataSetKey, PSResourceCacheSettings> m_settingsMap = 
         new Hashtable<>();

      /**
       * Map of table names to list of dataset keys, never <code>null</code>,
       * modified by calls to <code>addResource()</code> and
       * <code>removeResource()</code>.  Key is the table name as a
       * <code>String</code>, value is a <code>List</code> of
       * <code>PSDataSetKey</code> objects.  Since this object contains lists,
       * access to this object must be synchronized to be thread safe and avoid
       * concurrent modification exceptions.
       */
      private Map<String, List<PSDataSetKey>> m_tableMap = 
         new HashMap<>();

      /**
       * Map of dataset to resource (page) names.  Key is the
       * <code>PSDataSetKey</code> object, value is corresponding page name from
       * its requestor as <code>String</code> objects.  Never <code>null</code>,
       * modified by calls to <code>addResource()</code> and
       * <code>removeResource()</code>.
       */
      private Map<PSDataSetKey, String> m_resourceMap = 
         new Hashtable<>();

      /**
       * Map of child resource (page) names to list of parent datasets, where
       * key is the child resource name as a <code>String</code> object, and
       * value is a <code>List</code> of <code>PSDataSetKey</code> objects.
       * Never <code>null</code>, modified by calls to
       * <code>addResource()</code> and <code>removeResource()</code>.  Since
       * this object contains lists, access to this object must be synchronized
       * to be thread safe and avoid concurrent modification exceptions.
       */
      private Map<String, List<PSDataSetKey>> m_parentMap = 
         new HashMap<>();

      /**
       * Map of additional key extractor for each cached resource.  Key is a
       * <code>PSDataSetKey</code> object, value is a <code>List</code> of
       * <code>IPSDataExtractor</code> objects. Never <code>null</code>,
       * modified by calls to <code>addResource()</code> and
       * <code>removeResource()</code>.  Since this object contains lists,
       * access to this object must be synchronized to be thread safe and avoid
       * concurrent modification exceptions.
       */
      private Map<PSDataSetKey, List<IPSDataExtractor>> m_keyExtractors = 
         new HashMap<>();
   }
}
