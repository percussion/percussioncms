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
package com.percussion.search;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.server.PSFieldRetriever;

import java.util.Collection;
import java.util.Map;

/**
 * This class is part of the pluggable, full text search engine architecture.
 * <p>Instances of this class must be obtained from the search engine interface.
 * <p>This class is responsible for providing support to upload data that needs
 * to be indexed. Data is submitted in units uniquely identified by a key
 * represented by the {@link PSSearchKey} class.
 * <p>The supported units are fragments of an item and include the following:
 * <ol>
 * <li>Base item, not including complex children (all other children types are
 * included w/ this fragment)</li>
 * <li>Each entry for each complex child</li>
 * </ol>
 * <p>When submitting a fragment, all fields must be supplied each time it is
 * updated except for 'binary' fields, which are treated specially. If a  binary
 * field has changed, it must be submitted. If it has not changed, then it 
 * should be left out of the submission. The engine will determine whether it 
 * needs to query the data during an update in this case. See {@link  
 * #update(PSSearchKey,Map,boolean) update} for more details.
 * <p>The search engine supports an update model akin to a 2-phase commit.  The 
 * 2 'phases' are called the processing phase and the commit phase. The 
 * processing phase performs enough work such that the submitted data is not 
 * lost if the engine crashes, but it is not made available in the active index 
 * (the index that is used by end-user queries). Following the processing, the 
 * data may optionally be 'committed', at which point it is made part of the 
 * active index.
 * <p>The server periodically initiates a commit automatically. (The 
 * implementation must see to it that this happens.)
 * 
 * @author paulhoward
 */
public abstract class PSSearchIndexer
{
   /**
    * Adds new data to the search index. Units of data include the following:
    * <ul>
    *    <li>Parent item, including simple children and sdmp</li>
    *    <li>Each entry for each complex child</li>
    * </ul>
    * Each unit must be submitted in its entirety except as noted below for 
    * 'treat as binary' fields. Any other fields not supplied will be cleared
    * in the index.
    * 
    * @param unitId Never <code>null</code>. At least the content type and 
    * content locator parts of the key must be valid in the supplied
    * key. The locator revision is ignored by the search engine.
    * 
    * @param itemFragment The data for all fields in the fragment. Data for 
    * 'treat as binary' fields may be one of 3 types: String, byte[] or 
    * InputStream. A treat-as-binary field may also be omitted from the map 
    * entirely. In this case, the indexer will only retrieve the content of 
    * that field (using the retriever supplied to the {@link 
    * PSSearchAdmin#update(PSItemDefinition, PSFieldRetriever, boolean)} method 
    * if it is 
    * required by the implementation. This method takes ownership of the stream
    * and will close it. It is assumed that if the binary field is not included 
    * in the map that it has not changed since the last update. All other
    * fields are processed by performing a <code>toString</code> on the
    * value. This method takes ownership of the map and may modify it. 
    * <p>Any fields included in the map that are not in the current index are
    * ignored.
    * <p>Fields defined as not user searchable are removed and not submitted
    * to the index.
    * <p><code>Null</code> or empty keys are ignored, values may be <code>null
    * </code>.
    * <p>The value of the <code>sys_lang</code> field is used to determine
    * under which language the item is indexed. Items indexed under a language
    * can only be retrieved by a query specifying that language. If not 
    * provided, the item will be indexed as english. 
    * 
    * @param commitToIndex This is only useful if the engine supports a "2-
    * phase commit". If <code>true</code>, the data will be available for
    * searching when this method returns. If <code>false</code>, the data
    * won't be available until it has been committed, either by the engine
    * (due to a scheduled operation) or activated by a call to {@link
    * #commit()}. Regardless of the settings of this flag, data is safe, 
    * meaning if the engine crashed for any reason, it would pick up this
    * data when it restarted, even if the flag was <code>false</code>. 
    * <p>If the caller is going to be submitting many units, then pass
    * <code>false</code> for the block of units and call <code>commit</code>
    * after the last one. Even if commit is not called by the programmer,
    * it will automatically be called by the engine on a periodic basis.
    * 
    * @throws PSSearchException If the data can't be added for any reason. 
    */
   public abstract void update(PSSearchKey unitId, Map itemFragment, 
         boolean commitToIndex)
      throws PSSearchException;
   
   /** 
    * Causes any data that has been submitted using the {@link #update(
    * PSSearchKey,Map,boolean) update} method with the <code>commitToIndex
    * </code> flag set to <code>false</code> since the last time the method
    * was called with the flag set to <code>true</code>. 
    * <p>Commited data is available for searching after this method returns.
    * <p><em>Note:</em> breaking up the operations of updating and commiting
    * should only be done w/in the context of a single instance of this 
    * class. However, even if commit is never called after an update, the
    * search engine commits all data periodically (configured in admin). 
    * 
    * @throws PSSearchException If the server can't be reached or cannot 
    * fulfill the request.
    */
   public abstract void commit()
      throws PSSearchException;
   
   /**
    * Like {@link #commit()}, except it commits uncommitted documents 
    * submitted by any instance of this class. 
    * <p>Probably implemented as a static in derived classes.
    * 
    * @throws PSSearchException If the server can't be reached or cannot 
    * fulfill the request.
    */
   public abstract void commitAll()
      throws PSSearchException;
   
   /**
    * Remove content from the indexer that is no longer needed.
    * 
    * @param unitIds Never <code>null</code>. Each entry is a {@link 
    * PSSearchKey}. For each entry in the collection, the data previously 
    * submitted for that unit and all child entries, is removed from the index. 
    * If such a unit is not found in the index, it is skipped. <code>Null</code> 
    * entries in the collection are ignored.
    * <p>The revision of the parent locator in the key is not used.
    *     
    * @throws PSSearchException If the operation does not succeed due to 
    * server failure or inability to reach the server. Invalid or <code>
    * null</code> id entries do not cause an exception.
    */   
   public abstract void delete(Collection unitIds)
      throws PSSearchException;
   
   /**
    * Removes all data ever submitted to the index, including any data 
    * submitted but not yet committed. 
    * 
    * @param contentType Identifies which indexes to clear. Never <code>null
    * </code>. 
    * 
    * @throws PSSearchException If the server can't be reached or cannot 
    * fulfill the request or if the type is invalid.
    */
   public abstract void clearIndex(PSKey contentType)
      throws PSSearchException;
   
   /**
    * Must be called when finished using the object. Cleans up resources used
    * by this object. May be called multiple times. 
    * <p>After calling this method, all other methods will throw an
    * <code>IllegalStateException</code> when called.
    * 
    * @throws PSSearchException If any errors while cleaning up.
    */
   public abstract void close()
      throws PSSearchException;
   
   /**
    * Optimize the indexes.  Also closes and cleans up IndexWriter objects to 
    * prevent any persistent memory issues.
    * @param optimize whether to optimize indexes before closing
    * @throws PSSearchException If the server can't be reached or cannot 
    * fulfill the request or if the type is invalid.
    */
   public abstract void close(boolean optimize)
      throws PSSearchException;
   
   /**
    * Instances of this class must be obtained from the search engine 
    * ({@link PSSearchEngine}).
    */
   protected PSSearchIndexer()
   {
      
   }
}
