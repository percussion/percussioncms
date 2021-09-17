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

package com.percussion.sitemanage.data;

import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PubInfo")
@JsonRootName("PubInfo")
public class PSPubInfo
{
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private String regionName;
    private String useAssumeRole;
    private String arnRole;

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String region) {
        this.regionName = region;
    }


    public PSPubInfo()
    {
    }
    public PSPubInfo(String bucketName, String accessKey, String secretKey, String regionName)
    {
        this.bucketName = bucketName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.regionName = regionName;
    }

    public String getBucketName()
    {
        return bucketName;
    }
    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }
    public String getAccessKey()
    {
        return accessKey;
    }
    public void setAccessKey(String accessKey)
    {
        this.accessKey = accessKey;
    }
    public String getSecretKey()
    {
        return secretKey;
    }
    public void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }

    public String getUseAssumeRole() {
        return useAssumeRole;
    }

    public void setUseAssumeRole(String useAssumeRole) {
        this.useAssumeRole = useAssumeRole;
    }

    public String getArnRole() {
        return arnRole;
    }

    public void setArnRole(String arnRole) {
        this.arnRole = arnRole;
    }
}
