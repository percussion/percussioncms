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

package com.percussion.services.siterename;

import com.percussion.services.sitemgr.IPSSite;

public interface IPSSiteRenameService
{
    /**
     * Delete all old entries from the DTS database.
     * Each DTS micro service should implement IPSRestService
     * which requires that each service have a renameSite method
     * which handles deletes of old data from each service from the database.
     *
     * @param site the {@link com.percussion.services.sitemgr.data.PSSite PSSite}
     *        object for the site that was renamed.
     */
    void deleteOldDTSEntries(IPSSite site, String serverType);
}
