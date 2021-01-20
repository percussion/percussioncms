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
package com.percussion.search;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.server.PSFieldRetriever;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * This class is part of the pluggable, full text search engine architecture.
 * <p>
 * Instances of this class must be obtained from the search engine interface.
 * <p>
 * This class provides access to the configuration information of the search
 * engine for modification and methods to manage the indexes.
 * <p>
 * The typical usage of this class is as follows (when starting up):
 * <ol>
 * <li>Call {@link #verify(Set)}to confirm with the external search engine
 * that the given set of content type ids are known to the system.
 * <li>Call {@link #update(PSItemDefinition, PSFieldRetriever, boolean) update}
 * for each content type.</li>
 * <li>Call {@link #save()}</li>
 * <li>Start the search engine (through the {@link PSSearchEngine}interface)
 * </li>
 * <ol>
 * <p>
 * While running, call <code>update</code> followed by <code>save</code> and
 * an engine restart each time a content editor is modified. Follow a similar
 * procedure each time a content editor is removed, except rather than calling
 * <code>update</code>, call <code>delete</code>.
 * <p>
 * Depending on the configuration changes required by the update, a re-index for
 * that content type may be required.
 * <p>
 * If the derived class does not override non-abstract methods, this class
 * manages the binary locator and item defition tracking.
 * <p>
 * This class may have {@link java.util.Observer Observers}registered that are
 * notified when an admin method is called. This is an implementation decision.
 * 
 * @author paulhoward
 */
public abstract class PSSearchAdmin
{
   /**
    * This method is used to modify the configuration of the search engine. The
    * configuration is driven by the content type definitions.
    * <p>
    * This method must be called for all running content types before the
    * indexer or query objects are used. This method may cause the server to
    * shut down and restart.
    * <p>
    * This method can be called in two different contexts. In one context, a
    * single content type has been added or updated. In that context, this
    * method will cause the information about the item definition to be updated
    * and the search server will restart immediately.
    * <p>
    * In the other context, a series of type modifications or registrations are
    * happening in a batch, e.g. MSM is installing a number of content types as
    * part of an archive. In this case, this method will be called with the
    * <code>notify</code> parameter set to <code>false</code> for all but
    * the final definition.
    * <p>
    * This first call will cause observers to know that a pending change has
    * started. Indexing will cease until they've been notified with the value
    * true. Note that the engine's update must be called first to avoid deadlock
    * issues.
    * 
    * @param def The content editor that is being added or modified. Never
    *           <code>null</code>.
    * 
    * @param retriever May be used by the indexer to obtain the content of
    *           unchanged binary fields. May be <code>null</code> if the
    *           supplied item has no binary fields in the parent or any complex
    *           child. A binary field is a field whose backend datatype is
    *           binary, or whose content editor definition has the 'treat as
    *           binary' flag set. May be <code>null</code> if the content type
    *           and all complex children don't contain binary fields.
    * 
    * @param notify This method is called in more than one context. When the
    *           server is initializing, this code is called for each content
    *           type. Later it is called when content type defintions change.
    *           This flag should be set to <code>false</code> to avoid having
    *           the underlying engine perform extra steps
    * 
    * @return <code>true</code> if the changes indicated by the newly supplied
    *         def require all content for this type to be re-indexed,
    *         <code>false</code> otherwise.
    * 
    * @throws PSSearchException If any problems occur.
    */
   synchronized public boolean update(PSItemDefinition def, 
         PSFieldRetriever retriever, boolean notify)
      throws PSSearchException
   {
      if (null == def)
      {
         throw new IllegalArgumentException("Content type def cannot be null");
      }
      
      cache(def, retriever);
      return update(def, notify);
   }
   
   /**
    * Causes all changes made with the {@link #update(PSItemDefinition,
    * PSFieldRetriever, boolean) update} method since the last call to this
    * method to be persisted. After the method has finished, the server may or
    * may not be running, depending on the implementation.
    * 
    * @throws IOException If any problems occur while storing the config.
    * @throws PSSearchException If any problems occur while checking and
    *            updating the libraries or restarting the server.
    */   
   public abstract void save()
      throws IOException, PSSearchException;
   
   /**
    * Causes the external search engine to validate the known content types
    * against the content types that it has allocated storage for. This can
    * allow the external engine to free up unneeded space. 
    * <p>This must be called before the search engine is started.
    * 
    * @param knownContentTypes A {@link Set set} of known content types,
    * must never be <code>null</code>. 
    * 
    * @throws PSSearchException On errors within the search engine.
    */
   public abstract void verify(Set<Long> knownContentTypes)
      throws PSSearchException;    

   /**
    * Deletes (if present) and recreates the index(es) associated with the 
    * supplied type. No content is re-indexed. The search server may be
    * shut down for anywhere from 0-100% of the time it takes to perform this
    * operation, depending on the implementation. If it is shut down, it is 
    * restarted before returning. 
    * 
    * @param contentTypes Identifies which indexes to clear. Never <code>null
    * </code>, at least 1 entry and no <code>null</code> entries. Invalid ids 
    * are skipped. What constitutes an invalid id is defined by the 
    * implementation.
    * 
    * @return The keys for which the indexes were created.
    * 
    * @throws PSSearchException If the server can't be reached or cannot 
    * fulfill the request or if the type is invalid.
    */
   public abstract PSKey[] rebuildIndexes(PSKey[] contentTypes)
      throws PSSearchException;
   
   /**
    * Rebuilds the search indexes with all existing content to reduce the 
    * space required and to optimize the structure. The search server may be
    * shut down for anywhere from 0-100% of the time it takes to perform this
    * operation, depending on the implementation. If it is shut down, it is 
    * restarted before returning. 
    * 
    * @param contentTypes Identifies which indexes to clear. Never <code>null
    * </code>, at least 1 entry and no <code>null</code> entries. Invalid ids 
    * are skipped. What constitutes an invalid id is defined by the 
    * implementation.
    * 
    * @return The keys for which the indexes were compacted.
    * 
    * @throws PSSearchException If the server can't be reached or cannot 
    * fulfill the request or if the type is invalid.
    */
   public abstract PSKey[] optimizeIndexes(PSKey[] contentTypes)
      throws PSSearchException;
   
   /**
    * Clears the locks on the indexes. This method depends on the implementation
    * on when to call. The safest way is to call at the time of server start up.
    */
   public abstract void clearIndexLocks();
   
   /**
    * A field retriever allows the search engine to get the content of an
    * unchanged binary field (changed binary fields should always be submitted
    * w/ the rest of the field data). Some search engines may require this data
    * when updating their indexes even though it was unchanged.
    * 
    * @param key The unique identifier for the content type of the item
    *           definition supplied in the {@link #update(PSItemDefinition,
    *           PSFieldRetriever, boolean) update} method. This is the key
    *           obtained from the item definition by calling
    *           {@link PSItemDefinition#getContentEditorKey()
    *           getContentEditorKey}.
    * 
    * @return The retriever set in the {@link #update(PSItemDefinition,
    *         PSFieldRetriever, boolean) update} method. May be
    *         <code>null</code>.
    * 
    * @throws PSInvalidContentTypeException If key was never registered or was
    *            deleted.
    */
   synchronized public PSFieldRetriever getFieldRetriever(PSKey key)
      throws PSInvalidContentTypeException
   {
      if (null == key)
      {
         throw new IllegalArgumentException("key cannot be null");
      }
      return getRetriever(key);
   }
   
   /**
    * Removes all definitions associated with the supplied content editor. If
    * there aren't any, no action is taken and no error is reported. The caller
    * is responsible for clearing the data in the index by using the
    * {@link PSSearchIndexer#clearIndex(PSKey)}method.
    * 
    * @param key The unique identifier for the content type of the item
    *           definition supplied in the {@link #update(PSItemDefinition,
    *           PSFieldRetriever, boolean) update} method. This is the key
    *           obtained from the item definition by calling
    *           {@link PSItemDefinition#getContentEditorKey()
    *           getContentEditorKey}. If <code>null</code> is supplied, all
    *           content type configurations are cleared.
    */
   synchronized public final void delete(PSKey key)
   throws PSSearchException
   {
      clear(key);
      doDelete(key);
   }
   
   /**
    * See {@link #delete(PSKey)} The base class manages binary retriever
    * cleanup before calling this method.
    */
   protected abstract void doDelete(PSKey key) throws PSSearchException;
   
   /**
    * Like {@link #update(PSItemDefinition, PSFieldRetriever, boolean)}, except
    * the retrievers are managed by the base class.
    * 
    * @param def The content editor that is being added or modified. Never 
    * <code>null</code>.
    * 
    * @param notify This method is called in more than one context. When the
    * server is initializing, this code is called for each content type. Later
    * it is called when content type defintions change. This flag should be
    * set to <code>false</code> to avoid having the underlying engine perform
    * extra steps
    */
   protected abstract boolean update(PSItemDefinition def, boolean notify)
      throws PSSearchException;
   
   /**
    * Used to save the supplied params locally for later retrieval using the
    * {@link #getDef(PSKey)} and {@link #getRetriever(PSKey)} methods.
    * @param def Assumed not <code>null</code>.
    * @param retriever Assumed not <code>null</code>.
    */
   private void cache(PSItemDefinition def, PSFieldRetriever retriever)
   {
      Info i = new Info(def, retriever);
      m_retrievers.put(def.getContentEditorKey(), i);  
   }
   
   /**
    * Used to remove info previously saved w/ the
    * {@link #save(PSItemDefinition, PSFieldRetriever)} method.
    * 
    * @param key See {@link #delete(PSKey)}for details.
    */
   private void clear(PSKey key)
   {
      if (null == key)
         m_retrievers.clear();
      else
         m_retrievers.remove(key);
   }
   
   /**
    * Used to retrieve the item definition stored in the <code>update</code> 
    * method.
    * 
    * @param key Assumed not <code>null</code>.  This is the key obtained from 
    * the item definition by calling {@link 
    * PSItemDefinition#getContentEditorKey() getContentEditorKey}. 
    * 
    * @return Never <code>null</code>.
    * 
    * @throws PSInvalidContentTypeException If a content type with this key was 
    * never registered or was deleted.
    */
   private PSItemDefinition getDef(PSKey key)
      throws PSInvalidContentTypeException
   {
      Info info = (Info) m_retrievers.get(key);
      if (null == info)
      {
         throw new PSInvalidContentTypeException(
               "" + key.getPart(key.getDefinition()[0]));
      }
      return info.mi_itemDef;
   }
   
   /**
    * Used to retrieve the binary locator stored in the <code>update</code> 
    * method.
    * 
    * @param key Assumed not <code>null</code>.  This is the key obtained from 
    * the item definition by calling {@link 
    * PSItemDefinition#getContentEditorKey() getContentEditorKey}. 
    * 
    * @return Never <code>null</code>.
    * 
    * @throws PSInvalidContentTypeException If a content type with this key was 
    * never registered or was deleted.
    */
   private PSFieldRetriever getRetriever(PSKey key)
      throws PSInvalidContentTypeException
   {
      Info info = (Info) m_retrievers.get(key);
      if (null == info)
      {
         throw new PSInvalidContentTypeException(
               "" + key.getPart(key.getDefinition()[0]));
      }
      return info.mi_retriever;
   }
   
   /**
    * A simple struct to group two pieces of information together for storage
    * as a value in a map. Members are accessed directly after construction.
    *
    * @author paulhoward
    */
   private class Info
   {
      /**
       * The only ctor.
       * @param def Assumed not <code>null</code>.
       * @param retriever Assumed not <code>null</code>.
       */
      Info(PSItemDefinition def, PSFieldRetriever retriever)
      {
         mi_itemDef = def;
         mi_retriever = retriever;
      }
      
      PSItemDefinition mi_itemDef;
      PSFieldRetriever mi_retriever; 
   }
   
   /**
    * Used to store the binary locators for each content type. Each entry
    * has a key that is a PSKey for the content type id and a value that is 
    * an {@link Info}. Never <code>null</code>. This member should only be 
    * accessed using the 4 methods: {@link #save()}, {@link #clear(PSKey)}, 
    * {@link #getDef(PSKey)} and {@link #getRetriever(PSKey)}.
    */
   private Map m_retrievers = new HashMap();
}
