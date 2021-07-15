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
