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
