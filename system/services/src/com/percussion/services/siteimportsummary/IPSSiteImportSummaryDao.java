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

package com.percussion.services.siteimportsummary;

import com.percussion.services.siteimportsummary.data.PSSiteImportSummary;
import com.percussion.share.dao.IPSGenericDao;

/**
 * Dao for site import summary. 
 *
 */
public interface IPSSiteImportSummaryDao
{
   /**
    * Saves site import summary object.
    * @param summary must not be <code>null</code>
    */
   void save(PSSiteImportSummary summary) throws IPSGenericDao.SaveException;
   
   /**
    * Finds a summary by the supplied summary id.
    * @param summaryId if not a valid summary id, returns <code>null</code> 
    * @return summary or <code>null</code> if not found.
    */
   PSSiteImportSummary findBySummaryId(int summaryId);
   
   /**
    * Finds a summary by the supplied site id.
    * @param siteId if not a valid site id, returns <code>null</code> 
    * @return summary or <code>null</code> if not found.
    */
   PSSiteImportSummary findBySiteId(int siteId);
   
   /**
    * Deletes the summary entry for the supplied summary object.
    * @param summary must not be <code>null</code>
    */
   void delete(PSSiteImportSummary summary);
}
