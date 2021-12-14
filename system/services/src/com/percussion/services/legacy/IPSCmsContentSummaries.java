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
package com.percussion.services.legacy;

import com.percussion.cms.objectstore.PSComponentSummary;

import java.util.Collection;
import java.util.List;

/**
 * This interface is provided as the public (Rx implementers) interface to this
 * service. This interface provides methods for loading legacy item summaries.
 *
 * @author paulhoward
 */
public interface IPSCmsContentSummaries
{
   /**
    * Load one or more component summaries by content id.
    * 
    * @param ids One or more content ids, never <code>null</code> or empty.
    * @return One or more component summaries, which may or may not be in the
    *         same order as the ids. The size of the returned list may not
    *         match the size of the content ids.
    */
   List<PSComponentSummary> loadComponentSummaries(Collection<Integer> ids);

   /**
    * Load a single component summary by content id.
    * 
    * @param id a content id
    * @return A valid summary or <code>null</code> if the summary is not
    * found.
    */
   PSComponentSummary loadComponentSummary(int id);
   PSComponentSummary loadComponentSummary(int id,boolean refresh);

}
