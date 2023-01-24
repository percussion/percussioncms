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

package com.percussion.licensemanagement.data;

import static org.apache.commons.lang.Validate.notNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "moduleLicenses")
public class PSModuleLicenses
{
    public List<PSModuleLicense> getModuleLicenses()
    {
        return moduleLicenses;
    }

    public void setModuleLicenses(List<PSModuleLicense> moduleLicenses)
    {
        this.moduleLicenses = moduleLicenses;
    }

    public String getLicenseServiceUrl()
    {
        return licenseServiceUrl;
    }

    public void setLicenseServiceUrl(String licenseServiceUrl)
    {
        this.licenseServiceUrl = licenseServiceUrl;
    }
    public void addModuleLicense(PSModuleLicense moduleLicense){
        notNull(moduleLicense);
        if(this.moduleLicenses == null) {
            this.moduleLicenses = new ArrayList<>();
        }
        for (Iterator<PSModuleLicense> iter = this.moduleLicenses.iterator();iter.hasNext();)
        {
            PSModuleLicense ml = iter.next();
            if(ml.getName().equalsIgnoreCase(moduleLicense.getName())){
                iter.remove();
                break;
            }
        }
        this.moduleLicenses.add(moduleLicense);
    }
    public void removeModuleLicense(PSModuleLicense moduleLicense){
        notNull(moduleLicense);
        if(this.moduleLicenses == null) {
            this.moduleLicenses = new ArrayList<>();
        }
        for (Iterator<PSModuleLicense> iter = this.moduleLicenses.iterator();iter.hasNext();)
        {
            PSModuleLicense ml = iter.next();
            if(ml.getName().equalsIgnoreCase(moduleLicense.getName())){
                iter.remove();
                break;
            }
        }
    }
    private List<PSModuleLicense> moduleLicenses;
    private String licenseServiceUrl;
}
