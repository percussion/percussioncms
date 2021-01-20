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
      
      m_revisions = new ArrayList<PSContentStatusHistory>();
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
      return new ArrayList<PSContentStatusHistory>(m_revisions);
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

