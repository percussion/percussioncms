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

package com.percussion.pageoptimizer;

import com.percussion.pageoptimizer.data.PSPageOptimizerData;
import com.percussion.pageoptimizer.data.PSPageOptimizerInfo;
import com.percussion.share.service.exception.PSDataServiceException;

/**
 * CMS page optimizer service
 *
 */
public interface IPSPageOptimizerService
{
    /**
     * @return <code>true</code> if a non-blank server property called PAGE_OPTIMIZER_URL exists otherwise <code>false</code>.
     */
    public boolean isPageOptimizerActive();
    
    /**
     * Gets the properties of the Page Optimizer service.
     * @return PSPageOptimizerInfo
     */
    public PSPageOptimizerInfo getPageOptimizerInfo();
    
    /**
     * Collects the data and consolidates them into PSPageOptimizerData object and returns.
     * @param pageId must be a valid page id, string form of guid, otherwise throws validation exception.
     * @return PSPageOptimizerData never <code>null</code>.
     */
    public PSPageOptimizerData getPageOptimizerData(String pageId);
    
    /**
     * This is a RuntimeException, it is tthrown when there is an error occurs in this service.
     */
    public static class PageOptimizerException extends PSDataServiceException
    {
        public PageOptimizerException()
        {
            super();
        }

        public PageOptimizerException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PageOptimizerException(String message)
        {
            super(message);
        }

        public PageOptimizerException(Throwable cause)
        {
            super(cause);
        }
    }
}
