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
package com.percussion.server.cache;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.IPSEditorChangeListener;
import com.percussion.cms.IPSRelationshipChangeListener;
import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.data.IPSTableChangeListener;
import com.percussion.data.PSTableChangeEvent;
import com.percussion.data.PSUpdateHandler;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSServerCacheSettings;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.server.PSUserSessionManager;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Handles the caching of Content Assembler pages.  Will only cache pages with
 * a request URL that matches a registered content variant using the ASSEMBLYURL
 * column of the PSX_TEMPLATE table.  Manages the automatic flushing of stale
 * or dirty pages in the cache by monitoring all content editor resource
 * for modify and workflow requests, the relationship handler for any
 * relationship changes or any update resources that delete from the
 * CONTENTSTATUS table.
 */
@SuppressWarnings("unchecked")
public class PSAssemblerCacheHandler extends PSCacheHandler
   implements IPSTableChangeListener, IPSEditorChangeListener,
      IPSRelationshipChangeListener

{
   /**
    * Constructs an instance of this handler.
    *
    * @param cacheSettings The server cache settings.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>cacheSettings</code> is
    * <code>null</code>.
    * @throws IllegalStateException if the <code>PSCacheManager</code> has not
    * been initialized.
    * @throws RuntimeException if there are any errors loading the key rules
    * resource file.
    *
    * @todo In future, handle resource file errors gracefully and disable
    * caching.
    */
   public PSAssemblerCacheHandler(PSServerCacheSettings cacheSettings)
   {
      super(KEY_SIZE, cacheSettings);

      // load resource file
      try(InputStream stream = PSKeyRules.class.getResourceAsStream(
         KEY_RULES_FILENAME)) {
         if (null == stream)
            throw new RuntimeException("Cannot locate resource: " +
                    KEY_RULES_FILENAME);
            Document doc = PSXmlDocumentBuilder.createXmlDocument(stream, false);
            Element root = doc.getDocumentElement();
            m_keyRules = new PSKeyRules(root);

      } catch (IOException | SAXException | PSUnknownNodeTypeException | PSNotFoundException | PSExtensionException e) {
         throw new RuntimeException("Error loading key rules resource file: "
                 + e.getLocalizedMessage());
      }
   }

   /**
    * Makes an internal request to load data from the content variants table and
    * creates the dependency tree.  Must be called before any listener events
    * or cache requests will be processed.
    *
    * @throws PSCacheException if there are any errors.
    */
   void start() throws PSCacheException
   {
      synchronized(m_variantMapMonitor)
      {
         loadContentVariants(m_variants);
         m_dependencyTree = new PSContentItemDependencyTree();
      }
   }

   /**
    * Determines if the request in the supplied cache context may be cached.
    * Only requests that match a registered content variant may be cached.
    * <p>
    * It always returns <code>false</code> if the HTML parameter,
    * {@link IPSHtmlParameters#SYS_IS_ASSEMBLER_CACHE_OFF} equals
    * {@link IPSConstants#BOOLEAN_TRUE}.
    * 
    * See base class for more info.
    *
    */
   public boolean isRequestCacheable(PSCacheContext context)
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");

      boolean isCacheable = false;
      
      PSRequest request = context.getRequest();
      if (!request.getUserSession().isAnonymous())
      {
         // see if app and requestpage (no extension) are in the variants map
         isCacheable = getVariantsMap().containsValue(request.getAppName() + 
            URL_SEP + request.getRequestPage(false));
            
         if (isCacheable)
         {
            // see if the assembler cache is off for this request
            String isCacheOffParam =
               request.getParameter(
                  IPSHtmlParameters.SYS_IS_ASSEMBLER_CACHE_OFF);
            isCacheable = !IPSConstants.BOOLEAN_TRUE.equals(isCacheOffParam);
         }
      }

      return isCacheable;
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
    * Used to register for the appropriate change events so that dirty cache
    * items may be detected and automatically flushed.  Will register with all
    * instances of <code>PSContentEditorHandler</code>,
    * <code>PSRelationshipCommandHandler</code> and any
    * <code>PSUpdateHandler</code> that will update the PSX_TEMPLATE or
    * CONTENTSTATUS tables.
    *
    * See base class for more info.
    */
   void initHandler(IPSRequestHandler requestHandler)
   {
      if (requestHandler == null)
         throw new IllegalArgumentException("requestHandler may not be null");

      if (requestHandler instanceof PSContentEditorHandler)
      {
         // add this instance as listener to all content editors
         PSContentEditorHandler ceh = (PSContentEditorHandler)requestHandler;
         ceh.addEditorChangeListener(this);

         /**
          * @todo uncomment the following code once all applications,
          *    stylesheets use the relationship command handler for relationship
          *    changes.
          */
         // ceh.addRelationshipChangeListener(this);
      }
      else if (requestHandler instanceof PSUpdateHandler)
      {
         // see if the update handler contains a table we care about.
         PSUpdateHandler uh = (PSUpdateHandler)requestHandler;
         PSDataSet ds = uh.getDataSet();
         PSCollection tableCol = ds.getPipe().getBackEndDataTank().getTables();

         boolean added = false;
         Iterator tables = tableCol.iterator();
         while (tables.hasNext() && !added)
         {
            PSBackEndTable table = (PSBackEndTable)tables.next();
            Map actions = (Map)m_updateTables.get(table.getTable());
            if (actions != null)
            {
               // we want to be notified
               uh.addTableChangeListener(this);
               added = true;
            }
         }
      }
   }

   // see base class
   void shutdownHandler(IPSRequestHandler requestHandler)
   {
      // noop
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
    * <td>AppName</td><td>The name of the application containing the assembler
    * for which cached pages should be flushed. Usually provided as the only
    * key to flush the cache when a particular application is shut down.</td>
    * <td>ContentId</td><td>The Contentid of the item.  Any assembler pages
    * based on an item with this content id will be flushed.</td>
    * <td>RevisionId</td><td>The revision id of the item to flush.  May only
    * be specified if <code>ContentId</code> is also specified. Will flush a
    * particular revision of the specified item.</td>
    * <td>VariantId</td><td>Identifies a particular variant.  If provided alone,
    * will flush all pages cached with this variant id.  If provided along with
    * <code>Contentid</code> and <code>RevisionId</code>, will flush the
    * assembler pages for the specified item that were built using the
    * provided variant id.</td>
    * <td>SessionId</td><td>Identifies a user session.  It will flush the
    * assembler pages for any items matching other supplied keys that are cached
    * under this session id.</td>
    * </tr>
    * </table>
    *
    * To omit a key, use <code>null</code> or an empty
    * <code>String</code> for the value of the entry.  For example, to flush
    * all items with a particular variant id, pass <code>null</code> for the
    * value of each key entry in the Map except for <code>VariantId</code>.
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

   // see base class
   void flushSession(String sessionId)
   {
      if (sessionId == null || sessionId.trim().length() == 0)
         throw new IllegalArgumentException(
            "sessionId may not be null or empty");

      // only flush if session still exists and is cacheable
      PSUserSession sess = PSUserSessionManager.getUserSession(sessionId);
      if (sess != null && !sess.isAnonymous())
      {
         Object[] keyset = new Object[KEY_SIZE];
         keyset[KEY_SESSIONID_INDEX] = sessionId;
   
         PSMultiLevelCache cache = getCache();
         if (cache != null)
         {
            logFlushMessage(keyset);
            cache.flush(keyset);
         }
      }
   }

   // see IPSTableChangeListener interface
   public Iterator getColumns(String tableName, int actionType)
   {
      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      Iterator columns = null;
      Map actionMap = (Map)m_updateTables.get(tableName);
      if (actionMap != null)
      {
         List colList = (List)actionMap.get(new Integer(actionType));
         if (colList != null)
            columns = colList.iterator();
      }

      return columns;
   }

   // see IPSTableChangeListener interface
   public void tableChanged(PSTableChangeEvent e)
   {
      if (e == null)
         throw new IllegalArgumentException("event may not be null");

      String table = e.getTableName();

      PSMultiLevelCache cache = getCache();
      if (cache == null)
         return;

      if (table.equals(IPSConstants.CONTENT_STATUS_TABLE))
         handleContentStatusChange(cache, e);
      else if (table.equals(IPSConstants.CONTENT_VARIANTS_TABLE))
         handleContentVariantChange(cache, e);
      else if (table.equals(IPSConstants.RXASSEMBLERPROPERTIES_TABLE))
         handleAssemblerPropertiesChange(cache, e);
   }

   // see IPSEditorChangeListener interface
   public void editorChanged(PSEditorChangeEvent e)
   {
      if (e == null)
         throw new IllegalArgumentException("event may not be null");

      PSMultiLevelCache cache = getCache();
      if (cache == null)
         return;

      int contentid = e.getContentId();
      int revisionid = e.getRevisionId();
      String strContentId = String.valueOf(contentid);
      String  strRevisionId = String.valueOf(revisionid);

      // flush the object in question and get the dependencies
      Object[] keys = new Object[KEY_SIZE];
      keys[KEY_CONTENTID_INDEX] = strContentId;

      switch (e.getActionType())
      {
         case PSEditorChangeEvent.ACTION_UPDATE:
         case PSEditorChangeEvent.ACTION_DELETE:
            // flush the specific revision and its parents
            keys[KEY_REVISIONID_INDEX] = strRevisionId;
            logFlushMessage(keys);
            cache.flush(keys);
            flushDependencies(cache, m_dependencyTree.getDependentItems(
               contentid, revisionid, -1));
            break;
         case PSEditorChangeEvent.ACTION_CHECKOUT:
         case PSEditorChangeEvent.ACTION_TRANSITION:
         case PSEditorChangeEvent.ACTION_CHECKIN:
            // flush all revisions and their parents
            logFlushMessage(keys);
            cache.flush(keys);
            // flush dependents of all revisions
            flushDependencies(cache, m_dependencyTree.getDependentItems(
               contentid, -1, -1));
            break;
      }
   }

   // see IPSRelationshipChangeListener interface
   public void relationshipChanged(PSRelationshipChangeEvent event)
   {
      PSMultiLevelCache cache = getCache();
      if (cache == null)
         return;

      List depKeys;
      Map done = new HashMap();
      List allKeys = new ArrayList();
      Iterator relationships = event.getRelationships().iterator();
      while (relationships.hasNext())
      {
         PSRelationship relationship = (PSRelationship) relationships.next();

         /**
          * We are only interested in relationship changes for Active Assembly
          * categories.
          */
         String category = relationship.getConfig().getCategory();
         if (category == null ||
            !category.equals(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY))
            continue;

         switch (event.getAction())
         {
            case PSRelationshipChangeEvent.ACTION_ADD:
            case PSRelationshipChangeEvent.ACTION_MODIFY:
            {
               int rid = relationship.getId();
               int ownerid = relationship.getOwner().getId();
               int ownerrevision = relationship.getOwner().getRevision();
               int dependentid = relationship.getDependent().getId();
               int variantid = -1;
               try
               {
                  variantid = Integer.parseInt(
                     relationship.getProperty(IPSHtmlParameters.SYS_VARIANTID));
               }
               catch (NumberFormatException e)
               {
                  // this should never happen
               }

               // flush the item
               Object[] keys = new Object[KEY_SIZE];
               keys[KEY_CONTENTID_INDEX] = Integer.toString(ownerid);
               keys[KEY_REVISIONID_INDEX] = Integer.toString(ownerrevision);
               logFlushMessage(keys);
               cache.flush(keys);

               // update the tree and flush anything related
               if (event.getAction() == event.ACTION_ADD)
                  depKeys = m_dependencyTree.addDependency(rid, ownerid,
                        ownerrevision, dependentid, variantid, done);
               else
                  depKeys = m_dependencyTree.updateDependency(rid, ownerid,
                        ownerrevision, dependentid, variantid, done);
               allKeys.addAll(depKeys);
               break;
            }

            case PSRelationshipChangeEvent.ACTION_REMOVE:
            {
               // update the tree and flush anything related - this will include
               // the item whose related content was removed.
               depKeys = m_dependencyTree.removeDependency(relationship.getId(), done);
               allKeys.addAll(depKeys);
               break;
            }
         }
      }
      
      flushDependencies(cache, allKeys);         
   }

   /**
    * Tests if the supplied columns are addressing a variant id row or not.
    *
    * @param columns the columns to test, assumed not <code>null</code>.
    * @return <code>true</code> if this is addressing a variant id row,
    *    <code>false</code> otherwise.
    */
   boolean isVariantIdRow(Map columns)
   {
      String propertyName = (String) columns.get(IPSConstants.RS_PROPERTYNAME);

      return (propertyName != null &&
         propertyName.equals(IPSHtmlParameters.SYS_VARIANTID));
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
    * <li>The contentid, revisionid and variantid must be numbers.</li>
    * <li>The revision id may not be specified if the contentid is not
    * specified.</li>
    * </ol>
    *
    * @param keys A map of key names and their values. May not be <code>null
    * </code> or empty.
    *
    * @throws IllegalArgumentException if keys is <code>null</code>.
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

      //validate all required keys are present and the values are valid.
      for(int i=0; i<numKeys; i++)
      {
         String keyName =  KEY_ENUM[i];

         if( !keys.containsKey( keyName ) )
         {
            throw new PSSystemValidationException(
               IPSServerErrors.MISSING_CACHE_KEY,
               new Object[] { keyName } );
         }

         if(i == KEY_CONTENTID_INDEX || i == KEY_REVISIONID_INDEX ||
            i == KEY_VARIANTID_INDEX)
         {
            String value = (String)keys.get( keyName );
            if(value != null && value.trim().length() > 0)
            {
               try {
                  Integer.parseInt(value);
               }
               catch(NumberFormatException e)
               {
                  throw new PSSystemValidationException(
                     IPSServerErrors.INVALID_NUMBER_CACHE_KEY,
                     new Object[] { value, keyName } );
               }
            }
         }
      }

      // validate no revision passed if no contentid passed
      String contentid = (String)keys.get(KEY_ENUM[KEY_CONTENTID_INDEX]);
      String revisionid = (String)keys.get(KEY_ENUM[KEY_REVISIONID_INDEX]);
      if ((contentid == null || contentid.trim().length() == 0) &&
         (revisionid != null) && revisionid.trim().length() > 0)
      {
         throw new PSSystemValidationException(
            IPSServerErrors.INVALID_REVISION_CACHE_KEY );
      }
   }


   /**
    * Handles a delete from the CONTENTSTATUS table.  Flushes all revisions
    * of the item deleted, and all dependent items as well.
    *
    * @param cache The cache to flush, assumed not <code>null</code>.
    * @param e The event, assumed to be not <code>null</code> and for the
    * CONTENTSTATUS table.
    */
   private void handleContentStatusChange(PSMultiLevelCache cache,
      PSTableChangeEvent e)
   {
      if (e.getActionType() == PSTableChangeEvent.ACTION_DELETE)
      {
         // get the content id
         String strContentId =
            (String)e.getColumns().get(IPSConstants.ITEM_PKEY_CONTENTID);
         if (strContentId != null)
         {
            // flush all items with this content id
            Object[] keys = new Object[KEY_SIZE];
            keys[KEY_CONTENTID_INDEX] = strContentId;
            logFlushMessage(keys);
            cache.flush(keys);

            // now flush anything dependent on it
            int contentid;
            try
            {
               contentid = Integer.parseInt(strContentId);
               flushDependencies(cache, m_dependencyTree.getDependentItems(
                  contentid, -1, -1));
            }
            catch (NumberFormatException n)
            {
               // ignore
            }
         }

      }
   }

   /**
    * Handles a change to the PSX_TEMPLATE table.  Flushes all pages
    * with the changed variantid, and all items dependent on that item.
    *
    * @param cache The cache to flush, assumed not <code>null</code>.
    * @param e The event, assumed to be not <code>null</code> and for the
    * PSX_TEMPLATE table.
    */
   private void handleContentVariantChange(PSMultiLevelCache cache,
      PSTableChangeEvent e)
   {
      // need to update variants map
      Map columns = e.getColumns();
      String strVariantId = (String)columns.get(IPSConstants.VARIANTID_COLUMN);
      if (strVariantId != null)
      {
         boolean changed = true;
         switch (e.getActionType())
         {
            case PSTableChangeEvent.ACTION_INSERT:
            case PSTableChangeEvent.ACTION_UPDATE:
               // get assembler url and update map
               String assemblyUrl =
                  (String)columns.get(IPSConstants.ASSEMBLYURL_COLUMN);
               if (assemblyUrl != null)
               {
                  String reqPage = getRequestPage(assemblyUrl);
                  if (reqPage != null)
                     getVariantsMap().put(strVariantId, reqPage);
               }
               break;
            case PSTableChangeEvent.ACTION_DELETE:
               // remove from map
               getVariantsMap().remove(strVariantId);
               break;
            default:
               changed = false;
         }
         if (changed)
         {
            // need to flush this variant and anything dependent on an item
            // with this variant
            Object[] keys = new Object[KEY_SIZE];
            keys[KEY_VARIANTID_INDEX] = strVariantId;
            logFlushMessage(keys);
            cache.flush(keys);
            try
            {
               int variantid = Integer.parseInt(strVariantId);
               flushDependencies(cache, m_dependencyTree.getDependentItems(
                  -1, -1, variantid));
            }
            catch (NumberFormatException n)
            {
               // ignore
            }
         }
      }
   }

   /**
    * Handles a change to the RXASSEMBLERPROPERTIES table.
    * If any of the assembler properties (context variables) change
    * then it is not feasible to determine, which page got affected and which didn't.
    * Assuming that such changes do not happen often we can justify
    * flushing the whole cache.
    *
    * @param cache The cache to flush, assumed not <code>null</code>.
    * @param e The event, assumed to be not <code>null</code> and for the
    * RXASSEMBLERPROPERTIES table.
    */
   @SuppressWarnings("unused")
   private void handleAssemblerPropertiesChange( PSMultiLevelCache cache,
      PSTableChangeEvent e)
   {
      //flush everything
      flush();
   }

   /**
    * Flushes all items in the provided dependency list.
    *
    * @param cache The cache to flush, assumed not <code>null</code>.
    * @param deps A list of contentid-revisionid pairs.  Each entry in the list
    * is a <code>String[2]</code> where the first entry is the contentid and the
    * second is the revisionid.  Assumed not <code>null</code>, may be empty.
    */
   private void flushDependencies(PSMultiLevelCache cache, List deps)
   {
      Object keys[] = new Object[KEY_SIZE];

      Iterator i = deps.iterator();
      while (i.hasNext())
      {
         String[] depKeys = (String[])i.next();
         keys[KEY_CONTENTID_INDEX] = depKeys[0];
         keys[KEY_REVISIONID_INDEX] = depKeys[1];
         logFlushMessage(keys);
         cache.flush(keys);
      }
   }

   /**
    * Extracts the required keyset from the provided context.  Key values
    * returned are as follows. For case-insensitive entries, they are lowercased
    * before they are added:
    * <ol>
    * <li>App name (case-sensitivity determined by server setting)</li>
    * <li>Content Id </li>
    * <li>Revision Id </li>
    * <li>Variant Id</li>
    * <li>Session Id</li>
    * <li>The following values concatenated together, delimited by a "/":
    * <ol>
    * <li>Protocol  (case-insensitive)
    * <li>Server/port (case-insensitive) - if port is 80, will not be added</li>
    * <li>Rhythmyx root (case-sensitivity determined by server's setting)</li>
    * <li>Resource Name including extension (case-insensitive)</li>
    * <li>all HTML params (case-sensitive), sorted by name in form name=value
    * </li>
    * </ol>
    * </li>
    *</ol>
    *
    * See base class for more info.
    */
   protected Object[] getKeys(PSCacheContext context) throws PSCacheException
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");

      PSRequest request = context.getRequest();

      Object[] keys = new Object[KEY_SIZE];

      keys[KEY_APP_INDEX] = request.getAppName();

      keys[KEY_CONTENTID_INDEX] = request.getParameter(
         IPSHtmlParameters.SYS_CONTENTID);
      keys[KEY_REVISIONID_INDEX] = request.getParameter(
         IPSHtmlParameters.SYS_REVISION);
      keys[KEY_VARIANTID_INDEX] = request.getParameter(
         IPSHtmlParameters.SYS_VARIANTID);

      // those are the required keys.  if any are null, return null
      if (keys[KEY_APP_INDEX] == null ||
          keys[KEY_APP_INDEX].toString().trim().length() == 0 ||
          keys[KEY_CONTENTID_INDEX] == null ||
          keys[KEY_REVISIONID_INDEX] == null ||
          keys[KEY_VARIANTID_INDEX] == null)
      {
         return null;
      }

      // add session id if this request meets the rule, otherwise use default
      // session id (essentially not cached by session)
      if (m_keyRules.isIncluded(KEY_ENUM[KEY_SESSIONID_INDEX], request))
         keys[KEY_SESSIONID_INDEX] = request.getUserSessionId();
      else
         keys[KEY_SESSIONID_INDEX] = NO_SESSION_ID;

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

      // add all other params, sorted by name, case-sensitive
      Map sortedParams = new TreeMap();
      sortedParams.putAll(request.getParameters());
      Iterator params = sortedParams.entrySet().iterator();
      while(params.hasNext())
      {
         Map.Entry entry = (Map.Entry)params.next();
         String key = entry.getKey().toString();

         // exclude session id and psredirect, also skip all params already used
         if (key.equals(IPSHtmlParameters.SYS_SESSIONID) ||
            key.equals(IPSHtmlParameters.DYNAMIC_REDIRECT_URL) ||
            key.equals(IPSHtmlParameters.SYS_CONTENTID) ||
            key.equals(IPSHtmlParameters.SYS_REVISION) ||
            key.equals(IPSHtmlParameters.SYS_VARIANTID))
         {
            continue;
         }

         buf.append(key);
         buf.append(PARAM_SEP);
         buf.append(entry.getValue().toString());
         buf.append(KEY_SEP);
      }

      keys[KEY_COMPOSITE_INDEX] = buf.toString();

      return keys;
   }

   /**
    * Queries the backend for all content variants and stores them in the
    * provided map.  Expects a document from the internal request conforming
    * to the following:
    * <pre><code>
    *    <!--
    *       Contains a list of zero or more contentvariant elements.
    *    -->
    *    <!ELEMENT cachevariants (contentvariant* )>
    *
    *    <!--
    *       Represents a content variant registration.
    *
    *       Attributes:
    *       variantid - The unique id used to identify this variant.
    *       assemblyurl - The url supported by this variant, specified the
    *          application and resource name, expected to be in the form
    *          ../<appName>/<requestPage>[.<ext>]
    *    -->
    *    <!ELEMENT contentvariant EMPTY>
    *    <!ATTLIST contentvariant
    *       variantid CDATA #REQUIRED
    *       assemblyurl CDATA #REQUIRED
    *    >
    * </code></pre>
    *
    * @param variantMap The map to which the variants will be added, may not be
    * <code>null</code>.  Map is cleared before adding the enties.  Key is the
    * variantid as an <code>String</code>, and the value is the assembly url as
    * a <code>String</code>.  Assumed not <code>null</code>.
    *
    * @throws PSCacheException if there is an problem making the request to the
    * backend.
    */
   private void loadContentVariants(Map variantMap)
      throws PSCacheException
   {
      // clear the map
      variantMap.clear();

      // make the request
      PSRequest req = PSRequest.getContextForRequest();
      PSInternalRequest iReq = PSServer.getInternalRequest(VARIANT_REQUEST_NAME,
         req, null, false);
      if (iReq == null)
      {
         throw new PSCacheException(
            IPSServerErrors.CACHE_NO_INTERNAL_REQUEST_HANDLER,
            VARIANT_REQUEST_NAME);
      }

      Document doc = null;
      try
      {
         doc = iReq.getResultDoc();
      }
      catch (Exception e)
      {
         throw new PSCacheException(
            IPSServerErrors.CACHE_INTERNAL_REQUEST_FAILURE, new Object[] {
               VARIANT_REQUEST_NAME, e.getLocalizedMessage()});
      }

      NodeList variants = doc.getElementsByTagName(VARIANT_NODE_NAME);
      for (int i = 0; i < variants.getLength(); i++)
      {
         Element variant = (Element)variants.item(i);
         String reqPage = getRequestPage(variant.getAttribute(
            ASSEMBLY_URL_ATTR));
         if (reqPage != null)
            variantMap.put(variant.getAttribute(VARIANT_ID_ATTR),
               reqPage);
      }
   }

   /**
    * Parses assembly url of a content variant and returns the base request
    * page (without the extension) in the form of <code>appname/pagename</code>.
    *
    * @param url The assembly url, assumed not <code>null</code>.  Forms that
    * are expected are the following:
    * <ul>
    * <li>../appname/pagename[.ext]</li>
    * <li>/Rhythmyx/appname/pagename[.ext]</li>
    * </ul>
    *
    * @return The resulting page, may be <code>null</code> if the provided url
    * cannot be parsed.  If server is not case-sensitive, appname in result is
    * lowercase.
    */
   private String getRequestPage(String url)
   {
      StringBuffer requestPage = new StringBuffer();
      boolean gotPage = false;
      if (url.trim().length() > 0)
      {
         // strip off first slash if there is one
         if (url.startsWith(URL_SEP) && url.length() > 1)
         {
            url = url.substring(1);
         }

         StringTokenizer tok = new StringTokenizer(url, URL_SEP);
         // first token should be rxroot or ".."
         String token = EMPTY_STRING;
         if (tok.hasMoreTokens())
            token = tok.nextToken();
         if ((token.equals(PSServer.getRequestRoot()) || token.equals("..")) &&
            tok.hasMoreTokens())
         {
            // next token is the appname
            if (!PSServer.isCaseSensitiveURL())
               requestPage.append(tok.nextToken().toLowerCase());
            else
               requestPage.append(tok.nextToken());

            // now add the page
            while (tok.hasMoreTokens())
            {
               gotPage = true;
               requestPage.append(URL_SEP);
               String pageName = tok.nextToken();
               // see if we need to strip the extension
               int dotPos = pageName.indexOf('.');
               if (dotPos != -1)
                  pageName = pageName.substring(0, dotPos);
               requestPage.append(pageName);
            }
         }
      }

      return gotPage ? requestPage.toString() : null;
   }

   /**
    * Provides synchronized access to the variants map.
    *
    * @return The map, never <code>null</code>.
    */
   private Map getVariantsMap()
   {
      synchronized(m_variantMapMonitor)
      {
         return m_variants;
      }
   }

   /**
    * The dependency tree used to autoflush dependent pages based on different
    * change events that may occur.  Intialized during construction, never
    * <code>null</code> or modified after that.
    */
   private PSContentItemDependencyTree m_dependencyTree;

   /**
    * Map of all registered content variants.  Variants are added during ctor,
    * and updated by calls to <code>tableChanged()</code>.  Key is the variant
    * id as a <code>String</code>, and the value is the assembly request as a
    * <code>String</code> in the form <appname>/<requestpage>.  Never
    * <code>null</code>.
    */
   private Map m_variants = new HashMap();

   /**
    * Monitor object to synchronize access to variant map.  Never
    * <code>null</code>.
    */
   private Object m_variantMapMonitor = new Object();

   /**
    * Key rules used to determine if optional keys should be used for a
    * particular request.  Currently the only key that is optional is
    * <code>sessionid</code>.  Intialized during construction, never
    * <code>null</code> or modified after that.
    */
   private PSKeyRules m_keyRules = null;

   /**
    * Constant for the key size this handler's cache will use.  Value is
    * currently <code>6</code>.   All <code>KEY_XXX_INDEX</code> values must be
    * between <code>0</code> and <code>KEY_SIZE - 1</code>, inclusive.
    */
   private static final int KEY_SIZE = 6;

   /**
    * Index into the keyset array for the application name.
    */
   private static final int KEY_APP_INDEX = 0;

   /**
    * Index into the keyset array for the content id
    */
   private static final int KEY_CONTENTID_INDEX = 1;

   /**
    * Index into the keyset array for the revision id
    */
   private static final int KEY_REVISIONID_INDEX = 2;

   /**
    * Index into the keyset array for the variant id
    */
   private static final int KEY_VARIANTID_INDEX = 3;

   /**
    * Index into the keyset array for the session id
    */
   private static final int KEY_SESSIONID_INDEX = 4;

   /**
    * Index into the keyset array for the composite key value. This is all other
    * key values concatenated together.
    */
   private static final int KEY_COMPOSITE_INDEX = 5;

   /**
    * Static array of key names, each at the position in the array of its
    * corresponding index in a valid keyset.  Never <code>null</code>.
    */
   public static final String[] KEY_ENUM =
   {
         "appname",
         "contentid",
         "revisionid",
         "variantid",
         "sessionid"
   };

   /**
    * Type of request this handler will cache.  See {@link #getType()} for more
    * information.
    */
   public static final String HANDLER_TYPE = "Assembler";

   /**
    * Default value used to cache items without regard to session id.
    */
   private static final String NO_SESSION_ID = "-1";

   /**
    * Constant for an empty string.
    */
   private static final String EMPTY_STRING = "";

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
    * Request name for app and resource to query all content variants.
    */
   private static final String VARIANT_REQUEST_NAME =
      "sys_Variants/cachevariants.xml";

   /**
    * Constant for element in query result doc defining a variant.
    */
   private static final String VARIANT_NODE_NAME = "contentvariant";

   /**
    * Constant for attribute of element in query result doc defining a variant
    * that contains the variant id.
    */
   private static final String VARIANT_ID_ATTR = "variantid";

   /**
    * Constant for attribute of element in query result doc defining a variant
    * that contains the assembly url.
    */
   private static final String ASSEMBLY_URL_ATTR = "assemblyurl";

   /**
    * Constant for / separator used in urls
    */
   private static final String URL_SEP = "/";

   /**
    * Name of the file containing the key rule definitions.
    */
   private static final String KEY_RULES_FILENAME = "PSAssemblerKeyRules.xml";

   /**
    * Map of update tables and the columns to get values for when registering
    * as a <code>IPSTableChangeListener</code>, by table name and action.
    * Key is the table name as a <code>String</code>, value is a
    * <code>Map</code> of <code>List</code> objects. The key is the
    * <code>PSTableChangeEvent.ACTION_xxx</code> as an <code>Integer</code>.
    * The value is a list of names to get values for in the event as
    * <code>String</code> objects.  Initialized by static intializer, never
    * <code>null</code> or modified after that.
    */
   private static Map m_updateTables;

   static
   {
      // initialize the update tables map
      m_updateTables = new HashMap(4);
      
      List colList;
      Map actionMap;

      // create lists for CONTENTSTATUS - just need delete
      actionMap = new HashMap(1);
      colList = new ArrayList(1);
      colList.add(IPSConstants.ITEM_PKEY_CONTENTID);
      actionMap.put(new Integer(PSTableChangeEvent.ACTION_DELETE), colList);
      m_updateTables.put(IPSConstants.CONTENT_STATUS_TABLE, actionMap);

      /**
       * @todo: remove the following code for relationship tables once all
       *    applications, stylesheets use the relationship command handler
       *    for relationship changes.
       */
      // create lists for {@link IPSConstants#PSX_RELATIONSHIPS} table
      actionMap = new HashMap(1);
      colList = new ArrayList(1);
      colList.add(IPSConstants.RS_RID);
      actionMap.put(new Integer(PSTableChangeEvent.ACTION_DELETE), colList);
      m_updateTables.put(IPSConstants.PSX_RELATIONSHIPS, actionMap);

      // create lists for {@link IPSConstants#PSX_RELATIONSHIPPROPERTIES} table
      actionMap = new HashMap(1);
      colList = new ArrayList(2);
      colList.add(IPSConstants.RS_RID);
      colList.add(IPSConstants.RS_PROPERTYNAME);
      actionMap.put(new Integer(PSTableChangeEvent.ACTION_INSERT), colList);
      actionMap.put(new Integer(PSTableChangeEvent.ACTION_UPDATE), colList);
      m_updateTables.put(IPSConstants.PSX_RELATIONSHIPPROPERTIES, actionMap);

      // create lists for PSX_TEMPLATE
      actionMap = new HashMap(3);
      colList = new ArrayList(2);
      colList.add(IPSConstants.VARIANTID_COLUMN);
      colList.add(IPSConstants.ASSEMBLYURL_COLUMN);
      actionMap.put(new Integer(PSTableChangeEvent.ACTION_INSERT), colList);
      actionMap.put(new Integer(PSTableChangeEvent.ACTION_UPDATE), colList);

      colList = new ArrayList(1);
      colList.add(IPSConstants.VARIANTID_COLUMN);
      actionMap.put(new Integer(PSTableChangeEvent.ACTION_DELETE), colList);
      m_updateTables.put(IPSConstants.CONTENT_VARIANTS_TABLE, actionMap);

      // create lists for RXASSEMBLERPROPERTIES
      actionMap = new HashMap(1);
      colList = new ArrayList(1);
      colList.add(IPSConstants.PROPERTYVALUE_COLUMN);

      actionMap.put(new Integer(PSTableChangeEvent.ACTION_UPDATE), colList);
      actionMap.put(new Integer(PSTableChangeEvent.ACTION_INSERT), colList);
      actionMap.put(new Integer(PSTableChangeEvent.ACTION_DELETE), colList);
      m_updateTables.put(IPSConstants.RXASSEMBLERPROPERTIES_TABLE, actionMap);
   }
}
