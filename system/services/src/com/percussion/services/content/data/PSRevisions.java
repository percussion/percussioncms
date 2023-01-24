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
package com.percussion.services.content.data;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.services.system.data.PSContentStatusHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent the revision history of an item
 */
public class PSRevisions
{
   /**
    * Construct a revisions from the item's current content status info and its
    * history.
    * 
    * @param sum The component summary, may not be <code>null</code>.
    * @param history The content status history rows from which to derive the
    * revision history, may contain multiple entries for each revision, it is  
    * <code>null</code> or empty if has not been checked in after its
    * creation, assumed to be in creation order.
    */
   public PSRevisions(PSComponentSummary sum, 
      List<PSContentStatusHistory> history)
   {
      if (sum == null)
         throw new IllegalArgumentException("sum may not be null");
      
      m_sum = sum;
      
      m_revisions = new ArrayList<>();
      if (history != null && history.size() > 0)
      {
         int curRev = -1;
         for (PSContentStatusHistory entry : history)
         {
            int rev = entry.getRevision();
            if (rev > curRev)
            {
               m_revisions.add(entry);
               curRev = rev;
            }
         }
      }
   }
   
   /**
    * Get the history rows representing the first entry for each revision.
    * 
    * @return A history entry for each revision of the item.  Modification to 
    * the returned list do not affect this object. Never <code>null</code>, but
    * may be empty.
    */
   public List<PSContentStatusHistory> getRevisions()
   {
      return new ArrayList<>(m_revisions);
   }
   
   /**
    * Get the component summary provided during construction.
    * 
    * @return The summary, never <code>null</code>.
    */
   public PSComponentSummary getSummary()
   {
      return m_sum;
   }
   
   /**
    * The summary supplied during construction, never <code>null</code> or 
    * modified after that.
    */
   private PSComponentSummary m_sum;
   
   /**
    * List of history entries supplied during construction filtered to contain
    * only the first entry for each revision, never <code>null</code>, empty, or
    * modified after that.
    */
   private List<PSContentStatusHistory> m_revisions;
}

