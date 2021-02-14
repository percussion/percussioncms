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
package com.percussion.pagemanagement.data;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinitionType;

/**
 * 
 * Represents a rendered link.
 * 
 * @author adamgent
 *
 */
@XmlRootElement(name = "RenderLink")
public class PSRenderLink
{


    private String url;
    private transient PSResourceDefinition resourceDefinition;
    
    private PSResourceDefinitionType resourceType;
    private String resourceDefinitionId;

    

    public PSRenderLink()
    {
    }
    
    public PSRenderLink(String url, PSResourceDefinition resourceDefinition)
    {
        super();
        this.url = url;
        setResourceDefinition(resourceDefinition);
    }

    @XmlTransient
    public PSResourceDefinition getResourceDefinition()
    {
        return resourceDefinition;
    }

    public void setResourceDefinition(PSResourceDefinition resourceDefinition)
    {
        this.resourceDefinition = resourceDefinition;
        if (resourceDefinition != null) {
            setResourceType(resourceDefinition.getResourceType());
            setResourceDefinitionId(resourceDefinition.getUniqueId());
        }
    }

/**
    * Gets value set by setter.
    * see setUrl
    * @return maybe <code>null</code>.
    */
    public String getUrl()
    {
        return url;
    }

    /**
     * Sets url for link item
     * 
     * @param url
     */
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    
    /**
     * Gets the resource definition unique id for this link.
     * This maybe null if the link was created outside
     * of the link service.
     * 
     * @return maybe <code>null</code>.
     */
    public String getResourceDefinitionId()
    {
        return resourceDefinitionId;
    }

    public void setResourceDefinitionId(String resourceDefinitionId)
    {
        this.resourceDefinitionId = resourceDefinitionId;
    }

    /**
     * Gets value set by setter.
     * see setResourceType
     * @return maybe <code>null</code>.
     */
    public PSResourceDefinitionType getResourceType()
    {
        return resourceType;
    }
    
    

    
    public void setResourceType(PSResourceDefinitionType resourceType)
    {
        this.resourceType = resourceType;
    }

    @Override
    public String toString()
    {
        return url;
    }


   
}
