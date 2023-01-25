/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.search;

import com.percussion.design.objectstore.PSLocator;

/**
 * This class encapsulates a single result (row) from the search engine. It
 * only includes the minimal properties needed by the Rx engine.
 * <p>Instances of this class are immutable.
 * 
 * @author paulhoward
 */
public class PSSearchResult
{
   /**
    * Ctor.
    * 
    * @param itemKey Never <code>null</code>. Any revision is allowed.
    * 
    * @param relavancy A value between -1 and 100, inclusive. -1 means there 
    * is no relevancy ranking for this result.
    */
   public PSSearchResult(PSLocator itemKey, int relevancy)
   {
      if (null == itemKey)
      {
         throw new IllegalArgumentException("Locator cannot be null.");
      }

      m_itemKey = itemKey;
      m_relevancyRanking = relevancy;
   }
   
   /**
    * The item that matched the search criteria. The match could occur in the
    * item itself or any of its children.
    * 
    * @return Never <code>null</code>. The revision is not valid in this key.
    */
   public PSLocator getKey()
   {
      return m_itemKey;   
   }

   /**
    * When a concept search is performed, each result has a property known
    * as the relevancy of the result. This is a value between 0 and 100 that
    * indicates how closely the engine calculates this entry to be to what 
    * you actually wanted. This is accomplished by considering several 
    * different factors such as the # of times the word and related words
    * appear in the result, how close the words are both physically within 
    * the doc and semantically, among other things. 
    * 
    * @return If this result is from a conceptual search, a value between 0
    * and 100, inclusive. If it is not (boolean search for example), -1.
    */   
   public int getRelevancy()
   {
      return m_relevancyRanking;
   }
   
   /**
    * See {@link #getKey()} for description. Set in ctor, then never <code>null
    * </code> or modified.
    */
   private PSLocator m_itemKey;
   
   /**
    * See {@link #getRelevancy()} for description and allowed values. Set in 
    * ctor, then never modified.
    */
   private int m_relevancyRanking;
}
