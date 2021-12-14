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
