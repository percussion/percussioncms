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
