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

package com.percussion.recycle.service;

import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSDataService;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

/**
 * @author chriswright
 */
public interface IPSRecycleService {

    void recycleItem(int dependentId);

    void recycleFolder(IPSGuid guid);

    void restoreItem(String guid);

    void restoreFolder(String guid);

    List<IPSItemSummary> findChildren(String path);

    IPSItemSummary findItem(String path) throws IPSDataService.DataServiceLoadException;

    /***
     * Returns a boolean indicating if the specified guid is in the Recycler.
     * @param guid A valid guid to search for, never null
     * @return true if guid is in the recycler, false if not
     */
    boolean isInRecycler(String guid);

    /***
     * Returns a boolean indicating if the specified guid is in the Recycler.
     * @param guid A valid guid to search for, never null
     * @return true if guid is in the recycler, false if not
     */
    boolean isNavInRecycler(String guid);

    /***
     * Returns a boolean indicating if the specified guid is in the Recycler.
     * @param guid A valid guid to search for, never null
     * @return true if guid is in the recycler, false if not
     */
    boolean isInRecycler(IPSGuid guid);

}
