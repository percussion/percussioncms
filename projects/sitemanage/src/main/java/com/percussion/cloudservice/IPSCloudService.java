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

package com.percussion.cloudservice;

import com.percussion.cloudservice.data.PSCloudLicenseType;
import com.percussion.cloudservice.data.PSCloudServiceInfo;
import com.percussion.cloudservice.data.PSCloudServicePageData;
import com.percussion.share.service.exception.PSDataServiceException;

public interface IPSCloudService
{
    /**
     * Determine if cloud services are active (licensed)
     * @return true if services are active; false otherwise
     */
    public boolean isActive();
    
    /**
     * Determine if the given license type is active
     * @param licenseType
     * @return true if service is active; false otherwise
     */
    public boolean isActive(PSCloudLicenseType licenseType);
    
    /**
     * Get a map of license types to isActive 
     * @return string in JSON format
     */
    public String getActiveState();
    
    /**
     * Get the cloud services info (client identity, UI provider, etc...)
     * @return cloud services info
     */
    public PSCloudServiceInfo getInfo() throws PSCloudServiceException;
    
    /**
     * Get the cloud services info (client identity, UI provider, etc...) for the given license type
     * @param licenseType
     * @return cloud services info
     */
    public PSCloudServiceInfo getInfo(PSCloudLicenseType licenseType) throws PSCloudServiceException;
    
    /**
     * Get the page data for the given page
     * @param pageId
     * @return page data
     */
    public PSCloudServicePageData getPageData(String pageId) throws PSCloudServiceException;
    
    /**
     * Get the page data for the given page using the given license type
     * @param licenseType
     * @param pageId
     * @return page data
     */
    public PSCloudServicePageData getPageData(PSCloudLicenseType licenseType, String pageId);
    
    /**
     * Save the page data
     * @param pageData
     */
    public void savePageData(PSCloudServicePageData pageData);

    /**
     * This is a RuntimeException, it is thrown when there is an error occurs in this service.
     */
    public static class PSCloudServiceException extends PSDataServiceException
    {
        public PSCloudServiceException()
        {
            super();
        }

        public PSCloudServiceException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSCloudServiceException(String message)
        {
            super(message);
        }

        public PSCloudServiceException(Throwable cause)
        {
            super(cause);
        }
    }
}
