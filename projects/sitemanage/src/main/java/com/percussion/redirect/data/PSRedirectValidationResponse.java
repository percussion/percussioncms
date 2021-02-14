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

package com.percussion.redirect.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.licensemanagement.data.PSModuleLicense;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "response")
@JsonRootName("response")
public class PSRedirectValidationResponse
{
    public String getErrorMessage()
    {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
    public RedirectValidationStatus getStatus()
    {
        return status;
    }
    public void setStatus(RedirectValidationStatus status)
    {
        this.status = status;
    }
    public PSModuleLicense getRedirectLicense()
    {
        return redirectLicense;
    }
    public void setRedirectLicense(PSModuleLicense redirectLicense)
    {
        this.redirectLicense = redirectLicense;
    }
    public String getBucketName()
    {
        return bucketName;
    }
    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }
    private String errorMessage;
    private PSModuleLicense redirectLicense;
    private String bucketName;
    private RedirectValidationStatus status;
    public enum RedirectValidationStatus{
        Published, NoLicense, NotPublished, NoChildren, Error, NotApplicable
    }
}
