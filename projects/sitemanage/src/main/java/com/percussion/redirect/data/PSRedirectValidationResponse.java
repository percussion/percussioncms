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
