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
