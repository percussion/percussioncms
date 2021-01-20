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
package com.percussion.rx.publisher.data;

import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Represents a single unit of work.
 *
 * @author Doug Rand
 */
public class PSDemandWork
{

   /**
    * The request counter used to allocate request ids.
    */
   private final static AtomicLong ms_requestIdCounter = new AtomicLong(1);
   
   /**
    * The list of content to publish. Each pair consists of a folder id and a
    * content id.
    */
   private final List<PSPair<IPSGuid, IPSGuid>> m_content =
         new ArrayList<PSPair<IPSGuid, IPSGuid>>();

   /**
    * The request that this work is associated with. Set when the work is
    * queued.
    */
   private final long m_request = ms_requestIdCounter.incrementAndGet();

   /**
    * Add a content item to the work to be published.
    * 
    * @param folderid the folder id, never <code>null</code>
    * @param contentitem the content item, never <code>null</code>
    */
   public void addItem(IPSGuid folderid, IPSGuid contentitem)
   {
      if (folderid == null)
      {
         throw new IllegalArgumentException("folderid may not be null");
      }
      if (contentitem == null)
      {
         throw new IllegalArgumentException("contentitem may not be null");
      }
      m_content.add(new PSPair<IPSGuid, IPSGuid>(folderid, contentitem));
   }

   /**
    * Get the content to be published.
    * 
    * @return the content never <code>null</code>. Each returned pair consists
    * of a folder id (first element) and a content id (second element).
    */
   public List<PSPair<IPSGuid, IPSGuid>> getContent()
   {
      return m_content;
   }

   /**
    * The request id is not set by the user of this object, rather it is set
    * when the work is queued with the service. The id is returned from the
    * queuing call as well as being set into this property for retrieval.
    * 
    * @return the request id
    */
   public long getRequest()
   {
      return m_request;
   }

   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(obj, this);
   }

   @Override
   public int hashCode()
   {
      return (int) m_request;
   }

   @Override
   public String toString()
   {
      StringBuilder buf = new StringBuilder();
      buf.append("\n[Work request: ");
      buf.append(m_request);
      buf.append(" item count: ");
      buf.append(m_content.size());
      buf.append("]");
      return buf.toString();
   }
}
