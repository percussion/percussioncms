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
package com.percussion.share.service;

import com.percussion.share.data.PSDataItemSummary;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;

import java.util.List;


public interface IPSDataItemSummaryService extends IPSItemSummaryService<PSDataItemSummary>
{

    /**
     * Returns the folders that are children to the given id.
     * The id should be an item that is a folder.
     * @param id never <code>null</code> or empty.
     * @return never <code>null</code>, maybe empty.
     * @throws DataServiceLoadException if the item is not valid to have children or does not exist.
     */
    List<PSDataItemSummary> findChildFolders(String id);

    PSDataItemSummary find(String id, String relationshipTypeName);

}
