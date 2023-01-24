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

package com.percussion.sitemanage.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.percussion.share.data.PSAbstractPersistantObject;
import com.percussion.sitemanage.service.IPSSiteDataService.PublishType;

/**
 * It contains the publishing information for a site  
 * 
 * @author radharanisonnathi
 *
 */
@XmlRootElement(name = "SitePublishProperties")
public class PSSitePublishProperties extends PSAbstractPersistantObject
{
    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * returns the site id
     */
    @XmlElement
    @Override
    public String getId()
    { 
        return id;
    }
    
    /**
     * @param id sets the site id
     */
    @Override
    public void setId(String id)
    {
      this.id = id;  
    }
    
    /**
     * @return the deliveryRootPath
     */
    public String getDeliveryRootPath()
    {
        return deliveryRootPath;
    }

    /**
     * @param deliveryRootPath the deliveryRootPath to set
     */
    public void setDeliveryRootPath(String deliveryRootPath)
    {
        this.deliveryRootPath = deliveryRootPath;
    }

    /**
     * @return the ftpServerName
     */
    public String getFtpServerName()
    {
        return ftpServerName;
    }

    /**
     * @param ftpServerName the ftpServerName to set
     */
    public void setFtpServerName(String ftpServerName)
    {
        this.ftpServerName = ftpServerName;
    }

    /**
     * @return the ftpPassword
     */
    public String getFtpPassword()
    {
        return ftpPassword;
    }

    /**
     * @param ftpPassword the ftpPassword to set
     */
    public void setFtpPassword(String ftpPassword)
    {
        this.ftpPassword = ftpPassword;
    }
    
    public String getPrivateKey()
    {
        return privateKey;
    }

    public void setPrivateKey(String privateKey)
    {
        this.privateKey = privateKey;
    }

    /**
     * @return the ftpServerPort
     */
    public Integer getFtpServerPort()
    {
        return ftpServerPort;
    }
    
    /**
     * @param siteName the siteName to set
     */
    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }
    
    /**
     * @return the siteName
     */
    public String getSiteName()
    {
        return siteName;
    } 
    
    /**
     * @param ftpServerPort the ftpServerPort to set
     */
    public void setFtpServerPort(Integer ftpServerPort)
    {
        this.ftpServerPort = ftpServerPort;
    }
    
    /**
     * @return the publishType
     */
    public PublishType getPublishType()
    {
        return publishType;
    }

    /**
     * @param publishType the publishType to set
     */
    public void setPublishType(PublishType publishType)
    {
        this.publishType = publishType;
    }
    
    /**
     * @return the ftpUserName
     */
    public String getFtpUserName()
    {
        return ftpUserName;
    }

    /**
     * @param ftpUserName the ftpUserName to set
     */
    public void setFtpUserName(String ftpUserName)
    {
        this.ftpUserName = ftpUserName;
    }
    
    /**
     * @return the secure
     */
    public Boolean getSecure()
    {
        return secure;
    }

    /**
     * @param secure the secure to set
     */
    public void setSecure(Boolean secure)
    {
        this.secure = secure;
    }
    
    /**
     * See {@link #getId()} for detail
     */
    private String id;
    
    /**
     * See {@link #getDeliveryRootPath()} for detail
     */
    
    private String deliveryRootPath;
    
    /**
     * See {@link #getFtpServerName()} for detail
     */
    private String ftpServerName;
    
    /**
     * See {@link #getFtpPassword()} for detail
     */
    private String ftpPassword;
    
    private String privateKey;

    /**
     * See {@link #getFtpServerPort()}for detail
     */
    private Integer ftpServerPort;
    
    /**
     * See {@link #getFtpUserName()} for detail
     */
    private String ftpUserName;
    
    /**
     * See {@link #getSiteName()} for detail
     */
    private String siteName;
    
    /**
     * See {@link #getPublishType()}} for detail
     */
    private PublishType publishType;
    
    /**
     * See {@link #getSecure()}} for detail
     */
    private Boolean secure;
    
}
