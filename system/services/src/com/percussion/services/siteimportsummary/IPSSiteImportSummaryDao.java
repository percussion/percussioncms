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
