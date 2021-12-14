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
