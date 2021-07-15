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

package com.percussion.services.publisher.data;


import static org.apache.commons.lang.Validate.isTrue;

import java.util.Iterator;

/**
 * A container for the content list result processing.
 * @author adamgent
 *
 */
public final class PSContentListResults
{

   private Iterator<PSContentListItem> m_iterator;
   public long m_estimatedSize = 0;
   
   
   public PSContentListResults(Iterator<PSContentListItem> iterator,
         long estimatedSize)
   {
      super();
      m_iterator = iterator;
      this.m_estimatedSize = estimatedSize;
   }

   public long getEstimatedSize()
   {
      return m_estimatedSize;
   }

   public Iterator<PSContentListItem> iterator() {
      Iterator<PSContentListItem> it = m_iterator;
      isTrue(it != null, "Iterator should not have been called yet or has been called more than once.");
      m_iterator = null;
      return it;
   }
}
