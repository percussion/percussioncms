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
package com.percussion.pubserver.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ignacioerro
 *
 */
@XmlRootElement(name = "serverInfo")
public class PSPublishServerInfo
{
    private static final long serialVersionUID = 1L;
    
    private Long serverId;
    
    private String serverName;
    
    private Boolean isDefault;
    
    private String description;
    
    private String type;
    
    private String serverType;
    
    private List<PSPublishServerProperty> properties = new ArrayList<>();

    private Boolean isModified;
    
    private Boolean canIncrementalPublish;
    
    private Boolean isFullPublishRequired;
    
    private Date lastFullPublishDate;
    
    private Date lastIncrementalPublishDate;
    
    
    /**
     * @return the serverId
     */
    public Long getServerId()
    {
        return serverId;
    }

    /**
     * @param serverId the serverId to set
     */
    public void setServerId(Long serverId)
    {
        this.serverId = serverId;
    }

    /**
     * @return the serverName
     */
    public String getServerName()
    {
        return serverName;
    }

    /**
     * @param serverName the serverName to set
     */
    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }

    /**
     * @return the isDefault
     */
    public Boolean getIsDefault()
    {
        return isDefault;
    }

    /**
     * @param isDefault the isDefault to set
     */
    public void setIsDefault(Boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return the properties
     */
    public List<PSPublishServerProperty> getProperties()
    {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(List<PSPublishServerProperty> properties)
    {
        this.properties = properties;
    }

    public void setIsModified(Boolean isModified)
    {
        this.isModified = isModified;
    }

    public Boolean getIsModified()
    {
        return isModified;
    }
    
    public String findProperty(String key)
    {
        for (PSPublishServerProperty property : properties)
        {
            if (property.getKey().equalsIgnoreCase(key))
                return property.getValue();
        }
        return null;
    }

    public String getServerType() {
	return serverType;
    }

    public void setServerType(String serverType) {
	this.serverType = serverType;
    }

    public Boolean getCanIncrementalPublish()
    {
        return canIncrementalPublish;
    }

    public void setCanIncrementalPublish(Boolean canIncrementalPublish)
    {
        this.canIncrementalPublish = canIncrementalPublish;
    }

    public Boolean getIsFullPublishRequired()
    {
        return isFullPublishRequired;
    }

    public void setIsFullPublishRequired(Boolean isFullPublishRequired)
    {
        this.isFullPublishRequired = isFullPublishRequired;
    }

    public Date getLastFullPublishDate()
    {
        return lastFullPublishDate;
    }

    public void setLastFullPublishDate(Date lastFullPublishDate)
    {
        this.lastFullPublishDate = lastFullPublishDate;
    }

    public Date getLastIncrementalPublishDate()
    {
        return lastIncrementalPublishDate;
    }

    public void setLastIncrementalPublishDate(Date lastIncrementalPublishDate)
    {
        this.lastIncrementalPublishDate = lastIncrementalPublishDate;
    }

}
